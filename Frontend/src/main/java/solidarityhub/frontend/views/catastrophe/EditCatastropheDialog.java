package solidarityhub.frontend.views.catastrophe;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.i18n.Translator;
import org.pingu.domain.enums.EmergencyLevel;
import solidarityhub.frontend.model.Catastrophe;
import solidarityhub.frontend.model.GPSCoordinates;
import solidarityhub.frontend.service.CatastropheService;

import java.time.LocalDate;
import java.util.Locale;

public class EditCatastropheDialog extends Dialog {

    private static Translator translator = new Translator();
    private final CatastropheService catastropheService;
    private final CatastropheDTO catastrophe;

    private TextField nameField;
    private TextArea descriptionField;
    private DatePicker dateField;
    private NumberField locationXField;
    private NumberField locationYField;
    private ComboBox<EmergencyLevel> emergencyLevelComboBox;

    private Button saveButton;
    private Button cancelButton;

    public EditCatastropheDialog(CatastropheDTO catastrophe) {
        this.catastrophe = catastrophe;
        this.catastropheService = new CatastropheService();

        translator.initializeTranslator();
        buildDialog();
    }

    private void buildDialog() {
        setHeaderTitle("Editar catástrofe");
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setWidth("600px");

        add(createForm());
        getFooter().add(createButtons());
    }

    private Component createForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        nameField = new TextField(translator.get("add_catastrophe_name"));
        nameField.setRequired(true);
        nameField.setPlaceholder(translator.get("add_catastrophe_name_placeholder"));
        nameField.setMaxLength(255);
        nameField.setHelperText(translator.get("add_catastrophe_text_helper"));
        nameField.setValue(catastrophe.getName());

        descriptionField = new TextArea(translator.get("add_catastrophe_description"));
        descriptionField.setPlaceholder(translator.get("add_catastrophe_description_placeholder"));
        descriptionField.setHeight("150px");
        descriptionField.setMaxLength(255);
        descriptionField.setHelperText(translator.get("add_catastrophe_text_helper"));
        descriptionField.setValue(catastrophe.getDescription());

        dateField = new DatePicker(translator.get("add_catastrophe_start_date"));
        dateField.setValue(catastrophe.getStartDate());
        dateField.setRequired(true);

        locationXField = new NumberField(translator.get("add_catastrophe_coordX"));
        locationXField.setValue(catastrophe.getLocationX());
        locationXField.setStep(0.000001);
        locationXField.setStepButtonsVisible(true);
        locationXField.setHelperText(translator.get("add_catastrophe_coords_helper"));

        locationYField = new NumberField(translator.get("add_catastrophe_coordY"));
        locationYField.setValue(catastrophe.getLocationY());
        locationYField.setStep(0.000001);
        locationYField.setStepButtonsVisible(true);
        locationYField.setHelperText(translator.get("add_catastrophe_coords_helper"));

        emergencyLevelComboBox = new ComboBox<>(translator.get("add_catastrophe_emergency_level"));
        emergencyLevelComboBox.setItems(EmergencyLevel.LOW, EmergencyLevel.MEDIUM, EmergencyLevel.HIGH, EmergencyLevel.VERYHIGH);
        emergencyLevelComboBox.setValue(catastrophe.getEmergencyLevel());
        emergencyLevelComboBox.setRequired(true);
        emergencyLevelComboBox.setItemLabelGenerator(this::formatEmergencyLevel);

        formLayout.add(
                nameField, descriptionField, dateField,
                locationXField, locationYField, emergencyLevelComboBox
        );

        formLayout.setColspan(descriptionField, 2);

        return formLayout;
    }

    private Component createButtons() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();

        cancelButton = new Button("Cancelar", e -> close());
        saveButton = new Button("Guardar", e -> saveChanges());

        buttonsLayout.add(cancelButton, saveButton);
        buttonsLayout.setSpacing(true);

        return buttonsLayout;
    }

    private void saveChanges() {
        if (!validateForm()) {
            Notification.show("Por favor, verifica los campos requeridos",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            // Crear objeto GPSCoordinates con los nuevos valores
            GPSCoordinates location = new GPSCoordinates(locationYField.getValue(), locationXField.getValue());

            // Crear un objeto Catastrophe con los nuevos valores
            Catastrophe updatedCatastrophe = new Catastrophe(
                    nameField.getValue(),
                    descriptionField.getValue(),
                    location,
                    dateField.getValue(),
                    emergencyLevelComboBox.getValue()
            );

            // Crear DTO para enviarlo al servicio
            CatastropheDTO updatedDTO = new CatastropheDTO(updatedCatastrophe);

            // Actualizar la catástrofe
            catastropheService.updateCatastrophe(catastrophe.getId(), updatedDTO);

            Notification.show("Catástrofe actualizada correctamente",
                            3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            close();

            // Refrescar la vista actual sin navegar a otra página
            UI.getCurrent().getPage().reload();

        } catch (Exception e) {
            Notification.show("Error al actualizar la catástrofe: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean validateForm() {
        return !nameField.isEmpty() &&
                !emergencyLevelComboBox.isEmpty() &&
                !dateField.isEmpty();
    }

    private String formatEmergencyLevel(EmergencyLevel level) {
        return switch (level) {
            case LOW -> translator.get("low_emergency_level");
            case MEDIUM -> translator.get("medium_emergency_level");
            case HIGH -> translator.get("high_emergency_level");
            case VERYHIGH -> translator.get("very_high_emergency_level");
        };
    }
}