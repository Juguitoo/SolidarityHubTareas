package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.model.enums.Status;
import solidarityhub.frontend.service.TaskService;

import java.time.LocalDateTime;
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

    private TaskDTO originalTask;
    private int taskId;

    protected ComboBox<Status> taskStatusComboBox;

    public EditTaskView() {
        super();
        this.taskService = new TaskService();

        getElement().getChildren()
                .filter(element -> element.getChildren()
                        .anyMatch(child -> child.getTag().equals("h1")))
                .findFirst().flatMap(header -> header.getChildren()
                        .filter(child -> child.getTag().equals("h1"))
                        .findFirst()).ifPresent(title -> title.setText("Editar tarea"));

    }

    @Override
    protected void buildView() {
        super.buildView();

        if (taskPreview != null) {
            taskPreview.enabledEditButton(false);
        }
    }

    @Override
    protected Component getForms() {
        taskStatusComboBox = new ComboBox<>("Estado de la tarea");
        taskStatusComboBox.setItems(Status.TO_DO, Status.IN_PROGRESS, Status.FINISHED);
        taskStatusComboBox.setItemLabelGenerator(status -> switch (status) {
            case TO_DO -> "Por hacer";
            case IN_PROGRESS -> "En progreso";
            case FINISHED -> "Finalizada";
        });
        taskStatusComboBox.setRequiredIndicatorVisible(true);
        taskStatusComboBox.setRequired(true);

        FormLayout addFormLayout = (FormLayout) super.getForms();
        addFormLayout.add(taskStatusComboBox);
        return addFormLayout;
    }

    //===============================Load data=========================================
    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {

        QueryParameters queryParameters = beforeEvent.getLocation().getQueryParameters();
        Map<String, List<String>> parameterMap = queryParameters.getParameters();

        if (parameterMap.containsKey("id") && !parameterMap.get("id").isEmpty()) {
            try {
                taskId = Integer.parseInt(parameterMap.get("id").getFirst());
                loadTaskData();
            } catch (NumberFormatException e) {
                Notification.show("ID de tarea inválido");
                UI.getCurrent().navigate("tasks");
            }
        } else {
            Notification.show("No se especificó ID de tarea");
            UI.getCurrent().navigate("tasks");
        }
    }

    private void loadTaskData() {
        try {
            originalTask = taskService.getTaskById(taskId);
            if (originalTask != null) {
                setFormValues(originalTask);
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

        //startDateTimePicker configuration
        starDateTimePicker.setMin(null);
        starDateTimePicker.setValue(task.getStartTimeDate());

        if (task.getStartTimeDate().isAfter(LocalDateTime.now())) {
            starDateTimePicker.setMin(LocalDateTime.now());
        }

        List<String> needs = new ArrayList<>(allNeedsWithoutTask.stream()
                .map(NeedDTO::getDescription)
                .toList());
        List<String> taskNeeds = task.getNeeds().stream()
                .map(NeedDTO::getDescription)
                .toList();
        needs.addAll(taskNeeds);

        needsMultiSelectComboBox.setItems(needs);
        needsMultiSelectComboBox.select(taskNeeds);

        endDatePicker.setValue(task.getEstimatedEndTimeDate().toLocalDate());
        taskLocation.setValue(task.getMeetingDirection());
        taskStatusComboBox.setValue(task.getStatus());

        Set<String> volunteerNames = task.getVolunteers().stream()
                .map(VolunteerDTO::getFirstName)
                .collect(Collectors.toSet());


        if (volunteerNames.isEmpty()) {
            volunteerMultiSelectComboBox.setItems("Elegir voluntarios automáticamente");
            volunteerMultiSelectComboBox.select("Elegir voluntarios automáticamente");
        } else {
            volunteerMultiSelectComboBox.setItems(volunteerNames);
            volunteerMultiSelectComboBox.select(volunteerNames);
        }

        taskPreview.updateName(task.getName());
        taskPreview.updateDescription(task.getDescription());
        taskPreview.updateDate(formatDate(task.getStartTimeDate()));
        taskPreview.updatePriority(task.getPriority().toString());
        taskPreview.updateEmergencyLevel(getEmergencyLevelString(task.getEmergencyLevel()));
        taskPreview.enabledEditButton(false);
    }

    //===============================Get Components=========================================
    @Override
    protected Component getButtons() {
        HorizontalLayout buttons = new HorizontalLayout();

        Button updateButton = new Button("Actualizar");
        updateButton.addClickListener(e -> updateTask());

        Button deleteButton = new Button("Eliminar");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> deleteTask());

        Button cancelButton = new Button("Salir");
        cancelButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("tasks")));

        buttons.add(cancelButton, deleteButton, updateButton);
        setAlignSelf(Alignment.END, buttons);

        return buttons;
    }

    //===============================Modify task=========================================
    private void updateTask() {
        if (validateForm()) {
            try {
                List<String> selectedNeeds = needsMultiSelectComboBox.getSelectedItems().stream().toList();
                List<NeedDTO> needs = new ArrayList<>();
                for (String need : selectedNeeds) {
                    needService.getAllNeeds(selectedCatastrophe.getId()).stream()
                            .filter(n -> n.getDescription().equals(need))
                            .findFirst().ifPresent(needs::add);
                }

                List<VolunteerDTO> selectedVolunteers = new ArrayList<>();
                List<VolunteerDTO> finalSelectedVolunteers = selectedVolunteers;
                selectedVolunteers = volunteerMultiSelectComboBox.getSelectedItems().stream()
                        .map(name -> {
                            if (name.equals("Elegir voluntarios automáticamente")) {
                                finalSelectedVolunteers.addAll(volunteerService.getVolunteers("", new TaskDTO()).subList(0, 1));
                            }
                            return volunteerService.getVolunteers("", new TaskDTO()).stream()
                                    .filter(v -> v.getFirstName().equals(name))
                                    .findFirst()
                                    .orElse(null);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());



                TaskDTO updatedTaskDTO = new TaskDTO(
                        taskName.getValue(),
                        taskDescription.getValue(),
                        starDateTimePicker.getValue(),
                        endDatePicker.getValue().atTime(23, 59),
                        needs.getFirst().getTaskType(),
                        taskPriority.getValue(),
                        taskEmergency.getValue(),
                        taskStatusComboBox.getValue(),
                        needs,
                        selectedVolunteers,
                        selectedCatastrophe.getId(),
                        taskLocation.getValue()
                );

                taskService.updateTask(taskId, updatedTaskDTO);
                taskService.clearCache();
                Notification.show("Tarea actualizada correctamente");
                VaadinSession.getCurrent().setAttribute("cache", true);
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

    private void deleteTask() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.add(new Span("¿Está seguro de que desea eliminar esta tarea? Esta acción no se puede deshacer."));

        confirmDialog.add(dialogContent);

        Button confirmButton = new Button("Eliminar", event -> {
            try {
                taskService.deleteTask(taskId);
                VaadinSession.getCurrent().setAttribute("cache", true);
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