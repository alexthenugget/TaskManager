package com.github.alex_the_nugget.taskhub.taskhub.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private final String user = System.getenv("userDB");
    private final String password = System.getenv("passwordDB");

    public Connection connectToDB(String dbName) {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + dbName, user, password);
        } catch (Exception e) {
            log.error("Error connecting to database: ", e);
        }
        return conn;
    }

    public void createUsersTable(Connection conn, String dbName) {
        try (PreparedStatement statement = conn.prepareStatement(
                "create table " + dbName + "(id SERIAL, name varchar(200), login varchar(100)," +
                        "password varchar(100), position varchar(100), " +
                        "department varchar(200), chatId bigint, timezone varchar(50), notificationtime time, primary key(id))")) {
            statement.executeUpdate();
        } catch (Exception e) {
            log.error("Error creating users table: ", e);
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
        }
    }

    public boolean doesTableExist(Connection conn, String tableName) {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)")) {
            statement.setString(1, tableName.toLowerCase());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
                return false;
            }
        } catch (Exception e) {
            log.error("Error checking if table exists: ", e);
            return false;
        }
    }
}