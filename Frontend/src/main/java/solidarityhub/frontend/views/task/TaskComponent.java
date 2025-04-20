package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
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
    public Button editButton;

    public TaskComponent(int taskId, String name, String description, String startTimeDate, String priority, String emergencyLevel) {
        this.taskId = taskId;
        this.taskName = name;
        this.taskDescription = description;
        this.startTimeDate = startTimeDate;
        this.priority = priority;
        this.emergencyLevel = emergencyLevel;

        creatComponent();
    }

    public TaskComponent(TaskDTO taskDTO) {
        this.taskId = taskDTO.getId();
        this.taskName = taskDTO.getName();
        this.taskDescription = taskDTO.getDescription();
        this.startTimeDate = taskDTO.getStartTimeDate().toString();
        this.priority = taskDTO.getPriority().toString();
        this.emergencyLevel = taskDTO.getEmergencyLevel().toString();

        creatComponent();
    }

    private void creatComponent() {
        addClassName("task-card");

        HorizontalLayout header = new HorizontalLayout();
        header.add(getTaskName(), getStartDateTime());
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.setAlignItems(Alignment.CENTER);
        footer.add(getPriorityLevel(), getSettings());

        add(
            header,
            new HorizontalLayout(getImg(), getTaskDescription()),
            footer
        );
    }

    //===============================Get Components=========================================
    public Image getImg() {
        Image taskImg = new Image("images/task_default.png", "Icono tarea");
        return switch (emergencyLevel) {
            case "low", "Baja" -> {
                taskImg = new Image("images/task_low.png", "Tarea de emergencia baja");
                yield taskImg;
            }
            case "medium", "Media" -> {
                taskImg = new Image("images/task_medium.png", "Tarea de emergencia media");
                yield taskImg;
            }
            case "high", "Alta" -> {
                taskImg = new Image("images/task_high.png", "Tarea de emergencia alta");
                yield taskImg;
            }
            case "veryHigh", "Muy alta" -> {
                taskImg = new Image("images/task_veryHigh.png", "Tarea de emergencia muy alta");
                yield taskImg;
            }
            default -> taskImg;
        };
    }

    public Component getTaskName() {
        H2 taskNameTitle = new H2(taskName);
        taskNameTitle.addClassName("task-title");
        return taskNameTitle;
    }

    public Component getTaskDescription() {
        Div taskDescriptionSpan = new Div(taskDescription);
        taskDescriptionSpan.addClassName("task-description");
        return taskDescriptionSpan;
    }

    public Component getPriorityLevel() {
        Span emergencyLevelSpan = new Span(priority);
        emergencyLevelSpan.addClassName("task-priority");
        return emergencyLevelSpan;
    }

    public Component getSettings() {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setPadding(false);
        buttonLayout.setAlignItems(Alignment.END);

        Icon editTaskIcon = VaadinIcon.COG.create();
        editButton = new Button(editTaskIcon);
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

    //===============================Update Components=========================================
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
        creatComponent();
    }

    public void enabledEditButton(Boolean enabled) {
        editButton.setEnabled(enabled);
    }
}
