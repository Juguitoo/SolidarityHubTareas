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

    // Campos del formulario
    private final TextField codeField = new TextField("Código");
    private final DatePicker dateField = new DatePicker("Fecha");
    private final ComboBox<DonationType> typeField = new ComboBox<>("Tipo");
    private final TextArea descriptionField = new TextArea("Descripción");
    private final NumberField quantityField = new NumberField("Cantidad");
    private final TextField unitField = new TextField("Unidad de medida");
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

        // Título
        H3 title = new H3(isEditMode ? "Editar donación" : "Registrar nueva donación");
        title.addClassName("donation-form-title");
        title.getStyle().set("margin", "1rem");

        // Inicializar formulario
        initForm();

        // Diseño del formulario
        FormLayout form = new FormLayout();
        form.addClassName("form-layout");
        form.getStyle().set("padding", "0 1rem");

        // Añadir campos del formulario
        form.add(codeField, dateField, typeField, statusField);
        form.add(volunteerField, quantityField, unitField);

        // La descripción debe abarcar 2 columnas
        descriptionField.addClassName("full-width");
        form.add(descriptionField);

        // Botones
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

        // Añadir componentes al diálogo
        dialogLayout.add(title, form, buttonLayout);
        add(dialogLayout);
    }

    private void initForm() {
        // Configurar campos
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

        // Inicializar campos de cantidad y unidad
        if (isEditMode) {
            quantityField.setValue(donationDTO.getQuantity());
            unitField.setValue(donationDTO.getUnit());
        } else {
            // Valores predeterminados
            quantityField.setValue(0.0);
            unitField.setValue("€");
        }

        // Establecer valores predeterminados de unidad según el tipo
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

        // Configurar binder
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

        // Configurar el binder para los campos de cantidad y unidad
        binder.forField(quantityField)
                .withValidator(value -> value != null && value >= 0,
                        "La cantidad debe ser un valor positivo")
                .bind(DonationDTO::getQuantity, DonationDTO::setQuantity);

        binder.forField(unitField)
                .asRequired("La unidad de medida es obligatoria")
                .bind(DonationDTO::getUnit, DonationDTO::setUnit);

        binder.forField(dateField)
                .asRequired("La fecha es obligatoria")
                .bind(DonationDTO::getDate, DonationDTO::setDate);
    }

    private void save() {
        try {
            // Validar formulario
            binder.writeBean(donationDTO);

            // Establecer ID de catástrofe
            donationDTO.setCatastropheId(selectedCatastrophe.getId());
            donationDTO.setCatastropheName(selectedCatastrophe.getName());

            // Generar cantidad a partir de quantity y unit
            donationDTO.setCantidad(donationDTO.getQuantity() + " " + donationDTO.getUnit());

            // Guardar donación
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

            // Cerrar diálogo
            close();
        } catch (ValidationException e) {
            Notification.show("Por favor, corrija los errores en el formulario: " + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Error al guardar la donación: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
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
            donationService.clearCache();
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

        return switch (type) {
            case FINANCIAL -> "Económica";
            case MATERIAL -> "Material";
            case SERVICE -> "Servicio";
            default -> type.toString();
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
            default -> status.toString();
        };
    }
}