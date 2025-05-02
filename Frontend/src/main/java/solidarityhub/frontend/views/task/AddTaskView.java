package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.service.CoordinatesService;
import solidarityhub.frontend.service.NeedService;
import solidarityhub.frontend.service.TaskService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import solidarityhub.frontend.dto.VolunteerDTO;
import solidarityhub.frontend.model.enums.EmergencyLevel;
import solidarityhub.frontend.model.enums.Priority;
import solidarityhub.frontend.model.enums.Status;
import solidarityhub.frontend.model.enums.TaskType;
import solidarityhub.frontend.service.VolunteerService;
import solidarityhub.frontend.views.headerComponent;
import solidarityhub.frontend.views.volunteer.VolunteerInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Añadir tarea")
@Route("addtask")
public class AddTaskView extends VerticalLayout {

    protected final TaskService taskService;
    protected final VolunteerService volunteerService;
    protected final NeedService needService;
    protected final CatastropheDTO selectedCatastrophe;
    protected final CoordinatesService coordinatesService;

    protected TaskComponent taskPreview;

    protected final TextField taskName = new TextField("Nombre de la tarea");
    protected final TextArea taskDescription = new TextArea("Descripción de la tarea");
    protected final ComboBox<Priority> taskPriority = new ComboBox<>("Prioridad");
    protected final ComboBox<EmergencyLevel> taskEmergency = new ComboBox<>("Nivel de peligrosidad");
    protected final DateTimePicker starDateTimePicker = new DateTimePicker("Fecha y hora de comienzo");
    protected final DatePicker endDatePicker = new DatePicker("Fecha estimada de finalización");
    protected final TextField taskLocation = new TextField("Punto de reunión ");
    protected final MultiSelectComboBox<String> volunteerMultiSelectComboBox = new MultiSelectComboBox<>("Voluntarios");
    protected final MultiSelectComboBox<String> needsMultiSelectComboBox = new MultiSelectComboBox<>("Necesidades");

    @Autowired
    public AddTaskView(TaskService taskService) {
        this.taskService = taskService;
        this.volunteerService = new VolunteerService();
        this.needService = new NeedService();
        this.coordinatesService = new CoordinatesService();
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        headerComponent header = new headerComponent("Añadir tarea", "tasks");

        add(
            header,
            getPreview(),
            getForms(),
            getButtons()
        );

        setAlignItems(Alignment.CENTER);
    }


    private void saveNewTask(){
        if (validateForm()) {
            try {
                TaskDTO newTaskDTO = getTaskDTO();
                taskService.addTask(newTaskDTO);

                getConfirmationDialog().open();

            } catch (Exception ex) {
                Notification.show("Error al guardar la tarea: " + ex.getMessage(),
                        5000, Notification.Position.MIDDLE);
            }
        } else {
            Notification.show("Por favor, complete todos los campos obligatorios",
                    3000, Notification.Position.MIDDLE);
        }
    }

