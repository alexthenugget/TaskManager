package com.github.alex_the_nugget.taskhub.taskhub.services;

import com.github.alex_the_nugget.taskhub.taskhub.database.DatabaseConnectionManager;
import com.github.alex_the_nugget.taskhub.taskhub.database.TaskRepository;
import com.github.alex_the_nugget.taskhub.taskhub.database.UserRepository;
import com.github.alex_the_nugget.taskhub.taskhub.models.Task;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TelegramBotService {
    private final TaskRepository taskRepository = new TaskRepository();
    private final UserRepository userRepository = new UserRepository();
    private final DatabaseConnectionManager databaseConnectionManager = new DatabaseConnectionManager();

    private String formatTask(Task task) {
        return formatTask(task, ZoneId.systemDefault());
    }

    private String formatTask(Task task, ZoneId userZoneId) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(userZoneId);

        String startDateStr = task.getStartDate() != null
                ? dateTimeFormatter.format(task.getStartDate().atZone(ZoneId.systemDefault()))
                : "не указано";

        String endDateStr = task.getEndDate() != null
                ? dateTimeFormatter.format(task.getEndDate().atZone(ZoneId.systemDefault()))
                : "не указано";

        return String.format(
                "🔹 *%s*\n" +
                        "👨‍💼 Менеджер: %s\n" +
                        "🏷 Тег: %s\n" +
                        "📅 Начало: %s\n" +
                        "⏳ Окончание: %s\n" +
                        "📝 Описание: %s\n" +
                        "✅ Статус: %s\n" +
                        "⭐ Оценка: %s",
                task.getTaskName() != null ? task.getTaskName() : "не указано",
                task.getManagerName() != null ? task.getManagerName() : "не указано",
                task.getTag() != null ? task.getTag() : "не указано",
                startDateStr,
                endDateStr,
                task.getDescription() != null ? task.getDescription() : "не указано",
                task.getStatus() != null ? task.getStatus() : "не указан",
                task.getRating() != null ? task.getRating() : "не указана"
        );
    }

    public String getUserTasks(String login) {
        try (Connection conn = databaseConnectionManager.connectToDB("TaskManager_DB")) {
            String taskTableName = login.toLowerCase() + "TaskTable";
            if (!databaseConnectionManager.doesTableExist(conn, taskTableName)) {
                return "У вас пока нет задач";
            }
            List<Task> tasks = taskRepository.findUserTasksByLogin(conn, taskTableName);

            if (tasks.isEmpty()) {
                return "У вас нет задач.";
            }

            StringBuilder tasksMessage = new StringBuilder("Ваши задачи:\n\n");
            for (Task task : tasks) {
                tasksMessage.append(formatTask(task)).append("\n\n");
            }

            return tasksMessage.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Произошла ошибка при получении задач. Попробуйте позже.";
        }
    }

    public String getUserTasksToday(String login) {
        try (Connection conn = databaseConnectionManager.connectToDB("TaskManager_DB")) {
            String taskTableName = login.toLowerCase() + "TaskTable";
            if (!databaseConnectionManager.doesTableExist(conn, taskTableName)) {
                return "У вас пока нет задач";
            }

            String timeZoneStr = userRepository.getUserTimeZone(conn, "SystemUsers", login);
            if (timeZoneStr == null) {
                return "Сначала установите ваш часовой пояс с помощью /setcity";
            }
            ZoneId userZoneId = ZoneId.of(timeZoneStr);
            List<Task> allTasks = taskRepository.findUserTasksByLogin(conn, taskTableName);

            LocalDateTime today = LocalDateTime.now(userZoneId);

            List<Task> todayTasks = allTasks.stream()
                    .filter(task -> {
                        LocalDateTime startDate = task.getStartDate() != null
                                ? task.getStartDate().toLocalDate().atStartOfDay()
                                : null;
                        LocalDateTime endDate = task.getEndDate() != null
                                ? task.getEndDate().toLocalDate().atStartOfDay()
                                : null;

                        boolean isActiveToday = (startDate != null && endDate != null &&
                                (today.isEqual(startDate) || today.isEqual(endDate) ||
                                        (today.isAfter(startDate) && today.isBefore(endDate)))) ||
                                (startDate != null && endDate == null && today.isEqual(startDate)) ||
                                (startDate == null && endDate != null && today.isEqual(endDate));

                        boolean hasValidStatus = task.getStatus() != null &&
                                ("new".equalsIgnoreCase(task.getStatus()) ||
                                        "in progress".equalsIgnoreCase(task.getStatus()));

                        return isActiveToday && hasValidStatus;
                    })
                    .toList();

            if (todayTasks.isEmpty()) {
                return "На сегодня у вас нет активных задач со статусом 'new' или 'in progress'.";
            }

            StringBuilder tasksMessage = new StringBuilder("Ваши задачи на сегодня:\n\n");
            for (Task task : todayTasks) {
                tasksMessage.append(formatTask(task, userZoneId)).append("\n\n");
            }

            return tasksMessage.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Произошла ошибка при получении задач на сегодня. Попробуйте позже.";
        }
    }

    public String getUserTasksDueTomorrow(String login) {
        try (Connection conn = databaseConnectionManager.connectToDB("TaskManager_DB")) {
            String taskTableName = login.toLowerCase() + "TaskTable";
            if (!databaseConnectionManager.doesTableExist(conn, taskTableName)) {
                return "У вас пока нет задач";
            }
            String timeZoneStr = userRepository.getUserTimeZone(conn, "SystemUsers", login);
            if (timeZoneStr == null) {
                return "Сначала установите ваш часовой пояс с помощью /setcity";
            }
            ZoneId userZoneId = ZoneId.of(timeZoneStr);

            List<Task> allTasks = taskRepository.findUserTasksByLogin(conn, taskTableName);

            LocalDateTime tomorrow = LocalDateTime.now(userZoneId).plusDays(1);

            List<Task> tomorrowTasks = allTasks.stream()
                    .filter(task -> {
                        LocalDateTime endDate = task.getEndDate() != null
                                ? task.getEndDate().toLocalDate().atStartOfDay()
                                : null;

                        return endDate != null
                                && endDate.toLocalDate().isEqual(tomorrow.toLocalDate())
                                && !"done".equalsIgnoreCase(task.getStatus());
                    })
                    .toList();

            if (tomorrowTasks.isEmpty()) {
                return "На завтра дедлайнов нет! 🎉";
            }

            StringBuilder tasksMessage = new StringBuilder("⚠️ *Дедлайны на завтра:*\n\n");
            for (Task task : tomorrowTasks) {
                tasksMessage.append(formatTask(task)).append("\n\n");
            }

            return tasksMessage.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка при получении задач на завтра.";
        }
    }

}