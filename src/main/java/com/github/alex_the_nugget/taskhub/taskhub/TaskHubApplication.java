package com.github.alex_the_nugget.taskhub.taskhub;

import com.github.alex_the_nugget.taskhub.taskhub.database.DataBase;
import com.github.alex_the_nugget.taskhub.taskhub.services.TelegramBot;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;


import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.sql.Connection;

public class TaskHubApplication extends Application {
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
        DataBase DB = new DataBase();
        Connection conn = DB.connectToDB("TaskManager_DB");
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch();
    }
}