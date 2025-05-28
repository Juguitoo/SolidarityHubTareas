package solidarityhub.frontend.views.resources.donation;

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
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.DonationDTO;
import org.pingu.domain.DTO.DonorDTO;
import solidarityhub.frontend.i18n.Translator;
import org.pingu.domain.enums.DonationStatus;
import org.pingu.domain.enums.DonationType;
import solidarityhub.frontend.service.DonationService;
import solidarityhub.frontend.service.DonorService;
import java.time.LocalDate;
import java.util.Locale;

public class AddDonationDialog extends Dialog {

    protected final DonationService donationService;
    protected final DonorService donorService;
    protected static Translator translator = new Translator();

    protected final CatastropheDTO selectedCatastrophe;
    protected DonationDTO donation;

    protected DatePicker dateField;
    protected ComboBox<DonationType> typeField;
    protected TextArea descriptionField;
    protected NumberField quantityField;
    protected TextField unitField;
    protected ComboBox<DonationStatus> statusField;
    protected TextField donorDniField;
    protected TextField donorNameField;
    protected Checkbox updateResourcesCheckbox;

    public AddDonationDialog(CatastropheDTO selectedCatastrophe) {
        this(selectedCatastrophe, null);
    }

    protected AddDonationDialog(CatastropheDTO selectedCatastrophe, DonationDTO donation) {
        this.donationService = new DonationService();
        this.donorService = new DonorService();
        this.selectedCatastrophe = selectedCatastrophe;
        this.donation = donation;

        translator.initializeTranslator();
        buildView();
    }

