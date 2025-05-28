package solidarityhub.frontend.views.catastrophe;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import solidarityhub.frontend.views.HeaderComponent;

import java.time.LocalDate;
import java.util.Locale;


@Route("add-catastrophe")
@PageTitle("Añadir Catástrofe")
public class AddCatastropheView extends VerticalLayout {
    private static Translator translator = new Translator();

    private TextField nameField;
    private TextArea descriptionField;
    private DatePicker dateField;
    private NumberField locationXField;
    private NumberField locationYField;
    private ComboBox<EmergencyLevel> emergencyLevelComboBox;

    private final CatastropheService catastropheService;

    public AddCatastropheView() {
        this.catastropheService = new CatastropheService();

        translator.initializeTranslator();

        buildView();
    }

    protected void buildView() {
        addClassName("add-catastrophe-view");
        setSizeFull();

        HeaderComponent header = new HeaderComponent(translator.get("add_catastrophe_title"), "window.history.back()");

        add(header, getForms(), getButtons());
    }

    private void saveCatastrophe() {
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
            goBack();

        } catch (Exception e) {

            Notification.show(translator.get("error_adding_catastrophe") + e.getMessage(),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);


            e.printStackTrace();
        }
    }

    //===============================Get Components=========================================
    private Component getForms(){
        FormLayout formLayout = new FormLayout();
        formLayout.addClassName("form-layout");

        nameField = new TextField(translator.get("add_catastrophe_name"));
        nameField.setPlaceholder(translator.get("add_catastrophe_name_placeholder"));
        nameField.setRequired(true);

        descriptionField = new TextArea(translator.get("add_catastrophe_description"));
        descriptionField.setPlaceholder(translator.get("add_catastrophe_description_placeholder"));

        dateField = new DatePicker(translator.get("add_catastrophe_start_date"));
        dateField.setValue(LocalDate.now());
        dateField.setRequired(true);

        emergencyLevelComboBox = new ComboBox<>(translator.get("add_catastrophe_emergency_level"));
        emergencyLevelComboBox.setItems(EmergencyLevel.LOW, EmergencyLevel.MEDIUM, EmergencyLevel.HIGH, EmergencyLevel.VERYHIGH);
        emergencyLevelComboBox.setRequired(true);
        emergencyLevelComboBox.setItemLabelGenerator(this::formatEmergencyLevel);

        locationXField = new NumberField(translator.get("add_catastrophe_coordX"));
        locationXField.setValue(0.0);
        locationXField.setStep(0.000001);
        locationXField.setStepButtonsVisible(true);
        locationXField.setHelperText(translator.get("add_catastrophe_coords_helper"));
        locationXField.setRequired(true);

        locationYField = new NumberField(translator.get("add_catastrophe_coordY"));
        locationYField.setValue(0.0);
        locationYField.setStep(0.000001);
        locationYField.setStepButtonsVisible(true);
        locationYField.setHelperText(translator.get("add_catastrophe_coords_helper"));
        locationYField.setRequired(true);

        formLayout.add(
                nameField, descriptionField,
                dateField, emergencyLevelComboBox,
                locationXField, locationYField
        );

        formLayout.setColspan(descriptionField, 2);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2),
                new FormLayout.ResponsiveStep("750px", 3)
        );

        return formLayout;
    }

    private Component getButtons() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);

        Button cancelButton = new Button(translator.get("add_catastrophe_cancel_button"));
        cancelButton.addClickListener(e -> goBack());

        Button saveButton = new Button(translator.get("add_catastrophe_save_button"));
        saveButton.addClickListener(e -> saveCatastrophe());

        buttonLayout.add(cancelButton, saveButton);

        return buttonLayout;
    }

    //===============================Other methods=========================================
    private String formatEmergencyLevel(EmergencyLevel level) {
        return switch (level) {
            case LOW -> translator.get("low_emergency_level");
            case MEDIUM -> translator.get("medium_emergency_level");
            case HIGH -> translator.get("high_emergency_level");
            case VERYHIGH -> translator.get("very_high_emergency_level");
        };
    }

    private void goBack() {
        getUI().ifPresent(ui -> ui.navigate(""));
    }

}