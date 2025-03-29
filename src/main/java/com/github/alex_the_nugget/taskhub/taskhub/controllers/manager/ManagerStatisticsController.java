package com.github.alex_the_nugget.taskhub.taskhub.controllers.manager;

import com.github.alex_the_nugget.taskhub.taskhub.services.StatisticsService;
import com.github.alex_the_nugget.taskhub.taskhub.session.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ManagerStatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(ManagerStatisticsController.class);
    private final UserSession userSession = UserSession.getInstance();
    private StatisticsService statisticsService;

    @FXML
    private ImageView imageMyTasks;

    @FXML
    private Label response;

    @FXML
    private PieChart ratingsPieChart;

    @FXML
    private BarChart<String, Number> efficiencyChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private ImageView imageAssignTask;

    public void initialize() {
        try {
            loadStatistics();
            setupCharts();
        } catch (Exception e) {
            logger.error("Error during initialization", e);
            response.setText("Error loading statistics. Please try again.");
        }
    }

    private void loadStatistics() {
        String userLogin = userSession.getLogin();
        try {
            if (userLogin != null && statisticsService != null) {
                Map<String, Double> employeeEfficiency = statisticsService.calculateEmployeeEfficiency();
                Map<String, Long> taskRatings = statisticsService.calculateTaskRatings();
                updateEfficiencyChart(employeeEfficiency);
                updateRatingsChart(taskRatings);
            }
        } catch (Exception e) {
            logger.error("Error loading statistics for manager: {}", userLogin, e);
            throw e;
        }
    }

    private void setupCharts() {
        try {
            xAxis.setLabel("Employee");
            yAxis.setLabel("Efficiency Score");
            efficiencyChart.setTitle("Department Efficiency");
            efficiencyChart.setStyle("-fx-font-family: 'Lato'; -fx-font-size: 19px;");

            ratingsPieChart.setTitle("Task Ratings Distribution");
            ratingsPieChart.setStyle("-fx-font-family: 'Lato'; -fx-font-size: 19px;");

            for (Node node : efficiencyChart.lookupAll(".chart-legend-item")) {
                node.setStyle("-fx-font-family: 'Lato'; -fx-font-size: 12px;");
            }
            for (Node node : ratingsPieChart.lookupAll(".chart-legend-item")) {
                node.setStyle("-fx-font-family: 'Lato'; -fx-font-size: 12px;");
            }
        } catch (Exception e) {
            logger.error("Error setting up charts", e);
            throw e;
        }
    }

    private void updateEfficiencyChart(Map<String, Double> employeeEfficiency) {
        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Efficiency");

            employeeEfficiency.forEach((employee, score) ->
                    series.getData().add(new XYChart.Data<>(employee, score))
            );

            efficiencyChart.getData().clear();
            efficiencyChart.getData().add(series);
        } catch (Exception e) {
            logger.error("Error updating efficiency chart", e);
            throw e;
        }
    }

    private void updateRatingsChart(Map<String, Long> ratingsCount) {
        try {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            ratingsCount.forEach((rating, count) -> {
                PieChart.Data data = new PieChart.Data(rating + " (" + count + ")", count);
                pieChartData.add(data);

                data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        switch (rating.toLowerCase()) {
                            case "good":
                                newValue.setStyle("-fx-pie-color: #4CAF50;");
                                break;
                            case "ok":
                                newValue.setStyle("-fx-pie-color: #FFC107;");
                                break;
                            case "bad":
                                newValue.setStyle("-fx-pie-color: #F44336;");
                                break;
                        }
                    }
                });
            });

            ratingsPieChart.setData(pieChartData);
        } catch (Exception e) {
            logger.error("Error updating ratings chart", e);
            throw e;
        }
    }

    public void setStatisticsService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
        try {
            loadStatistics();
        } catch (Exception e) {
            logger.error("Error setting statistics service", e);
            response.setText("Error loading statistics service. Please try again.");
        }
    }

    @FXML
    protected void assignTask() {
        String userLogin = userSession.getLogin();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/alex_the_nugget/taskhub/taskhub/AssignTasksPage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) imageAssignTask.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.error("Error loading AssignTasksPage for manager: {}", userLogin, e);
            response.setText("Error navigating to assign tasks page. Please try again.");
        }
    }

    @FXML
    protected void checkTasks() {
        String userLogin = userSession.getLogin();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/alex_the_nugget/taskhub/taskhub/ManagerCheckTasksPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) imageMyTasks.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.error("Error loading ManagerCheckTasksPage for manager: {}", userLogin, e);
            response.setText("Error navigating to tasks page. Please try again.");
        }
    }
}