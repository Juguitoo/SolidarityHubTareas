package com.example.application.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
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
public class addTaskView extends VerticalLayout {

    MultiSelectComboBox<String> volunteerMultiSelectComboBox = new MultiSelectComboBox<>("Voluntarios");

    public addTaskView() {
        H1 title = new H1("Añadir tarea");

        setAlignSelf(Alignment.CENTER, title);

        add(
                title,
                getPreview(),
                getTaskForm(),
                getButtons());
    }

    private Component getPreview(){
        VerticalLayout preview = new VerticalLayout();

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

        TextField taskName = new TextField("Nombre de la tarea");
        taskName.setRequiredIndicatorVisible(true);

        TextArea taskDescription = new TextArea("Descripción de la tarea");
        taskDescription.setRequiredIndicatorVisible(true);

        ComboBox<String> taskPriority = new ComboBox<String>("Prioridad");
        taskPriority.setItems("Baja", "Moderada", "Urgente");
        taskPriority.setRequiredIndicatorVisible(true);

        ComboBox<String> taskEmergency = new ComboBox<String>("Nivel de emergencia");
        taskEmergency.setItems("Baja", "Media", "Alta", "Muy alta");
        taskEmergency.setRequiredIndicatorVisible(true);

        ComboBox<String> taskNeed = new ComboBox<String>("Necesidad");
        taskNeed.setItems("Baja", "Moderada", "Urgente");
        taskNeed.setRequiredIndicatorVisible(true);

        DateTimePicker dateTimePicker = new DateTimePicker();
        dateTimePicker.setLabel("Fecha y hora de la tarea");
        dateTimePicker.setRequiredIndicatorVisible(true);



        addTaskForm.add(taskName, taskDescription, taskPriority, taskEmergency, taskNeed, dateTimePicker, getVolunteersForm());

        addTaskForm.setResponsiveSteps(
                // Use one column by default
                new FormLayout.ResponsiveStep("0", 1),
                // Use two columns, if layout's width exceeds 500px
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("750px", 3)
        );

        return addTaskForm;
    }

    private Component getVolunteersForm(){
        volunteerMultiSelectComboBox.setPlaceholder("Hacer clic para seleccionar");
        volunteerMultiSelectComboBox.setReadOnly(true);
        volunteerMultiSelectComboBox.setRequiredIndicatorVisible(true);

        Dialog selectVolunteersDialog = getDialogContent();
        volunteerMultiSelectComboBox.getElement().addEventListener("click", e -> {
            selectVolunteersDialog.open();
        });

        return volunteerMultiSelectComboBox;
    }

    private Dialog getDialogContent() {
        Dialog volunteerDialog = new Dialog();

        //Header
        volunteerDialog.setHeaderTitle("Seleccione los voluntarios");

        VerticalLayout dialogContent = new VerticalLayout();

        Checkbox volunteerCheckbox = new Checkbox();
        volunteerCheckbox.setLabel("Elegir volutarios automaticamente");

        MultiSelectListBox<String> volunteersListBox = new MultiSelectListBox<>();
        volunteersListBox.setItems("Lorenzo Lopez", "Abel antonino", "Pedro Sanchez");

        volunteerCheckbox.addClickListener(checkboxClickEvent -> {
            volunteersListBox.setEnabled(!volunteerCheckbox.getValue());
        });

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
        Button cancelButton = new Button("Cancelar", e -> volunteerDialog.close());
        volunteerDialog.getFooter().add(cancelButton);
        volunteerDialog.getFooter().add(saveButton);

        volunteerDialog.add(dialogContent);

        volunteerMultiSelectComboBox.getElement().addEventListener("click", e -> {
            volunteerDialog.open();
        });

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
            // Cancel task
        });

        buttons.add(save, cancel);
        setAlignSelf(Alignment.END, buttons);

        return buttons;
    }

    private String formatDate(LocalDateTime taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}
