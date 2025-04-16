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
import solidarityhub.frontend.dto.TaskDTO;

import java.util.Collections;

public class TaskComponent extends VerticalLayout {

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

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.add(getTaskName(), getStartDateTime());

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.setAlignItems(Alignment.CENTER);
        footer.add(getPriorityLevel(), getSettings());

        add(
            header, new HorizontalLayout(getImg(), getTaskDescription()),
            footer
        );
    }

    public TaskComponent(TaskDTO taskDTO) {
        this.taskId = taskDTO.getId();
        this.taskName = taskDTO.getName();
        this.taskDescription = taskDTO.getDescription();
        this.startTimeDate = taskDTO.getStartTimeDate().toString();
        this.priority = taskDTO.getPriority().toString();
        this.emergencyLevel = taskDTO.getEmergencyLevel().toString();

        addClassName("task-card");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);
        header.add(getTaskName(), getStartDateTime());

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.setAlignItems(Alignment.CENTER);
        footer.add(getPriorityLevel(), getSettings());

        add(
                header, new HorizontalLayout(getImg(), getTaskDescription()),
                footer
        );
    }

    public Image getImg() {
        switch (emergencyLevel) {
            case "low", "Baja":
                Image imgLow = new Image("images/task_low.png", "Tarea de peligrosidad baja");
                imgLow.addClassName("task-icon");
                return imgLow;
            case "medium", "Media":
                Image imgMedium = new Image("images/task_medium.png", "Tarea de peligrosidad media");
                imgMedium.addClassName("task-icon");
                return imgMedium;
            case "high", "Alta":
                Image imgHigh = new Image("images/task_high.png", "Tarea de peligrosidad alta");
                imgHigh.addClassName("task-icon");
                return imgHigh;
            case "veryHigh", "Muy alta":
                Image imgVeryHigh = new Image("images/task_veryHigh.png", "Tarea de peligrosidad muy alta");
                imgVeryHigh.addClassName("task-icon");
                return imgVeryHigh;
        }
        Image img = new Image("images/task_default.png", "Icono tarea");
        img.addClassName("task-icon");
        return img;
    }

    public Component getTaskName() {
        H2 taskNameTitle = new H2(taskName);
        taskNameTitle.addClassName("task-title");
        return taskNameTitle;
    }

    public Component getTaskDescription() {
        Span taskDescriptionSpan = new Span(taskDescription);
        taskDescriptionSpan.addClassName("task-description");
        return taskDescriptionSpan;
    }

    public Component getTaskInfo() {
        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setSizeFull();
        textLayout.addClassName("task-info");


        H2 taskNameTitle = new H2(taskName);
        taskNameTitle.addClassName("task-title");

        Span taskDescriptionSpan = new Span(taskDescription);
        taskDescriptionSpan.addClassName("task-description");

        textLayout.add(taskNameTitle, taskDescriptionSpan);
        return textLayout;
    }

    public Component getPriorityLevel() {
        Span prioritySpan = new Span(priority);
        prioritySpan.addClassName("task-priority");
        return prioritySpan;
    }

    public Component getSettings() {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.addClassName("settings");

        Icon icon = VaadinIcon.COG.create();
        editButton = new Button(icon);
        editButton.addClassName("edit-button");

        editButton.addClickListener(event -> UI.getCurrent().navigate("editTask", QueryParameters.simple(
                Collections.singletonMap("id", String.valueOf(this.taskId)))));

        buttonLayout.add(editButton);
        return buttonLayout;
    }

    public Component getStartDateTime() {
        Span startDateTimeSpan = new Span(startTimeDate.replace('T', ' '));
        startDateTimeSpan.addClassName("task-date");
        return startDateTimeSpan;
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
