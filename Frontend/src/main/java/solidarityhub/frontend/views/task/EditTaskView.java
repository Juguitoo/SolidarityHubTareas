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
                        .findFirst()).ifPresent(title -> title.setText(translator.get("edit_task_title")));
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
        taskStatusComboBox = new ComboBox<>(translator.get("task_status_label"));
        taskStatusComboBox.setItems(Status.TO_DO, Status.IN_PROGRESS, Status.FINISHED);
        taskStatusComboBox.setItemLabelGenerator(this::formatStatus);
        taskStatusComboBox.setRequiredIndicatorVisible(true);
        taskStatusComboBox.setRequired(true);

        FormLayout addFormLayout = (FormLayout) super.getForms();
        addFormLayout.add(taskStatusComboBox);
        return addFormLayout;
    }

    @Override
    protected void setupStatusComboBox() {
        if (taskStatusComboBox != null) {
            taskStatusComboBox.setItemLabelGenerator(this::formatStatus);
        }
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
                Notification.show(translator.get("invalid_task_id"));
                UI.getCurrent().navigate("tasks");
            }
        } else {
            Notification.show(translator.get("no_task_id_specified"));
            UI.getCurrent().navigate("tasks");
        }
    }

    private void loadTaskData() {
        try {
            originalTask = taskService.getTaskById(taskId);
            if (originalTask != null) {
                setFormValues(originalTask);
                Notification.show(translator.get("task_loaded_success"));
            } else {
                Notification.show(translator.get("task_not_found"));
                UI.getCurrent().navigate("tasks");
            }
        } catch (Exception e) {
            Notification.show(translator.get("error_loading_task") + e.getMessage());
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
            volunteerMultiSelectComboBox.setItems(translator.get("auto_select_volunteers"));
            volunteerMultiSelectComboBox.select(translator.get("auto_select_volunteers"));
        } else {
            volunteerMultiSelectComboBox.setItems(volunteerNames);
            volunteerMultiSelectComboBox.select(volunteerNames);
        }

        taskPreview.updateName(task.getName());
        taskPreview.updateDescription(task.getDescription());
        taskPreview.updateDate(formatDate(task.getStartTimeDate()));

        // Actualizar prioridad con formato traducido
        String priorityText = switch (task.getPriority()) {
            case LOW -> translator.get("low_priority");
            case MODERATE -> translator.get("moderate_priority");
            case URGENT -> translator.get("urgent_priority");
        };
        taskPreview.updatePriority(priorityText);

        taskPreview.updateEmergencyLevel(getEmergencyLevelString(task.getEmergencyLevel()));
        taskPreview.enabledEditButton(false);
    }

    //===============================Get Components=========================================
    @Override
    protected Component getButtons() {
        HorizontalLayout buttons = new HorizontalLayout();

        Button updateButton = new Button(translator.get("update_button"));
        updateButton.addClickListener(e -> updateTask());

        Button deleteButton = new Button(translator.get("delete_button"));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> deleteTask());

        Button cancelButton = new Button(translator.get("cancel_button"));
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
                            if (name.equals(translator.get("auto_select_volunteers"))) {
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
                Notification.show(translator.get("task_updated_success"));
                VaadinSession.getCurrent().setAttribute("cache", true);
                UI.getCurrent().navigate("tasks");
            } catch (Exception e) {
                Notification.show(translator.get("error_updating_task") + e.getMessage(),
                        5000, Notification.Position.MIDDLE);
            }
        } else {
            Notification.show(translator.get("check_fields"),
                    3000, Notification.Position.MIDDLE);
        }
    }

    private void deleteTask() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle(translator.get("confirm_deletion_title"));

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.add(new Span(translator.get("confirm_task_deletion_text")));

        confirmDialog.add(dialogContent);

        Button confirmButton = new Button(translator.get("delete_button"), event -> {
            try {
                taskService.deleteTask(taskId);
                VaadinSession.getCurrent().setAttribute("cache", true);
                confirmDialog.close();
                Notification.show(translator.get("task_deleted_success"));
                UI.getCurrent().navigate("tasks");
            } catch (Exception e) {
                Notification.show(translator.get("error_deleting_task") + e.getMessage());
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button(translator.get("cancel_button"), event -> confirmDialog.close());

        confirmDialog.getFooter().add(cancelButton, confirmButton);

        confirmDialog.open();
    }
}