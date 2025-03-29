package com.github.alex_the_nugget.taskhub.taskhub.controllers.manager;

import com.github.alex_the_nugget.taskhub.taskhub.controllers.shared.BaseCheckTasksController;
import com.github.alex_the_nugget.taskhub.taskhub.models.Task;
import com.github.alex_the_nugget.taskhub.taskhub.services.ManagerService;
import com.github.alex_the_nugget.taskhub.taskhub.services.StatisticsService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class ManagerCheckTasksController extends BaseCheckTasksController {
    private static final Logger logger = LoggerFactory.getLogger(ManagerCheckTasksController.class);
    private final ManagerService managerService;
    private final StatisticsService statisticsService;

    @FXML
    private ImageView imageAssignTask;

    public ManagerCheckTasksController() {
        this.managerService = new ManagerService();
        this.statisticsService = new StatisticsService();
    }

    @FXML
    public void initialize() {
        loadTasks();
    }

    @Override
    protected List<Task> getTasksList() {
        return managerService.returnTasksList();
    }

    @Override
    protected void handleTaskClick(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/alex_the_nugget/taskhub/taskhub/ManagerTaskInfo.fxml"));
            Parent root = loader.load();

            ManagerTaskInfoController taskInfoController = loader.getController();
            taskInfoController.setManagerService(managerService);
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
                "/com/github/alex_the_nugget/taskhub/taskhub/ManagerStatisticsPage.fxml",
                imageStatistics.getScene().getWindow(),
                controller -> ((ManagerStatisticsController) controller).setStatisticsService(statisticsService)
        );
    }

    @FXML
    protected void assignTask() {
        loadFXMLPage(
                "/com/github/alex_the_nugget/taskhub/taskhub/AssignTasksPage.fxml",
                imageAssignTask.getScene().getWindow(),
                controller -> {}
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