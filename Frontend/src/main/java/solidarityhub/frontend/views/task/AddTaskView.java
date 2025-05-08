package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.model.Need;
import solidarityhub.frontend.service.*;
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
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.volunteer.VolunteerInfo;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Añadir tarea")
@Route("addtask")
public class AddTaskView extends VerticalLayout implements BeforeEnterObserver {

    protected final TaskService taskService;
    protected final VolunteerService volunteerService;
    protected final NeedService needService;
    protected final CoordinatesService coordinatesService;
    protected final CatastropheService catastropheService;

    protected CatastropheDTO selectedCatastrophe;
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

    //Volunteers list
    protected List<VolunteerDTO> allVolunteersList = new ArrayList<>();
    protected List<VolunteerDTO> distanceVolunteersList = new ArrayList<>();
    protected List<VolunteerDTO> availabilityVolunteersList = new ArrayList<>();
    protected List<VolunteerDTO> skillsVolunteersList = new ArrayList<>();

    protected List<NeedDTO> allNeedsWithoutTask;

    @Autowired
    public AddTaskView(TaskService taskService) {
        this.taskService = taskService;
        this.volunteerService = new VolunteerService();
        this.needService = new NeedService();
        this.coordinatesService = new CoordinatesService();
        this.catastropheService = new CatastropheService();

        beforeEnter(null);
        buildView();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
        catastropheService.isCatastropheSelected(event, selectedCatastrophe);
        allNeedsWithoutTask = needService.getNeedsWithoutTask(selectedCatastrophe.getId());
    }

    protected void buildView() {
        removeAll();

        HeaderComponent header = new HeaderComponent("Añadir tarea", "tasks");

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
            allNeedsWithoutTask.stream()
                    .filter(n -> n.getDescription().equals(need))
                    .findFirst().ifPresent(needs::add);
        }

