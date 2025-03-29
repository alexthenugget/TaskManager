package com.github.alex_the_nugget.taskhub.taskhub.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.net.URI;
import javafx.stage.Stage;
import java.awt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotFormController {
    private static final Logger logger = LoggerFactory.getLogger(BotFormController.class);


    @FXML
    private void handleNextButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/alex_the_nugget/taskhub/taskhub/AdditionalInfo.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error("Ошибка при переходе на AdditionalInfo сцену", e);
        }
    }

    @FXML
    public void openTelegramBot() {
        try {
            Desktop.getDesktop().browse(new URI("tg://resolve?domain=task_manager_task_bot"));
        } catch (Exception e) {
            logger.warn("Не удалось открыть Telegram через десктопный протокол, пробуем веб-версию", e);
            try {
                Desktop.getDesktop().browse(new URI("https://t.me/task_manager_task_bot"));
            } catch (Exception ex) {
                logger.error("Не удалось открыть Telegram ссылку ни одним способом", ex);
            }
        }
    }
}