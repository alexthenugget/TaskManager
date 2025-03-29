package com.github.alex_the_nugget.taskhub.taskhub.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    public boolean searchByLogin(Connection conn, String nameDB, String login) {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM " + nameDB + " WHERE login = ?")) {
            statement.setString(1, login);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            log.error("Error searching by login: ", e);
            return false;
        }
    }

    public boolean searchByLoginAndPasswordCheck(Connection conn, String nameDB, String login, String password) {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM " + nameDB + " WHERE login = ?")) {
            statement.setString(1, login);
            try (ResultSet rs = statement.executeQuery()) {
                return checkPassword(rs, password);
            }
        } catch (Exception e) {
            log.error("Error verifying password: ", e);
            return false;
        }
    }

    private boolean checkPassword(ResultSet rs, String inputPassword) throws SQLException {
        if (rs.next()) {
            String dbPassword = rs.getString("password");
            return dbPassword.equals(inputPassword);
        }
        return false;
    }

    public void addNewUser(Connection conn, String tableName, String name, String login, String password) {
        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO " + tableName + "(name, login, password) VALUES (?, ?, ?)")) {
            statement.setString(1, name);
            statement.setString(2, login);
            statement.setString(3, password);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error adding new user: ", e);
        }
    }

    public void updateNotificationTime(Connection conn, String nameDB, String login, String time){
        String sql = "UPDATE " + nameDB + " SET notificationtime = ? WHERE login = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTime(1, Time.valueOf(time));
            stmt.setString(2, login);
            stmt.executeUpdate();
        } catch (Exception e) {
        log.error("Error updating time: ", e);
        }
    }

    public void updateTimeZone(Connection conn, String nameDB, String login, String timezone) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE " + nameDB + " SET timezone = ? WHERE login = ?")) {
            stmt.setString(1, timezone);
            stmt.setString(2, login);
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Error updating time zone: ", e);
        }
    }

    public void updateUserId(Connection conn, String nameDB, String login, long botId) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE " + nameDB + " SET chatid = ? WHERE login = ?")) {
            stmt.setLong(1, botId);
            stmt.setString(2, login);
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Error updating user ID: ", e);
        }
    }

    public String getUserTimeZone(Connection conn, String tableName, String login) throws SQLException {
        String sql = "SELECT timezone FROM " + tableName + " WHERE login = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("timezone") : null;
        }
    }

    public void updateUserPositionAndDepartment(Connection conn, String nameDB, String login,
                                                String position, String department) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE " + nameDB + " SET position = ?, department = ? WHERE login = ?")) {
            stmt.setString(1, position);
            stmt.setString(2, department);
            stmt.setString(3, login);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating user position and department: ", e);
        }
    }

    public String findLoginByUserName(Connection conn, String nameDB, String name) {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT login FROM " + nameDB + " WHERE name = ?")) {
            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getString("login") : null;
            }
        } catch (Exception e) {
            log.error("Error finding login by username: ", e);
            return null;
        }
    }

    public String findDepartmentByLogin(Connection conn, String nameDB, String login) {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT department FROM " + nameDB + " WHERE login = ?")) {
            statement.setString(1, login);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getString("department") : null;
            }
        } catch (Exception e) {
            log.error("Error finding department by login: ", e);
            return null;
        }
    }

    public String findPositionByLogin(Connection conn, String nameDB, String login) {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT position FROM " + nameDB + " WHERE login = ?")) {
            statement.setString(1, login);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getString("position") : null;
            }
        } catch (Exception e) {
            log.error("Error finding position by login: ", e);
            return null;
        }
    }

    public String findNameByLogin(Connection conn, String nameDB, String login) {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT name FROM " + nameDB + " WHERE login = ?")) {
            statement.setString(1, login);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getString("name") : null;
            }
        } catch (Exception e) {
            log.error("Error finding name by login: ", e);
            return null;
        }
    }

    public String[] findEmployeesByDepartment(Connection conn, String nameDB, String managerLogin) {
        String managerDepartment = findDepartmentByLogin(conn, nameDB, managerLogin);
        if (managerDepartment == null) {
            return new String[0];
        }

        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT name FROM " + nameDB + " WHERE department = ?")) {
            statement.setString(1, managerDepartment);
            try (ResultSet rs = statement.executeQuery()) {
                List<String> employeesList = new ArrayList<>();
                while (rs.next()) {
                    employeesList.add(rs.getString("name"));
                }
                return employeesList.toArray(new String[0]);
            }
        } catch (Exception e) {
            log.error("Error finding employees by department: ", e);
            return new String[0];
        }
    }

    public Long getUserChatId(Connection conn, String tableName, String login) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT chatid FROM " + tableName + " WHERE name = ?")) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong("chatid") : null;
            }
        }
    }
}