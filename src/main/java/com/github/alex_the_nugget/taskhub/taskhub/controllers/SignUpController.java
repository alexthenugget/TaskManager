package com.github.alex_the_nugget.taskhub.taskhub.controllers;
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

public class SignUpController {

    private final AuthService authService = new AuthService();

    @FXML
    private TextField fullName;

    @FXML
    private TextField login;

    @FXML
    private TextField password;

    @FXML
    private Button signUpButton;

    @FXML
    private Button signInButton;

    @FXML
    private Label response;

    @FXML
    private void handleSignUpButtonAction(ActionEvent event) {
        String nameText = fullName.getText();
        String loginText = login.getText();
        String passwordText = password.getText();
        if (nameText.isEmpty() || loginText.isEmpty() || passwordText.isEmpty()) {
            response.setText("There is an empty field");
        } else {
            authService.signUpAddUser(nameText, loginText, passwordText);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/alex_the_nugget/taskhub/taskhub/BotForm.fxml"));
                Parent root = loader.load();
                BotFormController botFormController = loader.getController();
                botFormController.setUserLogin(loginText);
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            e.printStackTrace();
        }
    }
}
