package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.model.enums.EmergencyLevel;
import solidarityhub.frontend.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import solidarityhub.frontend.views.catastrophe.CatastropheSelectionView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Tareas")
@Route("tasks")
@Menu(order = 1, icon = LineAwesomeIconUrl.TASKS_SOLID)
public class TaskView extends VerticalLayout implements BeforeEnterObserver {

    private final TaskService taskService;
    private CatastropheDTO selectedCatastrophe;

    public TaskView(TaskService taskService) {
        this.taskService = taskService;
        addClassName("tasks-container");
        beforeEnter(null);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Verificar si hay una catástrofe seleccionada
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        // Si no hay catástrofe seleccionada, redireccionar a la pantalla de selección
        if (selectedCatastrophe == null) {
            Notification.show("Por favor, selecciona una catástrofe primero",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            event.forwardTo(CatastropheSelectionView.class);
            return;
        }

        // Construir la vista con la catástrofe seleccionada
        buildView();
    }

    private void buildView() {
        // Limpiar componentes previos si los hay
        removeAll();

        // Título con el nombre de la catástrofe
        H1 title = new H1("Tareas: " + selectedCatastrophe.getName());
        title.addClassNames("task-title");

        Button addTask = new Button(new Icon("vaadin", "plus"));
        addTask.addClickListener(e -> UI.getCurrent().navigate("addtask"));
        addTask.addClassNames("button", "addTaskButton");

        Button moreTasks = new Button("Ver todas las tareas");
        moreTasks.addClickListener(e -> UI.getCurrent().navigate("moretasks"));
        moreTasks.addClassName("more-tasks-button");

        add(
            title,
            getTasks(),
            moreTasks,
            addTask
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
            toDoTasks = taskService.getToDoTasksByCatastrophe( selectedCatastrophe.getId(), 3).stream()
                    .map(task -> new TaskComponent(task.getId(), task.getName(), task.getDescription(), formatDate(task.getStartTimeDate()), task.getPriority().toString(), formatEmergencyLevel(task.getEmergencyLevel())))
                    .toList();

            if (toDoTasks.isEmpty()) {
                toDoTasksLayout.add(new Span("No hay tareas pendientes para esta catástrofe."));
            } else {
                toDoTasks.forEach(toDoTasksLayout::add);
            }
        } catch (Exception e) {
            toDoTasksLayout.add(new Span("Error al cargar tareas: " + e.getMessage()));
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
            // Obtener las tareas filtradas por la catástrofe seleccionada
            doingTasks = taskService.getDoingTasksByCatastrophe(selectedCatastrophe.getId(),3).stream()
                    .map(task -> new TaskComponent(task.getId(), task.getName(), task.getDescription(), formatDate(task.getStartTimeDate()), task.getPriority().toString(), "high"))
                    .toList();

            if (doingTasks.isEmpty()) {
                doingTasksLayout.add(new Span("No hay tareas en proceso para esta catástrofe."));
            } else {
                doingTasks.forEach(doingTasksLayout::add);
            }
        } catch (Exception e) {
            doingTasksLayout.add(new Span("Error al cargar tareas: " + e.getMessage()));
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
            // Obtener las tareas filtradas por la catástrofe seleccionada
            doneTasks = taskService.getDoneTasksByCatastrophe(selectedCatastrophe.getId(),3).stream()
                    .map(task -> new TaskComponent(task.getId(), task.getName(), task.getDescription(), formatDate(task.getStartTimeDate()), task.getPriority().toString(), "high"))
                    .toList();

            if (doneTasks.isEmpty()) {
                doneTasksLayout.add(new Span("No hay tareas terminadas para esta catástrofe."));
            } else {
                doneTasks.forEach(doneTasksLayout::add);
            }
        } catch (Exception e) {
            doneTasksLayout.add(new Span("Error al cargar tareas: " + e.getMessage()));
        }

        return doneTasksLayout;
    }

    private String formatDate(LocalDateTime taskDate) {
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    private String formatEmergencyLevel(EmergencyLevel level) {
        return switch (level) {
            case LOW -> "Baja";
            case MEDIUM -> "Media";
            case HIGH -> "Alta";
            case VERYHIGH -> "Muy alta";
        };
    }
}
