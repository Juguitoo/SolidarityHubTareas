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
import solidarityhub.frontend.i18n.Translator;
import org.pingu.domain.enums.TaskType;

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
        initializeTranslator();

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
        initializeTranslator();

        this.taskId = taskDTO.getId();
        this.taskName = taskDTO.getName();
        this.taskDescription = taskDTO.getDescription();
        this.startTimeDate = taskDTO.getStartTimeDate().toString();
        this.priority = taskDTO.getPriority().toString();
        this.emergencyLevel = taskDTO.getEmergencyLevel().toString();
        this.taskType = taskDTO.getType();

        creatComponent();
    }

    private void initializeTranslator() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());
    }

    private void creatComponent() {
        addClassName("task-card");
        setWidthFull();
        setMinWidth("100%");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.add(getTaskNameComponent(), getStartDateTimeComponent());
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        HorizontalLayout contentLayout = new HorizontalLayout(getImg(), getTaskDescriptionComponent());
        contentLayout.setWidthFull();
        contentLayout.setFlexGrow(1, getTaskDescriptionComponent());

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        footer.setAlignItems(Alignment.CENTER);
        footer.add(getPriorityLevelComponent(), getNeedTypeComponent(), getSettingsComponent());

        add(
            header,
            contentLayout,
            footer
        );
    }

    //===============================Get Components=========================================
    public Image getImg() {
        Image taskImg;

        // Asignar imagen según nivel de emergencia
        switch (emergencyLevel) {
            case "LOW", "Low", "Bajo", "Baix" -> {
                taskImg = new Image("images/task_low.png", translator.get("alt_task_low_icon"));
            }
            case "MEDIUM", "Medium", "Medio", "Mitjà" -> {
                taskImg = new Image("images/task_medium.png", translator.get("alt_task_medium_icon"));
            }
            case "HIGH", "High", "Alto", "Alt" -> {
                taskImg = new Image("images/task_high.png", translator.get("alt_task_high_icon"));
            }
            case "VERYHIGH", "MUY ALTO", "MOLT ALT" -> {
                taskImg = new Image("images/task_veryHigh.png", translator.get("alt_task_very_high_icon"));
            }
            default -> {
                // Imagen por defecto en modo claro, la reemplazaremos si es oscuro
                taskImg = new Image("images/task_default.png", translator.get("alt_task_icon"));

                // Cambiar a imagen blanca si el tema es oscuro (HTML usa data-theme en <html>)
                UI.getCurrent().getPage().executeJs("""
                const theme = document.documentElement.getAttribute('data-theme');
                if (theme === 'dark') {
                    $0.src = 'images/task_default-white.png';
                }
            """, taskImg.getElement());
            }
        }

        return taskImg;
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

        String priorityText;
        switch (priority) {
            case "LOW" -> priorityText = translator.get("low_priority");
            case "MODERATE" -> priorityText = translator.get("moderate_priority");
            case "URGENT" -> priorityText = translator.get("urgent_priority");
            default -> priorityText = priority;
        }

        emergencyLevelSpan.setText(priorityText);
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

    public void updateTaskType(TaskType taskType) {
        this.taskType = taskType;
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
            return translator.get("task_type_not_specified");
        }

        return switch (taskType) {
            case MEDICAL -> translator.get("task_type_medical");
            case POLICE -> translator.get("task_type_police");
            case FIREFIGHTERS -> translator.get("task_type_firefighters");
            case CLEANING -> translator.get("task_type_cleaning");
            case FEED -> translator.get("task_type_feed");
            case PSYCHOLOGICAL -> translator.get("task_type_psychological");
            case BUILDING -> translator.get("task_type_building");
            case CLOTHING -> translator.get("task_type_clothing");
            case REFUGE -> translator.get("task_type_refuge");
            case OTHER -> translator.get("task_type_other");
            case SEARCH -> translator.get("task_type_search");
            case LOGISTICS -> translator.get("task_type_logistics");
            case COMMUNICATION -> translator.get("task_type_communication");
            case MOBILITY -> translator.get("task_type_mobility");
            case PEOPLEMANAGEMENT -> translator.get("task_type_people_management");
            case SAFETY -> translator.get("task_type_safety");
        };
    }

}
