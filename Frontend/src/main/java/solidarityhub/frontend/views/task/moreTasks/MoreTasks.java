package solidarityhub.frontend.views.task.moreTasks;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.FormatService;
import solidarityhub.frontend.service.TaskService;
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.catastrophe.CatastropheView;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@PageTitle("Ver más tareas")
@Route("tasks/moretasks")
public class MoreTasks extends VerticalLayout implements BeforeEnterObserver {
    private static Translator translator = new Translator();
    private final FormatService formatService;

    private final TaskService taskService;
    private CatastropheDTO selectedCatastrophe;
    private final Grid<TaskDTO> taskGrid;

    // Valores de filtro
    private String statusFilterValue = "";
    private String priorityFilterValue = "";
    private String typeFilterValue = "";
    private String emergencyFilterValue = "";

    public MoreTasks() {
        translator.initializeTranslator();

        this.taskService = new TaskService();
        this.formatService = FormatService.getInstance();

        this.taskGrid = new Grid<>(TaskDTO.class, false);
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

        taskGrid.addClassName("moreTasks_grid");
        taskGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        add(header, getButtons(), taskGrid);
        populateTaskGrid();
    }

    private Component getButtons(){
        Button filterButton = new Button(translator.get("filter_tasks"), new Icon("vaadin", "filter"));
        filterButton.addClassName("filter-button");

        Button clearFiltersButton = new Button(translator.get("clear_filters"), new Icon("vaadin", "trash"));
        clearFiltersButton.addClassName("clear-filters-button");

        filterButton.addClickListener(e -> {
            CompletableFuture<List<String>> filters = openFilterDialog();
            filters.thenAccept(filterValues -> {
                if (!filterValues.isEmpty()) {
                    statusFilterValue = filterValues.get(0);
                    priorityFilterValue = filterValues.get(1);
                    typeFilterValue = filterValues.get(2);
                    emergencyFilterValue = filterValues.get(3);
                    refreshTasks();
                }
            });

        });

        clearFiltersButton.addClickListener(e -> {
            statusFilterValue = "";
            priorityFilterValue = "";
            typeFilterValue = "";
            emergencyFilterValue = "";
            refreshTasks();
        });

        HorizontalLayout filterLayout = new HorizontalLayout(clearFiltersButton, filterButton);
        filterLayout.setAlignItems(Alignment.CENTER);
        filterLayout.setWidthFull();
        filterLayout.setJustifyContentMode(JustifyContentMode.START);

        return filterLayout;
    }

    private void refreshTasks() {
        taskGrid.setItems(getTasksList());
        taskGrid.getDataProvider().refreshAll();
    }

    // ===============================Get Grid Items=========================================
    private List<TaskDTO> getTasksList() {
        if (selectedCatastrophe != null) {
            return taskService.getTasks(statusFilterValue, priorityFilterValue, typeFilterValue, emergencyFilterValue, selectedCatastrophe.getId());
        } else {
            return Collections.emptyList();
        }
    }

    private void populateTaskGrid() {
        ListDataProvider<TaskDTO> tasksDataProvider = new ListDataProvider<>(getTasksList());

        if (tasksDataProvider.getItems().isEmpty()) {
            taskGrid.setVisible(false);
            add(new Span(translator.get("no_tasks")));
        } else {
            taskGrid.setVisible(true);
            taskGrid.setDataProvider(tasksDataProvider);

            setupColumns();
        }

        taskGrid.addItemClickListener(event -> {
            if (event.getClickCount() == 2) {
                TaskDTO selectedTask = event.getItem();
                UI.getCurrent().navigate("tasks/editTask", QueryParameters.simple(
                        Collections.singletonMap("id", String.valueOf(selectedTask.getId()))));
            }
        });
    }

    private void setupColumns() {
        taskGrid.addColumn(TaskDTO::getName)
                .setHeader(translator.get("more_tasks_name"))
                .setAutoWidth(true);

        // Columna de descripción
        taskGrid.addColumn(task ->
                        task.getDescription().length() > 30 ?
                                task.getDescription().substring(0, 30) + "..." :
                                task.getDescription())
                .setHeader(translator.get("more_tasks_description"))
                .setAutoWidth(true);

        // Columna de tipo de tarea
        taskGrid.addColumn(task -> formatService.formatTaskType(task.getType()))
                .setHeader(translator.get("more_tasks_type"))
                .setAutoWidth(true);

        // Columna de prioridad
        taskGrid.addColumn(task -> formatService.formatPriority(task.getPriority()))
                .setHeader(translator.get("more_tasks_priority"))
                .setAutoWidth(true);

        // Columna de nivel de emergencia
        taskGrid.addColumn(task -> formatService.formatEmergencyLevel(task.getEmergencyLevel()))
                .setHeader(translator.get("preview_task_emergency_level"))
                .setAutoWidth(true);

        // Columna de estado
        taskGrid.addColumn(task -> formatService.formatTaskStatus(task.getStatus()))
                .setHeader(translator.get("more_tasks_status"))
                .setAutoWidth(true);

        // Columna de fecha de inicio
        taskGrid.addColumn(task -> formatService.formatDateTime(task.getStartTimeDate()))
                .setHeader(translator.get("more_tasks_start_date"))
                .setAutoWidth(true);

        // Columna de fecha estimada de finalización
        taskGrid.addColumn(task -> formatService.formatDate(task.getEstimatedEndTimeDate().toLocalDate()))
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

    private CompletableFuture<List<String>> openFilterDialog() {
        FilterMoreTasksDialog filterMoreTasksDialog = new FilterMoreTasksDialog();
        filterMoreTasksDialog.setWidth("550px");
        filterMoreTasksDialog.setHeight("480px");
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        filterMoreTasksDialog.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                List<String> filters = filterMoreTasksDialog.getSelectedFilters();
                future.complete(filters);
            }
        });
        filterMoreTasksDialog.open();

        return future;
    }
}