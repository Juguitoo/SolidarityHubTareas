package com.example.application.views;

import com.example.application.dto.TaskDTO;
import com.example.application.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Tareas2")
@Route("tasks")
@Menu(order = 3, icon = LineAwesomeIconUrl.TASKS_SOLID)
public class pruebaRest extends VerticalLayout {
    private final TaskService taskService;
    private final Grid<TaskDTO> grid;

    public pruebaRest() {
        this.taskService = new TaskService();
        this.grid = new Grid<>(TaskDTO.class);

        add(new Button("Load Tasks", e -> loadTasks()));
        add(grid);
    }

    private void loadTasks() {
        grid.setItems(taskService.getTasks());
    }
}
