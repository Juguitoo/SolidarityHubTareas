package solidarityhub.frontend.views.task;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.model.enums.Priority;
import solidarityhub.frontend.service.TaskService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.catastrophe.CatastropheSelectionView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@PageTitle("Ver más tareas")
@Route("moretasks")
public class MoreTasks extends VerticalLayout implements BeforeEnterObserver {

    private final TaskService taskService;
    private CatastropheDTO selectedCatastrophe;
    private ListDataProvider<TaskDTO> tasksDataProvider;
    private final Grid<TaskDTO> taskGrid;

    public MoreTasks() {
        this.taskService = new TaskService();

        this.taskGrid = new Grid<>(TaskDTO.class, false);
        taskGrid.addClassName("moreTasks_grid");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        if (selectedCatastrophe == null) {
            Notification.show("Por favor, selecciona una catástrofe primero",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            event.forwardTo(CatastropheSelectionView.class);
            return;
        }

        buildView();
    }

    private void buildView() {
        removeAll();
        addClassName("moreTasks_Container");

        //Header
        HeaderComponent header = new HeaderComponent(
            "Tareas de la catástrofe " + selectedCatastrophe.getName(), "tasks"
        );


        add(header, getFilters(), taskGrid);
        populateTaskGrid();
    }

    //===============================Get Grid Items=========================================
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
            add(new Span("No hay tareas disponibles para esta catástrofe."));
        } else {
            taskGrid.setVisible(true);
            taskGrid.setDataProvider(tasksDataProvider);
            taskGrid.addColumn(TaskDTO::getName).setHeader("Nombre");
            taskGrid.addColumn(task -> task.getDescription().length() > 50 ? task.getDescription().substring(0, 50) + "..." : task.getDescription()).setHeader("Descripción");
            taskGrid.addColumn(TaskDTO::getPriority).setHeader("Prioridad");
            taskGrid.addColumn(task -> formatDate(task.getStartTimeDate())).setHeader("Fecha de comienzo");
            taskGrid.addColumn(task -> formatDate(task.getEstimatedEndTimeDate())).setHeader("Fecha estimada de finalización");
            taskGrid.addColumn(task -> task.getNeeds().size()).setHeader("Cantidad de necesidades cubiertas");
            taskGrid.addColumn(task -> task.getVolunteers().size()).setHeader("Cantidad de Voluntarios");
        }

        taskGrid.addItemClickListener(event -> {
            if (event.getClickCount() == 2) {
                TaskDTO selectedTask = event.getItem();
                UI.getCurrent().navigate("editTask", QueryParameters.simple(
                        Collections.singletonMap("id", String.valueOf(selectedTask.getId()))));
            }
        });
    }

    //===============================Get Components=========================================
    private Component getFilters() {
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setAlignItems(Alignment.BASELINE);
        filterLayout.setSpacing(true);

        TextField searchField = getSearchField();
        Button searchButton = getSearchButton(searchField);

        filterLayout.add(getPriorityFilter());

        return filterLayout;
    }

    private MultiSelectComboBox<Priority> getPriorityFilter() {
        MultiSelectComboBox<Priority> filter = new MultiSelectComboBox<>("Filtrar por prioridad");
        filter.setItems(Priority.LOW, Priority.MODERATE, Priority.URGENT);
        filter.setItemLabelGenerator(priority -> switch (priority) {
            case LOW -> "Baja";
            case MODERATE -> "Moderada";
            case URGENT -> "Urgente";
        });
        filter.addValueChangeListener(event -> applyPriorityFilter(event.getValue()));
        return filter;
    }

    private TextField getSearchField() {
        TextField searchField = new TextField("Buscar tarea");
        searchField.addKeyPressListener(Key.ENTER, e -> applySearchFilter(searchField.getValue()));
        return searchField;
    }

    private Button getSearchButton(TextField searchField) {
        Button searchButton = new Button("Buscar", e -> applySearchFilter(searchField.getValue()));
        searchButton.addClassName("search-button"); // Agregar la clase CSS
        return searchButton;
    }

    //===============================Manage Filters=========================================
    private void applySearchFilter(String searchTerm) {
        String term = searchTerm.trim().toLowerCase();
        tasksDataProvider.clearFilters();
        if (!term.isEmpty()) {
            tasksDataProvider.addFilter(task -> task.getName().toLowerCase().contains(term));
        }
        tasksDataProvider.refreshAll();
    }

    private void applyPriorityFilter(Set<Priority> selectedPriorities) {
        tasksDataProvider.clearFilters();
        if (!selectedPriorities.isEmpty()) {
            tasksDataProvider.addFilter(task -> selectedPriorities.contains(task.getPriority()));
        }
        tasksDataProvider.refreshAll();
    }


    private String formatDate(LocalDateTime taskDate) {
        if (taskDate == null) {
            return "No disponible";
        }
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}
