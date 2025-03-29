package com.github.alex_the_nugget.taskhub.taskhub.database;

import com.github.alex_the_nugget.taskhub.taskhub.models.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private static final Logger log = LoggerFactory.getLogger(TaskRepository.class);

    public void addNewUserTask(Connection conn, String nameUsersTable, String managerLogin,
                               String employee, String tag, String taskName,
                               String startDate, String endDate, String taskDescription,
                               UserRepository userRepository) throws SQLException {

        String employeeLogin = userRepository.findLoginByUserName(conn, nameUsersTable, employee);
        String managerName = userRepository.findNameByLogin(conn, nameUsersTable, managerLogin);
        String tableName = (employeeLogin + "TaskTable").toLowerCase();

        String insertSQL = "INSERT INTO " + tableName +
                " (id, managerName, tag, taskName, startDate, endDate, description) " +
                "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL,
                Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, managerName);
            pstmt.setString(2, tag);
            pstmt.setString(3, taskName);
            pstmt.setString(4, startDate);
            pstmt.setString(5, endDate);
            pstmt.setString(6, taskDescription);

            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }
        }
    }

    public List<Task> findUserTasksByLogin(Connection conn, String nameTable) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM " + nameTable + " ORDER BY tag, startdate";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setManagerName(rs.getString("managername"));
                task.setTag(rs.getString("tag"));
                task.setTaskName(rs.getString("taskname"));
                task.setStartDate(rs.getTimestamp("startdate").toLocalDateTime());
                task.setEndDate(rs.getTimestamp("enddate") != null ?
                        rs.getTimestamp("enddate").toLocalDateTime() : null);
                task.setDescription(rs.getString("description"));
                task.setStatus(rs.getString("status"));
                task.setRating(rs.getString("rating"));

                tasks.add(task);
            }
        }
        return tasks;
    }

    public void updateTaskStatus(Connection conn, String userTasksTable, int id, String newStatus) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE " + userTasksTable + " SET status = ? WHERE id = ?")) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating task status: ", e);
        }
    }

    public void updateTaskRating(Connection conn, String userTasksTable, int id, String rating) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE " + userTasksTable + " SET rating = ? WHERE id = ?")) {
            stmt.setString(1, rating);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating task rating: ", e);
        }
    }


}