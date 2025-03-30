package solidarityhub.frontend.views.catastrophe;

import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.service.CatastropheService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;

@Route("add-catastrophe")
@PageTitle("Añadir Catástrofe")
public class AddCatastrophe extends VerticalLayout {

    private TextField nameField;
    private TextArea descriptionField;
    private DatePicker dateField;
    private NumberField locationXField;
    private NumberField locationYField;
    private ComboBox<String> emergencyLevelComboBox;
    private Button saveButton;
    private Button cancelButton;

    private final CatastropheService catastropheService;

    public AddCatastrophe(CatastropheService catastropheService) {
        this.catastropheService = catastropheService;
        addClassName("add-catastrophe-view");

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // [El resto de la configuración del formulario sigue igual]
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

        dateField = new DatePicker("Fecha de inicio");
        dateField.setValue(LocalDate.now());

        locationXField = new NumberField("Coordenada X (Longitud)");
        locationXField.setValue(0.0);
        locationXField.setStep(0.000001);
        locationXField.setStepButtonsVisible(true);

        locationYField = new NumberField("Coordenada Y (Latitud)");
        locationYField.setValue(0.0);
        locationYField.setStep(0.000001);
        locationYField.setStepButtonsVisible(true);

        emergencyLevelComboBox = new ComboBox<>("Nivel de emergencia");
        emergencyLevelComboBox.setItems("LOW", "MEDIUM", "HIGH", "CRITICAL");
        emergencyLevelComboBox.setValue("MEDIUM");

        // Crear botones
        saveButton = new Button("Guardar");
        saveButton.addClassName("search-button");
        saveButton.addClickListener(e -> guardarCatastrofe());

        cancelButton = new Button("Cancelar");
        cancelButton.addClassName("back-button");
        cancelButton.addClickListener(e -> volver());

        // Crear layout de formulario
        FormLayout formLayout = new FormLayout();
        formLayout.add(
                nameField, descriptionField, dateField,
                locationXField, locationYField, emergencyLevelComboBox
        );
        formLayout.addClassName("form-layout");
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(descriptionField, 2);

        // Añadir componentes al layout principal
        add(header, formLayout, createButtonLayout());
    }

    private void guardarCatastrofe() {
        // Validar campos requeridos
        if (nameField.isEmpty()) {
            Notification.show("El nombre de la catástrofe es obligatorio",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            // Crear el DTO con los datos del formulario
            CatastropheDTO catastropheDTO = new CatastropheDTO(
                    nameField.getValue(),
                    descriptionField.getValue(),
                    locationXField.getValue(),
                    locationYField.getValue(),
                    dateField.getValue(),
                    emergencyLevelComboBox.getValue()
            );

            // Guardar la catástrofe usando el servicio
            CatastropheDTO savedCatastrophe = catastropheService.saveCatastrophe(catastropheDTO);

            // Mostrar notificación de éxito
            Notification.show("Catástrofe '" + savedCatastrophe.getName() +
                                    "' guardada correctamente",
                            3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Navegar de vuelta a la vista principal de catástrofes
            volver();

        } catch (Exception e) {
            // Manejar errores
            Notification.show("Error al guardar la catástrofe: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);

            // Loguear el error para depuración
            e.printStackTrace();
        }
    }

    private void volver() {
        // Navegar de vuelta a la vista principal de catástrofes
        getUI().ifPresent(ui -> ui.navigate("catastrophe"));
    }

    private VerticalLayout createButtonLayout() {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setWidth("100%");
        buttonLayout.setPadding(true);
        buttonLayout.setSpacing(true);
        buttonLayout.addClassName("button-container");

        // Añadir botones al layout
        buttonLayout.add(saveButton, cancelButton);

        return buttonLayout;
    }
}
