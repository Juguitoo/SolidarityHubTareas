package com.example.application.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Tareas")
@Route("task")
@Menu(order = 2, icon = LineAwesomeIconUrl.TASKS_SOLID)

public class TaskView extends VerticalLayout {

    private final List<TaskComponent> tasks;

    public TaskView() {
        tasks = new ArrayList<>();
        addClassName("tasks-container");
        H1 title = new H1("Tareas");
        title.addClassNames("task-title");
        Button addTask = new Button("Crear tarea");
        addTask.addClickListener(e -> {
            // Add task
        });
        addTask.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTask.addClassNames(
                LumoUtility.Margin.Bottom.MEDIUM,
                LumoUtility.Margin.Left.AUTO,
                LumoUtility.Margin.Right.AUTO
        );

        Button moreTasks = new Button("Ver todas las tareas");
        moreTasks.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("moretasks")));

        setAlignSelf(Alignment.CENTER, title);
        setAlignSelf(Alignment.START, addTask);

        add(title,
            getTasks(),
            addTask,
            moreTasks
        );
    }

    private Component getTasks(){
        HorizontalLayout tasksListsLayout = new HorizontalLayout();
        setAlignSelf(Alignment.CENTER, tasksListsLayout);
        tasksListsLayout.addClassName("tasks-lists");
        tasksListsLayout.add(
            getToDoTasks(),
            getDoingTasks(),
            getDoneTasks()
        );
        return tasksListsLayout;
    }

    private Component getToDoTasks(){
        VerticalLayout toDoTasksList = new VerticalLayout();
        H3 todoTitle = new H3("Por hacer");
        setAlignSelf(Alignment.CENTER, todoTitle, toDoTasksList);

        TaskComponent toDoTask = new TaskComponent("Tarea 1", "Tarea por hacer", formatDate(LocalDateTime.now()), "High", "veryHigh");
        tasks.add(toDoTask);
        toDoTasksList.add(todoTitle, toDoTask);

        return toDoTasksList;
    }

    private Component getDoingTasks(){
        VerticalLayout doingTasksList = new VerticalLayout();
        H3 doingTitle = new H3("En proceso");
        setAlignSelf(Alignment.CENTER, doingTitle, doingTasksList);

        TaskComponent doingTask = new TaskComponent("Tarea 2", "Tarea en proceso", formatDate(LocalDateTime.now()), "High", "high");
        tasks.add(doingTask);
        doingTasksList.add(doingTitle, doingTask);

        return doingTasksList;
    }

    private Component getDoneTasks(){
        VerticalLayout doneTasksList = new VerticalLayout();
        H3 doneTitle = new H3("Terminadas");
        setAlignSelf(Alignment.CENTER, doneTitle, doneTasksList);

        TaskComponent doneTask = new TaskComponent("Tarea 3", "Tarea terminada", formatDate(LocalDateTime.now()), "low", "medium");
        tasks.add(doneTask);
        doneTasksList.add(doneTitle, doneTask);

        return doneTasksList;
    }

    private String formatDate(LocalDateTime taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    public List<TaskComponent> getAllTasks() {
        return tasks;
    }
}
