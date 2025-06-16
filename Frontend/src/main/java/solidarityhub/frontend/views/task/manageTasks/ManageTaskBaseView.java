package solidarityhub.frontend.views.task.manageTasks;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinSession;
import org.pingu.domain.enums.EmergencyLevel;
import org.pingu.domain.enums.Priority;
import org.pingu.domain.enums.Status;
import org.pingu.domain.enums.TaskType;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.dto.VolunteerDTO;
import solidarityhub.frontend.dto.ResourceAssignmentDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.CoordinatesService;
import solidarityhub.frontend.service.FormatService;
import solidarityhub.frontend.service.NeedService;
import solidarityhub.frontend.service.ResourceAssignmentService;
import solidarityhub.frontend.service.TaskService;
import solidarityhub.frontend.service.VolunteerService;
import solidarityhub.frontend.views.task.AssignResourceDialog;
import solidarityhub.frontend.views.task.TaskComponent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ManageTaskBaseView extends VerticalLayout implements BeforeEnterObserver {

    protected final TaskService taskService;
    protected final VolunteerService volunteerService;
    protected final NeedService needService;
    protected final FormatService formatService;
    protected final CoordinatesService coordinatesService;
    protected final CatastropheService catastropheService;
    protected final ResourceAssignmentService resourceAssignmentService;
    protected static Translator translator = new Translator();

    protected CatastropheDTO selectedCatastrophe;
    protected TaskComponent taskPreview;

    //Form components
    protected TextField taskName;
    protected TextArea taskDescription;
    protected ComboBox<Priority> taskPriority;
    protected ComboBox<EmergencyLevel> taskEmergency;
    protected DateTimePicker startDateTimePicker;
    protected DatePicker endDatePicker;
    protected TextField taskLocation;
    protected MultiSelectComboBox<String> volunteerMultiSelectComboBox;
    protected MultiSelectComboBox<String> needsMultiSelectComboBox;

    //Dialog forms
    protected MultiSelectListBox<NeedDTO> needsListBox = new MultiSelectListBox<>();

    //Volunteers list
    protected List<VolunteerDTO> allVolunteersList = new ArrayList<>();
    protected List<VolunteerDTO> distanceVolunteersList = new ArrayList<>();
    protected List<VolunteerDTO> skillsVolunteersList = new ArrayList<>();
    protected List<VolunteerDTO> selectedVolunteersList = new ArrayList<>();
    protected List<NeedDTO> allNeedsWithoutTask;

    protected TaskType taskType;
    protected int nAutoSelectVolunteers;

    public ManageTaskBaseView() {
        this.taskService = new TaskService();
        this.volunteerService = new VolunteerService();
        this.needService = new NeedService();
        this.coordinatesService = new CoordinatesService();
        this.catastropheService = new CatastropheService();
        this.formatService = FormatService.getInstance();
        this.resourceAssignmentService = new ResourceAssignmentService();

        translator.initializeTranslator();

        taskName = new TextField(translator.get("preview_task_name"));
        taskDescription = new TextArea(translator.get("preview_task_description"));
        taskPriority = new ComboBox<>(translator.get("preview_task_priority"));
        taskEmergency = new ComboBox<>(translator.get("preview_task_emergency_level"));
        startDateTimePicker = new DateTimePicker(translator.get("preview_start_date"));
        endDatePicker = new DatePicker(translator.get("preview_end_date"));
        taskLocation = new TextField(translator.get("preview_meeting_point"));
        volunteerMultiSelectComboBox = new MultiSelectComboBox<>(translator.get("preview_volunteers"));
        needsMultiSelectComboBox = new MultiSelectComboBox<>(translator.get("preview_needs"));
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

        addClassName("addTaskView");

        add(
            getHeader(),
            getPreview(),
            getForms(),
            getActionButtons()
        );

        setAlignItems(Alignment.CENTER);
    }

    protected TaskDTO getTaskDTO() {
        CatastropheDTO catastropheDTO = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
        LocalDateTime endDateTime = null;
        if (endDatePicker.getValue() != null) {
            endDateTime = endDatePicker.getValue().atTime(23, 59);
        }

        return new TaskDTO(taskName.getValue(), taskDescription.getValue(), startDateTimePicker.getValue(), endDateTime,
                getTaskType(), taskPriority.getValue(), taskEmergency.getValue(), Status.TO_DO, getNeedsList(), selectedVolunteersList, catastropheDTO.getId(), taskLocation.getValue());
    }

    protected List<NeedDTO> getNeedsList() {
        List<String> selectedNeeds = needsMultiSelectComboBox.getSelectedItems().stream().toList();
        List<NeedDTO> needs = new ArrayList<>();
        for (String need : selectedNeeds) {
            allNeedsWithoutTask.stream()
                    .filter(n -> n.getDescription().equals(need)).forEach(needs::add);
        }
        return needs;
    }

    protected TaskType getTaskType(){
        List<NeedDTO> needs = getNeedsList();
        return needs.isEmpty() ? null : needs.getFirst().getTaskType();
    }

    //===============================Get Components=========================================
    protected abstract Component getHeader();

    protected Component getPreview(){
        VerticalLayout preview = new VerticalLayout();
        preview.addClassName("previewContainer");
        preview.setPadding(false);
        preview.setWidth("40%");

        taskPreview = new TaskComponent(
                0,
                translator.get("preview_task_name"),
                translator.get("preview_task_description"),
                formatService.formatDateTime(LocalDateTime.now()),
                translator.get("preview_task_priority"),
                translator.get("preview_task_emergency_level"),
                TaskType.OTHER
        );
        taskPreview.editButton.setVisible(false);
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
        taskPriority.setItemLabelGenerator(formatService::formatPriority);
        taskPriority.setRequiredIndicatorVisible(true);
        taskPriority.setRequired(true);

        taskEmergency.setItems(EmergencyLevel.LOW, EmergencyLevel.MEDIUM, EmergencyLevel.HIGH, EmergencyLevel.VERYHIGH);
        taskEmergency.setItemLabelGenerator(formatService::formatEmergencyLevel);
        taskEmergency.setRequiredIndicatorVisible(true);
        taskEmergency.setRequired(true);

        startDateTimePicker.setRequiredIndicatorVisible(true);
        startDateTimePicker.setMin(LocalDateTime.now());
        startDateTimePicker.setDatePickerI18n(new DatePicker.DatePickerI18n().setFirstDayOfWeek(1));
        startDateTimePicker.setHelperText(translator.get("start_date_field_helper"));

        endDatePicker.setRequiredIndicatorVisible(true);
        endDatePicker.setMin(LocalDate.now());
        endDatePicker.setI18n(new DatePicker.DatePickerI18n().setFirstDayOfWeek(1));
        endDatePicker.setHelperText(translator.get("end_date_field_helper"));

        taskLocation.setRequiredIndicatorVisible(true);
        taskLocation.setRequired(true);
        taskLocation.setHelperText(translator.get("meeting_point_helper"));

        addTaskForm.add(taskName, taskDescription, startDateTimePicker, taskPriority, getNeedsForm(), endDatePicker, taskEmergency, getVolunteersForm(), taskLocation, getResourceAssignmentsBtn());

        startDateTimePicker.addValueChangeListener(event -> {
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
            if (startDateTimePicker.isEmpty() || endDatePicker.isEmpty()) {
                Notification notification = Notification.show(
                        translator.get("select_date_to_filter"),
                        3000,
                        Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            } else {
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

    protected Component getResourceAssignmentsBtn() {
        return new Button(translator.get("assign_resource"),
                e -> {
                    AssignResourceDialog dialog = new AssignResourceDialog(selectedCatastrophe);
                    dialog.open();
                }
        );
    }

    protected abstract Component getActionButtons();

    //=============================== Dialogs =========================================
    private Dialog getVolunteersDialogContent() {
        TaskDTO currentTaskDTO = getTaskDTO();
        if(allVolunteersList.isEmpty()) {
            allVolunteersList = new ArrayList<>(volunteerService.getVolunteers("disponibilidad", currentTaskDTO));
        }

        Dialog volunteerDialog = new Dialog();
        volunteerDialog.setHeaderTitle(translator.get("select_volunteers"));

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);

        HorizontalLayout autoSelectLayout = new HorizontalLayout();
        autoSelectLayout.setAlignItems(Alignment.CENTER);

        Checkbox volunteerCheckbox = new Checkbox();
        volunteerCheckbox.setLabel(translator.get("auto_select_volunteers"));

        IntegerField volunteersQuantity = new IntegerField();
        volunteersQuantity.setValue(2);
        volunteersQuantity.setStepButtonsVisible(true);
        volunteersQuantity.setMin(1);
        volunteersQuantity.setMax(allVolunteersList.size());
        volunteersQuantity.setEnabled(false);
        volunteersQuantity.setWidth("100px");

        autoSelectLayout.add(volunteerCheckbox, volunteersQuantity);

        Tabs tabs = new Tabs(
                new Tab(translator.get("all_volunteers")),
                new Tab(translator.get("by_distance")),
                new Tab(translator.get("by_skills"))
        );

        MultiSelectListBox<VolunteerDTO> volunteersListBox = new MultiSelectListBox<>();
        volunteersListBox.setWidthFull();

        Span noVolunteersMessage = new Span(translator.get("no_available_volunteers"));
        noVolunteersMessage.setVisible(false);

        if (startDateTimePicker.isEmpty() || endDatePicker.isEmpty()) {
            Notification.show(translator.get("first_select_dates"),
                    3000, Notification.Position.MIDDLE);
            volunteerDialog.close();
            return volunteerDialog;
        }

        //More volunteers info
        volunteersListBox.setRenderer(getComponentVolunteerDTOComponentRenderer());
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

        volunteersListBox.select(selectedVolunteersList);
        if (volunteerMultiSelectComboBox.getSelectedItems().contains(translator.get("auto_select_volunteers"))) {
            volunteerCheckbox.setValue(true);
            volunteersListBox.setEnabled(false);
            volunteersQuantity.setEnabled(true);
            volunteersQuantity.setValue(selectedVolunteersList.size());
        }
        volunteerCheckbox.addClickListener(checkboxClickEvent -> {
            volunteersListBox.setEnabled(!volunteerCheckbox.getValue());
            volunteersQuantity.setEnabled(volunteerCheckbox.getValue());
        });

        dialogContent.add(autoSelectLayout,tabs, noVolunteersMessage, volunteersListBox);

        // Footer
        Button saveButton = new Button(translator.get("save_button"), e -> {
            if (volunteerCheckbox.getValue()) {
                volunteerMultiSelectComboBox.setItems(translator.get("auto_select_volunteers"));
                volunteerMultiSelectComboBox.select(translator.get("auto_select_volunteers"));
                nAutoSelectVolunteers = volunteersQuantity.getValue();

                selectedVolunteersList = allVolunteersList.stream()
                        .limit(volunteersQuantity.getValue())
                        .collect(Collectors.toList());

            } else {
                selectedVolunteersList = volunteersListBox.getSelectedItems().stream().toList();
                Set<String> selectedVolunteersNames = volunteersListBox.getSelectedItems().stream()
                        .map(VolunteerDTO::getFirstName)
                        .collect(Collectors.toSet());

                volunteerMultiSelectComboBox.clear();
                volunteerMultiSelectComboBox.setItems(selectedVolunteersNames);
                volunteerMultiSelectComboBox.select(selectedVolunteersNames);
            }
            volunteerDialog.close();
        });

        Button cancelButton = new Button(translator.get("cancel_button"), e -> {
            volunteersListBox.select(selectedVolunteersList);
            volunteersListBox.getSelectedItems().stream()
                    .filter(item -> !volunteerMultiSelectComboBox.getSelectedItems().contains(item.getFirstName()))
                    .forEach(volunteersListBox::deselect);
            if (volunteerMultiSelectComboBox.getSelectedItems().contains(translator.get("auto_select_volunteers"))) {
                volunteerCheckbox.setValue(true);
                volunteersListBox.setEnabled(false);
            } else {
                volunteerCheckbox.setValue(false);
                volunteersListBox.setEnabled(true);
            }

            volunteerDialog.close();
        });

        volunteerDialog.getFooter().add(cancelButton, saveButton);
        volunteerDialog.add(dialogContent);

        return volunteerDialog;
    }

    private static ComponentRenderer<Component, VolunteerDTO> getComponentVolunteerDTOComponentRenderer() {
        return new ComponentRenderer<>(volunteer -> {
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
            emailSpan.getStyle().set("color", "var(--placeholder-color)");

            infoLayout.add(nameSpan, emailSpan);

            Span availabilityBadge;
            if(volunteer.getAvailabilityStatus() > 0){
                availabilityBadge = new Span("Disponible");
                availabilityBadge.getStyle()
                        .set("background-color", "var(--lumo-success-color)")
                        .set("padding", "4px 8px")
                        .set("border-radius", "4px")
                        .set("font-size", "0.8em")
                        .set("font-weight", "bold")
                        .set("margin-left", "auto")
                        .set("color", "#F0F6FC");

                layout.add(infoLayout, availabilityBadge);

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
                        .set("padding", "4px 8px")
                        .set("border-radius", "4px")
                        .set("font-size", "0.8em")
                        .set("font-weight", "bold")
                        .set("margin-left", "auto")
                        .set("color", "#F0F6FC");

                layout.add(infoLayout, availabilityBadge);

                layout.getStyle()
                        .set("border-left", "4px solid var(--lumo-error-color)")
                        .set("padding", "8px")
                        .set("border-radius", "4px")
                        .set("background-color", "rgba(244, 67, 54, 0.1)");

            }
            return layout;
        });
    }

    protected Dialog getNeedsDialogContent() {
        Dialog needsDialog = new Dialog();

        needsDialog.setHeaderTitle(translator.get("select_needs"));

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);

        needsListBox.setItems(allNeedsWithoutTask);
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

    protected Dialog getConfirmationDialog() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle(translator.get("correct_task"));

        confirmDialog.add(
                new Span(translator.get("the_task") + " " + taskName.getValue() + " " + translator.get("successfully_added") +"\n" + translator.get("next_step"))
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

    //===============================Modify Preview=========================================
    protected void setupFormListeners() {
        taskName.addValueChangeListener(e ->
                updatePreview(e.getValue(), taskDescription.getValue(), taskPriority.getValue(), taskEmergency.getValue(), taskType, startDateTimePicker.getValue())
        );

        taskDescription.addValueChangeListener(e ->
                updatePreview(taskName.getValue(), e.getValue(), taskPriority.getValue(), taskEmergency.getValue(), taskType, startDateTimePicker.getValue()));

        taskPriority.addValueChangeListener(e ->
                updatePreview(taskName.getValue(), taskDescription.getValue(), e.getValue(), taskEmergency.getValue(), taskType, startDateTimePicker.getValue()));

        taskEmergency.addValueChangeListener(e ->
                updatePreview(taskName.getValue(), taskDescription.getValue(), taskPriority.getValue(), e.getValue(), taskType, startDateTimePicker.getValue()));

        startDateTimePicker.addValueChangeListener(e -> {
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
            updatePreview(taskName.getValue(), taskDescription.getValue(), taskPriority.getValue(), taskEmergency.getValue(), taskType, startDateTimePicker.getValue());
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
            taskPreview.updatePriority(formatService.formatPriority(priority));
        }

        if (emergencyLevel != null) {
            taskPreview.updateEmergencyLevel(formatService.formatEmergencyLevel(emergencyLevel));
        }

        if(taskType != null){
            taskPreview.updateTaskType(taskType);
        }

        if(date != null){
            taskPreview.updateDate(formatService.formatDateTime(date));
        }
    }

    //===============================Form validation=========================================
    protected boolean validateForm() {
        boolean isValid = true;

        if (taskName.isEmpty()) {
            System.out.println("Campo requerido incompleto: " + translator.get("preview_task_name"));
            isValid = false;
        }

        if (taskDescription.isEmpty()) {
            System.out.println("Campo requerido incompleto: " + translator.get("preview_task_description"));
            isValid = false;
        }

        if (startDateTimePicker.getValue() == null) {
            System.out.println("Campo requerido incompleto: " + translator.get("preview_start_date"));
            isValid = false;
        }

        if (endDatePicker.getValue() == null) {
            System.out.println("Campo requerido incompleto: " + translator.get("preview_end_date"));
            isValid = false;
        }

        if (taskPriority.getValue() == null) {
            System.out.println("Campo requerido incompleto: " + translator.get("preview_task_priority"));
            isValid = false;
        }

        if (taskEmergency.getValue() == null) {
            System.out.println("Campo requerido incompleto: " + translator.get("preview_task_emergency_level"));
            isValid = false;
        }

        if (needsMultiSelectComboBox.getSelectedItems().isEmpty()) {
            System.out.println("Campo requerido incompleto: " + translator.get("preview_needs"));
            isValid = false;
        }

        if (volunteerMultiSelectComboBox.getSelectedItems().isEmpty()) {
            System.out.println("Campo requerido incompleto: " + translator.get("preview_volunteers"));
            isValid = false;
        }

        if (taskLocation.isEmpty()) {
            System.out.println("Campo requerido incompleto: " + translator.get("preview_meeting_point"));
            isValid = false;
        }

        return isValid;
    }

    protected boolean isFormFilled() {
        return !taskName.isEmpty() ||
                !taskDescription.isEmpty() ||
                taskPriority.getValue() != null ||
                taskEmergency.getValue() != null ||
                needsMultiSelectComboBox.getValue() != null ||
                startDateTimePicker.getValue() != null ||
                !volunteerMultiSelectComboBox.getSelectedItems().isEmpty() ||
                !taskLocation.isEmpty();
    }

    protected void exitWithoutSavingDialog(String message) {
        if (isFormFilled()) {
            Dialog confirmDialog = new Dialog();
            confirmDialog.setHeaderTitle(translator.get("confirm_title"));

            VerticalLayout dialogContent = new VerticalLayout();
            dialogContent.add(new Span(message));

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

    //===============================Assigned Resources Methods=========================================
    @SuppressWarnings("unchecked")
    private List<ResourceAssignmentDTO> getAssignedResourcesFromSession() {
        List<ResourceAssignmentDTO> resources = (List<ResourceAssignmentDTO>)
                VaadinSession.getCurrent().getAttribute("assignedResources");

        if (resources == null) {
            resources = new ArrayList<>();
        }

        return resources;
    }

    protected void clearAssignedResourcesFromSession() {
        VaadinSession.getCurrent().setAttribute("assignedResources", null);
    }

    protected void saveAssignedResources(int taskId) {
        List<ResourceAssignmentDTO> resourceAssignments = getAssignedResourcesFromSession();
        if (!resourceAssignments.isEmpty()) {
            for (ResourceAssignmentDTO resourceAssignment : resourceAssignments) {
                try {
                    resourceAssignmentService.assignResourceToTask(
                            taskId,
                            resourceAssignment.getResourceId(),
                            resourceAssignment.getQuantity(),
                            resourceAssignment.getUnits());
                } catch (Exception e) {
                    System.out.println("Error al asignar recursos al tarea: " + e.getMessage());
                }
            }
        }
    }

    protected void saveAssignedResources(TaskDTO taskDTO) {
        if (taskDTO == null || taskDTO.getId() <= 0) {
            System.err.println("Error: TaskDTO es nulo o no tiene ID");
            return;
        }

        saveAssignedResources(taskDTO.getId());
    }
}