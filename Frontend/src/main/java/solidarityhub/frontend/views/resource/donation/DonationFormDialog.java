package solidarityhub.frontend.views.resource.donation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.DonationDTO;
import solidarityhub.frontend.model.enums.DonationStatus;
import solidarityhub.frontend.model.enums.DonationType;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.DonationService;

import java.time.LocalDate;

public class DonationFormDialog extends Dialog {

    private final DonationService donationService;
    private final CatastropheService catastropheService;
    private final CatastropheDTO selectedCatastrophe;
    private final boolean isEditMode;
    private final DonationDTO donationDTO;

    private final Binder<DonationDTO> binder = new Binder<>(DonationDTO.class);

    // Form fields
    private final TextField codeField = new TextField("Código");
    private final DatePicker dateField = new DatePicker("Fecha");
    private final ComboBox<DonationType> typeField = new ComboBox<>("Tipo");
    private final TextArea descriptionField = new TextArea("Descripción");
    private final NumberField amountField = new NumberField("Cantidad (€)");
    private final ComboBox<DonationStatus> statusField = new ComboBox<>("Estado");
    private final TextField volunteerField = new TextField("Donante (DNI)");

    public DonationFormDialog(DonationService donationService,
                              CatastropheService catastropheService,
                              CatastropheDTO selectedCatastrophe) {
        this(donationService, catastropheService, selectedCatastrophe, null);
    }

