package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.dto.VolunteerDTO;
import solidarityhub.frontend.model.enums.EmergencyLevel;
import solidarityhub.frontend.model.enums.Priority;
import solidarityhub.frontend.model.enums.Status;
import solidarityhub.frontend.model.enums.TaskType;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.NeedService;
import solidarityhub.frontend.service.TaskService;
import solidarityhub.frontend.service.VolunteerService;
import solidarityhub.frontend.views.catastrophe.CatastropheSelectionView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Añadir tarea")
@Route("addtask")
public class AddTaskView extends VerticalLayout implements BeforeEnterObserver {

    private final TaskService taskService;
    private final VolunteerService volunteerService;
    private final NeedService needService;
    private final CatastropheService catastropheService;
    private CatastropheDTO selectedCatastrophe;

    private final TaskComponent taskPreview;

    private final TextField taskName = new TextField("Nombre de la tarea");
    private final TextArea taskDescription = new TextArea("Descripción de la tarea");
    private final ComboBox<Priority> taskPriority = new ComboBox<>("Prioridad");
    private final ComboBox<EmergencyLevel> taskEmergency = new ComboBox<>("Nivel de emergencia");
    private final DateTimePicker starDateTimePicker = new DateTimePicker("Fecha y hora de comienzo");
    private final DateTimePicker endDateTimePicker = new DateTimePicker("Fecha y hora estimada de finalización");
    private final MultiSelectComboBox<String> volunteerMultiSelectComboBox = new MultiSelectComboBox<>("Voluntarios");
    private final MultiSelectComboBox<String> needsMultiSelectComboBox = new MultiSelectComboBox<>("Necesidades");
    private final ComboBox<CatastropheDTO> catastropheComboBox = new ComboBox<>("Catástrofe");

    @Autowired
    public AddTaskView(TaskService taskService, CatastropheService catastropheService) {
        this.taskService = taskService;
        this.catastropheService = catastropheService;
        this.volunteerService = new VolunteerService();
        this.needService = new NeedService();

        this.taskPreview = new TaskComponent(
                1,"Nombre de la tarea",
                "Descripccion de la tarea",
                formatDate(LocalDateTime.now()),
                "Prioridad",
                "Nivel de emergencia"
        );
        taskPreview.enabledEditButton(false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Verificar si hay una catástrofe seleccionada
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        // Si no hay catástrofe seleccionada, redireccionar a la pantalla de selección
        if (selectedCatastrophe == null) {
            Notification.show("Por favor, selecciona una catástrofe primero",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            event.forwardTo(CatastropheSelectionView.class);
            return;
        }

        // Construir la vista con la catástrofe seleccionada
        buildView();
    }

    private void buildView() {
        removeAll();

        //Header
        Div header = new Div();
        header.addClassName("header");

        Button backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("tasks")));
        backButton.addClassName("back-button");

        H1 title = new H1("Añadir tarea para: " + selectedCatastrophe.getName());
        title.addClassName("title");

        header.add(backButton, title);

        add(
                header,
                getPreview(),
                getTaskForm(),
                getButtons()
        );
    }

