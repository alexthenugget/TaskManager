package com.github.alex_the_nugget.taskhub.taskhub.models;

import java.time.LocalDateTime;

public class Task {
    private int id;
    private String managerName;
    private String tag;
    private String taskName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String description;
    private String status;
    private String rating;

    public Task() {
    }

    public Task(int id, String managerName, String tag, String taskName, LocalDateTime startDate,
                LocalDateTime endDate, String description, String status, String rating) {
        this.id = id;
        this.managerName = managerName;
        this.tag = tag;
        this.taskName = taskName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.status = status;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", managerName='" + managerName + '\'' +
                ", tag='" + tag + '\'' +
                ", taskName='" + taskName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", rating='" + rating + '\'' +
                '}';
    }
}