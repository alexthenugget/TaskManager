package com.github.alex_the_nugget.taskhub.taskhub.controllers.auth;

import com.github.alex_the_nugget.taskhub.taskhub.session.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import com.github.alex_the_nugget.taskhub.taskhub.services.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignUpController {
    private static final Logger logger = LoggerFactory.getLogger(SignUpController.class);
    private final AuthService authService = new AuthService();
    private final UserSession userSession = UserSession.getInstance();

    @FXML
    private TextField fullName;

    @FXML
    private TextField login;

    @FXML
    private TextField password;

    @FXML
    private Label response;

    @FXML
    private void handleSignUpButtonAction(ActionEvent event) {
        try {
            String nameText = fullName.getText();
            String loginText = login.getText();
            String passwordText = password.getText();
            userSession.setLogin(loginText);
            if (nameText.isEmpty() || loginText.isEmpty() || passwordText.isEmpty()) {
                response.setText("There is an empty field");
                logger.warn("Attempt to sign up with empty fields");
                return;
            }

            authService.signUpAddUser(nameText, passwordText);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/alex_the_nugget/taskhub/taskhub/BotForm.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            logger.error("Error during sign up process for login: {}", login.getText(), e);
            response.setText("An error occurred during registration");
        }
    }

    @FXML
    private void handleSignInButtonAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/alex_the_nugget/taskhub/taskhub/SignIn.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error("Error navigating to sign in page", e);
            response.setText("An error occurred while navigating");
        }
    }
}