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

    @Autowired
    public EditSuggestedTask() {
        super();
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        // Cambiar el título de la vista
        getElement().getChildren()
                .filter(element -> element.getChildren()
                        .anyMatch(child -> child.getTag().equals("h1")))
                .findFirst().flatMap(header -> header.getChildren()
                        .filter(child -> child.getTag().equals("h1"))
                        .findFirst()).ifPresent(title -> title.setText("Editar tarea sugerida"));

        taskPreview.enabledEditButton(false);
    }

    //===============================Load data=========================================
    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
        selectedTask = (TaskDTO) VaadinSession.getCurrent().getAttribute("selectedSuggestedTask");
        if (selectedTask != null) {
            loadTaskData();
        } else {
            Notification.show("No se seleccionó ninguna tarea sugerida a editar");
            UI.getCurrent().navigate("suggested-tasks");
        }
    }

    private void loadTaskData() {
        try {
                setFormValues(selectedTask);
                Notification.show("Tarea cargada correctamente");
        } catch (Exception e) {
            Notification.show("Error al cargar la tarea: " + e.getMessage());
            UI.getCurrent().navigate("suggested-tasks");
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

        List<String> needs = new ArrayList<>(needService.getNeedsWithoutTask(task.getCatastropheId()).stream()
                .map(NeedDTO::getDescription)
                .toList());
        List<String> taskNeeds = task.getNeeds().stream()
                .map(NeedDTO::getDescription)
                .toList();
        needs.addAll(taskNeeds);

        needsMultiSelectComboBox.setItems(needs);
        needsMultiSelectComboBox.select(taskNeeds);

        endDatePicker.setValue(task.getEstimatedEndTimeDate().toLocalDate());

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

        Button updateButton = new Button("Aceptar");
        updateButton.addClickListener(e -> updateTask());

        Button cancelButton = new Button("Salir");
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



                TaskDTO suggestedTaskDTO = new TaskDTO(
                        taskName.getValue(),
                        taskDescription.getValue(),
                        starDateTimePicker.getValue(),
                        endDatePicker.getValue().atTime(23, 59),
                        needs.getFirst().getTaskType(),
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
            VaadinSession.getCurrent().setAttribute("cache", false);
        }
    }
}