package solidarityhub.frontend.views.catastrophe;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.model.Catastrophe;
import solidarityhub.frontend.model.GPSCoordinates;
import org.pingu.domain.enums.EmergencyLevel;
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
import java.util.Locale;


@Route("add-catastrophe")
@PageTitle("Añadir Catástrofe")
public class AddCatastropheView extends VerticalLayout {
    private static Translator translator;

    private final TextField nameField;
    private final TextArea descriptionField;
    private final DatePicker dateField;
    private final NumberField locationXField;
    private final NumberField locationYField;
    private final ComboBox<EmergencyLevel> emergencyLevelComboBox;
    private final Button saveButton;
    private final Button cancelButton;

    private final CatastropheService catastropheService;

    public AddCatastropheView(CatastropheService catastropheService) {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        }else{
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());

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

        H1 title = new H1(translator.get("add_catastrophe_title"));
        title.addClassName("title");

        header.add(backButton, title);

        // Crear campos del formulario alineados con la estructura de la base de datos
        nameField = new TextField(translator.get("add_catastrophe_name"));
        nameField.setRequired(true);
        nameField.setPlaceholder(translator.get("add_catastrophe_name_placeholder"));
        nameField.setMaxLength(255);
        nameField.setHelperText(translator.get("add_catastrophe_text_helper"));

        descriptionField = new TextArea(translator.get("add_catastrophe_description"));
        descriptionField.setPlaceholder(translator.get("add_catastrophe_description_placeholder"));
        descriptionField.setHeight("150px");
        descriptionField.setMaxLength(255);
        descriptionField.setHelperText(translator.get("add_catastrophe_text_helper"));

        dateField = new DatePicker(translator.get("add_catastrophe_start_date"));
        dateField.setValue(LocalDate.now());
        dateField.setRequired(true);

        locationXField = new NumberField(translator.get("add_catastrophe_coordX"));
        locationXField.setValue(0.0);
        locationXField.setStep(0.000001);
        locationXField.setStepButtonsVisible(true);
        locationXField.setHelperText(translator.get("add_catastrophe_coords_helper"));

        locationYField = new NumberField(translator.get("add_catastrophe_coordY"));
        locationYField.setValue(0.0);
        locationYField.setStep(0.000001);
        locationYField.setStepButtonsVisible(true);
        locationYField.setHelperText(translator.get("add_catastrophe_coords_helper"));

        emergencyLevelComboBox = new ComboBox<>(translator.get("add_catastrophe_emergency_level"));
        emergencyLevelComboBox.setItems(EmergencyLevel.LOW, EmergencyLevel.MEDIUM, EmergencyLevel.HIGH, EmergencyLevel.VERYHIGH);
        emergencyLevelComboBox.setValue(EmergencyLevel.MEDIUM);
        emergencyLevelComboBox.setRequired(true);
        emergencyLevelComboBox.setItemLabelGenerator(this::formatEmergencyLevel);

        // Crear botones
        saveButton = new Button(translator.get("add_catastrophe_save_button"));
        saveButton.addClassName("search-button");
        saveButton.addClickListener(e -> guardarCatastrofe());

        cancelButton = new Button(translator.get("add_catastrophe_cancel_button"));
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
            case LOW -> translator.get("low_emergency_level");
            case MEDIUM -> translator.get("medium_emergency_level");
            case HIGH -> translator.get("high_emergency_level");
            case VERYHIGH -> translator.get("very_high_emergency_level");
        };
    }

    private void guardarCatastrofe() {
        // Validar campos requeridos
        if (nameField.isEmpty() || emergencyLevelComboBox.isEmpty() || dateField.isEmpty()) {
            Notification.show(translator.get("check_fields"),
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
            Notification.show(translator.get("catastrophe") + " '" + nameField.getValue() +
                                    translator.get("correctly_added_catastrophe"),
                            3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Navegar de vuelta a la vista principal de catástrofes
            volver();

        } catch (Exception e) {

            Notification.show(translator.get("error_adding_catastrophe") + e.getMessage(),
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