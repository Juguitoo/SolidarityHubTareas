package com.example.application.views.task;

import com.example.application.model.enums.EmergencyLevel;
import com.example.application.model.enums.Priority;
import com.example.application.service.TaskService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

@PageTitle("Añadir tarea")
@Route("addtask")
public class AddTaskView extends VerticalLayout {

    private final TaskService taskService;

    private final TextField taskName = new TextField("Nombre de la tarea");
    private final TextArea taskDescription = new TextArea("Descripción de la tarea");
    private final ComboBox<Priority> taskPriority = new ComboBox<>("Prioridad");
    private final ComboBox<EmergencyLevel> taskEmergency = new ComboBox<>("Nivel de emergencia");
    private final ComboBox<String> taskNeed = new ComboBox<>("Necesidad");
    private final DateTimePicker dateTimePicker = new DateTimePicker("Fecha y hora de la tarea");
    private final MultiSelectComboBox<String> volunteerMultiSelectComboBox = new MultiSelectComboBox<>("Voluntarios");

    public AddTaskView() {
        this.taskService = new TaskService();

        //Header
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("header");

        Button backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("task")));
        backButton.addClassName("back-button");

        H1 title = new H1("Añadir tarea");
        title.addClassName("title");

        setAlignSelf(Alignment.CENTER, title, backButton);
        header.add(backButton, title);

        add(
            header,
            getPreview(),
            getTaskForm(),
            getButtons()
        );
    }

    private Component getPreview(){
        VerticalLayout preview = new VerticalLayout();
        preview.addClassName("previewContainer");

        H3 previewTitle = new H3("Previsualizción:");
        TaskComponent taskPreview = new TaskComponent(
            "Nombre de la tarea",
            "Descripccion de la tarea",
            formatDate(LocalDateTime.now()),
            "Prioridad",
            "Nivel de emergencia"
        );

        setAlignSelf(Alignment.CENTER, previewTitle, taskPreview);

        preview.add(previewTitle, taskPreview);
        return preview;
    }

    private Component getTaskForm(){
        FormLayout addTaskForm = new FormLayout();

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
        taskEmergency.setItemLabelGenerator(emergencyLevel -> switch (emergencyLevel) {
            case LOW -> "Bajo";
            case MEDIUM -> "Medio";
            case HIGH -> "Alto";
            case VERYHIGH -> "Muy alto";
        });
        taskEmergency.setRequiredIndicatorVisible(true);
        taskEmergency.setRequired(true);

        taskNeed.setItems("Baja", "Moderada", "Urgente");
        taskNeed.setRequiredIndicatorVisible(true);
        taskNeed.setRequired(true);

        dateTimePicker.setRequiredIndicatorVisible(true);

        addTaskForm.add(taskName, taskDescription, taskPriority, taskEmergency, taskNeed, dateTimePicker, getVolunteersForm());

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

        Dialog selectVolunteersDialog = getDialogContent();
        volunteerMultiSelectComboBox.getElement().addEventListener("click", e -> selectVolunteersDialog.open());

        return volunteerMultiSelectComboBox;
    }

//    private Component getNeedsForm(){
//        volunteerMultiSelectComboBox.setPlaceholder("Hacer clic para seleccionar");
//        volunteerMultiSelectComboBox.setReadOnly(true);
//        volunteerMultiSelectComboBox.setRequiredIndicatorVisible(true);
//        volunteerMultiSelectComboBox.setRequired(true);
//
//        Dialog selectVolunteersDialog = getDialogContent();
//        volunteerMultiSelectComboBox.getElement().addEventListener("click", e -> selectVolunteersDialog.open());
//
//        return volunteerMultiSelectComboBox;
//    }

    private Dialog getDialogContent() {
        Dialog volunteerDialog = new Dialog();

        //Header
        volunteerDialog.setHeaderTitle("Seleccione los voluntarios");

        VerticalLayout dialogContent = new VerticalLayout();

        Checkbox volunteerCheckbox = new Checkbox();
        volunteerCheckbox.setLabel("Elegir volutarios automaticamente");

        MultiSelectListBox<String> volunteersListBox = new MultiSelectListBox<>();
        volunteersListBox.setItems("Lorenzo Lopez", "Abel antonino", "Pedro Sanchez");

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


    private Component getButtons(){
        HorizontalLayout buttons = new HorizontalLayout();

        Button save = new Button("Guardar");
        save.addClickListener(e -> {
            // Save task
        });

        Button cancel = new Button("Cancelar");
        cancel.addClickListener(e -> {
            if (isFormFilled()) {
                Dialog confirmDialog = new Dialog();
                confirmDialog.setHeaderTitle("Confirmación");

                VerticalLayout dialogContent = new VerticalLayout();
                dialogContent.add(new Span("¿Está seguro de que desea cancelar? Los cambios no guardados se perderán."));

                Button confirmButton = new Button("Confirmar", event -> {
                    confirmDialog.close();
                    getUI().ifPresent(ui -> ui.navigate("task"));
                });
                confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                Button cancelButton = new Button("Cancelar", event -> confirmDialog.close());
                cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

                confirmDialog.getFooter().add(cancelButton, confirmButton);
                confirmDialog.add(dialogContent);
                confirmDialog.open();
            } else {
                getUI().ifPresent(ui -> ui.navigate("task"));
            }
        });

        buttons.add(save, cancel);
        setAlignSelf(Alignment.END, buttons);

        return buttons;
    }

    private boolean isFormFilled() {
        return !taskName.isEmpty() || !taskDescription.isEmpty() || taskPriority.getValue() != null || taskEmergency.getValue() != null || taskNeed.getValue() != null || dateTimePicker.getValue() != null || !volunteerMultiSelectComboBox.getSelectedItems().isEmpty();
    }

    private String formatDate(LocalDateTime taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}
