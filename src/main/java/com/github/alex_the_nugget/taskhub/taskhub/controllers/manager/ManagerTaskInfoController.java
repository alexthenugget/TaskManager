package com.github.alex_the_nugget.taskhub.taskhub.controllers.manager;

import com.github.alex_the_nugget.taskhub.taskhub.controllers.shared.CommonTaskFunctionality;
import com.github.alex_the_nugget.taskhub.taskhub.services.ManagerService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ManagerTaskInfoController extends CommonTaskFunctionality {
    private ManagerService managerService;

    @FXML
    Button okButton;

    @FXML
    private void initialize() {
        okButton.setOnAction(event -> {
            Stage stage = (Stage) okButton.getScene().getWindow();
            stage.close();
        });
    }

    public void setManagerService(ManagerService managerService) {
        this.managerService = managerService;
    }

    @Override
    protected void updateTaskStatus(int taskId, String status) {
        managerService.updateTaskStatus(taskId, status);
    }

    @Override
    protected void updateTaskRating(int taskId, String rating) {
        managerService.updateTaskRating(taskId, rating);
    }
}