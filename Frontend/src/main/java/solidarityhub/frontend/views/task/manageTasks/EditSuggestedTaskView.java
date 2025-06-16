package solidarityhub.frontend.views.task.manageTasks;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import org.pingu.domain.enums.EmergencyLevel;
import org.pingu.domain.enums.Priority;
import org.pingu.domain.enums.Status;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.dto.VolunteerDTO;
import solidarityhub.frontend.views.HeaderComponent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Route("tasks/editSuggestedTask")
@PageTitle("Confirmar tarea sugerida")
public class EditSuggestedTaskView extends ManageTaskBaseView implements HasUrlParameter<String> {

    private TaskDTO selectedTask;
    protected CatastropheDTO selectedCatastrophe;
    private final ComboBox<Status> taskStatusComboBox;
    MultiSelectListBox<NeedDTO> needsListBox = new MultiSelectListBox<>();
    private List<VolunteerDTO> allVolunteers;


    public EditSuggestedTaskView() {
        super();
        this.taskStatusComboBox = new ComboBox<>(translator.get("preview_task_status"));
        this.allVolunteers = new ArrayList<>();
    }

    @Override
    protected void buildView() {
        super.buildView();

        loadTaskData();

        if (taskPreview != null) {
            taskPreview.enabledEditButton(false);
        }
    }

    //===============================Load data=========================================
    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
        selectedTask = (TaskDTO) VaadinSession.getCurrent().getAttribute("selectedSuggestedTask");
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        if (allNeedsWithoutTask == null) {
            allNeedsWithoutTask = needService.getNeedsWithoutTask(selectedCatastrophe.getId());
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
                taskEmergency == null || startDateTimePicker == null || endDatePicker == null) {
            Notification.show(translator.get("form_not_initialized"));
            UI.getCurrent().navigate("suggested-tasks");
            return;
        }

        taskPriority.setItems(Priority.LOW, Priority.MODERATE, Priority.URGENT);
        taskEmergency.setItems(EmergencyLevel.LOW, EmergencyLevel.MEDIUM, EmergencyLevel.HIGH, EmergencyLevel.VERYHIGH);
        taskStatusComboBox.setItems(Status.TO_DO, Status.IN_PROGRESS, Status.FINISHED);

        taskName.setValue(task.getName());
        taskDescription.setValue(task.getDescription());
        taskPriority.setValue(task.getPriority());
        taskEmergency.setValue(task.getEmergencyLevel());
        taskStatusComboBox.setValue(task.getStatus());

        //startDateTimePicker configuration
        startDateTimePicker.setMin(null);
        startDateTimePicker.setValue(task.getStartTimeDate());

        if (task.getStartTimeDate().isAfter(LocalDateTime.now())) {
            startDateTimePicker.setMin(LocalDateTime.now());
        }

        // Configurar fecha estimada de finalización
        if (task.getEstimatedEndTimeDate() != null) {
            endDatePicker.setValue(task.getEstimatedEndTimeDate().toLocalDate());
        }

        // Configurar ubicación
        if (task.getMeetingDirection() != null) {
            taskLocation.setValue(task.getMeetingDirection());
        }

        configureNeeds(task);
        configureVolunteers(task);
    }

    private void configureNeeds(TaskDTO task) {
        try {
            List<String> allNeedDescriptions = new ArrayList<>(allNeedsWithoutTask.stream()
                    .map(NeedDTO::getDescription)
                    .toList());

            List<String> taskNeedDescriptions = task.getNeeds().stream()
                    .map(NeedDTO::getDescription)
                    .toList();

            Set<String> uniqueNeeds = new HashSet<>(allNeedDescriptions);
            uniqueNeeds.addAll(taskNeedDescriptions);

            needsMultiSelectComboBox.setItems(uniqueNeeds);
            needsMultiSelectComboBox.select(taskNeedDescriptions);

            List<NeedDTO> combinedNeeds = new ArrayList<>(allNeedsWithoutTask);
            combinedNeeds.addAll(task.getNeeds());

            List<NeedDTO> uniqueCombinedNeeds = new ArrayList<>(combinedNeeds.stream()
                    .collect(Collectors.toMap(
                            NeedDTO::getDescription,  // Clave para identificar duplicados
                            need -> need,             // Valor
                            (existing, replacement) -> existing)) // En caso de duplicado, mantener el existente
                    .values());

            needsListBox.setItems(uniqueCombinedNeeds);

            needsListBox.select(task.getNeeds());
        } catch (Exception e) {
            Notification.show("Error al cargar la tarea" + e.getMessage());
        }
    }

    private void configureVolunteers(TaskDTO task) {
        try {
            List<String> volunteerNames = task.getVolunteers().stream()
                    .map(VolunteerDTO::getFirstName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!volunteerNames.isEmpty()) {
                volunteerMultiSelectComboBox.setItems(volunteerNames);
                volunteerMultiSelectComboBox.select(volunteerNames);
            }
        } catch (Exception e) {
            Notification.show(translator.get("error_configuring_volunteers") + e.getMessage());
        }
    }

    //===============================Get Components=========================================
    @Override
    protected Component getHeader() {
        return new HeaderComponent(translator.get("confirm_suggested_tasks_title"), "window.history.back()");
    }

    @Override
    protected Component getForms() {
        var formLayout = (com.vaadin.flow.component.formlayout.FormLayout) super.getForms();

        taskStatusComboBox.setItems(Status.TO_DO, Status.IN_PROGRESS, Status.FINISHED);
        taskStatusComboBox.setItemLabelGenerator(formatService::formatTaskStatus);
        taskStatusComboBox.setRequiredIndicatorVisible(true);
        taskStatusComboBox.setRequired(true);

        formLayout.add(taskStatusComboBox);
        return formLayout;
    }

    @Override
    protected Component getActionButtons() {
        HorizontalLayout buttons = new HorizontalLayout();

        Button updateButton = new Button(translator.get("confirm"));
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
                allVolunteers = volunteerService.getVolunteers("None", getTaskDTO());
                Set<String> names = volunteerMultiSelectComboBox.getSelectedItems();
                names.forEach(name -> {
                    if (name.equals(translator.get("auto_select_volunteers"))) {
                        selectedVolunteers.addAll(allVolunteers.stream().limit(nAutoSelectVolunteers).toList());
                    } else {
                        allVolunteers.stream()
                                .filter(v -> v.getFirstName().equals(name)).forEach(selectedVolunteers::add);
                    }
                });

                TaskDTO suggestedTaskDTO = new TaskDTO(
                        taskName.getValue(),
                        taskDescription.getValue(),
                        startDateTimePicker.getValue(),
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