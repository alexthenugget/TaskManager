package com.github.alex_the_nugget.taskhub.taskhub.controllers.employee;

import com.github.alex_the_nugget.taskhub.taskhub.controllers.shared.BaseCheckTasksController;
import com.github.alex_the_nugget.taskhub.taskhub.models.Task;
import com.github.alex_the_nugget.taskhub.taskhub.services.EmployeeService;
import com.github.alex_the_nugget.taskhub.taskhub.services.StatisticsService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class EmployeeCheckTasksController extends BaseCheckTasksController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeCheckTasksController.class);
    private final EmployeeService employeeService;
    private final StatisticsService statisticsService;

    public EmployeeCheckTasksController() {
        this.employeeService = new EmployeeService();
        this.statisticsService = new StatisticsService();
    }

    @FXML
    public void initialize() {
        loadTasks();
    }

    @Override
    protected List<Task> getTasksList() {
        List<Task> tasks = employeeService.returnTasksList();
        if (tasks == null) {
            response.setText("There are no tasks yet.");
        }
        return tasks;
    }

    @Override
    protected void handleTaskClick(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/alex_the_nugget/taskhub/taskhub/EmployeeTaskInfo.fxml"));
            Parent root = loader.load();

            EmployeeTaskInfoController taskInfoController = loader.getController();
            taskInfoController.setEmployeeService(employeeService);
            taskInfoController.setTask(task);
            taskInfoController.setRefreshCallback(v -> loadTasks());

            Stage stage = createModalStage(root, "Task Details");
            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Failed to load task details for task {}", task.getId(), e);
            response.setText("Error loading task details");
        }
    }

    @Override
    @FXML
    protected void checkStatistics() {
        loadFXMLPage(
                "/com/github/alex_the_nugget/taskhub/taskhub/EmployeeStatisticsPage.fxml",
                imageStatistics.getScene().getWindow(),
                controller -> {
                    ((EmployeeStatisticsController) controller).setStatisticsService(statisticsService);
                }
        );
    }

    private void loadFXMLPage(String fxmlPath, Window window, Consumer<Object> controllerSetup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            controllerSetup.accept(loader.getController());

            Stage stage = (Stage) window;
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load FXML page: {}", fxmlPath, e);
        }
    }

    private Stage createModalStage(Parent root, String title) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/github/alex_the_nugget/taskhub/taskhub/images/task-list.png")));
        return stage;
    }
}