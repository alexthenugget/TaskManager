package com.github.alex_the_nugget.taskhub.taskhub.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBase {
    private static final Logger log = LoggerFactory.getLogger(DataBase.class);
    private final String user = System.getenv("userDB");
    private final String password = System.getenv("passwordDB");

    public Connection connectToDB(String dbName) {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + dbName, user, password);
            if (conn != null) {
                System.out.println("Connection Established");
            } else {
                System.out.println("Connection Failed");
            }
        } catch (Exception e) {
            log.error("e: ", e);
        }
        return conn;
    }

    public void createUsersTable(Connection conn, String dbName) {
        PreparedStatement statement;
        try {
            String query = "create table " + dbName + "(id SERIAL, name varchar(200), login varchar(100)," +
                    "password varchar(100), position varchar(100), " +
                    "department varchar(200), chatId bigint, city varchar(200), primary key(id));";
            statement = conn.prepareStatement(query);
            statement.executeUpdate();
            log.info("Table created");
        } catch (Exception e) {
            log.error("e: ", e);
        }
    }

    public boolean searchByLogin(Connection conn, String nameDB, String login) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            String query = "SELECT * FROM " + nameDB + " WHERE login = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, login);
            rs = statement.executeQuery();
            return rs.next();
        } catch (Exception e) {
            log.error("Error searching by login: ", e);
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
            } catch (Exception e) {
                log.error("Error closing resources: ", e);
            }
        }
    }

    public boolean searchByLoginAndPasswordCheck(Connection conn, String nameDB, String login, String password) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        boolean find;
        try {
            String query = "SELECT * FROM " + nameDB + " WHERE login = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, login);
            rs = statement.executeQuery();
            find = checkPassword(rs, password);
            return find;
        } catch (Exception e) {
            log.error("e: ", e);
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
            } catch (Exception e) {
                log.error("e: ", e);
            }
        }
    }

    private boolean checkPassword(ResultSet rs, String inputPassword) throws SQLException {
        if (rs.next()) {
            String dbPassword = rs.getString("password");
            if (dbPassword.equals(inputPassword)) {
                System.out.println("User found and password is correct.");
                return true;
            } else {
                System.out.println("Password is incorrect.");
                return false;
            }
        }
        return false;
    }

    public void addNewUser(Connection conn, String tableName, String name, String login, String password) {
        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO " + tableName + "(name, login, password) VALUES (?, ?, ?)")) {

            // Set parameters safely (prevents SQL injection)
            statement.setString(1, name);
            statement.setString(2, login);
            statement.setString(3, password);

            statement.executeUpdate();
            System.out.println("New user inserted successfully.");
        } catch (SQLException e) {
            log.error("Error adding new user: ", e);
        }
    }

    public void updateCity(Connection conn, String nameDB, String login, String city) {
        String sql = "UPDATE " + nameDB + " SET city = ? WHERE login = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, city);
            stmt.setString(2, login);
            stmt.executeUpdate();
        }catch (Exception e) {
            log.error("e: ", e);
        }
    }

    public void updateUserId(Connection conn, String nameDB, String login, long botId){
        String sql = "UPDATE " + nameDB + " SET chatid = ? WHERE login = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, botId);
            stmt.setString(2, login);
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("e: ", e);
        }
    }

    public void updateUserPositionAndDepartment(Connection conn, String nameDB, String login,
                                                String position, String department) {
        String sql = "UPDATE " + nameDB + " SET position = ?, department = ? WHERE login = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, position);
            stmt.setString(2, department);
            stmt.setString(3, login);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error updating user position and department: ", e);
        }
    }

    public String findLoginByUserName(Connection conn, String nameDB, String name) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            String query = "SELECT login FROM " + nameDB + " WHERE name = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, name);
            rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getString("login");
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Error searching by login: ", e);
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
            } catch (Exception e) {
                log.error("Error closing resources: ", e);
            }
        }
    }

    public void addNewUserTask(Connection conn, String nameUsersTable, String managerLogin,
                               String employee, String tag, String taskName,
                               String startDate, String endDate, String taskDescription)
            throws SQLException {

        String employeeLogin = findLoginByUserName(conn, nameUsersTable, employee);
        String managerName = findNameByLogin(conn, nameUsersTable, managerLogin);
        String tableName = (employeeLogin + "TaskTable").toLowerCase();

        createTasksTable(conn, tableName);

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

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    log.info("Task created with ID: {}", generatedKeys.getLong(1));
                }
            }
        }
    }

    public void createTasksTable(Connection conn, String dbName) throws SQLException {
        String tableName = dbName.toLowerCase();

        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id SERIAL PRIMARY KEY, " +
                "managerName VARCHAR(200), " +
                "tag VARCHAR(100), " +
                "taskName VARCHAR(500), " +
                "startDate VARCHAR(100), " +
                "endDate VARCHAR(100), " +
                "description VARCHAR(2000), " +
                "status VARCHAR(50) DEFAULT 'New', " +
                "rating VARCHAR(50) DEFAULT 'Ok')";

        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate(query);
            log.info("Table created or verified: {}", tableName);
        }
    }

    public String[] findEmployeesByDepartment(Connection conn, String nameDB, String managerLogin) {
        String managerDepartment = findDepartmentByLogin(conn, nameDB, managerLogin);
        if (managerDepartment == null) {
            return new String[0];
        }
        List<String> employeesList = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            String query = "SELECT name FROM " + nameDB + " WHERE department = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, managerDepartment);
            rs = statement.executeQuery();
            while (rs.next()) {
                employeesList.add(rs.getString("name"));
            }
            return employeesList.toArray(new String[0]);
        } catch (Exception e) {
            log.error("Error searching by department: ", e);
            return new String[0];
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
            } catch (Exception e) {
                log.error("Error closing resources: ", e);
            }
        }
    }

    public String findDepartmentByLogin(Connection conn, String nameDB, String login) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            String query = "SELECT department FROM " + nameDB + " WHERE login = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, login);
            rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getString("department");
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Error searching by login: ", e);
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
            } catch (Exception e) {
                log.error("Error closing resources: ", e);
            }
        }
    }

    public String findPositionByLogin(Connection conn, String nameDB, String login) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            String query = "SELECT position FROM " + nameDB + " WHERE login = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, login);
            rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getString("position");
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Error searching by login: ", e);
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
            } catch (Exception e) {
                log.error("Error closing resources: ", e);
            }
        }
    }

    public String findNameByLogin(Connection conn, String nameDB, String login) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            String query = "SELECT name FROM " + nameDB + " WHERE login = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, login);
            rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("Error searching by login: ", e);
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (statement != null) statement.close();
            } catch (Exception e) {
                log.error("Error closing resources: ", e);
            }
        }
    }
}

