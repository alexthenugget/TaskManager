package com.github.alex_the_nugget.taskhub.taskhub.services;

import com.github.alex_the_nugget.taskhub.taskhub.database.DataBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;


public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final DataBase userServiceDB = new DataBase();
    private final String nameDB = "TaskManager_DB";
    private final String nameUsersTable = "SystemUsers";

    public boolean signInCheckingService(String loginString, String passwordString){
        try (Connection conn = userServiceDB.connectToDB(nameDB)) {
            return userServiceDB.searchByLoginAndPasswordCheck(conn, nameUsersTable, loginString, passwordString);
        }
        catch(Exception e){
            log.error("e: ", e);
            return false;
        }
    }

    public void signUpAddUser(String nameString, String loginString, String passwordString){
        try (Connection conn = userServiceDB.connectToDB(nameDB)) {
            userServiceDB.addNewUser(conn, nameUsersTable, nameString, loginString, passwordString);
        }
        catch(Exception e){
            log.error("e: ", e);
        }
    }

    public void updateAdditionalInfo(String login, String position, String department) {
        try (Connection conn = userServiceDB.connectToDB(nameDB)) {
            userServiceDB.updateUserPositionAndDepartment(conn, nameUsersTable, login, position, department);
        }
        catch(Exception e){
            log.error("e: ", e);
        }
    }

    public String returnUsersPosition(String login){
        try (Connection conn = userServiceDB.connectToDB(nameDB)) {
            return userServiceDB.findPositionByLogin(conn, nameUsersTable, login);
        }
        catch(Exception e){
            log.error("e: ", e);
            return "";
        }
    }
}
