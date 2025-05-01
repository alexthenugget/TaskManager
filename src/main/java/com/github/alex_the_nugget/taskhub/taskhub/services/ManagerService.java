package com.github.alex_the_nugget.taskhub.taskhub.services;

import com.github.alex_the_nugget.taskhub.taskhub.database.DataBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class ManagerService {
    private static final Logger log = LoggerFactory.getLogger(ManagerService.class);
    private final DataBase userServiceDB = new DataBase();
    private final String nameDB = "TaskManager_DB";
    private final String nameUsersTable = "SystemUsers";

    public String[] returnAnEmployeeList(String managerLogin){
        try (Connection conn = userServiceDB.connectToDB(nameDB)) {
            return userServiceDB.findEmployeesByDepartment(conn, nameUsersTable, managerLogin);
        }
        catch(Exception e){
            log.error("e: ", e);
            return null;
        }
    }

    public void addNewUserTask(String managerLogin, String employee, String tag, String taskName,
                               String startDate, String endDate, String taskDescription)
    {
        try (Connection conn = userServiceDB.connectToDB(nameDB)) {
            userServiceDB.addNewUserTask(conn, nameUsersTable, managerLogin, employee, tag, taskName,
                    startDate, endDate, taskDescription);
        }
        catch(Exception e){
            log.error("e: ", e);
        }
    }
}
