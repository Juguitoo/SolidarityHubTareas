package solidarityhub.frontend.views.task;

import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.model.Need;
import solidarityhub.frontend.model.Task;
import solidarityhub.frontend.model.Volunteer;
import solidarityhub.frontend.model.enums.*;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import solidarityhub.frontend.dto.VolunteerDTO;

@PageTitle("Añadir tarea")
@Route("addtask")
public class AddTaskView extends VerticalLayout {

    private final TaskService taskService;

    private final TaskComponent taskPreview;

    private final TextField taskName = new TextField("Nombre de la tarea");
    private final TextArea taskDescription = new TextArea("Descripción de la tarea");
    private final ComboBox<Priority> taskPriority = new ComboBox<>("Prioridad");
    private final ComboBox<EmergencyLevel> taskEmergency = new ComboBox<>("Nivel de emergencia");
    private final DateTimePicker starDateTimePicker = new DateTimePicker("Fecha y hora de comienzo");
    private final DateTimePicker endDateTimePicker = new DateTimePicker("Fecha y hora estimada de finalización");
    private final MultiSelectComboBox<String> volunteerMultiSelectComboBox = new MultiSelectComboBox<>("Voluntarios");
    private final MultiSelectComboBox<String> needsMultiSelectComboBox = new MultiSelectComboBox<>("Necesidades");

    public AddTaskView() {
        this.taskService = new TaskService();

        this.taskPreview = new TaskComponent(
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
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("")));
        backButton.addClassName("back-button");

        H1 title = new H1("Añadir tarea");
        title.addClassName("title");

        header.add(backButton, title);