    private TaskDTO getTaskDTO() {
        List<String> selectedNeeds = needsMultiSelectComboBox.getSelectedItems().stream().toList();
        List<NeedDTO> needs = new ArrayList<>();
        for (String need : selectedNeeds) {
            needService.getNeedsWithoutTask(selectedCatastrophe.getId()).stream()
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

        TaskType taskType = needs.isEmpty() ? null : needs.getFirst().getTaskType();

        // Verificar que endDatePicker.getValue() no sea nulo antes de llamar a atTime()
        LocalDateTime endDateTime = null;
        if (endDatePicker.getValue() != null) {
            endDateTime = endDatePicker.getValue().atTime(23, 59);
        }

        return new TaskDTO(taskName.getValue(), taskDescription.getValue(), starDateTimePicker.getValue(), endDateTime,
                taskType, taskPriority.getValue(), taskEmergency.getValue(), Status.TO_DO, needs, selectedVolunteers, selectedCatastrophe.getId(), taskLocation.getValue());
    }

    //===============================Get Components=========================================
    private Component getPreview(){
        VerticalLayout preview = new VerticalLayout();
        preview.addClassName("previewContainer");
        preview.setPadding(false);
        preview.setWidth("40%");

        taskPreview = new TaskComponent(
                0,
                "Nombre de la tarea",
                "Descripción de la tarea...",
                formatDate(LocalDateTime.now()),
                "Prioridad",
                "Nivel de peligrosidad"
        );
        taskPreview.enabledEditButton(false);
        setAlignSelf(Alignment.CENTER, taskPreview);

        setupFormListeners();

        preview.add(taskPreview);
        return preview;
    }

    private Component getForms(){
        FormLayout addTaskForm = new FormLayout();
        addTaskForm.addClassName("addTaskForm");

        //Forms
        taskName.setRequiredIndicatorVisible(true);
        taskName.setRequired(true);

        taskDescription.setRequiredIndicatorVisible(true);
        taskDescription.setRequired(true);

        taskPriority.setItems(Priority.LOW, Priority.MODERATE, Priority.URGENT);
        taskPriority.setItemLabelGenerator(priority -> switch (priority) {
            case LOW -> "Baja";
            case MODERATE -> "Moderada";
            case URGENT -> "Urgente";
        });
        taskPriority.setRequiredIndicatorVisible(true);
        taskPriority.setRequired(true);

        taskEmergency.setItems(EmergencyLevel.LOW, EmergencyLevel.MEDIUM, EmergencyLevel.HIGH, EmergencyLevel.VERYHIGH);
        taskEmergency.setItemLabelGenerator(this::getEmergencyLevelString);
        taskEmergency.setRequiredIndicatorVisible(true);
        taskEmergency.setRequired(true);

        starDateTimePicker.setRequiredIndicatorVisible(true);
        starDateTimePicker.setMin(LocalDateTime.now());
        starDateTimePicker.setDatePickerI18n(new DatePicker.DatePickerI18n().setFirstDayOfWeek(1));
        starDateTimePicker.setHelperText("Sirve, además, como fecha de encuentro con los voluntarios ");

        endDatePicker.setRequiredIndicatorVisible(true);
        endDatePicker.setMin(LocalDate.now());
        endDatePicker.setI18n(new DatePicker.DatePickerI18n().setFirstDayOfWeek(1));
        endDatePicker.setHelperText("Esta fecha debe ser posterior a la fecha de comienzo");

        taskLocation.setRequiredIndicatorVisible(true);
        taskLocation.setRequired(true);
        taskLocation.setHelperText("Indique la dirección de encuentro de los voluntarios");

        addTaskForm.add(taskName, taskDescription, starDateTimePicker, taskPriority, getNeedsForm(), endDatePicker, taskEmergency, getVolunteersForm(), taskLocation);

        //Update endDateTimePicker min value when startDateTimePicker changes
        starDateTimePicker.addValueChangeListener(event -> {
            LocalDate startValue;
            try {
                startValue = event.getValue().toLocalDate();
                if (startValue != null) {
                    endDatePicker.setMin(startValue);

                    if (endDatePicker.getValue() != null &&
                            endDatePicker.getValue().isBefore(startValue)) {
                        endDatePicker.setValue(startValue);
                    } else if (endDatePicker.getValue().isBefore(startValue)) {
                        endDatePicker.setValue(null);
                    }
                }
            } catch (Exception e) {
                endDatePicker.setMin(LocalDate.now());
            }
        });

        taskLocation.addBlurListener(event -> {
            String address = taskLocation.getValue();
            if(address != null && !address.isEmpty()){
                Map<String, Double> coordinatesMap = coordinatesService.getCoordinates(address);
                if(coordinatesMap != null){
                    if(coordinatesMap.get("lat") != null && coordinatesMap.get("lon") != null){
                        taskLocation.setInvalid(false);
                    }else {
                        taskLocation.setInvalid(true);
                        taskLocation.setValue("");
                        Notification.show("La dirección ingresada no es válida. Por favor, intentelo de nuevo.", 3000, Notification.Position.MIDDLE);
                    }
                }else{
                    taskLocation.setInvalid(true);
                    taskLocation.setValue("");
                    Notification.show("La dirección ingresada no es válida. Por favor, intentelo de nuevo.", 3000, Notification.Position.MIDDLE);
                }
            }
        });


        //Responsive
        addTaskForm.setColspan(taskDescription, 2);
        addTaskForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("750px", 3)
        );

        return addTaskForm;
    }

