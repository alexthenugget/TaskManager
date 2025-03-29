package com.github.alex_the_nugget.taskhub.taskhub.services;

import com.github.alex_the_nugget.taskhub.taskhub.database.DatabaseConnectionManager;
import com.github.alex_the_nugget.taskhub.taskhub.database.TaskRepository;
import com.github.alex_the_nugget.taskhub.taskhub.models.Task;
import com.github.alex_the_nugget.taskhub.taskhub.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final TaskRepository taskRepository = new TaskRepository();
    private final DatabaseConnectionManager databaseConnectionManager = new DatabaseConnectionManager();
    private final String nameDB = "TaskManager_DB";
    private final UserSession userSession = UserSession.getInstance();

    public List<Task> returnTasksList() {
        String userLogin = userSession.getLogin();
        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            String userTasksTable = userLogin + "tasktable";

            if (!databaseConnectionManager.doesTableExist(conn, userTasksTable)) {
                return Collections.emptyList();
            }

            List<Task> tasks = taskRepository.findUserTasksByLogin(conn, userTasksTable);
            return tasks.isEmpty() ? Collections.emptyList() : tasks;
        }
        catch(SQLException e) {
            if (e.getMessage().contains("does not exist")) {
                return Collections.emptyList();
            }
            log.error("Database error: ", e);
            return null;
        }
        catch(Exception e) {
            log.error("Unexpected error: ", e);
            return null;
        }
    }

    public void updateTaskStatus(int id, String newStatus) {
        String userLogin = userSession.getLogin();
        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            String userTasksTable = userLogin + "tasktable";
            taskRepository.updateTaskStatus(conn, userTasksTable, id, newStatus);
        }
        catch(Exception e) {
            log.error("e: ", e);
        }
    }

    public void updateTaskRating(int id, String rating) {
        String userLogin = userSession.getLogin();
        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            String userTasksTable = userLogin + "tasktable";
            taskRepository.updateTaskRating(conn, userTasksTable, id, rating);
        }
        catch(Exception e) {
            log.error("e: ", e);
        }
    }
}