package solidarityhub.frontend.views.task.manageTasks;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import org.pingu.domain.enums.TaskType;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.dto.VolunteerDTO;
import org.pingu.domain.enums.EmergencyLevel;
import org.pingu.domain.enums.Priority;
import org.pingu.domain.enums.Status;
import solidarityhub.frontend.service.PDFCertificateService;
import solidarityhub.frontend.utils.NotificationManager;
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.task.AssignResourceDialog;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Route("tasks/editTask")
@PageTitle("Editar tarea")
public class EditTaskView extends ManageTaskBaseView implements HasUrlParameter<String> {


    private final PDFCertificateService pdfCertificateService;

    private int taskId;
    private TaskDTO selectedTask;

    private final ComboBox<Status> taskStatusComboBox;
    private Button generateCertificatesButton;

    private List<NeedDTO> allNeeds;
    private List<VolunteerDTO> allVolunteers;

    public EditTaskView() {
        super();
        this.taskStatusComboBox = new ComboBox<>(translator.get("preview_task_status"));
        this.pdfCertificateService = new PDFCertificateService();
        this.allNeeds = new ArrayList<>();
        this.allVolunteers = new ArrayList<>();
    }

    @Override
    protected void buildView() {
        super.buildView();

        loadTaskData();

        if (taskPreview != null) {
            taskPreview.editButton.setVisible(false);
        }
    }

    //===============================Get Task Data=========================================
    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        if (selectedCatastrophe == null) {
            Notification.show(translator.get("select_catastrophe_warning"));
            UI.getCurrent().navigate("");
            return;
        }

        QueryParameters queryParameters = beforeEvent.getLocation().getQueryParameters();
        Map<String, List<String>> parameterMap = queryParameters.getParameters();

        if (!parameterMap.containsKey("id") || parameterMap.get("id").isEmpty()) {
            Notification.show(translator.get("task_not_found"));
            UI.getCurrent().navigate("tasks");
            return;
        }

        try {
            if (allNeedsWithoutTask == null) {
                allNeedsWithoutTask = needService.getNeedsWithoutTask(selectedCatastrophe.getId());
            }

            taskId = Integer.parseInt(parameterMap.get("id").getFirst());
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
            allNeeds = needService.getAllNeeds(selectedCatastrophe.getId());

            selectedTask = taskService.getTaskById(taskId);
            if (selectedTask == null) {
                Notification.show(translator.get("task_not_found"));
                UI.getCurrent().navigate("tasks");
                return;
            }

            setFormValues(selectedTask);
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

        taskPriority.setItems(Priority.LOW, Priority.MODERATE, Priority.URGENT);
        taskEmergency.setItems(EmergencyLevel.LOW, EmergencyLevel.MEDIUM, EmergencyLevel.HIGH, EmergencyLevel.VERYHIGH);
        taskStatusComboBox.setItems(Status.TO_DO, Status.IN_PROGRESS, Status.FINISHED);

        taskName.setValue(task.getName());
        taskDescription.setValue(task.getDescription());
        taskPriority.setValue(task.getPriority());
        taskEmergency.setValue(task.getEmergencyLevel());
        taskStatusComboBox.setValue(task.getStatus());
        if(task.getStatus() == Status.FINISHED) {
            generateCertificatesButton.setVisible(true);
        }

        startDateTimePicker.setMin(null);  // Permitir editar fechas pasadas
        startDateTimePicker.setValue(task.getStartTimeDate());

        if (task.getEstimatedEndTimeDate() != null) {
            endDatePicker.setMin(null);  // Permitir editar fechas pasadas
            endDatePicker.setValue(task.getEstimatedEndTimeDate().toLocalDate());
        }

        taskLocation.setValue(task.getMeetingDirection());

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
        return new HeaderComponent(translator.get("edit_task_title"), "window.history.back()");
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
    protected Dialog getNeedsDialogContent() {
        Dialog needsDialog = new Dialog();
        needsDialog.setHeaderTitle(translator.get("select_needs"));

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);

        needsListBox.setWidthFull();

        needsListBox.setRenderer(
                new ComponentRenderer<>(need -> {
                    HorizontalLayout needContent = new HorizontalLayout();
                    needContent.addClassName("listBox__item");

                    Span needDescription = new Span(need.getDescription());
                    needDescription.addClassName("needContent--description");
                    Span needType = new Span(formatService.formatTaskType(need.getTaskType()));
                    needType.addClassName("need-type");

                    needContent.add(needDescription, needType);
                    needContent.setPadding(false);
                    needContent.setSpacing(false);

                    return needContent;
                })
        );

        Span infoText = new Span(translator.get("select_needs_same_type"));
        infoText.addClassName("needsDialog--infoText");

        dialogContent.add(infoText, needsListBox);

        //Needs type filter
        needsListBox.addSelectionListener(event -> {
            Set<NeedDTO> selectedNeeds = event.getValue();
            if (!selectedNeeds.isEmpty()) {
                TaskType selectedType = selectedNeeds.iterator().next().getTaskType();
                needsListBox.setItemEnabledProvider(needDTO -> selectedType.equals(needDTO.getTaskType()));
            } else {
                needsListBox.setItemEnabledProvider(needDTO -> true);
            }
        });

        // Footer
        Button saveButton = new Button(translator.get("save_button"), e -> {
            Set<String> selectedNeedDescriptions = needsListBox.getSelectedItems().stream()
                    .map(NeedDTO::getDescription)
                    .collect(Collectors.toSet());

            needsMultiSelectComboBox.clear();
            needsMultiSelectComboBox.setItems(selectedNeedDescriptions);
            needsMultiSelectComboBox.select(selectedNeedDescriptions);
            needsDialog.close();
        });

        Button cancelButton = new Button(translator.get("cancel_button"), e -> needsDialog.close());
        needsDialog.getFooter().add(cancelButton, saveButton);

        needsDialog.add(dialogContent);

        return needsDialog;
    }

