package com.example.application.views.catastrophe;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Route("add-catastrophe")
@PageTitle("Añadir Catástrofe")
public class AddCatastrophe extends VerticalLayout {

    private TextField nameField;
    private TextArea descriptionField;
    private DatePicker dateField;
    private TextField imageUrlField;
    private Button saveButton;
    private Button cancelButton;

    public AddCatastrophe() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        //Header
        Div header = new Div();
        header.addClassName("header");

        Button backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("")));
        backButton.addClassName("back-button");

        H1 title = new H1("Añadir Catástrofe");
        title.addClassName("title");

        header.add(backButton, title);

        // Crear campos del formulario
        nameField = new TextField("Nombre de la catástrofe");
        nameField.setRequired(true);
        nameField.setPlaceholder("Terremoto en...");

        descriptionField = new TextArea("Descripción");
        descriptionField.setPlaceholder("Información detallada sobre la catástrofe...");
        descriptionField.setHeight("150px");

        dateField = new DatePicker("Fecha");
        dateField.setValue(LocalDate.now());

        imageUrlField = new TextField("URL de la imagen");
        imageUrlField.setPlaceholder("https://ejemplo.com/imagen.jpg");

        // Crear botones
        saveButton = new Button("Guardar");
        saveButton.addClassName("search-button");
        saveButton.addClickListener(e -> guardarCatastrofe());

        cancelButton = new Button("Cancelar");
        cancelButton.addClassName("back-button");
        cancelButton.addClickListener(e -> volver());

        // Crear layout de formulario
        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, descriptionField, dateField, imageUrlField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(descriptionField, 2);

        // Añadir componentes al layout principal
        add(header, formLayout, createButtonLayout());
    }

    private void guardarCatastrofe() {
        // Aquí iría la lógica para guardar la catástrofe
        // Por ejemplo, llamar a un servicio o repositorio

        // Formatear la fecha a String
        String formattedDate = dateField.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Mostrar notificación o redirigir
        volver();
    }

    private void volver() {
        // Navegar de vuelta a la vista principal de catástrofes
        getUI().ifPresent(ui -> ui.navigate("catastrophes"));
    }

    private VerticalLayout createButtonLayout() {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setWidth("100%");
        buttonLayout.setPadding(true);
        buttonLayout.setSpacing(true);

        // Añadir botones al layout
        buttonLayout.add(saveButton, cancelButton);

        return buttonLayout;
    }
}
