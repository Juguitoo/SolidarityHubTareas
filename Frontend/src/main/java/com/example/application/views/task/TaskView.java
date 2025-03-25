package com.example.application.views.task;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.scheduling.config.Task;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import solidarityhub.backend.model.enums.Priority;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@PageTitle("Tareas")
@Route("task")
@Menu(order = 2, icon = LineAwesomeIconUrl.TASKS_SOLID)

public class TaskView extends VerticalLayout {

    public TaskView() {
        H1 title = new H1("Tareas");

        TaskComponent taskComponent1 = new TaskComponent("Tarea 1", "Descripcion de la tarea 1", formatDate(LocalDateTime.now()), "Urgente", "high");
        TaskComponent taskComponent2 = new TaskComponent("Tarea 1", "Descripcion de la tarea 1", formatDate(LocalDateTime.now()), "Urgente", "veryHigh");

        Button addTask = new Button("Crear tarea");
        addTask.addClickListener(e -> {
            // Add task
        });

        setAlignSelf(Alignment.CENTER, title);
        setAlignSelf(Alignment.START, addTask);

        add(title, taskComponent1, taskComponent2, addTask);
    }

    private String formatDate(LocalDateTime taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}