        List<VolunteerDTO> selectedVolunteers = new ArrayList<>();
        List<VolunteerDTO> finalSelectedVolunteers = selectedVolunteers;
        selectedVolunteers = volunteerMultiSelectComboBox.getSelectedItems().stream()
                .map(name -> {
                    if (name.equals("Elegir voluntarios automáticamente")) {
                        finalSelectedVolunteers.addAll(allVolunteersList.subList(0, 1));
                    }
                    return allVolunteersList.stream()
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
    protected Component getPreview(){
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

    protected Component getForms(){
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


        volunteerMultiSelectComboBox.getElement().addEventListener("click", e -> {
            if (starDateTimePicker.isEmpty() || endDatePicker.isEmpty()) {
                // Si no hay fechas seleccionadas, mostrar notificación
                Notification notification = Notification.show(
                        "Selecciona fecha para comprobar qué voluntarios están disponibles",
                        3000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            } else {
                // Si hay fechas, mostrar el diálogo normal
                Dialog selectVolunteersDialog = getVolunteersDialogContent();
                selectVolunteersDialog.open();
            }
        });

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
                new Tab("Disponibles"),
                new Tab("Por Distancia"),
                new Tab("Por Habilidades")
        );

        MultiSelectListBox<VolunteerDTO> volunteersListBox = new MultiSelectListBox<>();
        volunteersListBox.setWidthFull();

        // No volunteers message
        Span noVolunteersMessage = new Span("No se encontraron voluntarios disponibles para las fechas seleccionadas");
        noVolunteersMessage.setVisible(false);

        // Comprobar si tenemos fechas seleccionadas
        if (starDateTimePicker.isEmpty() || endDatePicker.isEmpty()) {
            Notification.show("Primero debe seleccionar las fechas de inicio y fin de la tarea",
                    3000, Notification.Position.MIDDLE);
            volunteerDialog.close();
            return volunteerDialog;
        }

        // Obtener las fechas para verificar disponibilidad
        LocalDateTime startDate = starDateTimePicker.getValue();
        LocalDateTime endDate = endDatePicker.getValue().atTime(23, 59);

        // Obtener todos los voluntarios y comprobar disponibilidad
        TaskDTO currentTaskDTO = getTaskDTO();
        allVolunteersList = new ArrayList<>(volunteerService.getVolunteers("None", currentTaskDTO));

        // Filtrar por disponibilidad
        List<VolunteerDTO> availableVolunteers = allVolunteersList.stream()
                .filter(v -> v.isAvailable() > 0) // Suponiendo que el servicio ya ha configurado la disponibilidad
                .collect(Collectors.toList());

        // Obtener voluntarios por distancia y habilidades para los filtros
        distanceVolunteersList = new ArrayList<>(volunteerService.getVolunteers("distancia", currentTaskDTO))
                .stream()
                .filter(v -> v.isAvailable() > 0) // Solo disponibles
                .collect(Collectors.toList());

        skillsVolunteersList = new ArrayList<>(volunteerService.getVolunteers("habilidades", currentTaskDTO))
                .stream()
                .filter(v -> v.isAvailable() > 0) // Solo disponibles
                .collect(Collectors.toList());

        // Configurar el visualizador de voluntarios
        ComponentRenderer<Component, VolunteerDTO> renderer = new ComponentRenderer<>(volunteer -> {
            // Mostrar información del voluntario con una etiqueta clara de disponibilidad
            HorizontalLayout layout = new HorizontalLayout();
            layout.setWidthFull();
            layout.setAlignItems(Alignment.CENTER);

            VerticalLayout infoLayout = new VerticalLayout();
            infoLayout.setPadding(false);
            infoLayout.setSpacing(false);

            Span nameSpan = new Span(volunteer.getFirstName() + " " + volunteer.getLastName());
            nameSpan.getStyle().set("font-weight", "bold");

            Span emailSpan = new Span(volunteer.getEmail());
            emailSpan.getStyle().set("font-size", "0.9em");
            emailSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");

            infoLayout.add(nameSpan, emailSpan);

            // Badge de disponibilidad
            Span availabilityBadge = new Span("Disponible");
            availabilityBadge.getStyle()
                    .set("background-color", "var(--lumo-success-color)")
                    .set("color", "white")
                    .set("padding", "4px 8px")
                    .set("border-radius", "4px")
                    .set("font-size", "0.8em")
                    .set("font-weight", "bold")
                    .set("margin-left", "auto");

            layout.add(infoLayout, availabilityBadge);

            // Agregar un borde de color verde para mayor claridad
            layout.getStyle()
                    .set("border-left", "4px solid var(--lumo-success-color)")
                    .set("padding", "8px")
                    .set("border-radius", "4px")
                    .set("background-color", "rgba(76, 175, 80, 0.1)");

            return layout;
        });

        volunteersListBox.setRenderer(renderer);

        // Inicializar con los voluntarios disponibles
        if (availableVolunteers.isEmpty()) {
            noVolunteersMessage.setText("No se encontraron voluntarios disponibles para las fechas seleccionadas");
            noVolunteersMessage.setVisible(true);
        } else {
            volunteersListBox.setItems(availableVolunteers);
        }

        // Listener para cambios de pestaña
        tabs.addSelectedChangeListener(event -> {
            String selectedTabName = tabs.getSelectedTab().getLabel();

            // Ocultar mensaje de error por defecto
            noVolunteersMessage.setVisible(false);

            switch (selectedTabName) {
                case "Disponibles":
                    if (availableVolunteers.isEmpty()) {
                        noVolunteersMessage.setText("No se encontraron voluntarios disponibles para las fechas seleccionadas");
                        noVolunteersMessage.setVisible(true);
                    } else {
                        volunteersListBox.setItems(availableVolunteers);
                    }
                    break;

                case "Por Distancia":
                    if (needsMultiSelectComboBox.getSelectedItems().isEmpty()) {
                        Notification.show("Seleccione una necesidad primero para poder filtrar por distancia",
                                3000, Notification.Position.MIDDLE);
                        tabs.setSelectedTab(tabs.getTabAt(0));
                        return;
                    }

                    if (distanceVolunteersList.isEmpty()) {
                        noVolunteersMessage.setText("No se encontraron voluntarios disponibles cercanos a la ubicación");
                        noVolunteersMessage.setVisible(true);
                    } else {
                        volunteersListBox.setItems(distanceVolunteersList);
                    }
                    break;

                case "Por Habilidades":
                    if (needsMultiSelectComboBox.getSelectedItems().isEmpty()) {
                        Notification.show("Seleccione una necesidad primero para poder filtrar por habilidades",
                                3000, Notification.Position.MIDDLE);
                        tabs.setSelectedTab(tabs.getTabAt(0));
                        return;
                    }

                    if (skillsVolunteersList.isEmpty()) {
                        noVolunteersMessage.setText("No se encontraron voluntarios disponibles con las habilidades requeridas");
                        noVolunteersMessage.setVisible(true);
                    } else {
                        volunteersListBox.setItems(skillsVolunteersList);
                    }
                    break;
            }
        });

        volunteerCheckbox.addClickListener(checkboxClickEvent ->
                volunteersListBox.setEnabled(!volunteerCheckbox.getValue())
        );

        dialogContent.add(volunteerCheckbox, tabs, noVolunteersMessage, volunteersListBox);

        // Footer
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
        needsListBox.setItems(allNeedsWithoutTask);
        needsListBox.setWidthFull();

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

        Span infoText = new Span("Solo es posible elegir necesidades de un único tipo.");
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
            getUI().ifPresent(ui -> ui.navigate("tasks"));
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
            availabilityVolunteersList.clear();
        });

        endDatePicker.addValueChangeListener(e -> {
            availabilityVolunteersList.clear();
        });

        needsMultiSelectComboBox.addSelectionListener(e -> {
            skillsVolunteersList.clear();
            distanceVolunteersList.clear();
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
