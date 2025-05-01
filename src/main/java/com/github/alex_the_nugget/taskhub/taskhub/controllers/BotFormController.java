package com.github.alex_the_nugget.taskhub.taskhub.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import java.net.URI;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;

public class BotFormController {
    @FXML
    private Button nextButton;

    private String userLogin;

    public void setUserLogin(String login) {
        this.userLogin = login;
    }

    @FXML
    private void handleNextButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/alex_the_nugget/taskhub/taskhub/AdditionalInfo.fxml"));
            Parent root = loader.load();
            AdditionalInfoController additionalInfoController = loader.getController();
            additionalInfoController.setUserLogin(userLogin);
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openTelegramBot() {
        try {
            Desktop.getDesktop().browse(new URI("tg://resolve?domain=task_manager_task_bot"));
        } catch (Exception e) {
            try {
                Desktop.getDesktop().browse(new URI("https://t.me/task_manager_task_bot"));
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Не удалось открыть ссылку!");
            }
        }
    }

}
