package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;

import java.util.Collections;

public class TaskComponent extends HorizontalLayout {

    private final int taskId;
    private String taskName;
    private String taskDescription;
    private String startTimeDate;
    private String priority;
    private String emergencyLevel;
    private Button editButton;

    public TaskComponent(int taskId, String name, String description, String startTimeDate, String priority, String emergencyLevel) {
        this.taskId = taskId;
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
            case "low", "Baja":
                Image imgLow = new Image("images/task_low.png", "Tarea de emergencia baja");
                imgLow.addClassName("task-icon");
                return imgLow;
            case "medium", "Media":
                Image imgMedium = new Image("images/task_medium.png", "Tarea de emergencia media");
                imgMedium.addClassName("task-icon");
                return imgMedium;
            case "high", "Alta":
                Image imgHigh = new Image("images/task_high.png", "Tarea de emergencia alta");
                imgHigh.addClassName("task-icon");
                return imgHigh;
            case "veryHigh", "Muy alta":
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
        editButton = new Button(icon);
        editButton.addClassName("edit-button");

        editButton.addClickListener(event -> {
            UI.getCurrent().navigate("editTask", QueryParameters.simple(
                    Collections.singletonMap("id", String.valueOf(taskId))));
        });

        buttonLayout.add(taskDateSpan, editButton);
        return buttonLayout;
    }


    public void enabledEditButton(Boolean enabled) {
        editButton.setEnabled(enabled);
    }

    //Update component methods
    public void updateName(String name) {
        this.taskName = name;
        updateComponent();
    }

    public void updateDescription(String description) {
        this.taskDescription = description;
        updateComponent();
    }

    public void updateDate(String date) {
        this.startTimeDate = date;
        updateComponent();
    }

    public void updatePriority(String priority) {
        this.priority = priority;
        updateComponent();
    }

    public void updateEmergencyLevel(String emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
        updateComponent();
    }

    private void updateComponent() {
        removeAll();
        add(
                getImg(),
                getTaskInfo(),
                getPriorityLevel(),
                getSettings()
        );
    }

}
