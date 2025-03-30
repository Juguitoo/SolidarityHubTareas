package solidarityhub.frontend.views.task;


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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@PageTitle("Ver más tareas")
@Route("moretasks")
public class MoreTasks extends VerticalLayout {

    private final TaskService taskService;

    private final ListDataProvider<TaskDTO> dataProvider;
    private final Grid<TaskDTO> taskGrid;

    public MoreTasks() {
        this.taskService = new TaskService();

        this.taskGrid = new Grid<>(TaskDTO.class);
        this.dataProvider = new ListDataProvider<>(initializeTasks());

        //Header
        Div header = new Div();
        header.addClassName("header");

        Button backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("")));
        backButton.addClassName("back-button");

        H1 title = new H1("Todas las Tareas");
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
            add(new Span("No hay datos disponibles."));
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
    }

    private List<TaskDTO> initializeTasks() {
        return taskService.getTasks();
    }

    private String formatDate(LocalDateTime taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}
