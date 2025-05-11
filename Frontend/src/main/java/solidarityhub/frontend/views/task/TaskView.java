package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.model.enums.EmergencyLevel;
import solidarityhub.frontend.model.enums.Status;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.TaskService;
import com.vaadin.flow.component.button.Button;
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
import solidarityhub.frontend.views.HeaderComponent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@PageTitle("Tareas")
@Route("tasks")
@Menu(order = 1, icon = LineAwesomeIconUrl.TASKS_SOLID)
public class TaskView extends VerticalLayout implements BeforeEnterObserver {
    protected static Translator translator;

    private final TaskService taskService;
    private final CatastropheService catastropheService;
    private CatastropheDTO selectedCatastrophe;

    public TaskView() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        }else{
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());

        this.taskService = new TaskService();
        catastropheService = new CatastropheService();

        addClassName("tasks-container");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Boolean cacheAttribute = (Boolean) VaadinSession.getCurrent().getAttribute("cache");
        if (cacheAttribute != null && cacheAttribute) {
            taskService.clearCache();
        }

        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
        if (!catastropheService.isCatastropheSelected(event, selectedCatastrophe)) {
            return;
        }

        buildView();
    }

    private void buildView() {
        removeAll();

        HeaderComponent header = new HeaderComponent(translator.get("task_view_title") + selectedCatastrophe.getName());

        add(
            header,
            getActionButtons(),
            getTasks()
        );
    }

    private Component getActionButtons() {
        HorizontalLayout actionButtonsLayout = new HorizontalLayout();
        actionButtonsLayout.addClassName("action-buttons__layout");

        Button addTaskButton = new Button(translator.get("add_task_button"), new Icon("vaadin", "plus"));
        addTaskButton.addClickListener(e ->UI.getCurrent().navigate("addtask"));
        addTaskButton.addClassNames("tasks-button");

        Button moreTasksButton = new Button(translator.get("all_tasks_button"), new Icon("vaadin", "clipboard"));
        moreTasksButton.addClickListener(e -> UI.getCurrent().navigate("moretasks"));
        moreTasksButton.addClassName("tasks-button");

        Button sugestedTasksButton = new Button(translator.get("suggested_tasks_button"), new Icon("vaadin", "lightbulb"));
        sugestedTasksButton.addClickListener(e -> UI.getCurrent().navigate("suggested-tasks"));
        sugestedTasksButton.addClassName("tasks-button");

        actionButtonsLayout.setAlignItems(Alignment.CENTER);

        actionButtonsLayout.add(addTaskButton, moreTasksButton, sugestedTasksButton);
        return actionButtonsLayout;
    }

    //===============================Get Tasks=========================================
    private Component getTasks(){
        HorizontalLayout tasksListsLayout = new HorizontalLayout();
        tasksListsLayout.addClassName("tasksLists__layout");
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

        H3 todoTitle = new H3(translator.get("todo_tasks"));
        todoTitle.addClassName("section-title");
        toDoLayout.add(todoTitle);

        Div taskListContainer = new Div();
        taskListContainer.addClassName("task-list__container");
        List<TaskComponent> toDoTasks;

        try {
            toDoTasks = taskService.getToDoTasksByCatastrophe(selectedCatastrophe.getId(), 3).stream()
                    .map(this::createDraggableTaskComponent)
                    .toList();

            if (toDoTasks.isEmpty()) {
                taskListContainer.add(new Span(translator.get("no_todo_tasks")));
            } else {
                toDoTasks.forEach(taskListContainer::add);
            }
        } catch (Exception e) {
            taskListContainer.add(new Span(translator.get("error_todo_tasks") + e.getMessage()));
        }

        toDoLayout.add(taskListContainer);

        // Configurar como drop target
        configureDropTarget(toDoLayout, Status.TO_DO);

        return toDoLayout;
    }

    private Component getDoingTasks() {
        VerticalLayout inProgressLayout = new VerticalLayout();
        inProgressLayout.addClassName("task-section");

        H3 doingTitle = new H3(translator.get("in_progress_tasks"));
        doingTitle.addClassName("section-title");
        inProgressLayout.add(doingTitle);

        Div taskListContainer = new Div();
        taskListContainer.addClassName("task-list__container");
        List<TaskComponent> doingTasks;

        try {
            doingTasks = taskService.getDoingTasksByCatastrophe(selectedCatastrophe.getId(), 3).stream()
                    .map(this::createDraggableTaskComponent)
                    .toList();

            if (doingTasks.isEmpty()) {
                taskListContainer.add(new Span(translator.get("no_in_progress_tasks")));
            } else {
                doingTasks.forEach(taskListContainer::add);
            }
        } catch (Exception e) {
            taskListContainer.add(new Span(translator.get("error_in_progress_tasks") + e.getMessage()));
        }

        inProgressLayout.add(taskListContainer);

        // Configurar como drop target
        configureDropTarget(inProgressLayout, Status.IN_PROGRESS);

        return inProgressLayout;
    }

    private Component getDoneTasks() {
        VerticalLayout doneLayout = new VerticalLayout();
        doneLayout.addClassName("task-section");

        H3 doneTitle = new H3(translator.get("terminated_tasks"));
        doneTitle.addClassName("section-title");
        doneLayout.add(doneTitle);

        Div taskListContainer = new Div();
        taskListContainer.addClassName("task-list__container");
        List<TaskComponent> doneTasks;

        try {
            doneTasks = taskService.getDoneTasksByCatastrophe(selectedCatastrophe.getId(), 3).stream()
                    .map(this::createDraggableTaskComponent)
                    .toList();

            if (doneTasks.isEmpty()) {
                taskListContainer.add(new Span(translator.get("no_terminated_tasks")));
            } else {
                doneTasks.forEach(taskListContainer::add);
            }
        } catch (Exception e) {
            taskListContainer.add(new Span(translator.get("error_terminated_tasks") + e.getMessage()));
        }

        doneLayout.add(taskListContainer);

        // Configurar como drop target
        configureDropTarget(doneLayout, Status.FINISHED);

        return doneLayout;
    }

    //===============================Update Status Methods=========================================
    private TaskComponent createDraggableTaskComponent(TaskDTO task) {
        TaskComponent taskComponent = new TaskComponent(task);

        // Configurar el componente como origen de arrastre según la documentación de Vaadin
        DragSource<TaskComponent> dragSource = DragSource.create(taskComponent);

        // Asociar los datos de la tarea para acceder a ellos al soltar
        dragSource.setDragData(task.getId());

        return taskComponent;
    }

    private void updateTaskStatus(int taskId, Status newStatus) {
        try {
            TaskDTO originalTask = taskService.getTaskById(taskId);
            if (originalTask != null) {
                if (originalTask.getStatus() == newStatus) {
                    return;
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
                        originalTask.getCatastropheId(),
                        originalTask.getMeetingDirection()
                );

                // Actualizar la tarea en el servicio
                taskService.updateTask(taskId, updatedTask);
                taskService.clearCache(); // Limpiar caché para forzar recarga

                // Mostrar notificación
                String statusText = newStatus == Status.TO_DO ? translator.get("todo_tasks") :
                        newStatus == Status.IN_PROGRESS ? translator.get("in_progress_tasks") : translator.get("terminated_tasks");
                Notification.show(translator.get("task") + " '" + originalTask.getName() + "' " + translator.get("moved_to")+ statusText,
                                3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Reconstruir la vista para reflejar los cambios
                buildView();
            }
        } catch (Exception e) {
            Notification.show(translator.get("error_updating_task") + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
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
                    Notification.show(translator.get("error_verifying_task") + e.getMessage(),
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
            case LOW -> translator.get("low_emergency_level");
            case MEDIUM -> translator.get("medium_emergency_level");
            case HIGH -> translator.get("high_emergency_level");
            case VERYHIGH -> translator.get("very_high_emergency_level");
        };
    }
}