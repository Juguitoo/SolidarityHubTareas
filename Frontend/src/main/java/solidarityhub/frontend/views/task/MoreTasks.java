package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import org.apache.commons.lang3.StringUtils;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.i18n.Translator;
import org.pingu.domain.enums.EmergencyLevel;
import org.pingu.domain.enums.Priority;
import org.pingu.domain.enums.Status;
import org.pingu.domain.enums.TaskType;
import solidarityhub.frontend.service.TaskService;
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.catastrophe.CatastropheView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@PageTitle("Ver más tareas")
@Route("moretasks")
public class MoreTasks extends VerticalLayout implements BeforeEnterObserver {
    private static Translator translator;

    private final TaskService taskService;
    private CatastropheDTO selectedCatastrophe;
    private ListDataProvider<TaskDTO> tasksDataProvider;
    private final Grid<TaskDTO> taskGrid;

    // Columnas para filtros
    private Grid.Column<TaskDTO> nameColumn;
    private Grid.Column<TaskDTO> statusColumn;
    private Grid.Column<TaskDTO> priorityColumn;
    private Grid.Column<TaskDTO> typeColumn;
    private Grid.Column<TaskDTO> emergencyLevelColumn;

    // Valores de filtro
    private String nameFilterValue = "";
    private Set<Status> statusFilterValues = new HashSet<>();
    private Set<Priority> priorityFilterValues = new HashSet<>();
    private Set<TaskType> typeFilterValues = new HashSet<>();
    private Set<EmergencyLevel> emergencyFilterValues = new HashSet<>();

