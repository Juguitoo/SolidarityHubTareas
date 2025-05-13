package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.dto.VolunteerDTO;
import org.pingu.domain.enums.EmergencyLevel;
import org.pingu.domain.enums.Priority;
import org.pingu.domain.enums.Status;
import solidarityhub.frontend.views.HeaderComponent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Route("editTask")
@PageTitle("Editar tarea")
public class EditTaskView extends AddTaskView implements HasUrlParameter<String> {

    private int taskId;
    private final ComboBox<Status> taskStatusComboBox;
    private CatastropheDTO selectedCatastrophe;

    public EditTaskView() {
        super();
        this.taskStatusComboBox = new ComboBox<>(translator.get("preview_task_status"));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
        if (!catastropheService.isCatastropheSelected(event, selectedCatastrophe)) {
            return;
        }

        buildView();
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
        // Obtener el formulario base de la clase padre
        var formLayout = (com.vaadin.flow.component.formlayout.FormLayout) super.getForms();

        // Configurar el ComboBox de estado
        taskStatusComboBox.setItems(Status.TO_DO, Status.IN_PROGRESS, Status.FINISHED);
        taskStatusComboBox.setItemLabelGenerator(this::formatStatus);
        taskStatusComboBox.setRequiredIndicatorVisible(true);
        taskStatusComboBox.setRequired(true);

        formLayout.add(taskStatusComboBox);
        return formLayout;
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
        // 1. Verificar si selectedCatastrophe está disponible
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        // 2. Redirigir a selección de catástrofes si no hay una seleccionada
        if (selectedCatastrophe == null) {
            Notification.show(translator.get("select_catastrophe_warning"));
            UI.getCurrent().navigate(""); // Navegar a la vista de selección de catástrofes
            return;
        }

        // 3. Procesar el parámetro de ID de tarea
        QueryParameters queryParameters = beforeEvent.getLocation().getQueryParameters();
        Map<String, List<String>> parameterMap = queryParameters.getParameters();

        if (!parameterMap.containsKey("id") || parameterMap.get("id").isEmpty()) {
            Notification.show(translator.get("task_not_found"));
            UI.getCurrent().navigate("tasks");
            return;
        }

        try {
            // 4. Inicializar allNeedsWithoutTask si es necesario
            if (allNeedsWithoutTask == null) {
                allNeedsWithoutTask = needService.getNeedsWithoutTask(selectedCatastrophe.getId());
            }

            // 5. Cargar la tarea
            taskId = Integer.parseInt(parameterMap.get("id").getFirst());
            loadTaskData();
        } catch (NumberFormatException e) {
            Notification.show(translator.get("task_not_found"));
            UI.getCurrent().navigate("tasks");
        } catch (Exception e) {
            Notification.show(translator.get("error_loading_task") + e.getMessage());
            UI.getCurrent().navigate("tasks");
        }
    }

    private void loadTaskData() {
        try {
            TaskDTO originalTask = taskService.getTaskById(taskId);
            if (originalTask == null) {
                Notification.show(translator.get("task_not_found"));
                UI.getCurrent().navigate("tasks");
                return;
            }
            // Aseguramos que los componentes estén inicializados antes de establecer valores
            setFormValues(originalTask);
            Notification.show(translator.get("task_loaded_success"));
        } catch (Exception e) {
            Notification.show(translator.get("error_loading_task") + e.getMessage());
        }
    }

    private void setFormValues(TaskDTO task) {
        if (task == null) {
            Notification.show(translator.get("task_not_found"));
            UI.getCurrent().navigate("tasks");
            return;
        }

        // 1. Primero configuramos todos los ComboBox con sus items
        taskPriority.setItems(Priority.LOW, Priority.MODERATE, Priority.URGENT);
        taskEmergency.setItems(EmergencyLevel.LOW, EmergencyLevel.MEDIUM, EmergencyLevel.HIGH, EmergencyLevel.VERYHIGH);
        taskStatusComboBox.setItems(Status.TO_DO, Status.IN_PROGRESS, Status.FINISHED);

        // 2. Luego asignamos los valores
        taskName.setValue(task.getName());
        taskDescription.setValue(task.getDescription());
        taskPriority.setValue(task.getPriority());
        taskEmergency.setValue(task.getEmergencyLevel());
        taskStatusComboBox.setValue(task.getStatus());

        // Configurar fechas
        starDateTimePicker.setMin(null);  // Permitir editar fechas pasadas
        starDateTimePicker.setValue(task.getStartTimeDate());

        if (task.getEstimatedEndTimeDate() != null) {
            endDatePicker.setMin(null);  // Permitir editar fechas pasadas
            endDatePicker.setValue(task.getEstimatedEndTimeDate().toLocalDate());
        }

        // Configurar ubicación de la tarea
        taskLocation.setValue(task.getMeetingDirection());

        // Configurar necesidades y voluntarios
        configureNeeds(task);
        configureVolunteers(task);

        // Actualizar la vista previa
        updateTaskPreview(task);
    }

