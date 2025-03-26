package com.example.application.views.task;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@PageTitle(" Ver más tareas")
@Route("moretasks")
public class moreTasks extends VerticalLayout {

    private final ListDataProvider<TaskComponent> dataProvider;

    public moreTasks() {
        List<TaskComponent> tasks = getTasks();
        dataProvider = new ListDataProvider<>(tasks);

        H1 title = new H1("Todas las Tareas");
        ComboBox<String> filterComboBox = new ComboBox<>("Filtrar por");
        filterComboBox.setItems("Nombre", "Prioridad", "Fecha", "Nivel de Emergencia");
        TextField searchField = new TextField("Buscar tarea");
        Button searchButton = new Button("Buscar");

        searchButton.addClickListener(e -> {
            String filter = filterComboBox.getValue();
            String searchTerm = searchField.getValue().toLowerCase();
            dataProvider.clearFilters();
            if (filter != null && !searchTerm.isEmpty()) {
                switch (filter) {
                    case "Nombre":
                        dataProvider.addFilter(task -> task.getTaskName().toLowerCase().contains(searchTerm));
                        break;
                    case "Prioridad":
                        dataProvider.addFilter(task -> task.getPriority().toLowerCase().contains(searchTerm));
                        break;
                    case "Fecha":
                        dataProvider.addFilter(task -> task.getStartTimeDate().toLowerCase().contains(searchTerm));
                        break;
                    case "Nivel de Emergencia":
                        dataProvider.addFilter(task -> task.getEmergencyLevel().toLowerCase().contains(searchTerm));
                        break;
                }
            }
        });

        Grid<TaskComponent> grid = new Grid<>(TaskComponent.class);
        grid.setDataProvider(dataProvider);
        grid.setColumns("taskName", "taskDescription", "startTimeDate", "priority", "emergencyLevel");

        add(title, filterComboBox, searchField, searchButton, grid);
    }

    private List<TaskComponent> getTasks() {
        List<TaskComponent> tasks = new ArrayList<>();
        tasks.add(new TaskComponent("Limpieza de baños", "Limpiar baños en el auditorio municipal", "01-01-2023 10:00", "Media", "low"));
        tasks.add(new TaskComponent("Abrir puertas", "Tarea en proceso", "04-01-2023 11:00", "Media", "high"));
        tasks.add(new TaskComponent("Tarea 3", "Tarea terminada", "03-01-2023 12:00", "Baja", "medium"));
        return tasks;
    }
}
