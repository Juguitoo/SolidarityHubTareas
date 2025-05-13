package solidarityhub.frontend.views.task.suggested;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.dto.VolunteerDTO;
import solidarityhub.frontend.views.task.AddTaskView;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Route("editSuggestedTask")
@PageTitle("Editar tarea")
public class EditSuggestedTask extends AddTaskView implements HasUrlParameter<String> {

    private TaskDTO selectedTask;
    protected final CatastropheDTO selectedCatastrophe;


    public EditSuggestedTask() {
        super();
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
    }

    @Override
    protected void buildView() {
        super.buildView();

        // Aseguramos que taskPreview no sea null antes de usar enabledEditButton
        if (taskPreview != null) {
            taskPreview.enabledEditButton(false);
        }
    }

    //===============================Load data=========================================
    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
        selectedTask = (TaskDTO) VaadinSession.getCurrent().getAttribute("selectedSuggestedTask");
        if (selectedTask != null) {
            loadTaskData();
        } else {
            Notification.show(translator.get("no_suggested_task_selected"));
            UI.getCurrent().navigate("suggested-tasks");
        }
    }

    private void loadTaskData() {
        try {
            setFormValues(selectedTask);
            Notification.show(translator.get("task_loaded_success"));
        } catch (Exception e) {
            Notification.show(translator.get("error_loading_task") + e.getMessage());
            UI.getCurrent().navigate("suggested-tasks");
        }
    }

    private void setFormValues(TaskDTO task) {
        // Asegurarse de que los componentes están inicializados
        if (taskName == null || taskDescription == null || taskPriority == null ||
                taskEmergency == null || starDateTimePicker == null || endDatePicker == null) {
            Notification.show(translator.get("form_not_initialized"));
            UI.getCurrent().navigate("suggested-tasks");
            return;
        }

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

        // Cargar necesidades
        try {
            List<String> needs = new ArrayList<>(needService.getNeedsWithoutTask(task.getCatastropheId()).stream()
                    .map(NeedDTO::getDescription)
                    .toList());
            List<String> taskNeeds = task.getNeeds().stream()
                    .map(NeedDTO::getDescription)
                    .toList();
            needs.addAll(taskNeeds);

            needsMultiSelectComboBox.setItems(needs);
            needsMultiSelectComboBox.select(taskNeeds);
        } catch (Exception e) {
            Notification.show(translator.get("error_loading_needs") + e.getMessage());
        }

        // Configurar fecha estimada de finalización
        if (task.getEstimatedEndTimeDate() != null) {
            endDatePicker.setValue(task.getEstimatedEndTimeDate().toLocalDate());
        }

        // Configurar ubicación
        if (task.getMeetingDirection() != null) {
            taskLocation.setValue(task.getMeetingDirection());
        }

        // Configurar voluntarios
        try {
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
        } catch (Exception e) {
            Notification.show(translator.get("error_loading_volunteers") + e.getMessage());
        }

        taskPreview.updateName(task.getName());
        taskPreview.updateDescription(task.getDescription());
        taskPreview.updateDate(formatDate(task.getStartTimeDate()));
        taskPreview.updatePriority(task.getPriority().toString());
        taskPreview.updateEmergencyLevel(getEmergencyLevelString(task.getEmergencyLevel()));
        taskPreview.updateTaskType(task.getType());
        taskPreview.enabledEditButton(false);
        // Actualizar vista previa si existe
        if (taskPreview != null) {
            taskPreview.updateName(task.getName());
            taskPreview.updateDescription(task.getDescription());
            taskPreview.updateDate(formatDate(task.getStartTimeDate()));
            taskPreview.updatePriority(task.getPriority().toString());
            taskPreview.updateEmergencyLevel(getEmergencyLevelString(task.getEmergencyLevel()));
            taskPreview.enabledEditButton(false);
        }
    }

    //===============================Get Components=========================================
    @Override
    protected Component getButtons() {
        HorizontalLayout buttons = new HorizontalLayout();

        Button updateButton = new Button(translator.get("update_button"));
        updateButton.addClickListener(e -> updateTask());

        Button cancelButton = new Button(translator.get("cancel_button"));
        cancelButton.addClickListener(e -> {
            VaadinSession.getCurrent().setAttribute("cache", false);
            getUI().ifPresent(ui -> ui.navigate("suggested-tasks"));
        });

        buttons.add(cancelButton, updateButton);
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

                TaskDTO suggestedTaskDTO = new TaskDTO(
                        taskName.getValue(),
                        taskDescription.getValue(),
                        starDateTimePicker.getValue(),
                        endDatePicker.getValue().atTime(23, 59),
                        needs.isEmpty() ? null : needs.getFirst().getTaskType(),
                        taskPriority.getValue(),
                        taskEmergency.getValue(),
                        selectedTask.getStatus(),
                        needs,
                        selectedVolunteers,
                        selectedCatastrophe.getId(),
                        taskLocation.getValue()
                );

                taskService.addTask(suggestedTaskDTO);
                taskService.taskCache.clear();
                taskService.suggestedTasksCache.remove(selectedTask);
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
            VaadinSession.getCurrent().setAttribute("cache", false);
        }
    }
}