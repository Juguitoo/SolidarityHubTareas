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

@PageTitle("Tareas")
@Route("task")
@Menu(order = 2, icon = LineAwesomeIconUrl.TASKS_SOLID)

public class TaskView extends VerticalLayout {

    public TaskView() {
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
        setAlignSelf(Alignment.CENTER, title);
        setAlignSelf(Alignment.START, addTask);

        add(title,
            getTasks(),
            addTask
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

        TaskComponent toDoTask;
        toDoTasksList.add(
            todoTitle,
            toDoTask = new TaskComponent("Tarea 1", "Tarea por hacer", formatDate(LocalDateTime.now()), "High", "veryHigh")
        );

        return toDoTasksList;
    }

    private Component getDoingTasks(){
        VerticalLayout doingTasksList = new VerticalLayout();
        H3 doingTitle = new H3("En proceso");
        setAlignSelf(Alignment.CENTER, doingTitle, doingTasksList);

        TaskComponent doingTask;
        doingTasksList.add(
                doingTitle,
                doingTask = new TaskComponent("Tarea 2", "Tara en proceso", formatDate(LocalDateTime.now()), "High", "high")
        );

        return doingTasksList;
    }

    private Component getDoneTasks(){
        VerticalLayout doneTasksList = new VerticalLayout();
        H3 doneTitle = new H3("Terminadas");
        setAlignSelf(Alignment.CENTER, doneTitle, doneTasksList);

        TaskComponent doneTask;
        doneTasksList.add(
                doneTitle,
                doneTask = new TaskComponent("Tarea 3", "Tara terminada", formatDate(LocalDateTime.now()), "low", "medium")
        );

        return doneTasksList;
    }

    private String formatDate(LocalDateTime taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}
