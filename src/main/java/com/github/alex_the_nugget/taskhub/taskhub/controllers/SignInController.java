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

import java.io.IOException;

public class SignInController {

    private final AuthService authService = new AuthService();

    @FXML
    private TextField login;

    @FXML
    private TextField password;

    @FXML
    private Button signInButton;

    @FXML
    private Button signUpButton;

    @FXML
    private Label response;

    @FXML
    private void handleSignInButtonAction(ActionEvent event) {
        String loginText = login.getText();
        String passwordText = password.getText();
        if (loginText.isEmpty() || passwordText.isEmpty()) {
            response.setText("There is an empty field");
        }
        else {
            if (authService.signInCheckingService(loginText, passwordText)) {
                try {
                    String position = authService.returnUsersPosition(loginText);
                    String fxmlFile = position.equals("Manager")
                            ? "/com/github/alex_the_nugget/taskhub/taskhub/AssignTasksPage.fxml"
                            : "/path/to/employee_interface.fxml";

                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                    Parent root = loader.load();

                    if (position.equals("Manager")) {
                        ManagerAssignTasksController managerController = loader.getController();
                        managerController.setUserLogin(loginText);
                    } else {
                        EmployeeCheckTasksController employeeCheckTasksController = loader.getController();
                        employeeCheckTasksController.setUserLogin(loginText);
                    }

                    Stage stage = (Stage) signInButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                response.setText("There is no such user in the system");
            }
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
            e.printStackTrace();
        }
    }
}