    protected Component getVolunteersForm(){
        volunteerMultiSelectComboBox.setPlaceholder("Hacer clic para seleccionar");
        volunteerMultiSelectComboBox.setReadOnly(true);
        volunteerMultiSelectComboBox.setRequiredIndicatorVisible(true);
        volunteerMultiSelectComboBox.setRequired(true);
        volunteerMultiSelectComboBox.addClassName("addTaskForm--multiSelectComboBox");

        Dialog selectVolunteersDialog = getVolunteersDialogContent();
        volunteerMultiSelectComboBox.getElement().addEventListener("click", e -> selectVolunteersDialog.open());

        return volunteerMultiSelectComboBox;
    }

    protected Component getNeedsForm() {
        needsMultiSelectComboBox.setPlaceholder("Hacer clic para seleccionar");
        needsMultiSelectComboBox.setReadOnly(true);
        needsMultiSelectComboBox.setRequiredIndicatorVisible(true);
        needsMultiSelectComboBox.setRequired(true);
        needsMultiSelectComboBox.addClassName("addTaskForm--multiSelectComboBox");

        Dialog selectNeedsDialog = getNeedsDialogContent();
        needsMultiSelectComboBox.getElement().addEventListener("click", e -> selectNeedsDialog.open());

        return needsMultiSelectComboBox;
    }

    protected Component getButtons(){
        HorizontalLayout buttons = new HorizontalLayout();

        Button saveTaskButton = new Button("Añadir");
        saveTaskButton.addClickListener(e -> saveNewTask());

        Button cancel = new Button("Cancelar");
        cancel.addClickListener(e -> exitWithoutSavingDialog());

        buttons.add(cancel, saveTaskButton);
        setAlignSelf(Alignment.END, buttons);

        return buttons;
    }

    private Dialog getConfirmationDialog() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Tarea creada con éxito");

        confirmDialog.add(
                new Span("La tarea " + taskName.getValue() + " ha sido creada correctamente.\n" + "¿Que deasea hacer?")
        );
        VaadinSession.getCurrent().setAttribute("cache", true);
        Button addNewTaskButton = new Button("Crear otra tarea", e -> {
            confirmDialog.close();
            getUI().ifPresent(ui -> ui.navigate("addtask"));
        });

        Button closeButton = new Button("Salir", e -> {
            confirmDialog.close();
            getUI().ifPresent(ui -> ui.navigate("tasks"));
        });

