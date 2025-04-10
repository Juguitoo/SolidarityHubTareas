package solidarityhub.frontend.views.catastrophe;

import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.model.Catastrophe;
import solidarityhub.frontend.model.GPSCoordinates;
import solidarityhub.frontend.model.enums.EmergencyLevel;
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
public class AddCatastropheView extends VerticalLayout {

    private TextField nameField;
    private TextArea descriptionField;
    private DatePicker dateField;
    private NumberField locationXField;
    private NumberField locationYField;
    private ComboBox<EmergencyLevel> emergencyLevelComboBox;
    private Button saveButton;
    private Button cancelButton;

    private final CatastropheService catastropheService;

    public AddCatastropheView(CatastropheService catastropheService) {
        this.catastropheService = catastropheService;
        addClassName("add-catastrophe-view");

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        Div header = new Div();
        header.addClassName("header");

        Button backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("")));
        backButton.addClassName("back-button");

        H1 title = new H1("Añadir Catástrofe");
        title.addClassName("title");

        header.add(backButton, title);

        // Crear campos del formulario alineados con la estructura de la base de datos
        nameField = new TextField("Nombre de la catástrofe");
        nameField.setRequired(true);
        nameField.setPlaceholder("Terremoto en...");
        nameField.setMaxLength(255);
        nameField.setHelperText("Máximo 255 caracteres");

        descriptionField = new TextArea("Descripción");
        descriptionField.setPlaceholder("Información detallada sobre la catástrofe...");
        descriptionField.setHeight("150px");
        descriptionField.setMaxLength(255);
        descriptionField.setHelperText("Máximo 255 caracteres");

        dateField = new DatePicker("Fecha de inicio");
        dateField.setValue(LocalDate.now());
        dateField.setRequired(true);

        locationXField = new NumberField("Coordenada X (Longitud)");
        locationXField.setValue(0.0);
        locationXField.setStep(0.000001);
        locationXField.setStepButtonsVisible(true);
        locationXField.setHelperText("Para la ubicación geográfica");

        locationYField = new NumberField("Coordenada Y (Latitud)");
        locationYField.setValue(0.0);
        locationYField.setStep(0.000001);
        locationYField.setStepButtonsVisible(true);
        locationYField.setHelperText("Para la ubicación geográfica");

        emergencyLevelComboBox = new ComboBox<>("Nivel de emergencia");
        emergencyLevelComboBox.setItems(EmergencyLevel.LOW, EmergencyLevel.MEDIUM, EmergencyLevel.HIGH, EmergencyLevel.VERYHIGH);
        emergencyLevelComboBox.setValue(EmergencyLevel.MEDIUM);
        emergencyLevelComboBox.setRequired(true);
        emergencyLevelComboBox.setItemLabelGenerator(this::formatEmergencyLevel);

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

    private String formatEmergencyLevel(EmergencyLevel level) {
        return switch (level) {
            case LOW -> "Bajo";
            case MEDIUM -> "Medio";
            case HIGH -> "Alto";
            case VERYHIGH -> "Muy Alto";
            default -> level.toString();
        };
    }

    private void guardarCatastrofe() {
        // Validar campos requeridos
        if (nameField.isEmpty() || emergencyLevelComboBox.isEmpty() || dateField.isEmpty()) {
            Notification.show("Complete todos los campos obligatorios",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {

            GPSCoordinates location = new GPSCoordinates(locationYField.getValue(), locationXField.getValue());


            Catastrophe catastrophe = new Catastrophe(
                    nameField.getValue(),
                    descriptionField.getValue(),
                    location,
                    dateField.getValue(),
                    emergencyLevelComboBox.getValue()
            );

            // Crear el DTO a partir del objeto Catastrophe
            CatastropheDTO catastropheDTO = new CatastropheDTO(catastrophe);

            // Guardar la catástrofe usando el servicio
            CatastropheDTO savedCatastrophe = catastropheService.saveCatastrophe(catastropheDTO);

            // Mostrar notificación de éxito
            Notification.show("Catástrofe '" + nameField.getValue() +
                                    "' guardada correctamente",
                            3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Navegar de vuelta a la vista principal de catástrofes
            volver();

        } catch (Exception e) {

            Notification.show("Error al guardar la catástrofe: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);


            e.printStackTrace();
        }
    }

    private void volver() {

        getUI().ifPresent(ui -> ui.navigate(""));
    }

    private VerticalLayout createButtonLayout() {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setWidth("100%");
        buttonLayout.setPadding(true);
        buttonLayout.setSpacing(true);
        buttonLayout.addClassName("button-container");


        buttonLayout.add(saveButton, cancelButton);

        return buttonLayout;
    }
}