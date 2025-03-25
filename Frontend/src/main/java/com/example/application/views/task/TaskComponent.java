package com.example.application.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
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

        getStyle().set("background-color", "#f0f0f0");
        getStyle().set("padding", "10px");
        getStyle().set("border-radius", "24px");
        setAlignItems(Alignment.CENTER);

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
                imgLow.setWidth(40, Unit.PIXELS);
                return imgLow;
            case "medium":
                Image imgMedium = new Image("images/task_medium.png", "Tarea de emergencia media");
                imgMedium.setWidth(40, Unit.PIXELS);
                return imgMedium;
            case "high":
                Image imgHigh = new Image("images/task_high.png", "Tarea de emergencia alta");
                imgHigh.setWidth(40, Unit.PIXELS);
                return imgHigh;
            case "veryHigh":
                Image imgVeryHigh = new Image("images/task_veryHigh.png", "Tarea de emergencia muy alta");
                imgVeryHigh.setWidth(40, Unit.PIXELS);
                return imgVeryHigh;
        }
        Image img = new Image("images/task_default.png", "Icono tarea");
        img.setWidth(40, Unit.PIXELS);
        return img;
    }

    private Component getTaskInfo() {
        VerticalLayout textLayout = new VerticalLayout();

        H2 taskNameTitle = new H2(taskName);
        Span tackDescriptionSpan = new Span(taskDescription);

        textLayout.add(taskNameTitle, tackDescriptionSpan);
        return textLayout;
    }

    private Component getPriorityLevel() {
        Span emergencyLevelSpan = new Span(priority);
        setAlignSelf(Alignment.END, emergencyLevelSpan);
        return emergencyLevelSpan;
    }

    private Component getSettings() {
        VerticalLayout buttonLayout = new VerticalLayout();

        Span taskDateSpan = new Span(startTimeDate);
        Button editButton = new Button("Edit");

        setAlignSelf(Alignment.END, taskDateSpan, editButton);

        buttonLayout.add(taskDateSpan, editButton);
        return buttonLayout;
    }
}