        confirmDialog.getFooter().add(addNewTaskButton, closeButton);
        return confirmDialog;
    }

    protected String getEmergencyLevelString(EmergencyLevel level) {
        return switch (level) {
            case LOW -> "Baja";
            case MEDIUM -> "Media";
            case HIGH -> "Alta";
            case VERYHIGH -> "Muy alta";
        };
    }

    //=============================== Dialogs =========================================
    private Dialog getVolunteersDialogContent() {
        Dialog volunteerDialog = new Dialog();
        volunteerDialog.setHeaderTitle("Seleccione los voluntarios");

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);

        Checkbox volunteerCheckbox = new Checkbox();
        volunteerCheckbox.setLabel("Elegir voluntarios automáticamente");

        Tabs tabs = new Tabs(
                new Tab("Todos"),
                new Tab("Distancia"),
                new Tab("Disponibilidad"),
                new Tab("Habilidades")
        );

        MultiSelectListBox<VolunteerDTO> volunteersListBox = new MultiSelectListBox<>();

        //No volunteers message
        Span noVolunteersMessage = new Span("No se encontraron voluntarios con las habilidades requeridas");
        noVolunteersMessage.setVisible(false);

        //Volunteers list
        List<VolunteerDTO> allVolunteers = new ArrayList<>(volunteerService.getVolunteers("None", getTaskDTO()));
        List<VolunteerDTO> distanceVolunteers = new ArrayList<>();
        List<VolunteerDTO> availabilityVolunteers = new ArrayList<>();
        List<VolunteerDTO> skillsVolunteers = new ArrayList<>();

        // Utilizando la nueva clase VolunteerInfo para el renderer
        volunteersListBox.setRenderer(new ComponentRenderer<>(volunteer ->
                new VolunteerInfo(volunteer, tabs.getSelectedTab() != null ? tabs.getSelectedTab().getLabel() : "Todos")
        ));

        tabs.setSelectedIndex(0);
        volunteersListBox.setItems(allVolunteers);

        // Listener para el cambio de pestañas
        tabs.addSelectedChangeListener(event -> {
            String selectedTabName = tabs.getSelectedTab().getLabel();
            TaskDTO currentTaskDTO = getTaskDTO();

            // Ocultar mensaje de error por defecto
            noVolunteersMessage.setVisible(false);

            switch (selectedTabName) {
                case "Todos":
                    volunteerCheckbox.clear();
                    if(allVolunteers.isEmpty()){
                        allVolunteers.addAll(volunteerService.getVolunteers("None", currentTaskDTO));
                    }
                    volunteersListBox.setItems(allVolunteers);
                    break;

                case "Distancia":
                    if(needsMultiSelectComboBox.getSelectedItems().isEmpty()){
                        Notification.show("Por favor, seleccione una necesidad primero para poder calcular las distancias.",
                                3000, Notification.Position.MIDDLE);
                        tabs.setSelectedTab(tabs.getTabAt(0));
                        return;
                    } else {
                        if(distanceVolunteers.isEmpty()){
                            distanceVolunteers.addAll(volunteerService.getVolunteers("distancia", currentTaskDTO));
                        }
                        volunteersListBox.setItems(distanceVolunteers);

                        if(distanceVolunteers.isEmpty()) {
                            noVolunteersMessage.setText("No se encontraron voluntarios cercanos a la ubicación de la tarea");
                            noVolunteersMessage.setVisible(true);
                        }
                    }
                    break;

                case "Disponibilidad":
                    if(starDateTimePicker.isEmpty() || endDatePicker.isEmpty()){
                        Notification.show("Por favor, seleccione primero una fecha de inicio y de fin de la tarea.",
                                3000, Notification.Position.MIDDLE);
                        tabs.setSelectedTab(tabs.getTabAt(0));
                        return;
                    } else {
                        if(availabilityVolunteers.isEmpty()){
                            availabilityVolunteers.addAll(volunteerService.getVolunteers("disponibilidad", currentTaskDTO));
                        }
                        volunteersListBox.setItems(availabilityVolunteers);

                        if(availabilityVolunteers.isEmpty()) {
                            noVolunteersMessage.setText("No se encontraron voluntarios disponibles en las fechas seleccionadas");
                            noVolunteersMessage.setVisible(true);
                        }
                    }
                    break;

                case "Habilidades":
                    if (needsMultiSelectComboBox.getSelectedItems().isEmpty()) {
                        Notification.show("Por favor, seleccione una necesidad primero para indicar el tipo de la tarea.",
                                3000, Notification.Position.MIDDLE);
                        tabs.setSelectedTab(tabs.getTabAt(0));
                        return;
                    } else {
                        if (skillsVolunteers.isEmpty()) {
                            skillsVolunteers.addAll(volunteerService.getVolunteers("habilidades", currentTaskDTO));
                        }
                        volunteersListBox.setItems(skillsVolunteers);

                        if(skillsVolunteers.isEmpty()) {
                            noVolunteersMessage.setText("No se encontraron voluntarios con la habilidad requerida para esta tarea");
                            noVolunteersMessage.setVisible(true);
                        }
                    }
                    break;
            }

            // Actualizar el renderer cuando cambia la pestaña para actualizar la información mostrada
            volunteersListBox.setRenderer(new ComponentRenderer<>(volunteer ->
                    new VolunteerInfo(volunteer, selectedTabName)
            ));
        });

        volunteerCheckbox.addClickListener(checkboxClickEvent ->
                volunteersListBox.setEnabled(!volunteerCheckbox.getValue())
        );

        dialogContent.add(volunteerCheckbox, tabs, noVolunteersMessage, volunteersListBox);

        //Footer
        Button saveButton = new Button("Guardar", e -> {
            if (volunteerCheckbox.getValue()) {
                volunteerMultiSelectComboBox.setItems("Elegir voluntarios automáticamente");
                volunteerMultiSelectComboBox.select("Elegir voluntarios automáticamente");
            } else {
                Set<String> selectedVolunteersNames = volunteersListBox.getSelectedItems().stream()
                        .map(VolunteerDTO::getFirstName)
                        .collect(Collectors.toSet());

                volunteerMultiSelectComboBox.clear();
                volunteerMultiSelectComboBox.setItems(selectedVolunteersNames);
                volunteerMultiSelectComboBox.select(selectedVolunteersNames);
            }
            volunteerDialog.close();
        });
        Button cancelButton = new Button("Cancelar", e -> volunteerDialog.close());

        volunteerDialog.getFooter().add(cancelButton, saveButton);
        volunteerDialog.add(dialogContent);

        return volunteerDialog;
    }

    private Dialog getNeedsDialogContent() {
        Dialog needsDialog = new Dialog();

        needsDialog.setHeaderTitle("Seleccione las necesidades a cubrir");

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);

        MultiSelectListBox<NeedDTO> needsListBox = new MultiSelectListBox<>();
        needsListBox.setItems(needService.getNeedsWithoutTask(selectedCatastrophe.getId()));

        needsListBox.setRenderer(
                new ComponentRenderer<>(need -> {
                    HorizontalLayout needContent = new HorizontalLayout();
                    needContent.addClassName("listBox__item");

                    Span needDescription = new Span(need.getDescription());
                    needDescription.addClassName("needContent--description");
                    Span needType = new Span(formatTaskType(need.getTaskType()));
                    needType.addClassName("listBox__item-detail");

                    needContent.add(needDescription, needType);
                    needContent.setPadding(false);
                    needContent.setSpacing(false);

                    return needContent;
                })
        );

        Span infoText = new Span("Al seleccionar una necesidad, solo podrá elegir necesidades adicionales del mismo tipo.");
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
        Button saveButton = new Button("Guardar", e -> {
            Set<String> selectedNeedDescriptions = needsListBox.getSelectedItems().stream()
                    .map(NeedDTO::getDescription)
                    .collect(Collectors.toSet());

            needsMultiSelectComboBox.clear();
            needsMultiSelectComboBox.setItems(selectedNeedDescriptions);
            needsMultiSelectComboBox.select(selectedNeedDescriptions);
            needsDialog.close();
        });
        Button cancelButton = new Button("Cancelar", e -> needsDialog.close());
        needsDialog.getFooter().add(cancelButton, saveButton);

        needsDialog.add(dialogContent);

        return needsDialog;
    }

    //===============================Validate Form=========================================
    protected boolean validateForm() {
        return !taskName.isEmpty() &&
                !taskDescription.isEmpty() &&
                starDateTimePicker.getValue() != null &&
                endDatePicker.getValue() != null &&  // Esto ya está, pero es importante verificarlo
                taskPriority.getValue() != null &&
                taskEmergency.getValue() != null &&
                !needsMultiSelectComboBox.getSelectedItems().isEmpty() &&
                !volunteerMultiSelectComboBox.getSelectedItems().isEmpty() &&
                !taskLocation.isEmpty();
    }

    protected boolean isFormFilled() {
        return !taskName.isEmpty() ||
                !taskDescription.isEmpty() ||
                taskPriority.getValue() != null ||
                taskEmergency.getValue() != null ||
                needsMultiSelectComboBox.getValue() != null ||
                starDateTimePicker.getValue() != null ||
                !volunteerMultiSelectComboBox.getSelectedItems().isEmpty() ||
                !taskLocation.isEmpty();
    }

    protected void exitWithoutSavingDialog() {
        if (isFormFilled()) {
            Dialog confirmDialog = new Dialog();
            confirmDialog.setHeaderTitle("Confirmación");

            VerticalLayout dialogContent = new VerticalLayout();
            dialogContent.add(new Span("¿Está seguro de que desea cancelar? Los cambios no guardados se perderán."));

            Button confirmButton = new Button("Confirmar", event -> {
                confirmDialog.close();
                VaadinSession.getCurrent().setAttribute("cache", true);
                getUI().ifPresent(ui -> ui.navigate("tasks"));
            });
            confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            Button cancelButton = new Button("Cancelar", event -> confirmDialog.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            confirmDialog.getFooter().add(cancelButton, confirmButton);
            confirmDialog.add(dialogContent);
            confirmDialog.open();
        } else {
            getUI().ifPresent(ui -> ui.navigate(""));
        }
    }

    //===============================Modify Preview=========================================
    protected void setupFormListeners() {
        taskName.addValueChangeListener(e ->
                updatePreview(e.getValue(), taskDescription.getValue(), taskPriority.getValue(), taskEmergency.getValue())
        );

        taskDescription.addValueChangeListener(e ->
                updatePreview(taskName.getValue(), e.getValue(), taskPriority.getValue(), taskEmergency.getValue()));

        taskPriority.addValueChangeListener(e ->
                updatePreview(taskName.getValue(), taskDescription.getValue(), e.getValue(), taskEmergency.getValue()));

        taskEmergency.addValueChangeListener(e ->
                updatePreview(taskName.getValue(), taskDescription.getValue(), taskPriority.getValue(), e.getValue()));

        starDateTimePicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                updatePreviewDate(e.getValue());
            }
        });
    }

    protected void updatePreview(String name, String description, Priority priority, EmergencyLevel emergencyLevel) {

        if (name != null && !name.trim().isEmpty()) {
            taskPreview.updateName(name);
        }

        if (description != null && !description.trim().isEmpty()) {
            taskPreview.updateDescription(description);
        }

        if (priority != null) {
            taskPreview.updatePriority(priority.toString());
        }

        if (emergencyLevel != null) {
            taskPreview.updateEmergencyLevel(getEmergencyLevelString(emergencyLevel));
        }
    }

    protected void updatePreviewDate(LocalDateTime date) {
        taskPreview.updateDate(formatDate(date));
    }

    //===============================Format Methods=========================================
    protected String formatDate(LocalDateTime taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    private String formatTaskType(TaskType taskType) {
        if (taskType == null) {
            return "No especificado";
        }

        return switch (taskType) {
            case MEDICAL -> "Medica";
            case POLICE -> "Policía";
            case FIREFIGHTERS -> "Bomberos";
            case CLEANING -> "Limpieza";
            case FEED -> "Alimentación";
            case PSYCHOLOGICAL -> "Psicológica";
            case BUILDING -> "Construcción";
            case CLOTHING -> "Ropa";
            case REFUGE -> "Refugio";
            case OTHER -> "Otra";
            case SEARCH -> "Búsqueda";
            case LOGISTICS -> "Logística";
            case COMMUNICATION -> "Comunicación";
            case MOBILITY -> "Movilidad";
            case PEOPLEMANAGEMENT -> "Gestión de personas";
            default -> "Otro";
        };
    }
}
