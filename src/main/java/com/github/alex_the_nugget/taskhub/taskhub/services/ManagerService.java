package com.github.alex_the_nugget.taskhub.taskhub.services;

import com.github.alex_the_nugget.taskhub.taskhub.controllers.bot.TelegramBot;
import com.github.alex_the_nugget.taskhub.taskhub.database.DatabaseConnectionManager;
import com.github.alex_the_nugget.taskhub.taskhub.database.TaskRepository;
import com.github.alex_the_nugget.taskhub.taskhub.database.UserRepository;
import com.github.alex_the_nugget.taskhub.taskhub.models.Task;
import com.github.alex_the_nugget.taskhub.taskhub.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;

public class ManagerService {
    private static final Logger log = LoggerFactory.getLogger(ManagerService.class);
    private final TaskRepository taskRepository = new TaskRepository();
    private final UserRepository userRepository = new UserRepository();
    private final DatabaseConnectionManager databaseConnectionManager = new DatabaseConnectionManager();
    private final String nameDB = "TaskManager_DB";
    private final String nameUsersTable = "SystemUsers";
    private final UserSession userSession = UserSession.getInstance();

    public ManagerService() {
    }

    public List<Task> returnTasksList() {
        String userLogin = userSession.getLogin();
        if (userLogin == null || userLogin.isEmpty()) {
            log.warn("No user login found in session");
            return null;
        }

        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            String userTasksTable = userLogin + "tasktable";
            return taskRepository.findUserTasksByLogin(conn, userTasksTable);
        }
        catch(Exception e){
            log.error("Error getting tasks for user {}: {}", userLogin, e.getMessage(), e);
            return null;
        }
    }

    public String[] returnAnEmployeeList() {
        String managerLogin = userSession.getLogin();
        if (managerLogin == null || managerLogin.isEmpty()) {
            log.warn("No manager login found in session");
            return null;
        }

        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            return userRepository.findEmployeesByDepartment(conn, nameUsersTable, managerLogin);
        }
        catch(Exception e) {
            log.error("Error getting employee list for manager {}: {}", managerLogin, e.getMessage(), e);
            return null;
        }
    }

    public void addNewUserTask(String employee, String tag, String taskName,
                               String startDate, String endDate, String taskDescription) {
        String managerLogin = userSession.getLogin();
        if (managerLogin == null || managerLogin.isEmpty()) {
            log.warn("No manager login found in session");
            return;
        }

        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            String employeeLogin = userRepository.findLoginByUserName(conn, nameUsersTable, employee);
            String employeeTasksTable = employeeLogin + "tasktable";
            if (!databaseConnectionManager.doesTableExist(conn, employeeTasksTable)) {
                databaseConnectionManager.createTasksTable(conn, employeeTasksTable);
            }
            taskRepository.addNewUserTask(conn, nameUsersTable, managerLogin, employee, tag, taskName,
                    startDate, endDate, taskDescription, userRepository);

            Long employeeChatId = userRepository.getUserChatId(conn, nameUsersTable, employee);
            String managerName = userRepository.findNameByLogin(conn, nameUsersTable, managerLogin);

            if (employeeChatId != null) {
                String notificationMessage = "Вам назначена новая задача:\n\n" +
                        "Название: " + taskName + "\n" +
                        "Тег: " + tag + "\n" +
                        "Описание: " + taskDescription + "\n" +
                        "Срок выполнения: " + endDate + "\n" +
                        "От: " + managerName;

                TelegramBot.getInstance().sendNewTaskNotification(notificationMessage, employeeChatId);
            } else {
                log.warn("No chatId found for employee {}", employee);
            }
        }
        catch(Exception e) {
            log.error("Error in addNewUserTask for employee {} (manager {}): {}",
                    employee, managerLogin, e.getMessage(), e);
        }
    }

    public void updateTaskStatus(int id, String newStatus) {
        String userLogin = userSession.getLogin();
        if (userLogin == null || userLogin.isEmpty()) {
            log.warn("No user login found in session");
            return;
        }

        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            String userTasksTable = userLogin + "tasktable";
            taskRepository.updateTaskStatus(conn, userTasksTable, id, newStatus);
        }
        catch(Exception e){
            log.error("Error updating task status for user {}: {}", userLogin, e.getMessage(), e);
        }
    }

    public void updateTaskRating(int id, String rating) {
        String userLogin = userSession.getLogin();
        if (userLogin == null || userLogin.isEmpty()) {
            log.warn("No user login found in session");
            return;
        }

        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            String userTasksTable = userLogin + "tasktable";
            taskRepository.updateTaskRating(conn, userTasksTable, id, rating);
        }
        catch(Exception e){
            log.error("Error updating task rating for user {}: {}", userLogin, e.getMessage(), e);
        }
    }
}