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
import com.vaadin.flow.server.VaadinSession;
import lombok.Getter;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.model.enums.Priority;
import solidarityhub.frontend.model.enums.TaskType;
import solidarityhub.frontend.i18n.Translator;

import javax.print.attribute.standard.Media;
import java.util.Collections;
import java.util.Locale;

@Getter
public class TaskComponent extends VerticalLayout {
    private static Translator translator;

    private final int taskId;
    private String taskName;
    private String taskDescription;
    private String startTimeDate;
    private String priority;
    private String emergencyLevel;
    public Button editButton;
    private TaskType taskType;

    public TaskComponent(int taskId, String name, String description, String startTimeDate, String priority, String emergencyLevel, TaskType taskType) {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());

        this.taskId = taskId;
        this.taskName = name;
        this.taskDescription = description;
        this.startTimeDate = startTimeDate;
        this.priority = priority;
        this.emergencyLevel = emergencyLevel;
        this.taskType = taskType;

        creatComponent();
    }

    public TaskComponent(TaskDTO taskDTO) {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());

        this.taskId = taskDTO.getId();
        this.taskName = taskDTO.getName();
        this.taskDescription = taskDTO.getDescription();
        this.startTimeDate = taskDTO.getStartTimeDate().toString();
        this.priority = taskDTO.getPriority().toString();
        this.emergencyLevel = taskDTO.getEmergencyLevel().toString();
        this.taskType = taskDTO.getType();

        creatComponent();
    }

    private void creatComponent() {
        addClassName("task-card");

        HorizontalLayout header = new HorizontalLayout();
        header.add(getTaskNameComponent(), getStartDateTimeComponent());
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.setAlignItems(Alignment.CENTER);
        footer.add(getPriorityLevelComponent(), getNeedTypeComponent(), getSettingsComponent());

        add(
            header,
            new HorizontalLayout(getImg(), getTaskDescriptionComponent()),
            footer
        );
    }

    //===============================Get Components=========================================
    public Image getImg() {
        Image taskImg = new Image("images/task_default.png", translator.get("alt_task_icon"));
        return switch (emergencyLevel) {
            case "LOW", "Low", "Bajo", "Baix" -> {
                taskImg = new Image("images/task_low.png", translator.get("alt_task_low_icon"));
                yield taskImg;
            }
            case "Medium", "Medio",  "MEDIUM", "Mitjà" -> {
                taskImg = new Image("images/task_medium.png", translator.get("alt_task_medium_icon"));
                yield taskImg;
            }
            case "HIGH", "High", "Alto", "Alt"-> {
                taskImg = new Image("images/task_high.png", translator.get("alt_task_high_icon"));
                yield taskImg;
            }
            case "VERYHIGH", "MUY ALTO", "MOLT ALT" -> {
                taskImg = new Image("images/task_veryHigh.png", translator.get("alt_task_very_high_icon"));
                yield taskImg;
            }
            default -> taskImg;
        };
    }

    public Component getTaskNameComponent() {
        H2 taskNameTitle = new H2(taskName);
        taskNameTitle.addClassName("task-title");
        return taskNameTitle;
    }

    public Component getStartDateTimeComponent() {
        Span startDateTimeSpan = new Span(startTimeDate.replace('T', ' '));
        startDateTimeSpan.addClassName("task-date");
        return startDateTimeSpan;
    }

    public Component getTaskDescriptionComponent() {
        Div taskDescriptionSpan = new Div(taskDescription);
        taskDescriptionSpan.addClassName("task-description");
        return taskDescriptionSpan;
    }

    public Component getPriorityLevelComponent() {
        Span emergencyLevelSpan = new Span();
        emergencyLevelSpan.addClassName("task-priority");

        switch (priority) {
            case "LOW" -> emergencyLevelSpan.setText(translator.get("low_priority"));
            case "MODERATE" -> emergencyLevelSpan.setText(translator.get("moderate_priority"));
            case "URGENT" -> emergencyLevelSpan.setText(translator.get("urgent_priority"));
            default -> emergencyLevelSpan.setText("Default");
        };
        return emergencyLevelSpan;
    }

    public Component getNeedTypeComponent() {
        Span needTypeSpan = new Span(formatTaskType(taskType));
        needTypeSpan.addClassName("need-type");
        return needTypeSpan;
    }

    public Component getSettingsComponent() {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setPadding(false);
        buttonLayout.setAlignItems(Alignment.END);

        Icon editTaskIcon = VaadinIcon.COG.create();
        editButton = new Button(editTaskIcon);
        editButton.addClassName("edit-button");
        // Agregar tooltip traducido
        editButton.getElement().setAttribute("title", translator.get("edit_task_button_tooltip"));

        editButton.addClickListener(event -> UI.getCurrent().navigate("editTask", QueryParameters.simple(
                Collections.singletonMap("id", String.valueOf(this.taskId)))));

        buttonLayout.add(editButton);
        return buttonLayout;
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

    //===============================Format Methods=========================================
    private String formatTaskType(TaskType taskType) {
        if (taskType == null) {
            return "No especificado";
        }

        return switch (taskType) {
            case MEDICAL -> "Medica";
            case POLICE -> "Policía";
            case FIREFIGHTERS -> "Bomberos";
            case CLEANING -> "Limpieza";
            case FEED -> "Alimentación";
            case PSYCHOLOGICAL -> "Psicológica";
            case BUILDING -> "Construcción";
            case CLOTHING -> "Ropa";
            case REFUGE -> "Refugio";
            case OTHER -> "Otra";
            case SEARCH -> "Búsqueda";
            case LOGISTICS -> "Logística";
            case COMMUNICATION -> "Comunicación";
            case MOBILITY -> "Movilidad";
            case PEOPLEMANAGEMENT -> "Gestión de personas";
            default -> "Otro";
        };
    }
}