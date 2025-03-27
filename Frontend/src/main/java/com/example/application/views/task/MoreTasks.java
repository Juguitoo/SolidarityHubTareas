package com.example.application.views.task;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@PageTitle(" Ver más tareas")
@Route("moretasks")
public class MoreTasks extends VerticalLayout {

    private final ListDataProvider<TaskComponent> dataProvider;


    public MoreTasks() {
        List<TaskComponent> tasks = getTasks();
        dataProvider = new ListDataProvider<>(tasks);

        H1 title = new H1("Todas las Tareas");
        MultiSelectComboBox<String> filterComboBox = new MultiSelectComboBox<>("Filtrar por prioridad");
        filterComboBox.setItems("Baja", "Media", "Alta");
        TextField searchField = new TextField("Buscar tarea");
        Button searchButton = new Button("Buscar");

        HorizontalLayout horizontalLayout = new HorizontalLayout(filterComboBox, searchField, searchButton);
        horizontalLayout.setAlignItems(Alignment.BASELINE);
        horizontalLayout.setSpacing(true);

        VerticalLayout verticalLayout = new VerticalLayout(title, horizontalLayout);
        verticalLayout.setDefaultHorizontalComponentAlignment(Alignment.CENTER);


        filterComboBox.addValueChangeListener(event -> {
            Set<String> selectedPriorities = event.getValue();
            dataProvider.clearFilters();
            if (!selectedPriorities.isEmpty()) {
                dataProvider.addFilter(task -> selectedPriorities.contains(task.getPriority()));
            }
        });


        searchButton.addClickListener(e -> {
            String searchTerm = searchField.getValue().toLowerCase();
            dataProvider.clearFilters();
            if (!searchTerm.isEmpty()) {
                dataProvider.addFilter(task -> task.getTaskName().toLowerCase().contains(searchTerm));
            }
        });

        Grid<TaskComponent> grid = new Grid<>(TaskComponent.class);
        grid.setDataProvider(dataProvider);
        grid.setColumns("taskName", "taskDescription", "startTimeDate", "priority", "emergencyLevel");

        add(verticalLayout, grid);

    }

    private List<TaskComponent> getTasks() {
        List<TaskComponent> tasks = new ArrayList<>();
        tasks.add(new TaskComponent("Limpieza de baños", "Limpiar baños en el auditorio municipal", "01-01-2023 10:00", "Media", "low"));
        tasks.add(new TaskComponent("Abrir puertas", "Tarea en proceso", "04-01-2023 11:00", "Media", "high"));
        tasks.add(new TaskComponent("Tarea 3", "Tarea terminada", "03-01-2023 12:00", "Baja", "medium"));
        return tasks;
    }
}
