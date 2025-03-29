package com.github.alex_the_nugget.taskhub.taskhub.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

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
            System.out.println("Table created");
        } catch (Exception e) {
            log.error("e: ", e);
        }
    }

    public void createTasksTable(Connection conn, String dbName) {
        PreparedStatement statement;
        try {
            String query = "create table " + dbName + "(id SERIAL, name varchar(200), login varchar(100)," +
                    "password varchar(100), position varchar(100), " +
                    "department varchar(200), chatId bigint, city varchar(200), primary key(id));";
            statement = conn.prepareStatement(query);
            statement.executeUpdate();
            System.out.println("Table created");
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

    public void addNewUser(Connection conn, String nameDB, String name, String login, String password){
        PreparedStatement statement;
        try {
            String query = String.format("insert into %s(name, login, password) " +
                    "values ('%s', '%s', '%s');", nameDB, name, login, password);
            statement = conn.prepareStatement(query);
            statement.executeUpdate();
            System.out.println("New user inserted.");
        } catch (Exception e) {
            log.error("e: ", e);
        }
    }

    public void updateCity(Connection conn, String nameDB, String login, String city) throws SQLException {
        String sql = "UPDATE " + nameDB + ".users SET city = ? WHERE login = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, city);
            statement.setString(2, login);
        }catch (Exception e) {
            log.error("e: ", e);
        }

    }

    public void updateUserId(Connection conn, String nameDB, String login, long botId){
        String sql = "UPDATE " + nameDB + ".users SET botId = ? WHERE login = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, botId);
            statement.setString(2, login);
        }catch (Exception e) {
            log.error("e: ", e);
        }
    }
}

