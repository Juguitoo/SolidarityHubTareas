package solidarityhub.frontend.views.task;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import solidarityhub.frontend.dto.CatastropheDTO;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.model.enums.Priority;
import solidarityhub.frontend.service.TaskService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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
    private ListDataProvider<TaskDTO> dataProvider;
    private Grid<TaskDTO> taskGrid;

    @Autowired
    public MoreTasks(TaskService taskService) {
        addClassName("moreTasks_Container");
        this.taskService = taskService;

        // Inicializamos el grid pero no el dataProvider aún
        this.taskGrid = new Grid<>(TaskDTO.class);
        taskGrid.addClassName("moreTasks_grid");
        taskGrid.addItemClickListener(event -> {
            if (event.getClickCount() == 2) { // Doble clic
                TaskDTO selectedTask = event.getItem();
                navigateToEditTask(selectedTask.getId());
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Verificar si hay una catástrofe seleccionada
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        // Si no hay catástrofe seleccionada, redireccionar a la pantalla de selección
        if (selectedCatastrophe == null) {
            Notification.show("Por favor, selecciona una catástrofe primero",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            event.forwardTo(CatastropheSelectionView.class);
            return;
        }

        // Ahora inicializamos el dataProvider con las tareas filtradas
        this.dataProvider = new ListDataProvider<>(initializeTasks());

        // Construir la vista con la catástrofe seleccionada
        buildView();
    }

    private void buildView() {
        // Limpiar componentes previos si los hay
        removeAll();

        //Header
        Div header = new Div();
        header.addClassName("header");
        Button backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("tasks")));
        backButton.addClassName("back-button");
        H1 title = new H1("Tareas de " + selectedCatastrophe.getName());
        title.addClassName("title");
        header.add(backButton, title);

        //Filtros
        MultiSelectComboBox<Priority> priorityFilter = createPriorityFilter();
        TextField searchField = createSearchField();
        Button searchButton = createSearchButton(searchField);
        HorizontalLayout filterLayout = new HorizontalLayout(priorityFilter, searchField, searchButton);
        filterLayout.setAlignItems(Alignment.BASELINE);
        filterLayout.setSpacing(true);

        add(header, filterLayout, taskGrid);
        populateTaskGrid();
    }

    private void navigateToEditTask(int taskId) {
        UI.getCurrent().navigate("editTask",
                QueryParameters.simple(Collections.singletonMap("id", String.valueOf(taskId))));
    }

    private MultiSelectComboBox<Priority> createPriorityFilter() {
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

    private TextField createSearchField() {
        TextField searchField = new TextField("Buscar tarea");
        searchField.addKeyPressListener(Key.ENTER, e -> applySearchFilter(searchField.getValue()));
        return searchField;
    }

    private Button createSearchButton(TextField searchField) {
        Button searchButton = new Button("Buscar", e -> applySearchFilter(searchField.getValue()));
        searchButton.addClassName("search-button"); // Agregar la clase CSS
        return searchButton;
    }

    private void populateTaskGrid() {
        if (dataProvider.getItems().isEmpty()) {
            taskGrid.setVisible(false);
            add(new Span("No hay tareas disponibles para esta catástrofe."));
        } else {
            taskGrid.setVisible(true);
            taskGrid.setDataProvider(dataProvider);
            taskGrid.setColumns("name", "description", "priority");
            taskGrid.addColumn(task -> formatDate(task.getStartTimeDate())).setHeader("Fecha de creación");
            taskGrid.addColumn(task -> formatDate(task.getEstimatedEndTimeDate())).setHeader("Fecha estimada de finalización");
            taskGrid.addColumn(task -> task.getNeeds().size()).setHeader("Cantidad de necesidades cubiertas");
            taskGrid.addColumn(task -> task.getVolunteers().size()).setHeader("Cantidad de Voluntarios");
            taskGrid.getColumnByKey("priority").setHeader("Prioridad");
        }
    }

    private void applySearchFilter(String searchTerm) {
        String term = searchTerm.trim().toLowerCase();
        dataProvider.clearFilters();
        if (!term.isEmpty()) {
            dataProvider.addFilter(task -> task.getName().toLowerCase().contains(term));
        }
        dataProvider.refreshAll();
    }

    private void applyPriorityFilter(Set<Priority> selectedPriorities) {
        dataProvider.clearFilters();
        if (!selectedPriorities.isEmpty()) {
            dataProvider.addFilter(task -> selectedPriorities.contains(task.getPriority()));
        }
        dataProvider.refreshAll();
    }

    private List<TaskDTO> initializeTasks() {
        // Filtrar por la catástrofe seleccionada si hay una
        if (selectedCatastrophe != null) {
            return taskService.getTasksByCatastrophe(selectedCatastrophe.getId());
        } else {
            return Collections.emptyList(); // Devolver lista vacía si no hay catástrofe seleccionada
        }
    }

    private String formatDate(LocalDateTime taskDate) {
        if (taskDate == null) {
            return "No disponible";
        }
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}
