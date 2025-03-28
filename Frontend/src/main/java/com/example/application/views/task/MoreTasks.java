package com.example.application.views.task;


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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@PageTitle("Ver más tareas")
@Route("moretasks")
public class MoreTasks extends VerticalLayout {

    private final ListDataProvider<TaskComponent> dataProvider;

    public MoreTasks() {
        this.dataProvider = new ListDataProvider<>(initializeTasks());

        Button goBack = createGoBackButton();
        H1 title = new H1("Todas las Tareas");
        MultiSelectComboBox<String> priorityFilter = createPriorityFilter();
        TextField searchField = createSearchField();
        Button searchButton = createSearchButton(searchField);
        Grid<TaskComponent> taskGrid = createTaskGrid();
        HorizontalLayout filterLayout = new HorizontalLayout(priorityFilter, searchField, searchButton);
        filterLayout.setAlignItems(Alignment.BASELINE);
        filterLayout.setSpacing(true);

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

    private Grid<TaskComponent> createTaskGrid() {
        Grid<TaskComponent> grid = new Grid<>(TaskComponent.class);
        grid.setDataProvider(dataProvider);
        grid.setColumns("taskName", "taskDescription", "startTimeDate", "priority", "emergencyLevel");
        return grid;
    }

    private void applySearchFilter(String searchTerm) {
        String term = searchTerm.trim().toLowerCase();
        dataProvider.clearFilters();
        if (!term.isEmpty()) {
            dataProvider.addFilter(task -> task.getTaskName().toLowerCase().contains(term));
        }
        dataProvider.refreshAll();
    }

    private void applyPriorityFilter(Set<String> selectedPriorities) {
        dataProvider.clearFilters();
        if (!selectedPriorities.isEmpty()) {
            dataProvider.addFilter(task -> selectedPriorities.contains(task.getPriority()));
        }
    }

    private List<TaskComponent> initializeTasks() {
        List<TaskComponent> tasks = new ArrayList<>();
        tasks.add(new TaskComponent("Limpieza de baños", "Limpiar baños en el auditorio municipal", "01-01-2023 10:00", "Media", "low"));
        tasks.add(new TaskComponent("Abrir puertas", "Tarea en proceso", "04-01-2023 11:00", "Media", "high"));
        tasks.add(new TaskComponent("Tarea 3", "Tarea terminada", "03-01-2023 12:00", "Baja", "medium"));
        return tasks;
    }
}