    private void saveNewTask() {
        if (validateForm()) {
            try {
                List<String> selectedNeeds = needsMultiSelectComboBox.getSelectedItems().stream().toList();
                List<NeedDTO> needs = new ArrayList<>();
                for (String need : selectedNeeds) {
                    NeedDTO needDTO = needService.getNeeds().stream()
                            .filter(n -> n.getDescription().equals(need))
                            .findFirst()
                            .orElse(null);
                    if (needDTO != null) {
                        needs.add(needDTO);
                    }
                }

                List<VolunteerDTO> selectedVolunteers = volunteerMultiSelectComboBox.getSelectedItems().stream()
                        .map(name -> {
                            if (name.equals("Elegir voluntarios automáticamente")) {
                                //Por hacer(Aplicar patron)
                                return new VolunteerDTO();
                            }
                            return volunteerService.getVolunteers().stream()
                                    .filter(v -> v.getFirstName().equals(name))
                                    .findFirst()
                                    .orElse(null);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                // Crear la tarea con la catástrofe seleccionada
                TaskDTO newTaskDTO = new TaskDTO(
                        taskName.getValue(),
                        taskDescription.getValue(),
                        starDateTimePicker.getValue(),
                        endDateTimePicker.getValue(),
                        needs.isEmpty() ? TaskType.OTHER : needs.get(0).getTaskType(),
                        taskPriority.getValue(),
                        taskEmergency.getValue(),
                        Status.TO_DO,
                        needs,
                        selectedVolunteers,
                        selectedCatastrophe.getId()  // Usar el ID de la catástrofe seleccionada
                );

                taskService.addTask(newTaskDTO);
                getConfirmationDialog().open();

            } catch (Exception ex) {
                String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Error desconocido";
                Notification.show("Error al guardar la tarea: " + errorMessage,
                        5000, Notification.Position.MIDDLE);
                ex.printStackTrace();
            }
        } else {
            Notification.show("Por favor, complete todos los campos obligatorios",
                    3000, Notification.Position.MIDDLE);
        }
    }

    //===============================Get Components=========================================
    private Component getPreview() {
        VerticalLayout preview = new VerticalLayout();
        preview.addClassName("previewContainer");

        H3 previewTitle = new H3("Previsualización:");

        setAlignSelf(Alignment.CENTER, previewTitle, taskPreview);

        setupFormListeners();

        preview.add(previewTitle, taskPreview);
        return preview;
    }

    private Component getTaskForm() {
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

        // Configurar campo de catástrofe
        catastropheComboBox.setItems(Collections.singletonList(selectedCatastrophe));
        catastropheComboBox.setValue(selectedCatastrophe);
        catastropheComboBox.setItemLabelGenerator(CatastropheDTO::getName);
        catastropheComboBox.setReadOnly(true); // No permitir cambiar la catástrofe
        catastropheComboBox.setHelperText("Catástrofe seleccionada");

        addTaskForm.add(
                taskName,
                taskDescription,
                catastropheComboBox,
                starDateTimePicker,
                taskPriority,
                getVolunteersForm(),
                endDateTimePicker,
                taskEmergency,
                getNeedsForm()
        );

        addTaskForm.setColspan(taskDescription, 2);
        addTaskForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("750px", 3)
        );

        return addTaskForm;
    }

    private Component getVolunteersForm() {
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
        volunteersListBox.setItems(volunteerService.getVolunteers().stream().map(VolunteerDTO::getFirstName).collect(Collectors.toList()));

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

        // Filtrar necesidades por la catástrofe seleccionada
        List<NeedDTO> filteredNeeds = needService.getNeeds().stream()
                .filter(n -> n.getTaskId() == -1) // Solo necesidades sin tarea asignada
                .filter(n -> n.getCatastropheId() == selectedCatastrophe.getId()) // Solo de la catástrofe seleccionada
                .collect(Collectors.toList());

        MultiSelectListBox<String> needsListBox = new MultiSelectListBox<>();
        if (filteredNeeds.isEmpty()) {
            needsListBox.setItems("No hay necesidades disponibles para esta catástrofe");
            needsListBox.setEnabled(false);
        } else {
            needsListBox.setItems(filteredNeeds.stream()
                    .map(NeedDTO::getDescription)
                    .collect(Collectors.toList()));
        }

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

    private Component getButtons() {
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
        });

        buttons.add(saveTaskButton, cancel);
        setAlignSelf(Alignment.END, buttons);

        return buttons;
    }

    private Dialog getConfirmationDialog() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Tarea creada con éxito");

        confirmDialog.add(
                new Span("La tarea " + taskName.getValue() + " ha sido creada correctamente.\n" + "¿Qué desea hacer?")
        );

        Button addNewTaskButton = new Button("Crear otra tarea", e -> {
            confirmDialog.close();
            getUI().ifPresent(ui -> ui.navigate("addtask"));
        });

        Button closeButton = new Button("Volver a tareas", e -> {
            confirmDialog.close();
            getUI().ifPresent(ui -> ui.navigate("tasks"));
        });

        confirmDialog.getFooter().add(addNewTaskButton, closeButton);
        return confirmDialog;
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

    private String formatDate(LocalDateTime taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}