    public MoreTasks() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());

        this.taskService = new TaskService();

        this.taskGrid = new Grid<>(TaskDTO.class, false);
        taskGrid.addClassName("moreTasks_grid");
        taskGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        taskGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        if (selectedCatastrophe == null) {
            Notification.show(translator.get("select_catastrophe_warning"),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            event.forwardTo(CatastropheView.class);
            return;
        }

        buildView();
    }

    private void buildView() {
        removeAll();
        addClassName("moreTasks_Container");

        // Header
        HeaderComponent header = new HeaderComponent(
                translator.get("task_view_title") + selectedCatastrophe.getName(), "tasks"
        );

        add(header, taskGrid);
        populateTaskGrid();
    }

    // ===============================Get Grid Items=========================================
    private List<TaskDTO> getTasksList() {
        if (selectedCatastrophe != null) {
            return taskService.getTasksByCatastrophe(selectedCatastrophe.getId());
        } else {
            return Collections.emptyList();
        }
    }

    private void populateTaskGrid() {
        this.tasksDataProvider = new ListDataProvider<>(getTasksList());

        if (tasksDataProvider.getItems().isEmpty()) {
            taskGrid.setVisible(false);
            add(new Span(translator.get("no_tasks")));
        } else {
            taskGrid.setVisible(true);
            taskGrid.setDataProvider(tasksDataProvider);

            // Definimos las columnas con sus filtros
            setupColumns();
            setupFilters();
        }

        taskGrid.addItemClickListener(event -> {
            if (event.getClickCount() == 2) {
                TaskDTO selectedTask = event.getItem();
                UI.getCurrent().navigate("editTask", QueryParameters.simple(
                        Collections.singletonMap("id", String.valueOf(selectedTask.getId()))));
            }
        });
    }

    private void setupColumns() {
        // Columna de nombre
        nameColumn = taskGrid.addColumn(TaskDTO::getName)
                .setHeader(translator.get("more_tasks_name"))
                .setAutoWidth(true);

        // Columna de descripción
        taskGrid.addColumn(task ->
                        task.getDescription().length() > 50 ?
                                task.getDescription().substring(0, 50) + "..." :
                                task.getDescription())
                .setHeader(translator.get("more_tasks_description"))
                .setAutoWidth(true);

        // Columna de tipo de tarea
        typeColumn = taskGrid.addColumn(task -> formatTaskType(task.getType()))
                .setHeader(translator.get("more_tasks_type"))
                .setAutoWidth(true);

        // Columna de prioridad
        priorityColumn = taskGrid.addColumn(task -> formatPriority(task.getPriority()))
                .setHeader(translator.get("more_tasks_priority"))
                .setAutoWidth(true);

        // Columna de nivel de emergencia
        emergencyLevelColumn = taskGrid.addColumn(task -> formatEmergencyLevel(task.getEmergencyLevel()))
                .setHeader(translator.get("more_tasks_emergency_level"))
                .setAutoWidth(true);

        // Columna de estado
        statusColumn = taskGrid.addColumn(task -> formatStatus(task.getStatus()))
                .setHeader(translator.get("more_tasks_status"))
                .setAutoWidth(true);

        // Columna de fecha de inicio
        taskGrid.addColumn(task -> formatDate(task.getStartTimeDate()))
                .setHeader(translator.get("more_tasks_start_date"))
                .setAutoWidth(true);

        // Columna de fecha estimada de finalización
        taskGrid.addColumn(task -> formatDate(task.getEstimatedEndTimeDate()))
                .setHeader(translator.get("more_tasks_end_date"))
                .setAutoWidth(true);

        // Columna de necesidades
        taskGrid.addColumn(task -> task.getNeeds().size())
                .setHeader(translator.get("more_tasks_needs"))
                .setAutoWidth(true);

        // Columna de voluntarios
        taskGrid.addColumn(task -> task.getVolunteers().size())
                .setHeader(translator.get("more_tasks_volunteers"))
                .setAutoWidth(true);
    }

    private void setupFilters() {
        // Filtro de búsqueda por nombre
        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder(translator.get("filter_by_name"));
        nameFilter.setClearButtonVisible(true);
        nameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        nameFilter.addValueChangeListener(event -> {
            nameFilterValue = event.getValue();
            applyAllFilters();
        });
        nameColumn.setHeader(getGridFilterHeader(translator.get("filter_name"), nameFilter));

        // Filtro de estado
        MultiSelectComboBox<Status> statusFilter = new MultiSelectComboBox<>();
        statusFilter.setPlaceholder(translator.get("filter_by_status"));
        statusFilter.setItems(Status.TO_DO, Status.IN_PROGRESS, Status.FINISHED);
        statusFilter.setItemLabelGenerator(this::formatStatus);
        statusFilter.addValueChangeListener(event -> {
            statusFilterValues = event.getValue();
            applyAllFilters();
        });
        statusColumn.setHeader(getGridFilterHeader(translator.get("filter_status"), statusFilter));

        // Filtro de prioridad
        MultiSelectComboBox<Priority> priorityFilter = new MultiSelectComboBox<>();
        priorityFilter.setPlaceholder(translator.get("filter_by_priority"));
        priorityFilter.setItems(Priority.LOW, Priority.MODERATE, Priority.URGENT);
        priorityFilter.setItemLabelGenerator(this::formatPriority);
        priorityFilter.addValueChangeListener(event -> {
            priorityFilterValues = event.getValue();
            applyAllFilters();
        });
        priorityColumn.setHeader(getGridFilterHeader(translator.get("filter_priority"), priorityFilter));

        // Filtro de tipo de tarea
        MultiSelectComboBox<TaskType> typeFilter = new MultiSelectComboBox<>();
        typeFilter.setPlaceholder(translator.get("filter_by_type"));
        typeFilter.setItems(TaskType.values());
        typeFilter.setItemLabelGenerator(this::formatTaskType);
        typeFilter.addValueChangeListener(event -> {
            typeFilterValues = event.getValue();
            applyAllFilters();
        });
        typeColumn.setHeader(getGridFilterHeader(translator.get("filter_type"), typeFilter));

        // Filtro de nivel de emergencia
        MultiSelectComboBox<EmergencyLevel> emergencyFilter = new MultiSelectComboBox<>();
        emergencyFilter.setPlaceholder(translator.get("filter_by_emergency_level"));
        emergencyFilter.setItems(EmergencyLevel.LOW, EmergencyLevel.MEDIUM, EmergencyLevel.HIGH, EmergencyLevel.VERYHIGH);
        emergencyFilter.setItemLabelGenerator(this::formatEmergencyLevel);
        emergencyFilter.addValueChangeListener(event -> {
            emergencyFilterValues = event.getValue();
            applyAllFilters();
        });
        emergencyLevelColumn.setHeader(getGridFilterHeader(translator.get("filter_emergency_level"), emergencyFilter));
    }

    private Component getGridFilterHeader(String headerText, Component filterComponent) {
        HorizontalLayout filterHeader = new HorizontalLayout();
        Span filterTitle = new Span(headerText);
        filterHeader.setAlignItems(Alignment.CENTER);
        filterTitle.addClassName("grid__filter-title");
        filterHeader.add(filterTitle, filterComponent);
        filterHeader.addClassName("grid__filter-header");
        return filterHeader;
    }

    // ===============================Manage Filters=========================================
    private void applyAllFilters() {
        tasksDataProvider.clearFilters();

        // Filtro por nombre
        if (!nameFilterValue.isEmpty()) {
            tasksDataProvider.addFilter(task ->
                    StringUtils.containsIgnoreCase(task.getName(), nameFilterValue));
        }

        // Filtro por estado
        if (!statusFilterValues.isEmpty()) {
            tasksDataProvider.addFilter(task ->
                    statusFilterValues.contains(task.getStatus()));
        }

        // Filtro por prioridad
        if (!priorityFilterValues.isEmpty()) {
            tasksDataProvider.addFilter(task ->
                    priorityFilterValues.contains(task.getPriority()));
        }

        // Filtro por tipo de tarea
        if (!typeFilterValues.isEmpty()) {
            tasksDataProvider.addFilter(task ->
                    typeFilterValues.contains(task.getType()));
        }

        // Filtro por nivel de emergencia
        if (!emergencyFilterValues.isEmpty()) {
            tasksDataProvider.addFilter(task ->
                    emergencyFilterValues.contains(task.getEmergencyLevel()));
        }

        tasksDataProvider.refreshAll();
    }

    // ===============================Format Methods=========================================
    private String formatDate(LocalDateTime taskDate) {
        if (taskDate == null) {
            return translator.get("more_tasks_no_available");
        }
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    private String formatStatus(Status status) {
        if (status == null) return "";
        return switch (status) {
            case TO_DO -> translator.get("status_todo");
            case IN_PROGRESS -> translator.get("status_in_progress");
            case FINISHED -> translator.get("status_finished");
        };
    }

    private String formatPriority(Priority priority) {
        if (priority == null) return "";
        return switch (priority) {
            case LOW -> translator.get("low_priority");
            case MODERATE -> translator.get("moderate_priority");
            case URGENT -> translator.get("urgent_priority");
        };
    }

    private String formatEmergencyLevel(EmergencyLevel level) {
        if (level == null) return "";
        return switch (level) {
            case LOW -> translator.get("low_emergency_level");
            case MEDIUM -> translator.get("medium_emergency_level");
            case HIGH -> translator.get("high_emergency_level");
            case VERYHIGH -> translator.get("very_high_emergency_level");
        };
    }

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