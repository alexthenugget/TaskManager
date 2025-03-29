package com.github.alex_the_nugget.taskhub.taskhub.services;

import com.github.alex_the_nugget.taskhub.taskhub.database.DatabaseConnectionManager;
import com.github.alex_the_nugget.taskhub.taskhub.database.TaskRepository;
import com.github.alex_the_nugget.taskhub.taskhub.database.UserRepository;
import com.github.alex_the_nugget.taskhub.taskhub.models.Task;
import com.github.alex_the_nugget.taskhub.taskhub.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsService {
    private static final Logger log = LoggerFactory.getLogger(StatisticsService.class);
    private final TaskRepository taskRepository = new TaskRepository();
    private final UserRepository userRepository = new UserRepository();
    private final DatabaseConnectionManager databaseConnectionManager = new DatabaseConnectionManager();
    private final String nameDB = "TaskManager_DB";
    private final String nameUsersTable = "SystemUsers";
    private final UserSession userSession = UserSession.getInstance();

    public List<Task> returnTasksListSortedByStatus() {
        String userLogin = userSession.getLogin();
        if (userLogin == null || userLogin.isEmpty()) {
            log.warn("No user login found in session");
            return Collections.emptyList();
        }

        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            log.debug("Fetching tasks for user: {}", userLogin);
            String userTasksTable = userLogin + "tasktable";
            List<Task> tasks = taskRepository.findUserTasksByLogin(conn, userTasksTable);

            if (tasks == null) {
                log.warn("No tasks found for user: {}", userLogin);
                return Collections.emptyList();
            }

            tasks.forEach(task -> {
                if (task.getStatus() != null && !task.getStatus().isEmpty()) {
                    String status = task.getStatus().toLowerCase();
                    status = status.substring(0, 1).toUpperCase() + status.substring(1);
                    task.setStatus(status);
                } else {
                    log.debug("Task with empty status found for user: {}", userLogin);
                }
            });

            List<Task> sortedTasks = tasks.stream()
                    .filter(task -> task.getStatus() != null)
                    .sorted(Comparator.comparing(task -> task.getStatus().toLowerCase()))
                    .collect(Collectors.toList());

            log.debug("Successfully sorted {} tasks by status for user: {}", sortedTasks.size(), userLogin);
            return sortedTasks;
        } catch (SQLException e) {
            log.error("SQL error while fetching tasks for user {}: {}", userLogin, e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error while fetching tasks for user {}: {}", userLogin, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Task> returnCompletedTasksSortedByEndDate() {
        String userLogin = userSession.getLogin();
        if (userLogin == null || userLogin.isEmpty()) {
            log.warn("No user login found in session");
            return Collections.emptyList();
        }

        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            log.debug("Fetching completed tasks for user: {}", userLogin);
            String userTasksTable = userLogin + "tasktable";
            List<Task> tasks = taskRepository.findUserTasksByLogin(conn, userTasksTable);

            if (tasks == null) {
                log.warn("No tasks found for user: {}", userLogin);
                return Collections.emptyList();
            }

            List<Task> completedTasks = tasks.stream()
                    .filter(task -> task.getStatus() != null && !task.getStatus().isEmpty())
                    .peek(task -> {
                        String status = task.getStatus().toLowerCase();
                        status = status.substring(0, 1).toUpperCase() + status.substring(1);
                        task.setStatus(status);
                    })
                    .filter(task -> task.getStatus().equals("Completed") && task.getEndDate() != null)
                    .sorted(Comparator.comparing(Task::getEndDate))
                    .collect(Collectors.toList());

            log.debug("Found {} completed tasks for user: {}", completedTasks.size(), userLogin);
            return completedTasks;
        } catch (SQLException e) {
            log.error("SQL error while fetching completed tasks for user {}: {}", userLogin, e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error while fetching completed tasks for user {}: {}", userLogin, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public Map<String, Double> calculateEmployeeEfficiency() {
        String managerLogin = userSession.getLogin();
        if (managerLogin == null || managerLogin.isEmpty()) {
            log.warn("No manager login found in session");
            return Collections.emptyMap();
        }

        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            String[] employees = userRepository.findEmployeesByDepartment(conn, nameUsersTable, managerLogin);

            if (employees == null || employees.length == 0) {
                log.warn("No employees found for manager: {}", managerLogin);
                return Collections.emptyMap();
            }

            Map<String, Double> efficiencyMap = new LinkedHashMap<>();
            log.debug("Found {} employees for manager {}", employees.length, managerLogin);

            for (String employee : employees) {
                try {
                    String employeeLogin = userRepository.findLoginByUserName(conn, nameUsersTable, employee);
                    if (employeeLogin != null) {
                        String userTasksTable = employeeLogin + "tasktable";

                        if (!databaseConnectionManager.doesTableExist(conn, userTasksTable)) {
                            log.debug("Task table doesn't exist for employee: {}", employee);
                            efficiencyMap.put(employee, 0.0);
                            continue;
                        }

                        List<Task> tasks = taskRepository.findUserTasksByLogin(conn, userTasksTable);

                        if (tasks == null || tasks.isEmpty()) {
                            log.debug("No tasks found for employee: {}", employee);
                            efficiencyMap.put(employee, 0.0);
                            continue;
                        }

                        long completedTasks = tasks.stream()
                                .filter(task -> task.getStatus() != null && "Completed".equalsIgnoreCase(task.getStatus()))
                                .count();

                        long totalTasks = tasks.size();
                        double efficiency = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

                        log.debug("Employee {} efficiency: {}% ({} out of {})",
                                employee, String.format("%.2f", efficiency), completedTasks, totalTasks);
                        efficiencyMap.put(employee, efficiency);
                    } else {
                        log.warn("Login not found for employee: {}", employee);
                        efficiencyMap.put(employee, 0.0);
                    }
                } catch (Exception e) {
                    log.error("Error processing employee {}: {}", employee, e.getMessage(), e);
                    efficiencyMap.put(employee, 0.0);
                }
            }

            Map<String, Double> sortedMap = efficiencyMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));
            return sortedMap;
        } catch (SQLException e) {
            log.error("SQL error while calculating employee efficiency for manager {}: {}",
                    managerLogin, e.getMessage(), e);
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("Unexpected error while calculating employee efficiency for manager {}: {}",
                    managerLogin, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    public Map<String, Long> calculateTaskRatings() {
        String managerLogin = userSession.getLogin();
        if (managerLogin == null || managerLogin.isEmpty()) {
            log.warn("No manager login found in session");
            return Collections.emptyMap();
        }

        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            String[] employees = userRepository.findEmployeesByDepartment(conn, nameUsersTable, managerLogin);

            if (employees == null || employees.length == 0) {
                log.warn("No employees found for manager: {}", managerLogin);
                return Collections.emptyMap();
            }

            Map<String, Long> ratingsCount = new HashMap<>();
            ratingsCount.put("Good", 0L);
            ratingsCount.put("Ok", 0L);
            ratingsCount.put("Bad", 0L);

            log.debug("Processing {} employees for task ratings", employees.length);
            for (String employee : employees) {
                try {
                    String employeeLogin = userRepository.findLoginByUserName(conn, nameUsersTable, employee);
                    if (employeeLogin != null) {
                        String userTasksTable = employeeLogin + "tasktable";

                        if (!databaseConnectionManager.doesTableExist(conn, userTasksTable)) {
                            log.debug("Task table doesn't exist for employee: {}", employee);
                            continue;
                        }

                        List<Task> tasks = taskRepository.findUserTasksByLogin(conn, userTasksTable);

                        if (tasks == null || tasks.isEmpty()) {
                            log.debug("No tasks found for employee: {}", employee);
                            continue;
                        }

                        tasks.stream()
                                .filter(task -> task.getRating() != null && !task.getRating().isEmpty())
                                .forEach(task -> {
                                    try {
                                        String rating = task.getRating();
                                        rating = rating.substring(0, 1).toUpperCase() +
                                                rating.substring(1).toLowerCase();
                                        ratingsCount.merge(rating, 1L, Long::sum);
                                        log.trace("Processed rating '{}' for employee {}", rating, employee);
                                    } catch (Exception e) {
                                        log.error("Error processing rating for task: {}", task, e);
                                    }
                                });
                    } else {
                        log.warn("Login not found for employee: {}", employee);
                    }
                } catch (Exception e) {
                    log.error("Error processing employee {}: {}", employee, e.getMessage(), e);
                }
            }
            return ratingsCount;
        } catch (SQLException e) {
            log.error("SQL error while calculating task ratings for manager {}: {}",
                    managerLogin, e.getMessage(), e);
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("Unexpected error while calculating task ratings for manager {}: {}",
                    managerLogin, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
}