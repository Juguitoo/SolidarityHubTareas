package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropEvent;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import lombok.extern.slf4j.Slf4j;
import org.pingu.web.BackendObservableService.observableList.Observer;
import org.pingu.web.BackendObservableService.observableList.ObserverChange;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.i18n.Translator;
import org.pingu.domain.enums.EmergencyLevel;
import org.pingu.domain.enums.Status;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.FormatService;
import solidarityhub.frontend.service.TaskService;
import solidarityhub.frontend.views.HeaderComponent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@PageTitle("Tareas")
@Route("tasks")
public class TaskView extends VerticalLayout implements BeforeEnterObserver {
    protected static Translator translator;

    private final TaskService taskService;
    private final FormatService formatService;
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

        //BackendDTOObservableService.GetInstancia().getTaskList().getValues().attach(this, ObserverChange.ADD_ALL);

        this.taskService = new TaskService();
        this.formatService = FormatService.getInstance();
        this.catastropheService = new CatastropheService();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
        if (!catastropheService.isCatastropheSelected(event, selectedCatastrophe)) {
            return;
        }

        buildView();
    }

    private void buildView() {
        removeAll();

        addClassName("tasks-container");
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
        tasksListsLayout.setWidthFull();
        tasksListsLayout.setAlignItems(Alignment.CENTER);

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

        List<TaskComponent> toDoTasks;

        try {
            toDoTasks = taskService.getToDoTasksByCatastrophe(selectedCatastrophe.getId(), 3).stream()
                    .map(this::createDraggableTaskComponent)
                    .toList();

            if (toDoTasks.isEmpty()) {
                toDoLayout.add(new Span(translator.get("no_todo_tasks")));
            } else {
                toDoTasks.forEach(toDoLayout::add);
            }
        } catch (Exception e) {
            toDoLayout.add(new Span(translator.get("error_todo_tasks") + e.getMessage()));
        }

        configureDropTarget(toDoLayout, Status.TO_DO);

        return toDoLayout;
    }

    private Component getDoingTasks() {
        VerticalLayout inProgressLayout = new VerticalLayout();
        inProgressLayout.addClassName("task-section");

        H3 doingTitle = new H3(translator.get("in_progress_tasks"));
        doingTitle.addClassName("section-title");
        inProgressLayout.add(doingTitle);

        List<TaskComponent> doingTasks;

        try {
            doingTasks = taskService.getDoingTasksByCatastrophe(selectedCatastrophe.getId(), 3).stream()
                    .map(this::createDraggableTaskComponent)
                    .toList();

            if (doingTasks.isEmpty()) {
                inProgressLayout.add(new Span(translator.get("no_in_progress_tasks")));
            } else {
                doingTasks.forEach(inProgressLayout::add);
            }
        } catch (Exception e) {
            inProgressLayout.add(new Span(translator.get("error_in_progress_tasks") + e.getMessage()));
        }


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

        List<TaskComponent> doneTasks;

        try {
            doneTasks = taskService.getDoneTasksByCatastrophe(selectedCatastrophe.getId(), 3).stream()
                    .map(this::createDraggableTaskComponent)
                    .toList();

            if (doneTasks.isEmpty()) {
                doneLayout.add(new Span(translator.get("no_terminated_tasks")));
            } else {
                doneTasks.forEach(doneLayout::add);
            }
        } catch (Exception e) {
            doneLayout.add(new Span(translator.get("error_terminated_tasks") + e.getMessage()));
        }

        // Configurar como drop target
        configureDropTarget(doneLayout, Status.FINISHED);

        return doneLayout;
    }

    //===============================Update Status Methods=========================================
    private TaskComponent createDraggableTaskComponent(TaskDTO task) {
        TaskComponent taskComponent = new TaskComponent(task);

        // Configurar drag source
        DragSource<TaskComponent> dragSource = DragSource.create(taskComponent);

        // CORRECCIÓN: Usar setDragData pero con verificación
        dragSource.setDragData(task.getId());

        // Opcional: Añadir efectos visuales básicos
        dragSource.addDragStartListener(event -> {
            event.getSource().addClassName("dragging");
        });

        dragSource.addDragEndListener(event -> {
            event.getSource().removeClassName("dragging");
        });

        return taskComponent;
    }

    private void updateTaskStatus(int taskId, Status newStatus) {
        try {
            TaskDTO originalTask = taskService.getTaskById(taskId);
            if (originalTask != null && originalTask.getStatus() != newStatus) {

                // Usar el método específico para solo cambiar estado
                taskService.updateTaskStatusOnly(taskId, newStatus);

                // Resto del código igual...
            }
        } catch (Exception e) {
            // Manejo de errores...
        }
    }

    private void updateTaskUIAfterStatusChange(TaskDTO task, Status newStatus) {
        Status oldStatus = task.getStatus();
        task.setStatus(newStatus);

        UI.getCurrent().access(() -> {
            // Buscar los contenedores de tareas por estado
            Optional<VerticalLayout> sourceContainer = findTaskContainerByStatus(oldStatus);
            Optional<VerticalLayout> targetContainer = findTaskContainerByStatus(newStatus);

            if (sourceContainer.isPresent() && targetContainer.isPresent()) {
                // Buscar el componente de tarea a mover
                TaskComponent taskComponent = findTaskComponentById(task.getId(), sourceContainer.get());

                if (taskComponent != null) {
                    // Eliminar de su contenedor actual
                    sourceContainer.get().remove(taskComponent);

                    // Añadir al nuevo contenedor
                    targetContainer.get().add(taskComponent);

                    // Actualizar el mensaje de "no hay tareas" si es necesario
                    updateEmptyStateMessages(sourceContainer.get(), oldStatus);
                    updateEmptyStateMessages(targetContainer.get(), newStatus);
                }
            }
        });
    }

    private Optional<VerticalLayout> findTaskContainerByStatus(Status status) {
        // Buscar el contenedor correspondiente al estado
        for (int i = 0; i < getComponentCount(); i++) {
            Component component = getComponentAt(i);
            if (component instanceof HorizontalLayout) {
                HorizontalLayout taskListsLayout = (HorizontalLayout) component;

                for (int j = 0; j < taskListsLayout.getComponentCount(); j++) {
                    Component column = taskListsLayout.getComponentAt(j);
                    if (column instanceof VerticalLayout) {
                        VerticalLayout taskColumn = (VerticalLayout) column;

                        // Verificar si es la columna correcta por título
                        if (isColumnForStatus(taskColumn, status)) {
                            return Optional.of(taskColumn);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private boolean isColumnForStatus(VerticalLayout column, Status status) {
        // Verificar el título de la columna
        if (column.getComponentCount() > 0) {
            Component titleComponent = column.getComponentAt(0);
            if (titleComponent instanceof H3) {
                H3 title = (H3) titleComponent;
                String titleText = title.getText();

                return (status == Status.TO_DO && titleText.equals(translator.get("todo_tasks"))) ||
                        (status == Status.IN_PROGRESS && titleText.equals(translator.get("in_progress_tasks"))) ||
                        (status == Status.FINISHED && titleText.equals(translator.get("terminated_tasks")));
            }
        }
        return false;
    }

    private TaskComponent findTaskComponentById(int taskId, VerticalLayout container) {
        // Buscar el componente de tarea por ID en el contenedor
        for (int i = 0; i < container.getComponentCount(); i++) {
            Component component = container.getComponentAt(i);
            if (component instanceof TaskComponent) {
                TaskComponent taskComponent = (TaskComponent) component;
                if (taskComponent.getTaskId() == taskId) {
                    return taskComponent;
                }
            }
        }
        return null;
    }

    private void updateEmptyStateMessages(VerticalLayout container, Status status) {
        // Si no hay tareas después de eliminar una, mostrar mensaje
        boolean hasTasks = false;
        for (int i = 0; i < container.getComponentCount(); i++) {
            if (container.getComponentAt(i) instanceof TaskComponent) {
                hasTasks = true;
                break;
            }
        }

        if (!hasTasks) {
            // No hay tareas, añadir mensaje
            String message = "";
            if (status == Status.TO_DO) {
                message = translator.get("no_todo_tasks");
            } else if (status == Status.IN_PROGRESS) {
                message = translator.get("no_in_progress_tasks");
            } else if (status == Status.FINISHED) {
                message = translator.get("no_terminated_tasks");
            }

            // Eliminar cualquier mensaje existente
            for (int i = container.getComponentCount() - 1; i >= 0; i--) {
                Component comp = container.getComponentAt(i);
                if (comp instanceof Span) {
                    container.remove(comp);
                }
            }

            // Añadir nuevo mensaje
            container.add(new Span(message));
        }
    }

    private void configureDropTarget(VerticalLayout layout, Status newStatus) {
        DropTarget<VerticalLayout> dropTarget = DropTarget.create(layout);
        dropTarget.setDropEffect(DropEffect.MOVE);

        dropTarget.addDropListener(event -> {
            try {
                // CORRECCIÓN: Manejo más robusto de los datos
                Optional<Object> dragDataOpt = event.getDragData();

                if (dragDataOpt.isPresent()) {
                    Object dragData = dragDataOpt.get();
                    Integer taskId = null;

                    // Intentar convertir a Integer de diferentes maneras
                    if (dragData instanceof Integer) {
                        taskId = (Integer) dragData;
                    } else if (dragData instanceof Number) {
                        taskId = ((Number) dragData).intValue();
                    } else if (dragData instanceof String) {
                        try {
                            taskId = Integer.parseInt((String) dragData);
                        } catch (NumberFormatException e) {
                            System.err.println("No se pudo convertir string a integer: " + dragData);
                        }
                    } else {
                        System.err.println("Tipo de dragData no soportado: " + dragData.getClass().getName());
                    }

                    if (taskId != null) {
                        TaskDTO task = taskService.getTaskById(taskId);

                        if (task != null && task.getStatus() != newStatus) {
                            System.out.println("Actualizando tarea " + taskId + " de " + task.getStatus() + " a " + newStatus);
                            updateTaskStatus(taskId, newStatus);
                        } else if (task == null) {
                            System.err.println("Tarea no encontrada con ID: " + taskId);
                        } else {
                            System.out.println("La tarea " + taskId + " ya está en estado " + newStatus);
                        }
                    } else {
                        System.err.println("No se pudo obtener ID de tarea válido");
                    }
                } else {
                    System.err.println("No hay datos de drag disponibles");
                }

            } catch (Exception e) {
                System.err.println("Error en drop listener: " + e.getMessage());
                e.printStackTrace();

                Notification.show("Error al mover la tarea. Intenta recargar la página.",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

    private Integer extractTaskId(DropEvent<VerticalLayout> event) {
        try {
            // Intentar obtener desde dragData
            Optional<Object> dragDataOpt = event.getDragData();
            if (dragDataOpt.isPresent()) {
                Object dragData = dragDataOpt.get();

                if (dragData instanceof Integer) {
                    return (Integer) dragData;
                } else if (dragData instanceof String) {
                    return Integer.parseInt((String) dragData);
                } else if (dragData instanceof Number) {
                    return ((Number) dragData).intValue();
                }
            }

            // Como respaldo, intentar desde el componente fuente
            if (event.getDragSourceComponent().isPresent()) {
                Component source = event.getDragSourceComponent().get();
                if (source instanceof TaskComponent) {
                    return ((TaskComponent) source).getTaskId();
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error extrayendo task ID: " + e.getMessage());
            return null;
        }
    }

}