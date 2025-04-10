package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.model.enums.EmergencyLevel;
import solidarityhub.frontend.model.enums.Status;
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

    @Autowired
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
            if (event != null) {
                event.forwardTo(CatastropheSelectionView.class);
            }
            return;
        }

        // Construir la vista con la catástrofe seleccionada
        buildView();
    }

    private void buildView() {
        removeAll();

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

    //===============================Get Tasks=========================================
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
        VerticalLayout toDoLayout = new VerticalLayout();
        toDoLayout.addClassName("task-section");

        H3 todoTitle = new H3("Por hacer");
        todoTitle.addClassName("section-title");
        toDoLayout.add(todoTitle);

        List<TaskComponent> toDoTasks;

        try {
            toDoTasks = taskService.getToDoTasksByCatastrophe(selectedCatastrophe.getId(), 3).stream()
                    .map(this::createDraggableTaskComponent)
                    .toList();

            if (toDoTasks.isEmpty()) {
                toDoLayout.add(new Span("No hay tareas pendientes para esta catástrofe."));
            } else {
                toDoTasks.forEach(toDoLayout::add);
            }
        } catch (Exception e) {
            toDoLayout.add(new Span("Error al cargar tareas: " + e.getMessage()));
        }

        // Configurar como drop target
        configureDropTarget(toDoLayout, Status.TO_DO);

        return toDoLayout;
    }

    private Component getDoingTasks() {
        VerticalLayout inProgressLayout = new VerticalLayout();
        inProgressLayout.addClassName("task-section");

        H3 doingTitle = new H3("En proceso");
        doingTitle.addClassName("section-title");
        inProgressLayout.add(doingTitle);

        List<TaskComponent> doingTasks;

        try {
            doingTasks = taskService.getDoingTasksByCatastrophe(selectedCatastrophe.getId(), 3).stream()
                    .map(this::createDraggableTaskComponent)
                    .toList();

            if (doingTasks.isEmpty()) {
                inProgressLayout.add(new Span("No hay tareas en proceso para esta catástrofe."));
            } else {
                doingTasks.forEach(inProgressLayout::add);
            }
        } catch (Exception e) {
            inProgressLayout.add(new Span("Error al cargar tareas: " + e.getMessage()));
        }

        // Configurar como drop target
        configureDropTarget(inProgressLayout, Status.IN_PROGRESS);

        return inProgressLayout;
    }

    private Component getDoneTasks() {
        VerticalLayout doneLayout = new VerticalLayout();
        doneLayout.addClassName("task-section");

        H3 doneTitle = new H3("Terminadas");
        doneTitle.addClassName("section-title");
        doneLayout.add(doneTitle);

        List<TaskComponent> doneTasks;
        try {
            doneTasks = taskService.getDoneTasksByCatastrophe(selectedCatastrophe.getId(), 3).stream()
                    .map(this::createDraggableTaskComponent)
                    .toList();

            if (doneTasks.isEmpty()) {
                doneLayout.add(new Span("No hay tareas terminadas para esta catástrofe."));
            } else {
                doneTasks.forEach(doneLayout::add);
            }
        } catch (Exception e) {
            doneLayout.add(new Span("Error al cargar tareas: " + e.getMessage()));
        }

        // Configurar como drop target
        configureDropTarget(doneLayout, Status.FINISHED);

        return doneLayout;
    }

    //===============================Update Status Methods=========================================
    private void updateTaskStatus(int taskId, Status newStatus) {
        try {
            TaskDTO originalTask = taskService.getTaskById(taskId);
            if (originalTask != null) {
                // Verificar si el estado actual es el mismo que el nuevo estado
                if (originalTask.getStatus() == newStatus) {
                    return; // No hacer nada si el estado no ha cambiado
                }

                // Crear un nuevo TaskDTO con el estado actualizado
                TaskDTO updatedTask = new TaskDTO(
                        originalTask.getName(),
                        originalTask.getDescription(),
                        originalTask.getStartTimeDate(),
                        originalTask.getEstimatedEndTimeDate(),
                        originalTask.getType(),
                        originalTask.getPriority(),
                        originalTask.getEmergencyLevel(),
                        newStatus, // Aquí actualizamos el estado
                        originalTask.getNeeds(),
                        originalTask.getVolunteers(),
                        originalTask.getCatastropheId()
                );

                // Actualizar la tarea en el servicio
                taskService.updateTask(taskId, updatedTask);
                taskService.clearCache(); // Limpiar caché para forzar recarga

                // Mostrar notificación
                String statusText = newStatus == Status.TO_DO ? "Por hacer" :
                        newStatus == Status.IN_PROGRESS ? "En proceso" : "Terminada";
                Notification.show("Tarea '" + originalTask.getName() + "' movida a " + statusText,
                                3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Reconstruir la vista para reflejar los cambios
                buildView();
            }
        } catch (Exception e) {
            Notification.show("Error al actualizar la tarea: " + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private TaskComponent createDraggableTaskComponent(TaskDTO task) {
        TaskComponent taskComponent = new TaskComponent(
                task.getId(),
                task.getName(),
                task.getDescription(),
                formatDate(task.getStartTimeDate()),
                task.getPriority().toString(),
                formatEmergencyLevel(task.getEmergencyLevel())
        );

        // Configurar el componente como origen de arrastre según la documentación de Vaadin
        DragSource<TaskComponent> dragSource = DragSource.create(taskComponent);

        // Asociar los datos de la tarea para acceder a ellos al soltar
        dragSource.setDragData(task.getId());

        return taskComponent;
    }

    private void configureDropTarget(VerticalLayout layout, Status newStatus) {
        DropTarget<VerticalLayout> dropTarget = DropTarget.create(layout);
        dropTarget.setDropEffect(DropEffect.MOVE);

        dropTarget.addDropListener(event -> {
            if (event.getDragData().isPresent() && event.getDragData().get() instanceof Integer) {
                int taskId = (Integer) event.getDragData().get();

                try {
                    TaskDTO task = taskService.getTaskById(taskId);

                    // Solo actualizar si el estado actual es diferente del nuevo estado
                    if (task != null && task.getStatus() != newStatus) {
                        updateTaskStatus(taskId, newStatus);
                    }
                } catch (Exception e) {
                    Notification.show("Error al verificar el estado de la tarea: " + e.getMessage(),
                                    3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
        });
    }

    //===============================Format Methods=========================================
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