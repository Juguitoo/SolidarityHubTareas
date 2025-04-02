package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.TaskService;

import java.util.*;

@Route("editTask")
@PageTitle("Editar tarea")
public class EditTaskView extends AddTaskView implements HasUrlParameter<String> {

    private int taskId;
    // Referencia al servicio de tareas
    private final TaskService taskServiceRef;

    // Constructor actualizado para aceptar los dos parámetros necesarios
    public EditTaskView(TaskService taskService, CatastropheService catastropheService) {
        // Llamar al constructor de la clase padre con ambos servicios
        super(taskService, catastropheService);
        this.taskServiceRef = taskService;

        // Cambiar el título de la vista
        getElement().getChildren()
                .filter(element -> element.getChildren()
                        .anyMatch(child -> child.getTag().equals("h1")))
                .findFirst().flatMap(header -> header.getChildren()
                        .filter(child -> child.getTag().equals("h1"))
                        .findFirst()).ifPresent(title -> title.setText("Editar tarea"));
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
        // También verifica si hay una catástrofe seleccionada como en la clase padre
        CatastropheDTO selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
        if (selectedCatastrophe == null) {
            Notification.show("Por favor, selecciona una catástrofe primero",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            beforeEvent.forwardTo(""); // Redirige a la página de selección de catástrofes
            return;
        }

        // Obtener el parámetro ID de la URL
        QueryParameters queryParameters = beforeEvent.getLocation().getQueryParameters();
        Map<String, List<String>> parameterMap = queryParameters.getParameters();

        if (parameterMap.containsKey("id") && !parameterMap.get("id").isEmpty()) {
            try {
                taskId = Integer.parseInt(parameterMap.get("id").getFirst());
                loadTaskData(taskId);
            } catch (NumberFormatException e) {
                Notification.show("ID de tarea inválido");
                UI.getCurrent().navigate("tasks");
            }
        } else {
            Notification.show("No se especificó ID de tarea");
            UI.getCurrent().navigate("tasks");
        }
    }

    private void loadTaskData(int taskId) {
        try {
            TaskDTO task = taskServiceRef.getTaskById(taskId);
            if (task != null) {
                // Usar métodos protegidos/públicos para acceder a los componentes
                setFormValues(task);
                Notification.show("Tarea cargada correctamente");
            } else {
                Notification.show("No se pudo encontrar la tarea");
                UI.getCurrent().navigate("tasks");
            }
        } catch (Exception e) {
            Notification.show("Error al cargar la tarea: " + e.getMessage());
            UI.getCurrent().navigate("tasks");
        }
    }

    private void setFormValues(TaskDTO task) {
        // Implementa este método para establecer los valores del formulario
        // Esto dependerá de cómo hayas implementado tus campos en AddTaskView
        // Por ejemplo:
        /*
        taskName.setValue(task.getName());
        taskDescription.setValue(task.getDescription());
        starDateTimePicker.setValue(task.getStartTimeDate());
        endDateTimePicker.setValue(task.getEstimatedEndTimeDate());
        taskPriority.setValue(task.getPriority());
        taskEmergency.setValue(task.getEmergencyLevel());

        // Para las necesidades y voluntarios, necesitarás establecer las selecciones en tus componentes
        */
    }


    protected Component getButtons() {
        HorizontalLayout buttons = new HorizontalLayout();

        Button updateButton = new Button("Actualizar");
        updateButton.addClickListener(e -> updateTask());

        Button deleteButton = new Button("Eliminar");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmDelete());

        Button cancelButton = new Button("Cancelar");
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("tasks")));

        buttons.add(updateButton, deleteButton, cancelButton);
        setAlignSelf(Alignment.END, buttons);

        return buttons;
    }

    private void updateTask() {
        // Implementa la lógica para actualizar la tarea
        // Deberás obtener los valores del formulario y llamar al servicio
        /*
        if (validateForm()) {
            try {
                TaskDTO taskToUpdate = new TaskDTO(
                    taskName.getValue(),
                    taskDescription.getValue(),
                    starDateTimePicker.getValue(),
                    endDateTimePicker.getValue(),
                    // obtener type
                    taskPriority.getValue(),
                    taskEmergency.getValue(),
                    // obtener status
                    getSelectedNeeds(),
                    getSelectedVolunteers()
                );

                // Establecer el ID de la catástrofe si es necesario
                CatastropheDTO selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
                if (selectedCatastrophe != null) {
                    taskToUpdate.setCatastropheId(selectedCatastrophe.getId());
                }

                taskServiceRef.updateTask(taskId, taskToUpdate);
                Notification.show("Tarea actualizada correctamente");
                UI.getCurrent().navigate("tasks");
            } catch (Exception e) {
                Notification.show("Error al actualizar la tarea: " + e.getMessage(),
                        5000, Notification.Position.MIDDLE);
            }
        } else {
            Notification.show("Por favor, complete todos los campos obligatorios",
                    3000, Notification.Position.MIDDLE);
        }
        */
    }

    private void confirmDelete() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.add(new Span("¿Está seguro de que desea eliminar esta tarea? Esta acción no se puede deshacer."));

        Button confirmButton = new Button("Eliminar", event -> {
            try {
                taskServiceRef.deleteTask(taskId);
                confirmDialog.close();
                Notification.show("Tarea eliminada correctamente");
                UI.getCurrent().navigate("tasks");
            } catch (Exception e) {
                Notification.show("Error al eliminar la tarea: " + e.getMessage());
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Cancelar", event -> confirmDialog.close());

        confirmDialog.getFooter().add(cancelButton, confirmButton);
        confirmDialog.add(dialogContent);
        confirmDialog.open();
    }
}