package com.example.application.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class TaskComponent extends HorizontalLayout {

    private final String taskName;
    private final String taskDescription;
    private final String startTimeDate;
    private final String priority;
    private final String emergencyLevel;

    public TaskComponent(String name, String description, String startTimeDate, String priority, String emergencyLevel) {
        this.taskName = name;
        this.taskDescription = description;
        this.startTimeDate = startTimeDate;
        this.priority = priority;
        this.emergencyLevel = emergencyLevel;

        addClassName("task-card");

        add(
            getImg(),
            getTaskInfo(),
            getPriorityLevel(),
            getSettings()
        );
    }

    private Image getImg() {
        switch (emergencyLevel) {
            case "low":
                Image imgLow = new Image("images/task_low.png", "Tarea de emergencia baja");
                imgLow.addClassName("task-icon");
                return imgLow;
            case "medium":
                Image imgMedium = new Image("images/task_medium.png", "Tarea de emergencia media");
                imgMedium.addClassName("task-icon");
                return imgMedium;
            case "high":
                Image imgHigh = new Image("images/task_high.png", "Tarea de emergencia alta");
                imgHigh.addClassName("task-icon");
                return imgHigh;
            case "veryHigh":
                Image imgVeryHigh = new Image("images/task_veryHigh.png", "Tarea de emergencia muy alta");
                imgVeryHigh.addClassName("task-icon");
                return imgVeryHigh;
        }
        Image img = new Image("images/task_default.png", "Icono tarea");
        img.addClassName("task-icon");
        return img;
    }

    private Component getTaskInfo() {
        VerticalLayout textLayout = new VerticalLayout();
        textLayout.addClassName("task-info");


        H2 taskNameTitle = new H2(taskName);
        taskNameTitle.addClassName("task-title");

        Span taskDescriptionSpan = new Span(taskDescription);
        taskDescriptionSpan.addClassName("task-description");

        textLayout.add(taskNameTitle, taskDescriptionSpan);
        return textLayout;
    }

    public Component getPriorityLevel() {
        Span emergencyLevelSpan = new Span(priority);
        emergencyLevelSpan.addClassName("task-priority");
        setAlignSelf(Alignment.END, emergencyLevelSpan);
        return emergencyLevelSpan;
    }

    private Component getSettings() {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.addClassName("settings");

        Span taskDateSpan = new Span(startTimeDate);
        taskDateSpan.addClassName("task-date");

        Icon icon = VaadinIcon.COG.create();
        Button editButton = new Button(icon);
        editButton.addClassName("edit-button");



        editButton.addClickListener(event -> {

        });

        buttonLayout.add(taskDateSpan, editButton);
        return buttonLayout;
    }


    //Delete
    public String getTaskName() {
        return taskName;
    }
    public String getTaskDescription() {
        return taskDescription;
    }

    public String getStartTimeDate() {
        return startTimeDate;
    }

    public String getPriority() {
        return priority;
    }
    public String getEmergencyLevel() {
        return emergencyLevel;
    }
}
