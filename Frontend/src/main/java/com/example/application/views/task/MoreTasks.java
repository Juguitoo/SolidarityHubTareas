package com.example.application.views.task;


import com.example.application.dto.TaskDTO;
import com.example.application.service.TaskService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.Set;

@PageTitle("Ver más tareas")
@Route("moretasks")
public class MoreTasks extends VerticalLayout {

    private final ListDataProvider<TaskDTO> dataProvider;
    private final TaskService taskService;
    private final Grid<TaskDTO> taskGrid;

    public MoreTasks() {
        this.taskGrid = new Grid<>(TaskDTO.class);
        this.taskService = new TaskService();
        this.dataProvider = new ListDataProvider<>(initializeTasks());

        Button goBack = createGoBackButton();

        H1 title = new H1("Todas las Tareas");

        //Filtros
        MultiSelectComboBox<String> priorityFilter = createPriorityFilter();
        TextField searchField = createSearchField();
        Button searchButton = createSearchButton(searchField);
        HorizontalLayout filterLayout = new HorizontalLayout(priorityFilter, searchField, searchButton);
        filterLayout.setAlignItems(Alignment.BASELINE);
        filterLayout.setSpacing(true);

        populateTaskGrid();

        add(goBack, title, filterLayout, taskGrid);
    }

    private Button createGoBackButton() {
        Button goBack = new Button(new Icon("vaadin", "arrow-left"));
        goBack.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("task")));
        goBack.addClassName("back-button");
        return goBack;
    }

    private MultiSelectComboBox<String> createPriorityFilter() {
        MultiSelectComboBox<String> filter = new MultiSelectComboBox<>("Filtrar por prioridad");
        filter.setItems("Baja", "Media", "Alta");
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
        taskGrid.setDataProvider(dataProvider);
        taskGrid.setColumns("name", "description", "startTimeDate", "priority");
        taskGrid.getColumnByKey("startTimeDate").setHeader("Start Time");
        taskGrid.getColumnByKey("priority").setHeader("Priority");
    }

    private void applySearchFilter(String searchTerm) {
        String term = searchTerm.trim().toLowerCase();
        dataProvider.clearFilters();
        if (!term.isEmpty()) {
            dataProvider.addFilter(task -> task.getName().toLowerCase().contains(term));
        }
        dataProvider.refreshAll();
    }

    private void applyPriorityFilter(Set<String> selectedPriorities) {
        dataProvider.clearFilters();
        if (!selectedPriorities.isEmpty()) {
            dataProvider.addFilter(task -> selectedPriorities.contains(task.getPriority()));
        }
    }

    private List<TaskDTO> initializeTasks() {
        return taskService.getTasks();
    }
}
