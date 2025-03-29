package com.github.alex_the_nugget.taskhub.taskhub;

import com.github.alex_the_nugget.taskhub.taskhub.database.DatabaseConnectionManager;
import com.github.alex_the_nugget.taskhub.taskhub.controllers.bot.TelegramBot;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.sql.Connection;

public class TaskHubApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(TaskHubApplication.class);



    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TaskHubApplication.class.getResource("SignIn.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        var appIcon = new Image(getClass().getResourceAsStream("/com/github/alex_the_nugget/taskhub/taskhub" +
                "/images/task-list.png"));
        stage.getIcons().add(appIcon);
        stage.setTitle("TaskManager");
        stage.setScene(scene);
        stage.show();
        String databaseName = "SystemUsers";
        DatabaseConnectionManager databaseConnectionManager = new DatabaseConnectionManager();
        Connection conn = databaseConnectionManager.connectToDB("TaskManager_DB");
        if (!databaseConnectionManager.doesTableExist(conn, databaseName)) {
            databaseConnectionManager.createUsersTable(conn, databaseName);
        }
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(TelegramBot.getInstance());
        } catch (Exception e) {
            logger.error("Failed to register Telegram bot", e);
        }

    }

    public static void main(String[] args) {
        launch();
    }
}