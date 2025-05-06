package solidarityhub.frontend.views.resources.donation;

import com.vaadin.flow.component.Component;
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
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.DonationDTO;
import solidarityhub.frontend.model.enums.DonationStatus;
import solidarityhub.frontend.model.enums.DonationType;
import solidarityhub.frontend.service.DonationService;

import java.time.LocalDate;

public class AddDonationDialog extends Dialog {

    protected final DonationService donationService;
    protected final CatastropheDTO selectedCatastrophe;
    protected DonationDTO donation;

    protected DatePicker dateField;
    protected ComboBox<DonationType> typeField;
    protected TextArea descriptionField;
    protected NumberField quantityField;
    protected TextField unitField;
    protected ComboBox<DonationStatus> statusField;
    protected TextField volunteerField;

    public AddDonationDialog(CatastropheDTO selectedCatastrophe) {
        this(selectedCatastrophe, null);
    }

    protected AddDonationDialog(CatastropheDTO selectedCatastrophe, DonationDTO donation) {
        this.donationService = new DonationService();
        this.selectedCatastrophe = selectedCatastrophe;
        this.donation = donation;

        buildView();
    }

    protected void buildView() {
        setHeaderTitle(donation == null ? "Registrar nueva donación" : "Editar donación");
        addClassNames("resources-dialog");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(getForms());

        add(dialogLayout);
        getFooter().add(getButtons());

        if (donation != null) {
            populateFormFields();
        }
    }

    protected void populateFormFields() {
        if (donation != null) {
            dateField.setValue(donation.getDate());
            typeField.setValue(donation.getType());
            descriptionField.setValue(donation.getDescription());
            quantityField.setValue(donation.getQuantity());
            unitField.setValue(donation.getUnit());
            statusField.setValue(donation.getStatus());
            volunteerField.setValue(donation.getVolunteerDni());
        }
    }

    //=============================== Get Components =========================================
    protected Component getForms() {
        FormLayout formLayout = new FormLayout();

        dateField = new DatePicker("Fecha");
        dateField.setValue(donation == null ? LocalDate.now() : donation.getDate());
        dateField.setRequiredIndicatorVisible(true);
        dateField.setRequired(true);

        typeField = new ComboBox<>("Tipo");
        typeField.setItems(DonationType.values());
        typeField.setItemLabelGenerator(this::formatDonationType);
        typeField.setRequiredIndicatorVisible(true);
        typeField.setRequired(true);

        typeField.addValueChangeListener(event -> {
            DonationType selectedType = event.getValue();
            if (selectedType == DonationType.FINANCIAL && unitField.isEmpty()) {
                unitField.setValue("€");
            } else if (selectedType == DonationType.MATERIAL && unitField.isEmpty()) {
                unitField.setValue("unidades");
            } else if (selectedType == DonationType.SERVICE && unitField.isEmpty()) {
                unitField.setValue("horas");
            }
        });

        quantityField = new NumberField("Cantidad");
        quantityField.setRequiredIndicatorVisible(true);
        quantityField.setValue(donation == null ? 0.0 : donation.getQuantity());
        quantityField.setRequired(true);

        unitField = new TextField("Unidad de medida");
        unitField.setRequiredIndicatorVisible(true);
        unitField.setValue(donation == null ? "€" : donation.getUnit());
        unitField.setRequired(true);

        descriptionField = new TextArea("Descripción");
        descriptionField.setPlaceholder("Ingrese una descripción detallada de la donación");
        descriptionField.setHeight("75px");
        descriptionField.setRequired(true);
        descriptionField.setRequiredIndicatorVisible(true);

        statusField = new ComboBox<>("Estado");
        statusField.setItems(DonationStatus.values());
        statusField.setItemLabelGenerator(this::formatDonationStatus);
        statusField.setValue(donation == null ? DonationStatus.COMPLETED : donation.getStatus());
        statusField.setRequiredIndicatorVisible(true);
        statusField.setRequired(true);

        volunteerField = new TextField("Donante (DNI)");
        volunteerField.addClassName("donate__dni-field");
        volunteerField.setRequiredIndicatorVisible(true);
        volunteerField.setRequired(true);

        formLayout.add(dateField, typeField, quantityField, unitField, descriptionField, statusField, volunteerField);

        formLayout.setColspan(descriptionField, 2);

        return formLayout;
    }

    protected Component getButtons() {
        HorizontalLayout buttonLayout = new HorizontalLayout();

        Button cancelButton = new Button("Cancelar", event -> close());
        Button saveButton = new Button(donation == null ? "Guardar" : "Actualizar", event -> saveDonation());

        buttonLayout.add(cancelButton);

        if (donation != null) {
            Button deleteButton = new Button("Eliminar", event -> deleteDonation());
            deleteButton.getStyle().set("color", "var(--lumo-error-text-color)");
            buttonLayout.add(deleteButton);
        }

        buttonLayout.add(saveButton);

        return buttonLayout;
    }

    //=============================== Manage Donation =========================================
    protected void saveDonation() {
        LocalDate date = dateField.getValue();
        DonationType type = typeField.getValue();
        String description = descriptionField.getValue();
        Double quantity = quantityField.getValue();
        String unit = unitField.getValue();
        DonationStatus status = statusField.getValue();
        String volunteerDni = volunteerField.getValue().toUpperCase();


        if (date == null || type == null || description == null || description.isEmpty() ||
                quantity == null || unit.isEmpty() || status == null || volunteerDni.isEmpty()) {
            Notification.show("Por favor, completa todos los campos.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            DonationDTO donationToSave = new DonationDTO();
            if (donation != null) {
                donationToSave.setId(donation.getId());
                donationToSave.setCode(donation.getCode());
            }

            donationToSave.setDate(date);
            donationToSave.setType(type);
            donationToSave.setDescription(description);
            donationToSave.setQuantity(quantity);
            donationToSave.setUnit(unit);
            donationToSave.setCantidad(quantity + " " + unit);
            donationToSave.setStatus(status);
            donationToSave.setVolunteerDni(volunteerDni);
            donationToSave.setCatastropheId(selectedCatastrophe.getId());
            donationToSave.setCatastropheName(selectedCatastrophe.getName());

            if (donation == null) {
                donationService.addDonation(donationToSave);
                Notification.show("Donación registrada correctamente", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                donationService.updateDonation(donation.getId(), donationToSave);
                Notification.show("Donación actualizada correctamente", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            close();
        } catch (Exception e) {
            String action = donation == null ? "registrar" : "actualizar";
            Notification.show("Error al " + action + " la donación: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    protected void deleteDonation() {
        if (donation == null) return;

        try {
            donationService.deleteDonation(donation.getId());
            donationService.clearCache();
            Notification.show("Donación eliminada correctamente", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
        } catch (Exception e) {
            Notification.show("Error al eliminar la donación: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    //=============================== Format Methods =========================================
    private String formatDonationType(DonationType type) {
        if (type == null) {
            return "";
        }

        return switch (type) {
            case FINANCIAL -> "Económica";
            case MATERIAL -> "Material";
            case SERVICE -> "Servicio";
        };
    }

    private String formatDonationStatus(DonationStatus status) {
        if (status == null) {
            return "";
        }

        return switch (status) {
            case COMPLETED -> "Completada";
            case IN_PROGRESS -> "En proceso";
            case SCHEDULED -> "Programada";
            case CANCELLED -> "Cancelada";
        };
    }
}