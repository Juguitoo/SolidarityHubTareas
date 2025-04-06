package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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

    protected final TaskComponent taskPreview;

    protected final TextField taskName = new TextField("Nombre de la tarea");
    protected final TextArea taskDescription = new TextArea("Descripción de la tarea");
    protected final ComboBox<Priority> taskPriority = new ComboBox<>("Prioridad");
    protected final ComboBox<EmergencyLevel> taskEmergency = new ComboBox<>("Nivel de emergencia");
    protected final DateTimePicker starDateTimePicker = new DateTimePicker("Fecha y hora de comienzo");
    protected final DateTimePicker endDateTimePicker = new DateTimePicker("Fecha y hora estimada de finalización");
    protected final MultiSelectComboBox<String> volunteerMultiSelectComboBox = new MultiSelectComboBox<>("Voluntarios");
    protected final MultiSelectComboBox<String> needsMultiSelectComboBox = new MultiSelectComboBox<>("Necesidades");

    @Autowired
    public AddTaskView(TaskService taskService) {
        this.taskService = taskService;
        this.volunteerService = new VolunteerService();
        this.needService = new NeedService();
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        this.taskPreview = new TaskComponent(
                0,
                "Nombre de la tarea",
                "Descripccion de la tarea",
                formatDate(LocalDateTime.now()),
                "Prioridad",
                "Nivel de emergencia"
        );
        taskPreview.enabledEditButton(false);

        //Header
        Div header = new Div();
        header.addClassName("header");

        Button backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("tasks")));
        backButton.addClassName("back-button");

        H1 title = new H1("Añadir tarea");
        title.addClassName("title");

        header.add(backButton, title);

        add(
            header,
            getPreview(),
            getAddTaskForms(),
            getButtons()
        );
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


    //===============================Get Components=========================================
    private Component getPreview(){
        VerticalLayout preview = new VerticalLayout();
        preview.addClassName("previewContainer");

        H3 previewTitle = new H3("Previsualización:");

        setAlignSelf(Alignment.CENTER, previewTitle, taskPreview);

        setupFormListeners();

        preview.add(previewTitle, taskPreview);
        return preview;
    }

    private Component getAddTaskForms(){
        FormLayout addTaskForm = new FormLayout();
        addTaskForm.addClassName("addTaskForm");

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
        endDateTimePicker.setRequiredIndicatorVisible(true);

        addTaskForm.add(taskName, taskDescription, starDateTimePicker, taskPriority, getNeedsForm(), endDateTimePicker, taskEmergency, getVolunteersForm());

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

        Dialog selectVolunteersDialog = getVolunteersDialogContent();
        volunteerMultiSelectComboBox.getElement().addEventListener("click", e -> selectVolunteersDialog.open());

        return volunteerMultiSelectComboBox;
    }

    private Dialog getVolunteersDialogContent() {
        Dialog volunteerDialog = new Dialog();
        volunteerDialog.setHeaderTitle("Seleccione los voluntarios");

        VerticalLayout dialogContent = new VerticalLayout();

        Checkbox volunteerCheckbox = new Checkbox();
        volunteerCheckbox.setLabel("Elegir voluntarios automáticamente");

        // Obtener los nombres de voluntarios
        List<String> allVolunteers = volunteerService.getVolunteers("None", new TaskDTO()).stream()
                .map(VolunteerDTO::getFirstName)
                .collect(Collectors.toList());
        List<String> volunteerNames = allVolunteers;


        MultiSelectListBox<String> volunteersListBox = new MultiSelectListBox<>();
        volunteersListBox.setItems(volunteerNames);

        Tabs tabs = new Tabs(
                new Tab("Todos"),
                new Tab("Distancia"),
                new Tab("Disponibilidad"),
                new Tab("Habilidades")
        );

        tabs.setSelectedIndex(0);

        // Listener para el cambio de pestañas
        tabs.addSelectedChangeListener(event -> {
            String selectedTabName = tabs.getSelectedTab().getLabel();
            switch (selectedTabName) {
                case "Todos":
                    volunteerNames.clear();
                    volunteerService.getVolunteers("None", getTaskDTO()).forEach(v -> {
                        volunteerNames.add(v.getFirstName());
                    });
                    volunteersListBox.setItems(allVolunteers);
                    break;
                case "Distancia":
                    if(needsMultiSelectComboBox.getSelectedItems().isEmpty()){
                        Notification.show("Por favor, seleccione una necesidad primero para poder calcular las distancias.", 3000, Notification.Position.MIDDLE);
                        tabs.setSelectedTab(tabs.getTabAt(0));
                        return;
                    }else{
                        volunteerNames.clear();
                        volunteerService.getVolunteers("distancia", getTaskDTO()).forEach(v -> {
                            volunteerNames.add(v.getFirstName());
                        });
                        volunteersListBox.setItems(volunteerNames);
                    }
                    break;
                case "Disponibilidad":
                    if(starDateTimePicker.isEmpty() || endDateTimePicker.isEmpty()){
                        Notification.show("Por favor, seleccione primero una fecha de inicio y de fin de la tarea.", 3000, Notification.Position.MIDDLE);
                        tabs.setSelectedTab(tabs.getTabAt(0));
                        return;
                    }else{
                        volunteerNames.clear();
                        volunteerService.getVolunteers("disponibilidad", getTaskDTO()).forEach(v -> {
                            volunteerNames.add(v.getFirstName());
                        });
                        volunteersListBox.setItems(volunteerNames);
                    }
                    break;
                case "Habilidades":
                    if (needsMultiSelectComboBox.getSelectedItems().isEmpty()) {
                        Notification.show("Por favor, seleccione una necesidad primero para indicar el tipo de la tarea.", 3000, Notification.Position.MIDDLE);
                        tabs.setSelectedTab(tabs.getTabAt(0));
                        return;
                    }else{
                        volunteerNames.clear();
                        volunteerService.getVolunteers("habilidades", getTaskDTO()).forEach(v -> {
                            volunteerNames.add(v.getFirstName());
                        });
                        volunteersListBox.setItems(volunteerNames);
                    }

                    break;
            }
        });

        volunteerCheckbox.addClickListener(checkboxClickEvent -> volunteersListBox.setEnabled(!volunteerCheckbox.getValue()));

        // Añadir todos los componentes
        dialogContent.add(volunteerCheckbox, tabs, volunteersListBox);

        //Footer
        Button saveButton = new Button("Guardar", e -> {
            if (volunteerCheckbox.getValue()) {
                volunteerMultiSelectComboBox.setItems("Elegir voluntarios automáticamente");
                volunteerMultiSelectComboBox.select("Elegir voluntarios automáticamente");
            } else {
                volunteerMultiSelectComboBox.setItems(volunteersListBox.getSelectedItems());
                volunteerMultiSelectComboBox.updateSelection(volunteersListBox.getSelectedItems(), new HashSet<>());
            }
            volunteerDialog.close();
        });

        Button cancelButton = new Button("Cancelar", e -> {
            volunteersListBox.select(volunteerMultiSelectComboBox.getSelectedItems());
            volunteersListBox.getSelectedItems().stream()
                    .filter(item -> !volunteerMultiSelectComboBox.getSelectedItems().contains(item))
                    .forEach(volunteersListBox::deselect);
            if (volunteerMultiSelectComboBox.getSelectedItems().contains("Elegir voluntarios automáticamente")) {
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

    protected Component getNeedsForm() {
        needsMultiSelectComboBox.setPlaceholder("Hacer clic para seleccionar");
        needsMultiSelectComboBox.setReadOnly(true);
        needsMultiSelectComboBox.setRequiredIndicatorVisible(true);
        needsMultiSelectComboBox.setRequired(true);

        Dialog selectNeedsDialog = getNeedsDialogContent();
        needsMultiSelectComboBox.getElement().addEventListener("click", e -> selectNeedsDialog.open());

        return needsMultiSelectComboBox;
    }

    private Dialog getNeedsDialogContent() {
        Dialog needsDialog = new Dialog();

        needsDialog.setHeaderTitle("Seleccione las necesidades a cubrir");

        VerticalLayout dialogContent = new VerticalLayout();

        MultiSelectListBox<String> needsListBox = new MultiSelectListBox<>();
        needsListBox.setItems(needService.getNeeds().stream().filter(n -> n.getTaskId()==-1).map(NeedDTO::getDescription).collect(Collectors.toList()));

        dialogContent.add(needsListBox);

        // Footer
        Button saveButton = new Button("Guardar", e -> {
            needsMultiSelectComboBox.clear();
            Set<String> selectedNeeds = needsListBox.getSelectedItems();
            needsMultiSelectComboBox.setItems(selectedNeeds);
            needsMultiSelectComboBox.select(selectedNeeds);
            needsDialog.close();
        });

        Button cancelButton = new Button("Cancelar", e -> needsDialog.close());

        needsDialog.getFooter().add(cancelButton, saveButton);
        needsDialog.add(dialogContent);

        return needsDialog;
    }

    protected Component getButtons(){
        HorizontalLayout buttons = new HorizontalLayout();

        Button saveTaskButton = new Button("Añadir");
        saveTaskButton.addClickListener(e -> saveNewTask());

        Button cancel = new Button("Cancelar");
        cancel.addClickListener(e -> {
            if (isFormFilled()) {
                Dialog confirmDialog = new Dialog();
                confirmDialog.setHeaderTitle("Confirmación");

                VerticalLayout dialogContent = new VerticalLayout();
                dialogContent.add(new Span("¿Está seguro de que desea cancelar? Los cambios no guardados se perderán."));

                Button confirmButton = new Button("Confirmar", event -> {
                    confirmDialog.close();
                    getUI().ifPresent(ui -> ui.navigate(""));
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
        });

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

    protected String formatDate(LocalDateTime taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    protected String getEmergencyLevelString(EmergencyLevel level) {
        return switch (level) {
            case LOW -> "Baja";
            case MEDIUM -> "Media";
            case HIGH -> "Alta";
            case VERYHIGH -> "Muy alta";
        };
    }

    //===============================Validate Form=========================================
    protected boolean validateForm() {
        return !taskName.isEmpty() &&
                !taskDescription.isEmpty() &&
                starDateTimePicker.getValue() != null &&
                endDateTimePicker.getValue() != null &&
                taskPriority.getValue() != null &&
                taskEmergency.getValue() != null &&
                !needsMultiSelectComboBox.getSelectedItems().isEmpty() &&
                !volunteerMultiSelectComboBox.getSelectedItems().isEmpty();
    }

    protected boolean isFormFilled() {
        return !taskName.isEmpty() || !taskDescription.isEmpty() || taskPriority.getValue() != null || taskEmergency.getValue() != null || needsMultiSelectComboBox.getValue() != null || starDateTimePicker.getValue() != null || !volunteerMultiSelectComboBox.getSelectedItems().isEmpty();
    }

    //===============================Modify Preview=========================================
    protected void setupFormListeners() {
        // Listener para el nombre de la tarea
        taskName.addValueChangeListener(e ->
                updatePreview(e.getValue(), taskDescription.getValue(),
                        taskPriority.getValue(), taskEmergency.getValue())
        );

        // Listener para la descripción
        taskDescription.addValueChangeListener(e ->
                updatePreview(taskName.getValue(), e.getValue(),
                        taskPriority.getValue(), taskEmergency.getValue()));

        // Listener para la prioridad
        taskPriority.addValueChangeListener(e ->
                updatePreview(taskName.getValue(), taskDescription.getValue(),
                        e.getValue(), taskEmergency.getValue()));

        // Listener para el nivel de emergencia
        taskEmergency.addValueChangeListener(e ->
                updatePreview(taskName.getValue(), taskDescription.getValue(),
                        taskPriority.getValue(), e.getValue()));

        // Listener para la fecha
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

    private TaskDTO getTaskDTO() {
        List<String> selectedNeeds = needsMultiSelectComboBox.getSelectedItems().stream().toList();
        List<NeedDTO> needs = new ArrayList<>();
        for (String need : selectedNeeds) {
            needService.getNeeds().stream()
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
        return new TaskDTO(taskName.getValue(), taskDescription.getValue(), starDateTimePicker.getValue(), endDateTimePicker.getValue(),
                taskType, taskPriority.getValue(), taskEmergency.getValue(), Status.TO_DO, needs, selectedVolunteers, selectedCatastrophe.getId());
    }

}
