package com.github.alex_the_nugget.taskhub.taskhub.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.net.URI;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

import java.awt.*;

public class BotFormController {
    @FXML
    private Button nextButton;


    @FXML
    private void handleNextButton(ActionEvent event) {

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