    @Override
    protected Component getResourceAssignmentsBtn() {
        return new Button(translator.get("assign_resource"),
                e -> {
                    AssignResourceDialog dialog = new AssignResourceDialog(selectedTask, selectedCatastrophe);
                    dialog.open();
                }
        );
    }

    @Override
    protected Component getActionButtons() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();

        Button updateButton = new Button(translator.get("update_button"));
        updateButton.addClickListener(e -> updateTask());

        Button cancelButton = new Button(translator.get("cancel_button"));
        cancelButton.addClickListener(e -> exitWithoutSavingDialog(translator.get("confirm_cancel_edit_text")));

        Button deleteButton = new Button(translator.get("delete_button"));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> deleteTask());

        generateCertificatesButton = new Button(translator.get("generate_certificates_button"));
        generateCertificatesButton.addClickListener(e -> generateCertificates());
        generateCertificatesButton.setVisible(false);

        HorizontalLayout certificatesButtonLayout = new HorizontalLayout();
        certificatesButtonLayout.add(generateCertificatesButton);
        certificatesButtonLayout.setWidthFull();
        certificatesButtonLayout.setJustifyContentMode(JustifyContentMode.START);
        certificatesButtonLayout.setAlignItems(Alignment.CENTER);

        buttons.add(certificatesButtonLayout, cancelButton, deleteButton, updateButton);
        setAlignSelf(Alignment.END, buttons);

        return buttons;
    }

    //===============================Task actions=========================================
    private void updateTask() {
        System.out.println(validateForm());
        if (validateForm()) {
            System.out.println(validateForm() + " - Formulario válido, entro al if");
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
            System.out.println(validateForm() + " - Formulario NO válido, entro al else");
            Notification.show(translator.get("check_fields"),
                    3000, Notification.Position.MIDDLE);
        }
    }

    private void proceedWithUpdateTask() {
        try{
            List<String> selectedNeeds = needsMultiSelectComboBox.getSelectedItems().stream().toList();
            List<NeedDTO> needs = new ArrayList<>();
            for (String need : selectedNeeds) {
                allNeeds.stream()
                        .filter(n -> n.getDescription().equals(need))
                        .findFirst().ifPresent(needs::add);
            }

            if (needs.isEmpty()) {
                Notification.show(translator.get("check_fields"),
                        3000, Notification.Position.MIDDLE);
                return;
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

            TaskDTO updatedTaskDTO = new TaskDTO(
                    taskName.getValue(),
                    taskDescription.getValue(),
                    startDateTimePicker.getValue(),
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

            System.out.println("TaskDTO creado - Estado: " + updatedTaskDTO.getStatus());
            System.out.println("Llamando a taskService.updateTask...");

            taskService.updateTask(taskId, updatedTaskDTO);

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    UI.getCurrent().access(() -> {
                        NotificationManager.forceRefreshNotificationIndicator();
                        System.out.println("🔔 Indicador actualizado después de editar tarea");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            System.out.println("✓ taskService.updateTask completado sin errores");

            System.out.println("Guardando recursos asignados...");
            saveAssignedResources(taskId);
            System.out.println("✓ Recursos asignados guardados");

            clearAssignedResourcesFromSession();

            Notification.show(translator.get("task_updated_success"));

            // Navegar de vuelta a la lista
            VaadinSession.getCurrent().setAttribute("cache", true);
            UI.getCurrent().navigate("tasks");

        } catch (Exception e) {
            System.err.println("✗ Error en proceedWithUpdateTask: " + e.getMessage());
            System.err.println("Tipo de excepción: " + e.getClass().getName());
            e.printStackTrace();

            Notification.show(translator.get("error_updating_task") + e.getMessage(),
                    5000, Notification.Position.MIDDLE);
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
                confirmDialog.close();
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        UI.getCurrent().access(() -> {
                            NotificationManager.forceRefreshNotificationIndicator();
                            System.out.println("🔔 Indicador actualizado después de eliminar tarea");
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
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

    //===============================Validate Form=========================================
    @Override
    protected boolean validateForm() {
        if (taskStatusComboBox.isEmpty()) {
            System.out.println("Campo requerido incompleto: taskStatusComboBox");
            return false;
        }

        return super.validateForm();
    }

    @Override
    protected boolean isFormFilled() {
        return super.isFormFilled() && !taskStatusComboBox.isEmpty();
    }

    //===============================Certificates Methods=========================================
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

    private void generateCertificates() {
        pdfCertificateService.createPDFCertificate(taskId);
        Notification.show(translator.get("certificates_generated"), 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

}