    public DonationFormDialog(DonationService donationService,
                              CatastropheService catastropheService,
                              CatastropheDTO selectedCatastrophe,
                              DonationDTO donationDTO) {
        this.donationService = donationService;
        this.catastropheService = catastropheService;
        this.selectedCatastrophe = selectedCatastrophe;
        this.isEditMode = donationDTO != null;
        this.donationDTO = isEditMode ? donationDTO : new DonationDTO();

        setWidth("600px");
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.addClassName("donation-form");

        // Title
        H3 title = new H3(isEditMode ? "Editar donación" : "Registrar nueva donación");
        title.addClassName("donation-form-title");
        title.getStyle().set("margin", "1rem");

        // Initialize form
        initForm();

        // Form layout
        FormLayout form = new FormLayout();
        form.addClassName("form-layout");
        form.getStyle().set("padding", "0 1rem");

        // Add form fields
        form.add(codeField, dateField, typeField, statusField);
        form.add(volunteerField, amountField);

        // Description should span 2 columns
        descriptionField.addClassName("full-width");
        form.add(descriptionField);

        // Buttons
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-container");
        buttonLayout.getStyle().set("padding", "1rem");
        buttonLayout.getStyle().set("margin-top", "1rem");

        Button saveButton = new Button("Guardar", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancelar", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        buttonLayout.add(cancelButton);

        // Botón de eliminar (solo en modo edición)
        if (isEditMode) {
            Button deleteButton = new Button("Eliminar", e -> confirmDelete());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            buttonLayout.add(deleteButton);
        }

        buttonLayout.add(saveButton);

        // Add components to dialog
        dialogLayout.add(title, form, buttonLayout);
        add(dialogLayout);
    }

    private void initForm() {
        // Setup fields
        codeField.setReadOnly(true);
        if (isEditMode) {
            codeField.setValue(donationDTO.getCode());
        } else {
            codeField.setValue("Se generará automáticamente");
        }

        dateField.setValue(isEditMode ? donationDTO.getDate() : LocalDate.now());

        typeField.setItems(DonationType.values());
        typeField.setItemLabelGenerator(this::formatDonationType);
        if (isEditMode) {
            typeField.setValue(donationDTO.getType());
        }

        descriptionField.setPlaceholder("Ingrese una descripción detallada de la donación");
        if (isEditMode) {
            descriptionField.setValue(donationDTO.getDescription());
        }

        // Inicialmente ocultar el campo de cantidad
        amountField.setVisible(false);

        if (isEditMode && donationDTO.getType() == DonationType.FINANCIAL) {
            amountField.setVisible(true);
            amountField.setValue(donationDTO.getAmount());
        }

        // Show/hide amount field based on selected type
        typeField.addValueChangeListener(event -> {
            boolean isFinancial = event.getValue() == DonationType.FINANCIAL;
            amountField.setVisible(isFinancial);

            // Si no es una donación financiera, establecer un valor por defecto de 0
            // para evitar problemas de validación
            if (!isFinancial) {
                amountField.setValue(0.0);
            } else if (amountField.isEmpty()) {
                amountField.setValue(0.0);
            }
        });

        statusField.setItems(DonationStatus.values());
        statusField.setItemLabelGenerator(this::formatDonationStatus);
        if (isEditMode) {
            statusField.setValue(donationDTO.getStatus());
        } else {
            statusField.setValue(DonationStatus.COMPLETED);
        }

        if (isEditMode) {
            volunteerField.setValue(donationDTO.getVolunteerDni());
        }

        // Setup binder
        binder.forField(descriptionField)
                .asRequired("La descripción es obligatoria")
                .bind(DonationDTO::getDescription, DonationDTO::setDescription);

        binder.forField(typeField)
                .asRequired("El tipo es obligatorio")
                .bind(DonationDTO::getType, DonationDTO::setType);

        binder.forField(statusField)
                .asRequired("El estado es obligatorio")
                .bind(DonationDTO::getStatus, DonationDTO::setStatus);

        binder.forField(volunteerField)
                .asRequired("El DNI del donante es obligatorio")
                .bind(DonationDTO::getVolunteerDni, DonationDTO::setVolunteerDni);

        // Configurar el binder para el campo de cantidad solo si es una donación financiera
        binder.forField(amountField)
                .withValidator(value -> {
                    // Solo validar si es una donación financiera
                    if (typeField.getValue() == DonationType.FINANCIAL) {
                        return value != null && value >= 0;
                    }
                    return true; // No validar para otros tipos
                }, "La cantidad debe ser un valor positivo")
                .bind(DonationDTO::getAmount, DonationDTO::setAmount);

        binder.forField(dateField)
                .asRequired("La fecha es obligatoria")
                .bind(DonationDTO::getDate, DonationDTO::setDate);
    }

    private void save() {
        try {
            // Asegurar que la cantidad sea 0 para donaciones no financieras
            if (typeField.getValue() != DonationType.FINANCIAL) {
                donationDTO.setAmount(0.0);
            }

            // Validate form
            binder.writeBean(donationDTO);

            // Set catastrophe ID
            donationDTO.setCatastropheId(selectedCatastrophe.getId());
            donationDTO.setCatastropheName(selectedCatastrophe.getName());

            // Save donation
            if (isEditMode) {
                donationService.updateDonation(donationDTO.getId(), donationDTO);
                Notification.show("Donación actualizada correctamente",
                                3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                donationService.addDonation(donationDTO);
                Notification.show("Donación registrada correctamente",
                                3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            // Close dialog
            close();
        } catch (ValidationException e) {
            Notification.show("Por favor, corrija los errores en el formulario: " + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace(); // Para depuración
        } catch (Exception e) {
            Notification.show("Error al guardar la donación: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            e.printStackTrace(); // Para depuración
        }
    }

    private void confirmDelete() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");

        VerticalLayout confirmContent = new VerticalLayout();
        confirmContent.add(new Span("¿Está seguro de que desea eliminar esta donación? Esta acción no se puede deshacer."));
        confirmContent.setPadding(true);

        Button cancelButton = new Button("Cancelar", e -> confirmDialog.close());
        Button confirmButton = new Button("Eliminar", e -> {
            deleteDonation();
            confirmDialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        confirmDialog.getFooter().add(cancelButton, confirmButton);
        confirmDialog.add(confirmContent);

        confirmDialog.open();
    }

    private void deleteDonation() {
        try {
            donationService.deleteDonation(donationDTO.getId());
            Notification.show("Donación eliminada correctamente",
                            3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
        } catch (Exception e) {
            Notification.show("Error al eliminar la donación: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String formatDonationType(DonationType type) {
        if (type == null) {
            return "";
        }

        switch (type) {
            case FINANCIAL:
                return "Económica";
            case MATERIAL:
                return "Material";
            case SERVICE:
                return "Servicio";
            default:
                return type.toString();
        }
    }

    private String formatDonationStatus(DonationStatus status) {
        if (status == null) {
            return "";
        }

        switch (status) {
            case COMPLETED:
                return "Completada";
            case IN_PROGRESS:
                return "En proceso";
            case SCHEDULED:
                return "Programada";
            case CANCELLED:
                return "Cancelada";
            default:
                return status.toString();
        }
    }
}