    protected void buildView() {
        setHeaderTitle(donation == null ?
                translator.get("add_donation_button") :
                translator.get("edit_donation_button"));
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
            donorDniField.setValue(donation.getDonorDni());
            donorNameField.setValue(donation.getDonorName());
        }
    }

    //=============================== Get Components =========================================
    protected Component getForms() {
        FormLayout formLayout = new FormLayout();

        formLayout.add(getDonationForms(), getDonorForms());

        return formLayout;
    }

    protected Component getDonationForms() {
        FormLayout formLayout = new FormLayout();

        dateField = new DatePicker(translator.get("donation_date"));
        dateField.setValue(donation == null ? LocalDate.now() : donation.getDate());
        dateField.setRequiredIndicatorVisible(true);
        dateField.setRequired(true);

        typeField = new ComboBox<>(translator.get("donation_type"));
        typeField.setItems(DonationType.values());
        typeField.setItemLabelGenerator(this::formatDonationType);
        typeField.setRequiredIndicatorVisible(true);
        typeField.setRequired(true);

        typeField.addValueChangeListener(event -> {
            DonationType selectedType = event.getValue();
            if (selectedType == DonationType.FINANCIAL && unitField.isEmpty()) {
                unitField.setValue("€");
            } else if (selectedType == DonationType.MATERIAL && unitField.isEmpty()) {
                unitField.setValue(translator.get("units"));
            } else if (selectedType == DonationType.SERVICE && unitField.isEmpty()) {
                unitField.setValue(translator.get("hours"));
            }
        });

        quantityField = new NumberField(translator.get("resource_quantity"));
        quantityField.setRequiredIndicatorVisible(true);
        quantityField.setValue(donation == null ? 0.0 : donation.getQuantity());
        quantityField.setRequired(true);

        unitField = new TextField(translator.get("unit"));
        unitField.setRequiredIndicatorVisible(true);
        unitField.setValue(donation == null ? "€" : donation.getUnit());
        unitField.setRequired(true);

        descriptionField = new TextArea(translator.get("donation_description"));
        descriptionField.setPlaceholder(translator.get("donation_description_placeholder"));
        descriptionField.setHeight("75px");
        descriptionField.setRequired(true);
        descriptionField.setRequiredIndicatorVisible(true);

        statusField = new ComboBox<>(translator.get("donation_status"));
        statusField.setItems(DonationStatus.values());
        statusField.setItemLabelGenerator(this::formatDonationStatus);
        statusField.setValue(donation == null ? DonationStatus.COMPLETED : donation.getStatus());
        statusField.setRequiredIndicatorVisible(true);
        statusField.setRequired(true);

        updateResourcesCheckbox = new Checkbox(translator.get("update_resources"));
        updateResourcesCheckbox.setValue(true);
        updateResourcesCheckbox.setLabel(translator.get("update_resources_help"));

        formLayout.add(dateField, typeField, quantityField, unitField, descriptionField, statusField, updateResourcesCheckbox);

        typeField.addValueChangeListener(event -> {
            DonationType selectedType = event.getValue();
            if (selectedType == DonationType.FINANCIAL) {
                unitField.setValue("€");
                updateResourcesCheckbox.setLabel(translator.get("update_monetary_resources"));
                updateResourcesCheckbox.setLabel(translator.get("update_monetary_resources_help"));
            } else if (selectedType == DonationType.MATERIAL) {
                updateResourcesCheckbox.setLabel(translator.get("update_material_resources"));
                updateResourcesCheckbox.setLabel(translator.get("update_material_resources_help"));
            } else if (selectedType == DonationType.SERVICE) {
                unitField.setValue(translator.get("hours"));
                updateResourcesCheckbox.setValue(false);
                updateResourcesCheckbox.setEnabled(false);
            }
        });

        formLayout.setColspan(descriptionField, 2);

        return formLayout;
    }

    protected Component getDonorForms() {
        FormLayout formLayout = new FormLayout();

        donorDniField = new TextField(translator.get("donor_id"));
        donorDniField.addClassName("donate__dni-field");
        donorDniField.setRequiredIndicatorVisible(true);
        donorDniField.setRequired(true);
        donorDniField.setPlaceholder("11111111A");

        donorNameField = new TextField(translator.get("donor_name"));
        donorNameField.setRequiredIndicatorVisible(true);
        donorDniField.setRequiredIndicatorVisible(true);
        donorNameField.setRequired(true);

        formLayout.add(donorDniField, donorNameField);

        donorDniField.setValueChangeMode(ValueChangeMode.LAZY); // Establece el modo para esperar
        donorDniField.setValueChangeTimeout(500); // Define el tiempo de espera en milisegundos
        donorDniField.addValueChangeListener(event -> {
            String dni = event.getValue();
            if (dni != null && !dni.isEmpty()) {
                DonorDTO donor = donorService.getDonorByDni(dni);
                if (donor != null) {
                    donorNameField.setValue(donor.getName());
                } else {
                    donorNameField.clear();
                }
            }
        });
        return formLayout;
    }

    protected Component getButtons() {
        HorizontalLayout buttonLayout = new HorizontalLayout();

        Button cancelButton = new Button(translator.get("cancel_button"), event -> close());
        Button saveButton = new Button(
                donation == null ? translator.get("save_button") : translator.get("edit_button"),
                event -> saveDonation()
        );

        buttonLayout.add(cancelButton);

        if (donation != null) {
            Button deleteButton = new Button(translator.get("delete_button"), event -> deleteDonation());
            deleteButton.getStyle().set("color", "var(--lumo-error-text-color)");
            buttonLayout.add(deleteButton);
        }

        buttonLayout.add(saveButton);

        return buttonLayout;
    }

    //=============================== Action Methods =========================================
    protected void saveDonation() {
        if (!validateForms()) {
            Notification.show(translator.get("check_fields"), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        //Donation data
        LocalDate date = dateField.getValue();
        DonationType type = typeField.getValue();
        String description = descriptionField.getValue();
        Double quantity = quantityField.getValue();
        String unit = unitField.getValue();
        DonationStatus status = statusField.getValue();
        boolean updateResources = updateResourcesCheckbox.getValue();

        DonorDTO donor = saveDonor();

        try {
            DonationDTO donationToSave = new DonationDTO();

            if (donation != null) {
                donationToSave.setId(donation.getId());
            }

            // Set donation data
            donationToSave.setDate(date);
            donationToSave.setType(type);
            donationToSave.setDescription(description);
            donationToSave.setQuantity(quantity);
            donationToSave.setUnit(unit);
            donationToSave.setCantidad(quantity + " " + unit);
            donationToSave.setStatus(status);
            donationToSave.setCatastropheId(selectedCatastrophe.getId());
            donationToSave.setDonorDni(donor.getDni());
            donationToSave.setUpdateResources(updateResources);

            if (donation == null) {
                donationService.addDonation(donationToSave);
                Notification.show(translator.get("donation_registered_success"), 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                donationService.updateDonation(donation.getId(), donationToSave);
                Notification.show(translator.get("donation_updated_success"), 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            close();
        } catch (Exception e) {
            String action = donation == null ? translator.get("register_action") : translator.get("update_action");
            Notification.show(translator.get("error_prefix") + " " + action + " " +
                                    translator.get("donation_lowercase") + ": " + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    protected DonorDTO saveDonor(){
        DonorDTO donorToSave = new DonorDTO();
        String dni = donorDniField.getValue();
        String name = donorNameField.getValue();

        if(exitsDonor(dni)){
            donorToSave = donorService.getDonorByDni(dni);
        } else {
            donorToSave.setDni(dni);
            donorToSave.setName(name);
        }

        try {
            donorService.addDonor(donorToSave);
            return donorToSave;
        } catch (Exception e) {
            Notification.show(translator.get("error_register_donor"), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return null;
        }
    }

    protected void deleteDonation() {
        if (donation == null) return;

        try {
            donationService.deleteDonation(donation.getId());
            donationService.clearCache();
            Notification.show(translator.get("donation_deleted_success"), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
        } catch (Exception e) {
            Notification.show(translator.get("error_deleting_donation") + ": " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    //=============================== Format Methods =========================================
    private String formatDonationType(DonationType type) {
        if (type == null) {
            return "";
        }

        return switch (type) {
            case FINANCIAL -> translator.get("donation_type_financial");
            case MATERIAL -> translator.get("donation_type_material");
            case SERVICE -> translator.get("donation_type_service");
        };
    }

    private String formatDonationStatus(DonationStatus status) {
        if (status == null) {
            return "";
        }

        return switch (status) {
            case COMPLETED -> translator.get("donation_status_completed");
            case IN_PROGRESS -> translator.get("donation_status_in_progress");
            case SCHEDULED -> translator.get("donation_status_scheduled");
            case CANCELLED -> translator.get("donation_status_cancelled");
        };
    }

    //===============================Validate Form=========================================
    private boolean validateForms() {
        return !dateField.isEmpty() &&
                !typeField.isEmpty() &&
                !descriptionField.isEmpty() &&
                !quantityField.isEmpty() &&
                !unitField.isEmpty() &&
                !statusField.isEmpty() &&
                !donorDniField.isEmpty() &&
                !donorNameField.isEmpty();
    }

    private boolean exitsDonor(String donorDni) {
        return donorService.getDonorByDni(donorDni) != null;
    }
}