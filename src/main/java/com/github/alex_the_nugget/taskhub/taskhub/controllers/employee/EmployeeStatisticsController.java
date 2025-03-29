package com.github.alex_the_nugget.taskhub.taskhub.controllers.employee;

import com.github.alex_the_nugget.taskhub.taskhub.models.Task;
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
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class EmployeeStatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(com.github.alex_the_nugget.taskhub.taskhub.controllers.employee.EmployeeStatisticsController.class);
    private final UserSession userSession = UserSession.getInstance();
    private StatisticsService statisticsService;

    @FXML
    private ImageView imageMyTasks;

    @FXML
    private Label response;

    @FXML
    private PieChart tasksPieChart;

    @FXML
    private BarChart<String, Number> monthlyTasksChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    public void initialize() {
        try {
            loadTaskStatistics();
            setupMonthlyChart();
        } catch (Exception e) {
            logger.error("Error during initialization", e);
            response.setText("Error loading statistics. Please try again.");
        }
    }

    private void loadTaskStatistics() {
        String userLogin = userSession.getLogin();
        try {
            if (userLogin != null && statisticsService != null) {
                List<Task> tasks = statisticsService.returnTasksListSortedByStatus();
                List<Task> completedTasks = statisticsService.returnCompletedTasksSortedByEndDate();

                updatePieChart(tasks);
                updateMonthlyChart(completedTasks);
            }
        } catch (Exception e) {
            logger.error("Error loading task statistics for user: {}", userLogin, e);
            throw e;
        }
    }

    private void setupMonthlyChart() {
        try {
            xAxis.setLabel("Month");
            yAxis.setLabel("Number of Completed Tasks");
            monthlyTasksChart.setTitle("Completed Tasks by Month");
            monthlyTasksChart.setStyle("-fx-font-family: 'Lato'; -fx-font-size: 19px;");

            for (Node node : monthlyTasksChart.lookupAll(".chart-legend-item")) {
                node.setStyle("-fx-font-family: 'Lato'; -fx-font-size: 12px;");
            }
        } catch (Exception e) {
            logger.error("Error setting up monthly chart", e);
            throw e;
        }
    }

    private void updatePieChart(List<Task> tasks) {
        try {
            Map<String, Long> statusCounts = tasks.stream()
                    .collect(Collectors.groupingBy(
                            Task::getStatus,
                            Collectors.counting()
                    ));

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            statusCounts.forEach((status, count) ->
                    pieChartData.add(new PieChart.Data(status + " (" + count + ")", count))
            );

            tasksPieChart.setData(pieChartData);
            tasksPieChart.setTitle("Task Status Distribution");

            tasksPieChart.setStyle("-fx-font-family: 'Lato'; -fx-font-size: 19px;");

            for (Node node : tasksPieChart.lookupAll(".chart-legend-item")) {
                node.setStyle("-fx-font-family: 'Lato'; -fx-font-size: 12px;");
            }
        } catch (Exception e) {
            logger.error("Error updating pie chart", e);
            throw e;
        }
    }

    private void updateMonthlyChart(List<Task> completedTasks) {
        try {
            Map<Month, Long> tasksByMonth = completedTasks.stream()
                    .filter(task -> task.getEndDate() != null)
                    .collect(Collectors.groupingBy(
                            task -> task.getEndDate().getMonth(),
                            Collectors.counting()
                    ));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Completed Tasks");

            Arrays.stream(Month.values())
                    .sorted()
                    .forEach(month -> {
                        long count = tasksByMonth.getOrDefault(month, 0L);
                        series.getData().add(new XYChart.Data<>(
                                month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                                count));
                    });

            monthlyTasksChart.getData().clear();
            monthlyTasksChart.getData().add(series);
        } catch (Exception e) {
            logger.error("Error updating monthly chart", e);
            throw e;
        }
    }

    public void setStatisticsService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
        try {
            loadTaskStatistics();
        } catch (Exception e) {
            logger.error("Error setting statistics service", e);
            response.setText("Error loading statistics service.");
        }
    }

    @FXML
    public void handleMyTasksClick() {
        String userLogin = userSession.getLogin();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/github/alex_the_nugget/taskhub/taskhub/EmployeeCheckTasksPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) imageMyTasks.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.error("Error loading EmployeeCheckTasksPage for user: {}", userLogin, e);
            response.setText("Error navigating to tasks page.");
        }
    }
}