        add(
            header,
            getPreview(),
            getTaskForm(),
            getButtons()
        );
    }

    private void saveNewTask(){
        if (validateForm()) {
            try {
                List<Need> selectdNeeds = needsMultiSelectComboBox.getSelectedItems().stream()
                        .map(this::stringToNeedType)
                        .map(needType -> new Need("", UrgencyLevel.MODERATE, needType, null, null))
                        .collect(Collectors.toList());

                List<Volunteer> selectedVolunteers = volunteerMultiSelectComboBox.getSelectedItems().stream()
                        .map(name -> {
                            if (name.equals("Elegir voluntarios automáticamente")) {
                                //Por hacer(Aplicar patron)
                                return new Volunteer("1234", "Automático", "", "");
                            }
                            return taskService.getVolunteers().stream()
                                    .filter(v -> v.getFirstName().equals(name))
                                    .findFirst()
                                    .map(dto -> new Volunteer(dto.getDni(), dto.getFirstName(), dto.getLastName(), dto.getEmail()))
                                    .orElse(null);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                // Crear el objeto TaskDTO
                Task newTask = new Task(
                        selectdNeeds,
                        taskName.getValue(),
                        taskDescription.getValue(),
                        starDateTimePicker.getValue(),
                        endDateTimePicker.getValue(),
                        taskPriority.getValue(),
                        Status.TO_DO,
                        selectedVolunteers
                );

                TaskDTO newTaskDTO = new TaskDTO(newTask);
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

    private Component getTaskForm(){
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

        addTaskForm.add(taskName, taskDescription, starDateTimePicker, taskPriority, getVolunteersForm(), endDateTimePicker, taskEmergency, getNeedsForm());

        addTaskForm.setColspan(taskDescription, 2);
        addTaskForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("750px", 3)
        );

        return addTaskForm;
    }

    private Component getVolunteersForm(){
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
        volunteerCheckbox.setLabel("Elegir voluntarios automaticamente");

        MultiSelectListBox<String> volunteersListBox = new MultiSelectListBox<>();
        volunteersListBox.setItems(taskService.getVolunteers().stream().map(VolunteerDTO::getFirstName).collect(Collectors.toList()));

        volunteerCheckbox.addClickListener(checkboxClickEvent -> volunteersListBox.setEnabled(!volunteerCheckbox.getValue()));

        dialogContent.add(volunteerCheckbox, volunteersListBox);

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
            }
            volunteerDialog.close();
        });
        volunteerDialog.getFooter().add(cancelButton);
        volunteerDialog.getFooter().add(saveButton);

        volunteerDialog.add(dialogContent);

        return volunteerDialog;
    }

    private Component getNeedsForm() {
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

        MultiSelectListBox<NeedType> needsListBox = new MultiSelectListBox<>();
        needsListBox.setItems(NeedType.values());

        // Usar el formatNeedType para mostrar los valores como strings legibles
        needsListBox.setItemLabelGenerator(this::formatNeedType);

        dialogContent.add(needsListBox);

        // Footer
        Button saveButton = new Button("Guardar", e -> {
            needsMultiSelectComboBox.clear();
            Set<String> selectedNeedsFormatted = needsListBox.getSelectedItems().stream()
                    .map(this::formatNeedType)
                    .collect(Collectors.toSet());
            needsMultiSelectComboBox.setItems(selectedNeedsFormatted);
            needsMultiSelectComboBox.select(selectedNeedsFormatted);
            needsDialog.close();
        });

        Button cancelButton = new Button("Cancelar", e -> needsDialog.close());

        needsDialog.getFooter().add(cancelButton, saveButton);
        needsDialog.add(dialogContent);

        return needsDialog;
    }

    private Component getButtons(){
        HorizontalLayout buttons = new HorizontalLayout();

        Button saveTaskButton = new Button("Guardar");
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

        buttons.add(saveTaskButton, cancel);
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
            getUI().ifPresent(ui -> ui.navigate(""));
        });

        confirmDialog.getFooter().add(addNewTaskButton, closeButton);
        return confirmDialog;
    }


    private String formatNeedType(NeedType needType) {
        return switch (needType) {
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
        };
    }

    private NeedType stringToNeedType(String formattedNeed) {
        return switch (formattedNeed) {
            case "Medica" -> NeedType.MEDICAL;
            case "Policía" -> NeedType.POLICE;
            case "Bomberos" -> NeedType.FIREFIGHTERS;
            case "Limpieza" -> NeedType.CLEANING;
            case "Alimentación" -> NeedType.FEED;
            case "Psicológica" -> NeedType.PSYCHOLOGICAL;
            case "Construcción" -> NeedType.BUILDING;
            case "Ropa" -> NeedType.CLOTHING;
            case "Refugio" -> NeedType.REFUGE;
            default -> NeedType.OTHER;
        };
    }

    private String formatDate(LocalDateTime taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    private String getEmergencyLevelString(EmergencyLevel level) {
        return switch (level) {
            case LOW -> "Baja";
            case MEDIUM -> "Media";
            case HIGH -> "Alta";
            case VERYHIGH -> "Muy alta";
        };
    }

    //===============================Validate Form=========================================
    private boolean validateForm() {
        return !taskName.isEmpty() &&
                !taskDescription.isEmpty() &&
                starDateTimePicker.getValue() != null &&
                endDateTimePicker.getValue() != null &&
                taskPriority.getValue() != null &&
                taskEmergency.getValue() != null &&
                !needsMultiSelectComboBox.getSelectedItems().isEmpty() &&
                !volunteerMultiSelectComboBox.getSelectedItems().isEmpty();
    }

    private boolean isFormFilled() {
        return !taskName.isEmpty() || !taskDescription.isEmpty() || taskPriority.getValue() != null || taskEmergency.getValue() != null || needsMultiSelectComboBox.getValue() != null || starDateTimePicker.getValue() != null || !volunteerMultiSelectComboBox.getSelectedItems().isEmpty();
    }

    //===============================Modify Preview=========================================
    private void setupFormListeners() {
        // Listener para el nombre de la tarea
        taskName.addValueChangeListener(e ->
                updatePreview(e.getValue(), taskDescription.getValue(),
                        taskPriority.getValue(), taskEmergency.getValue()));

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

    private void updatePreview(String name, String description, Priority priority, EmergencyLevel emergencyLevel) {

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

    private void updatePreviewDate(LocalDateTime date) {
        taskPreview.updateDate(formatDate(date));
    }

}
