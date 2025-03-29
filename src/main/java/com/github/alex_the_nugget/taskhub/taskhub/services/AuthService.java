package com.github.alex_the_nugget.taskhub.taskhub.services;

import com.github.alex_the_nugget.taskhub.taskhub.database.DatabaseConnectionManager;
import com.github.alex_the_nugget.taskhub.taskhub.database.UserRepository;
import com.github.alex_the_nugget.taskhub.taskhub.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;


public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository = new UserRepository();
    private final DatabaseConnectionManager databaseConnectionManager = new DatabaseConnectionManager();
    private final String nameDB = "TaskManager_DB";
    private final String nameUsersTable = "SystemUsers";
    private final UserSession userSession = UserSession.getInstance();

    public boolean signInCheckingService(String passwordString){
        String userLogin = userSession.getLogin();
        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            return userRepository.searchByLoginAndPasswordCheck(conn, nameUsersTable, userLogin, passwordString);
        }
        catch(Exception e){
            log.error("e: ", e);
            return false;
        }
    }

    public void signUpAddUser(String nameString, String passwordString){
        String userLogin = userSession.getLogin();
        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            userRepository.addNewUser(conn, nameUsersTable, nameString, userLogin, passwordString);
        }
        catch(Exception e){
            log.error("e: ", e);
        }
    }

    public void updateAdditionalInfo(String position, String department) {
        String userLogin = userSession.getLogin();
        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            userRepository.updateUserPositionAndDepartment(conn, nameUsersTable, userLogin, position, department);
        }
        catch(Exception e){
            log.error("e: ", e);
        }
    }

    public String returnUsersPosition(){
        String userLogin = userSession.getLogin();
        try (Connection conn = databaseConnectionManager.connectToDB(nameDB)) {
            return userRepository.findPositionByLogin(conn, nameUsersTable, userLogin);
        }
        catch(Exception e){
            log.error("e: ", e);
            return "";
        }
    }
}
