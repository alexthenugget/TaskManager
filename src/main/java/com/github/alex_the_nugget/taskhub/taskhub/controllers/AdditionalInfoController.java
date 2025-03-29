package com.github.alex_the_nugget.taskhub.taskhub.controllers;

import com.github.alex_the_nugget.taskhub.taskhub.services.AuthService;
import com.github.alex_the_nugget.taskhub.taskhub.session.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AdditionalInfoController {
    private static final Logger logger = LoggerFactory.getLogger(AdditionalInfoController.class);

    private final AuthService authService = new AuthService();
    private final UserSession userSession = UserSession.getInstance();

    @FXML
    private ComboBox<String> position;

    @FXML
    private ComboBox<String> department;

    @FXML
    private Button startButton;

    @FXML
    public void handlePositionComboBox(ActionEvent event) {
        checkSelections();
    }

    @FXML
    public void handleDepartmentComboBox(ActionEvent event) {
        checkSelections();
    }

    @FXML
    public void initialize() {
        position.getItems().addAll("Manager", "Employee");
        department.getItems().addAll(
                "Frontend Development",
                "Backend Development",
                "Mobile Development",
                "Testing Development",
                "DevOps Development",
                "Design",
                "Product Management"
        );

        startButton.setDisable(true);
        position.valueProperty().addListener((_, _, _) -> checkSelections());
        department.valueProperty().addListener((_, _, _) -> checkSelections());
    }

    private void checkSelections() {
        startButton.setDisable(position.getValue() == null || department.getValue() == null);
    }

    @FXML
    public void handleStartButton(ActionEvent event) {
        String selectedPosition = position.getValue();
        String selectedDepartment = department.getValue();
        String userLogin = userSession.getLogin();

        if (userLogin != null) {
            updateUserInfo(selectedPosition, selectedDepartment);

            try {
                String fxmlFile = selectedPosition.equals("Manager")
                        ? "/com/github/alex_the_nugget/taskhub/taskhub/AssignTasksPage.fxml"
                        : "/com/github/alex_the_nugget/taskhub/taskhub/EmployeeCheckTasksPage.fxml";

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();

                Stage stage = (Stage) startButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                logger.error("Failed to load FXML file for {}: {}", selectedPosition, e.getMessage(), e);
            }
        } else {
            logger.error("No user login found in session");
        }
    }

    private void updateUserInfo(String position, String department) {
        authService.updateAdditionalInfo(position, department);
    }
}