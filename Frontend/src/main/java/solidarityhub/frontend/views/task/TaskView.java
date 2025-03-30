package solidarityhub.frontend.views.task;

import solidarityhub.frontend.service.TaskService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Tareas")
@Route("")
@Menu(order = 1, icon = LineAwesomeIconUrl.TASKS_SOLID)

public class TaskView extends VerticalLayout {

    private final TaskService taskService = new TaskService();

    public TaskView() {
        addClassName("tasks-container");

        H1 title = new H1("Tareas");
        title.addClassNames("task-title");

        Button addTask = new Button("Crear tarea");
        addTask.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("addtask")));
        addTask.addClassName("button");

        Button moreTasks = new Button("Ver todas las tareas");
        moreTasks.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("moretasks")));
        moreTasks.addClassName("button");


        add(title,
            getTasks(),
            addTask,
            moreTasks
        );
    }

    private Component getTasks(){
        HorizontalLayout tasksListsLayout = new HorizontalLayout();
        tasksListsLayout.addClassName("tasks-lists");
        tasksListsLayout.add(
            getToDoTasks(),
            getDoingTasks(),
            getDoneTasks()
        );
        tasksListsLayout.setDefaultVerticalComponentAlignment(Alignment.START);
        return tasksListsLayout;
    }

    private Component getToDoTasks() {
        VerticalLayout toDoTasksLayout = new VerticalLayout();
        toDoTasksLayout.addClassName("task-section");

        H3 todoTitle = new H3("Por hacer");
        todoTitle.addClassName("section-title");
        toDoTasksLayout.add(todoTitle);


        List<TaskComponent> toDoTasks;

        try {
            toDoTasks = taskService.getToDoTasks(3).stream()
                    .map(task -> new TaskComponent(task.getName(), task.getDescription(), formatDate(task.getStartTimeDate()), task.getPriority().toString(), "high"))
                    .toList();
            toDoTasks.forEach(toDoTasksLayout::add);
        } catch (Exception e) {
            toDoTasksLayout.add(new Span("No hay tareas."));
        }

        return toDoTasksLayout;
    }

    private Component getDoingTasks() {
        VerticalLayout doingTasksLayout = new VerticalLayout();
        doingTasksLayout.addClassName("task-section");

        H3 doingTitle = new H3("En proceso");
        doingTitle.addClassName("section-title");
        doingTasksLayout.add(doingTitle);

        List<TaskComponent> doingTasks;

        try {
            doingTasks = taskService.getDoingTasks(3).stream()
                    .map(task -> new TaskComponent(task.getName(), task.getDescription(), formatDate(task.getStartTimeDate()), task.getPriority().toString(), "high"))
                    .toList();
            doingTasks.forEach(doingTasksLayout::add);
        } catch (Exception e) {
            doingTasksLayout.add(new Span("No hay tareas."));
        }

        return doingTasksLayout;
    }

    private Component getDoneTasks() {
        VerticalLayout doneTasksLayout = new VerticalLayout();
        doneTasksLayout.addClassName("task-section");

        H3 doneTitle = new H3("Terminadas");
        doneTitle.addClassName("section-title");
        doneTasksLayout.add(doneTitle);

        List<TaskComponent> doneTasks;
        try {
            doneTasks = taskService.getDoneTasks(3).stream()
                    .map(task -> new TaskComponent(task.getName(), task.getDescription(), formatDate(task.getStartTimeDate()), task.getPriority().toString(), "high"))
                    .toList();
            doneTasks.forEach(doneTasksLayout::add);
        } catch (Exception e) {
            doneTasksLayout.add(new Span("No hay tareas."));
        }

        return doneTasksLayout;
    }


    private String formatDate(LocalDateTime taskDate) {
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

}
