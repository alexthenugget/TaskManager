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

import java.io.IOException;

public class SignInController {
    private static final Logger logger = LoggerFactory.getLogger(SignInController.class);
    private final AuthService authService = new AuthService();
    private final UserSession userSession = UserSession.getInstance();

    @FXML
    private TextField login;

    @FXML
    private TextField password;

    @FXML
    private Button signInButton;

    @FXML
    private Label response;

    @FXML
    private void handleSignInButtonAction(ActionEvent event) {
        try {
            String loginText = login.getText();
            String passwordText = password.getText();
            userSession.setLogin(loginText);
            if (loginText.isEmpty() || passwordText.isEmpty()) {
                response.setText("There is an empty field");
                logger.warn("Attempt to sign in with empty fields");
                return;
            }

            if (!authService.signInCheckingService(passwordText)) {
                response.setText("There is no such user in the system");
                logger.warn("Failed sign in attempt for login: {}", loginText);
                return;
            }


            String position = authService.returnUsersPosition();
            String fxmlFile = position.equals("Manager")
                    ? "/com/github/alex_the_nugget/taskhub/taskhub/AssignTasksPage.fxml"
                    : "/com/github/alex_the_nugget/taskhub/taskhub/EmployeeCheckTasksPage.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) signInButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            logger.error("Error during sign in process", e);
            response.setText("An error occurred during sign in");
            throw new RuntimeException("Error during sign in process", e);
        }
    }

    @FXML
    private void handleSignUpButtonAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/alex_the_nugget/taskhub/taskhub/SignUp.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error("Error navigating to sign up page", e);
            response.setText("An error occurred while navigating");
        }
    }
}