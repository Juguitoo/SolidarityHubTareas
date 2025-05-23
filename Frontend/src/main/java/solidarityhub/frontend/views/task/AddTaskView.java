package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.*;
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
import org.pingu.domain.enums.EmergencyLevel;
import org.pingu.domain.enums.Priority;
import org.pingu.domain.enums.Status;
import org.pingu.domain.enums.TaskType;
import solidarityhub.frontend.views.HeaderComponent;
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
    protected static Translator translator;

    protected CatastropheDTO selectedCatastrophe;
    protected TaskComponent taskPreview;

    protected TextField taskName;
    protected TextArea taskDescription;
    protected ComboBox<Priority> taskPriority;
    protected ComboBox<EmergencyLevel> taskEmergency;
    protected DateTimePicker starDateTimePicker;
    protected DatePicker endDatePicker;
    protected TextField taskLocation;
    protected MultiSelectComboBox<String> volunteerMultiSelectComboBox;
    protected MultiSelectComboBox<String> needsMultiSelectComboBox;

    //Volunteers list
    protected List<VolunteerDTO> allVolunteersList = new ArrayList<>();
    protected List<VolunteerDTO> distanceVolunteersList = new ArrayList<>();
    protected List<VolunteerDTO> skillsVolunteersList = new ArrayList<>();

    protected List<NeedDTO> allNeedsWithoutTask;

    protected TaskType taskType;

    public AddTaskView() {
        this.taskService = new TaskService();
        this.volunteerService = new VolunteerService();
        this.needService = new NeedService();
        this.coordinatesService = new CoordinatesService();
        this.catastropheService = new CatastropheService();

        initializeTranslator();

        taskName = new TextField(translator.get("preview_task_name"));
        taskDescription = new TextArea(translator.get("preview_task_description"));
        taskPriority = new ComboBox<>(translator.get("preview_task_priority"));
        taskEmergency = new ComboBox<>(translator.get("preview_task_emergency_level"));
        starDateTimePicker = new DateTimePicker(translator.get("preview_start_date"));
        endDatePicker = new DatePicker(translator.get("preview_end_date"));
        taskLocation = new TextField(translator.get("preview_meeting_point"));
        volunteerMultiSelectComboBox = new MultiSelectComboBox<>(translator.get("preview_volunteers"));
        needsMultiSelectComboBox = new MultiSelectComboBox<>(translator.get("preview_needs"));
    }

    protected void initializeTranslator() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
        if (!catastropheService.isCatastropheSelected(event, selectedCatastrophe)) {
            return;
        }
        allNeedsWithoutTask = needService.getNeedsWithoutTask(selectedCatastrophe.getId());

        buildView();
    }

    protected void buildView() {
        removeAll();

        add(
            getHeader(),
            getPreview(),
            getForms(),
            getButtons()
        );

        setAlignItems(Alignment.CENTER);
        setupPreviewWithTranslations();
    }

    private void saveNewTask(){
        if (validateForm()) {
            try {
                TaskDTO newTaskDTO = getTaskDTO();
                taskService.addTask(newTaskDTO);

                getConfirmationDialog().open();

            } catch (Exception ex) {
                Notification.show(translator.get("error_saving_task") + ex.getMessage(),
                        5000, Notification.Position.MIDDLE);
            }
        } else {
            Notification.show(translator.get("check_fields"),
                    3000, Notification.Position.MIDDLE);
        }
    }

    private TaskDTO getTaskDTO() {
        List<VolunteerDTO> selectedVolunteers = new ArrayList<>();
        List<VolunteerDTO> finalSelectedVolunteers = selectedVolunteers;
        selectedVolunteers = volunteerMultiSelectComboBox.getSelectedItems().stream()
                .map(name -> {
                    if (name.equals(translator.get("auto_select_volunteers"))) {
                        finalSelectedVolunteers.addAll(allVolunteersList.subList(0, 1));
                    }
                    return allVolunteersList.stream()
                            .filter(v -> v.getFirstName().equals(name))
                            .findFirst()
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());



        LocalDateTime endDateTime = null;
        if (endDatePicker.getValue() != null) {
            endDateTime = endDatePicker.getValue().atTime(23, 59);
        }

        return new TaskDTO(taskName.getValue(), taskDescription.getValue(), starDateTimePicker.getValue(), endDateTime,
                getTaskType(), taskPriority.getValue(), taskEmergency.getValue(), Status.TO_DO, getNeedsList(), selectedVolunteers, selectedCatastrophe.getId(), taskLocation.getValue());
    }

    private List<NeedDTO> getNeedsList() {
        List<String> selectedNeeds = needsMultiSelectComboBox.getSelectedItems().stream().toList();
        List<NeedDTO> needs = new ArrayList<>();
        for (String need : selectedNeeds) {
            allNeedsWithoutTask.stream()
                    .filter(n -> n.getDescription().equals(need))
                    .findFirst().ifPresent(needs::add);
        }
        return needs;
    }

    private TaskType getTaskType(){
        List<NeedDTO> needs = getNeedsList();
        return needs.isEmpty() ? null : needs.getFirst().getTaskType();
    }

    //===============================Get Components=========================================
    protected Component getHeader(){
        return new HeaderComponent(translator.get("add_task_title"), "tasks");
    }

    protected Component getPreview(){
        VerticalLayout preview = new VerticalLayout();
        preview.addClassName("previewContainer");
        preview.setPadding(false);
        preview.setWidth("40%");

        taskPreview = new TaskComponent(
                0,
                translator.get("preview_task_name"),
                translator.get("preview_task_description"),
                formatDate(LocalDateTime.now()),
                translator.get("preview_task_priority"),
                translator.get("preview_task_emergency_level"),
                TaskType.OTHER
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
            case LOW -> translator.get("low_priority");
            case MODERATE -> translator.get("moderate_priority");
            case URGENT -> translator.get("urgent_priority");
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
        starDateTimePicker.setHelperText(translator.get("start_date_field_helper"));

        endDatePicker.setRequiredIndicatorVisible(true);
        endDatePicker.setMin(LocalDate.now());
        endDatePicker.setI18n(new DatePicker.DatePickerI18n().setFirstDayOfWeek(1));
        endDatePicker.setHelperText(translator.get("end_date_field_helper"));

        taskLocation.setRequiredIndicatorVisible(true);
        taskLocation.setRequired(true);
        taskLocation.setHelperText(translator.get("meeting_point_helper"));

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
                    } else if (endDatePicker.getValue() != null && endDatePicker.getValue().isBefore(startValue)) {
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
                        Notification.show(translator.get("wrong_meeting_point"), 3000, Notification.Position.MIDDLE);
                    }
                }else{
                    taskLocation.setInvalid(true);
                    taskLocation.setValue("");
                    Notification.show(translator.get("wrong_meeting_point"), 3000, Notification.Position.MIDDLE);
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
        volunteerMultiSelectComboBox.setPlaceholder(translator.get("click_to_select"));
        volunteerMultiSelectComboBox.setReadOnly(true);
        volunteerMultiSelectComboBox.setRequiredIndicatorVisible(true);
        volunteerMultiSelectComboBox.setRequired(true);
        volunteerMultiSelectComboBox.addClassName("addTaskForm--multiSelectComboBox");


        volunteerMultiSelectComboBox.getElement().addEventListener("click", e -> {
            if (starDateTimePicker.isEmpty() || endDatePicker.isEmpty()) {
                // Si no hay fechas seleccionadas, mostrar notificación
                Notification notification = Notification.show(
                        translator.get("select_date_to_filter"),
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
        needsMultiSelectComboBox.setPlaceholder(translator.get("click_to_select"));
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

        Button saveTaskButton = new Button(translator.get("add_button"));
        saveTaskButton.addClickListener(e -> saveNewTask());

        Button cancel = new Button(translator.get("cancel_button"));
        cancel.addClickListener(e -> exitWithoutSavingDialog());

        buttons.add(cancel, saveTaskButton);
        setAlignSelf(Alignment.END, buttons);

        return buttons;
    }

    protected String getEmergencyLevelString(EmergencyLevel level) {
        return switch (level) {
            case LOW -> translator.get("low_emergency_level");
            case MEDIUM -> translator.get("medium_emergency_level");
            case HIGH -> translator.get("high_emergency_level");
            case VERYHIGH -> translator.get("very_high_emergency_level");
        };
    }

    //=============================== Dialogs =========================================
    private Dialog getVolunteersDialogContent() {
        Dialog volunteerDialog = new Dialog();
        volunteerDialog.setHeaderTitle(translator.get("select_volunteers"));

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);

        Checkbox volunteerCheckbox = new Checkbox();
        volunteerCheckbox.setLabel(translator.get("auto_select_volunteers"));

        Tabs tabs = new Tabs(
                new Tab(translator.get("all_volunteers")),
                new Tab(translator.get("by_distance")),
                new Tab(translator.get("by_skills"))
        );

        MultiSelectListBox<VolunteerDTO> volunteersListBox = new MultiSelectListBox<>();
        volunteersListBox.setWidthFull();

        // No volunteers message
        Span noVolunteersMessage = new Span(translator.get("no_available_volunteers"));
        noVolunteersMessage.setVisible(false);

        // Comprobar si tenemos fechas seleccionadas
        if (starDateTimePicker.isEmpty() || endDatePicker.isEmpty()) {
            Notification.show(translator.get("first_select_dates"),
                    3000, Notification.Position.MIDDLE);
            volunteerDialog.close();
            return volunteerDialog;
        }

        // Obtener todos los voluntarios y comprobar disponibilidad
        TaskDTO currentTaskDTO = getTaskDTO();
        if(allVolunteersList.isEmpty()) {
            allVolunteersList = new ArrayList<>(volunteerService.getVolunteers("disponibilidad", currentTaskDTO));
        }

        // Configurar el visualizador de voluntarios
        // POSIBLE REFACTORING: EXTRACT METHOD
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
            Span availabilityBadge;
            if(volunteer.getAvailabilityStatus() > 0){
                availabilityBadge = new Span("Disponible");
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
            }else{
                availabilityBadge = new Span("No disponible");
                availabilityBadge.getStyle()
                        .set("background-color", "var(--lumo-error-color)")
                        .set("color", "white")
                        .set("padding", "4px 8px")
                        .set("border-radius", "4px")
                        .set("font-size", "0.8em")
                        .set("font-weight", "bold")
                        .set("margin-left", "auto");

                layout.add(infoLayout, availabilityBadge);

                // Agregar un borde de color rojo para mayor claridad
                layout.getStyle()
                        .set("border-left", "4px solid var(--lumo-error-color)")
                        .set("padding", "8px")
                        .set("border-radius", "4px")
                        .set("background-color", "rgba(244, 67, 54, 0.1)");

            }
            return layout;
        });

        volunteersListBox.setRenderer(renderer);

        volunteersListBox.setItems(allVolunteersList);

        tabs.addSelectedChangeListener(event -> {
            String selectedTabName = tabs.getSelectedTab().getLabel();

            noVolunteersMessage.setVisible(false);

            switch (selectedTabName) {
                case "Todos los voluntarios", "All Volunteers", "Tots els voluntaris":
                    if (allVolunteersList.isEmpty()) {
                        allVolunteersList = new ArrayList<>(volunteerService.getVolunteers("disponibilidad", currentTaskDTO));
                        if(allVolunteersList.isEmpty()) {
                            noVolunteersMessage.setText(translator.get("no_available_volunteers"));
                            noVolunteersMessage.setVisible(true);
                        } else {
                            volunteersListBox.setItems(allVolunteersList);
                        }
                    } else {
                        volunteersListBox.setItems(allVolunteersList);
                    }
                    break;

                case "Por distancia", "By distance", "Per distància":
                    if (needsMultiSelectComboBox.getSelectedItems().isEmpty()) {
                        Notification.show(translator.get("first_select_need_distance"),
                                3000, Notification.Position.MIDDLE);
                        tabs.setSelectedTab(tabs.getTabAt(0));
                        return;
                    }

                    if (distanceVolunteersList.isEmpty()) {
                        distanceVolunteersList = new ArrayList<>(volunteerService.getVolunteers("distancia", currentTaskDTO));
                        if(distanceVolunteersList.isEmpty()) {
                            noVolunteersMessage.setText(translator.get("no_near_volunteers"));
                            noVolunteersMessage.setVisible(true);
                        } else {
                            volunteersListBox.setItems(distanceVolunteersList);
                        }
                    } else {
                        volunteersListBox.setItems(distanceVolunteersList);
                    }
                    break;

                case "Por habilidades", "By skills", "Per habilitats":
                    if (needsMultiSelectComboBox.getSelectedItems().isEmpty()) {
                        Notification.show(translator.get("first_select_need_skill"),
                                3000, Notification.Position.MIDDLE);
                        tabs.setSelectedTab(tabs.getTabAt(0));
                        return;
                    }

                    if (skillsVolunteersList.isEmpty()) {
                        skillsVolunteersList = new ArrayList<>(volunteerService.getVolunteers("habilidades", currentTaskDTO));
                        if(skillsVolunteersList.isEmpty()) {
                            noVolunteersMessage.setText(translator.get("no_skill_volunteers"));
                            noVolunteersMessage.setVisible(true);
                        } else {
                            volunteersListBox.setItems(skillsVolunteersList);
                        }
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
        Button saveButton = new Button(translator.get("save_button"), e -> {
            if (volunteerCheckbox.getValue()) {
                volunteerMultiSelectComboBox.setItems(translator.get("auto_select_volunteers"));
                volunteerMultiSelectComboBox.select(translator.get("auto_select_volunteers"));
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

        Button cancelButton = new Button(translator.get("cancel_button"), e -> volunteerDialog.close());

        volunteerDialog.getFooter().add(cancelButton, saveButton);
        volunteerDialog.add(dialogContent);

        return volunteerDialog;
    }

    private Dialog getNeedsDialogContent() {
        Dialog needsDialog = new Dialog();

        needsDialog.setHeaderTitle(translator.get("select_needs"));

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

    private Dialog getConfirmationDialog() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle(translator.get("correct_task"));

        confirmDialog.add(
                new Span(translator.get("the_task") + taskName.getValue() + translator.get("successfully_added") +"\n" + translator.get("next_step"))
        );
        VaadinSession.getCurrent().setAttribute("cache", true);
        Button addNewTaskButton = new Button(translator.get("create_other_task"), e -> {
            confirmDialog.close();
            getUI().ifPresent(ui -> ui.navigate("addtask"));
        });

        Button closeButton = new Button(translator.get("exit_button"), e -> {
            confirmDialog.close();
            getUI().ifPresent(ui -> ui.navigate("tasks"));
        });

        confirmDialog.getFooter().add(addNewTaskButton, closeButton);
        return confirmDialog;
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
            confirmDialog.setHeaderTitle(translator.get("confirm_title"));

            VerticalLayout dialogContent = new VerticalLayout();
            dialogContent.add(new Span(translator.get("confirm_text")));

            Button confirmButton = new Button(translator.get("confirm_button"), event -> {
                confirmDialog.close();
                VaadinSession.getCurrent().setAttribute("cache", true);
                getUI().ifPresent(ui -> ui.navigate("tasks"));
            });
            confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            Button cancelButton = new Button(translator.get("cancel_button"), event -> confirmDialog.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            confirmDialog.getFooter().add(cancelButton, confirmButton);
            confirmDialog.add(dialogContent);
            confirmDialog.open();
        } else {
            getUI().ifPresent(ui -> ui.navigate("tasks"));
        }
    }

    //===============================Modify Preview=========================================
    protected void setupPreviewWithTranslations() {
        if (taskPreview != null) {
            taskPreview.updatePriority(translator.get("preview_task_priority"));
            taskPreview.updateName(translator.get("preview_task_name"));
            taskPreview.updateDescription(translator.get("preview_task_description"));
            taskPreview.updateEmergencyLevel(translator.get("preview_task_emergency_level"));
        }
    }

    protected void setupFormListeners() {
        taskName.addValueChangeListener(e ->
                updatePreview(e.getValue(), taskDescription.getValue(), taskPriority.getValue(), taskEmergency.getValue(), taskType, starDateTimePicker.getValue())
        );

        taskDescription.addValueChangeListener(e ->
                updatePreview(taskName.getValue(), e.getValue(), taskPriority.getValue(), taskEmergency.getValue(), taskType, starDateTimePicker.getValue()));

        taskPriority.addValueChangeListener(e ->
                updatePreview(taskName.getValue(), taskDescription.getValue(), e.getValue(), taskEmergency.getValue(), taskType, starDateTimePicker.getValue()));

        taskEmergency.addValueChangeListener(e ->
                updatePreview(taskName.getValue(), taskDescription.getValue(), taskPriority.getValue(), e.getValue(), taskType, starDateTimePicker.getValue()));

        starDateTimePicker.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                updatePreview(taskName.getValue(), taskDescription.getValue(), taskPriority.getValue(), taskEmergency.getValue(), taskType, e.getValue());
            }
            allVolunteersList.clear();
            distanceVolunteersList.clear();
            skillsVolunteersList.clear();
        });

        endDatePicker.addValueChangeListener(e -> {
            allVolunteersList.clear();
            distanceVolunteersList.clear();
            skillsVolunteersList.clear();
        });

        needsMultiSelectComboBox.addSelectionListener(e -> {
            skillsVolunteersList.clear();
            distanceVolunteersList.clear();
            taskType = getTaskType();
            updatePreview(taskName.getValue(), taskDescription.getValue(), taskPriority.getValue(), taskEmergency.getValue(), taskType, starDateTimePicker.getValue());
        });
    }

    protected void updatePreview(String name, String description, Priority priority, EmergencyLevel emergencyLevel, TaskType taskType, LocalDateTime date) {

        if (name != null && !name.trim().isEmpty()) {
            taskPreview.updateName(name);
        }

        if (description != null && !description.trim().isEmpty()) {
            taskPreview.updateDescription(description);
        }

        if (priority != null) {
            String priorityText = switch (priority) {
                case LOW -> translator.get("low_priority");
                case MODERATE -> translator.get("moderate_priority");
                case URGENT -> translator.get("urgent_priority");
            };
            taskPreview.updatePriority(priorityText);
        }

        if (emergencyLevel != null) {
            taskPreview.updateEmergencyLevel(getEmergencyLevelString(emergencyLevel));
        }

        if(taskType != null){
            taskPreview.updateTaskType(taskType);
        }

        if(date != null){
            taskPreview.updateDate(formatDate(date));
        }
    }

    //===============================Format Methods=========================================
    protected String formatDate(LocalDateTime taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    protected String formatTaskType(TaskType taskType) {
        if (taskType == null) {
            return translator.get("task_type_not_specified");
        }

        return switch (taskType) {
            case MEDICAL -> translator.get("task_type_medical");
            case POLICE -> translator.get("task_type_police");
            case FIREFIGHTERS -> translator.get("task_type_firefighters");
            case CLEANING -> translator.get("task_type_cleaning");
            case FEED -> translator.get("task_type_feed");
            case PSYCHOLOGICAL -> translator.get("task_type_psychological");
            case BUILDING -> translator.get("task_type_building");
            case CLOTHING -> translator.get("task_type_clothing");
            case REFUGE -> translator.get("task_type_refuge");
            case OTHER -> translator.get("task_type_other");
            case SEARCH -> translator.get("task_type_search");
            case LOGISTICS -> translator.get("task_type_logistics");
            case COMMUNICATION -> translator.get("task_type_communication");
            case MOBILITY -> translator.get("task_type_mobility");
            case PEOPLEMANAGEMENT -> translator.get("task_type_people_management");
            case SAFETY -> translator.get("task_type_safety");
        };
    }

    protected String formatStatus(Status status) {
        if (status == null) return "";

        return switch (status) {
            case TO_DO -> translator.get("status_todo");
            case IN_PROGRESS -> translator.get("status_in_progress");
            case FINISHED -> translator.get("status_finished");
        };
    }
}
