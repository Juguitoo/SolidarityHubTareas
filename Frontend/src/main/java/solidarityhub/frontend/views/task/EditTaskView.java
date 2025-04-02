package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.model.enums.Status;
import solidarityhub.frontend.service.TaskService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.VolunteerDTO;
import java.util.*;

@Route("editTask")
@PageTitle("Editar tarea")
public class EditTaskView extends AddTaskView implements HasUrlParameter<String> {

    private final TaskService taskService;

    private int taskId;

    public EditTaskView() {
        this.taskService = new TaskService();

        // Cambiar el título de la vista
        getElement().getChildren()
                .filter(element -> element.getChildren()
                        .anyMatch(child -> child.getTag().equals("h1")))
                .findFirst().flatMap(header -> header.getChildren()
                        .filter(child -> child.getTag().equals("h1"))
                        .findFirst()).ifPresent(title -> title.setText("Editar tarea"));

        taskPreview.enabledEditButton(false);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {

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
            TaskDTO task = taskService.getTaskById(taskId);
            if (task != null) {
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
        taskName.setValue(task.getName());
        taskDescription.setValue(task.getDescription());
        taskPriority.setValue(task.getPriority());
        taskEmergency.setValue(task.getEmergencyLevel());
        starDateTimePicker.setValue(task.getStartTimeDate());
        endDateTimePicker.setValue(task.getEstimatedEndTimeDate());

        Set<String> needDescriptions = task.getNeeds().stream()
                .map(NeedDTO::getDescription)
                .collect(Collectors.toSet());
        needsMultiSelectComboBox.setItems(needDescriptions);
        needsMultiSelectComboBox.select(needDescriptions);

        Set<String> volunteerNames = task.getVolunteers().stream()
                .map(VolunteerDTO::getFirstName)
                .collect(Collectors.toSet());

        //Cambiar cuando este implementado el patron
        if (volunteerNames.isEmpty()) {
            volunteerMultiSelectComboBox.setItems("Elegir voluntarios automáticamente");
            volunteerMultiSelectComboBox.select("Elegir voluntarios automáticamente");
        } else {
            volunteerMultiSelectComboBox.setItems(volunteerNames);
            volunteerMultiSelectComboBox.select(volunteerNames);
        }

        // Actualizar la vista previa
        taskPreview.updateName(task.getName());
        taskPreview.updateDescription(task.getDescription());
        taskPreview.updateDate(formatDate(task.getStartTimeDate()));
        taskPreview.updatePriority(task.getPriority().toString());
        taskPreview.updateEmergencyLevel(getEmergencyLevelString(task.getEmergencyLevel()));
    }

    @Override
    protected Component getButtons() {
        HorizontalLayout buttons = new HorizontalLayout();

        Button updateButton = new Button("Actualizar");
        updateButton.addClickListener(e -> updateTask());

        Button deleteButton = new Button("Eliminar");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> confirmDelete());

        Button cancelButton = new Button("Salir");
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("tasks")));

        buttons.add(updateButton, deleteButton, cancelButton);
        setAlignSelf(Alignment.END, buttons);

        return buttons;
    }

    private void updateTask() {
        if (validateForm()) {
            try {
                List<String> selectedNeeds = needsMultiSelectComboBox.getSelectedItems().stream().toList();
                List<NeedDTO> needs = new ArrayList<>();
                for (String need : selectedNeeds) {
                    needService.getNeeds().stream()
                            .filter(n -> n.getDescription().equals(need))
                            .findFirst().ifPresent(needs::add);
                }

                List<VolunteerDTO> selectedVolunteers = volunteerMultiSelectComboBox.getSelectedItems().stream()
                        .map(name -> {
                            if (name.equals("Elegir voluntarios automáticamente")) {
                                //Por hacer(Aplicar patron)
                                return new VolunteerDTO();
                            }
                            return volunteerService.getVolunteers().stream()
                                    .filter(v -> v.getFirstName().equals(name))
                                    .findFirst()
                                    .orElse(null);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                TaskDTO updatedTaskDTO = new TaskDTO(taskName.getValue(), taskDescription.getValue(), starDateTimePicker.getValue(), endDateTimePicker.getValue(),
                        needs.getFirst().getTaskType(), taskPriority.getValue(), taskEmergency.getValue(), Status.TO_DO, needs, selectedVolunteers);

                taskService.updateTask(taskId, updatedTaskDTO);

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
    }

    private void confirmDelete() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.add(new Span("¿Está seguro de que desea eliminar esta tarea? Esta acción no se puede deshacer."));

        confirmDialog.add(dialogContent);

        Button confirmButton = new Button("Eliminar", event -> {
            try {
                taskService.deleteTask(taskId);
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

        confirmDialog.open();
    }

}