    @Override
    protected boolean isFormFilled() {
        return taskName != null && !taskName.isEmpty() ||
                taskDescription != null && !taskDescription.isEmpty() ||
                taskPriority != null && taskPriority.getValue() != null ||
                taskEmergency != null && taskEmergency.getValue() != null ||
                needsMultiSelectComboBox != null && !needsMultiSelectComboBox.getSelectedItems().isEmpty() ||
                starDateTimePicker != null && starDateTimePicker.getValue() != null ||
                volunteerMultiSelectComboBox != null && !volunteerMultiSelectComboBox.getSelectedItems().isEmpty() ||
                taskLocation != null && !taskLocation.isEmpty();
    }

    private void updateTaskPreview(TaskDTO task) {
        if (taskPreview != null) {
            taskPreview.updateName(task.getName());
            taskPreview.updateDescription(task.getDescription());
            taskPreview.updateDate(formatDate(task.getStartTimeDate()));
            taskPreview.updatePriority(task.getPriority().toString());
            taskPreview.updateEmergencyLevel(getEmergencyLevelString(task.getEmergencyLevel()));
            taskPreview.updateTaskType(task.getType());
            taskPreview.enabledEditButton(false);
        }
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
        } catch (Exception e) {
            Notification.show("Error al cargar la tarea" + e.getMessage());
        }
    }

    private void configureVolunteers(TaskDTO task) {
        try {
            if (task.getVolunteers() == null || task.getVolunteers().isEmpty()) {
                // Si no hay voluntarios, configurar la opción de autoselección
                volunteerMultiSelectComboBox.setItems(translator.get("auto_select_volunteers"));
                volunteerMultiSelectComboBox.select(translator.get("auto_select_volunteers"));
                return;
            }

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
        return new HeaderComponent(translator.get("edit_task_title"), "tasks");
    }

    @Override
    protected Component getButtons() {
        HorizontalLayout buttons = new HorizontalLayout();

        Button updateButton = new Button(translator.get("update_button"));
        updateButton.addClickListener(e -> updateTask());

        Button cancelButton = new Button(translator.get("cancel_button"));
        cancelButton.addClickListener(e -> exitWithoutSavingDialog());

        Button deleteButton = new Button(translator.get("delete_button"));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> deleteTask());

        buttons.add(cancelButton, deleteButton, updateButton);
        setAlignSelf(Alignment.END, buttons);

        return buttons;
    }

    //===============================Task actions=========================================
    private void updateTask() {
        if (validateForm()) {
                Status estado = taskStatusComboBox.getValue();
                if(estado == Status.FINISHED) {
                    getCreateCertificatesDialog().thenAccept(result->{
                        if(result) {
                            proceedWithUpdateTask();
                        }
                    });
                }else{
                    proceedWithUpdateTask();
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
                taskService.clearCache();
                confirmDialog.close();
                Notification.show(translator.get("task_deleted_success"));

                VaadinSession.getCurrent().setAttribute("cache", true);
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

    @Override
    protected boolean validateForm() {
        return super.validateForm() && !taskStatusComboBox.isEmpty();
    }

    private CompletableFuture<Boolean> getCreateCertificatesDialog() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle(translator.get("create_certificates_title"));

        confirmDialog.add(new Span(translator.get("create_certificates_text")));

        Button confirmButton = new Button(translator.get("confirm_button"), e -> {
            confirmDialog.close();
            future.complete(true);
        });

        Button closeButton = new Button(translator.get("cancel_button"), e -> {
            confirmDialog.close();
            future.complete(false);
        });

        confirmDialog.getFooter().add(closeButton, confirmButton);
        confirmDialog.open();
        return future;
    }

    private void proceedWithUpdateTask() {
        try{
            // Obtener necesidades seleccionadas
            List<String> selectedNeeds = needsMultiSelectComboBox.getSelectedItems().stream().toList();
            List<NeedDTO> needs = new ArrayList<>();
            for (String need : selectedNeeds) {
                needService.getAllNeeds(selectedCatastrophe.getId()).stream()
                        .filter(n -> n.getDescription().equals(need))
                        .findFirst().ifPresent(needs::add);
            }

            if (needs.isEmpty()) {
                Notification.show(translator.get("check_fields"),
                        3000, Notification.Position.MIDDLE);
                return;
            }

            // Obtener voluntarios seleccionados
            List<VolunteerDTO> selectedVolunteers = new ArrayList<>();
            volunteerMultiSelectComboBox.getSelectedItems()
                    .forEach(name -> {
                        if (name.equals(translator.get("auto_select_volunteers"))) {
                            // Autoselección de voluntarios
                            selectedVolunteers.addAll(allVolunteersList.subList(0, Math.min(1, allVolunteersList.size())));
                        } else {
                            // Búsqueda manual de voluntarios por nombre
                            allVolunteersList.stream()
                                    .filter(v -> v.getFirstName().equals(name))
                                    .findFirst()
                                    .ifPresent(selectedVolunteers::add);
                        }
                    });

            // Crear DTO con los datos actualizados
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

            // Actualizar en backend
            taskService.updateTask(taskId, updatedTaskDTO);
            taskService.clearCache();
            Notification.show(translator.get("task_updated_success"));

            // Navegar de vuelta a la lista
            VaadinSession.getCurrent().setAttribute("cache", true);
            UI.getCurrent().navigate("tasks");
        } catch (Exception e) {
            Notification.show(translator.get("error_updating_task") + e.getMessage(),
                    5000, Notification.Position.MIDDLE);
        }
    }
}