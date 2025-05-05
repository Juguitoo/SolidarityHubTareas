package solidarityhub.frontend.views.resources.resource;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.dto.StorageDTO;
import solidarityhub.frontend.model.enums.ResourceType;
import solidarityhub.frontend.service.ResourceService;
import solidarityhub.frontend.service.StorageService;
import java.util.List;

public class AddResourceDialog extends Dialog {

    protected final ResourceService resourceService;
    protected final StorageService storageService;
    protected final CatastropheDTO selectedCatastrophe;

    protected TextField nameField;
    protected Select<ResourceType> typeField;
    protected NumberField quantityField;
    protected TextField unitField;
    protected Select<StorageDTO> storageField;

    protected ResourceDTO resource;

    public AddResourceDialog(CatastropheDTO selectedCatastrophe) {
        this(selectedCatastrophe, null);
    }

    protected AddResourceDialog(CatastropheDTO selectedCatastrophe, ResourceDTO resource) {
        this.resourceService = new ResourceService();
        this.storageService = new StorageService();
        this.selectedCatastrophe = selectedCatastrophe;
        this.resource = resource;

        buildView();
    }

    public void buildView() {
        setHeaderTitle(resource == null ? "Añadir nuevo recurso" : "Editar recurso");
        addClassNames("resources-dialog");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(getForms());

        add(dialogLayout);
        getFooter().add(getButtons());

        if (resource != null) {
            populateFormFields();
        }
    }

    protected void populateFormFields() {
    }

    //=============================== Get Components =========================================
    protected Component getForms() {
        FormLayout formLayout = new FormLayout();
        nameField = new TextField("Nombre del recurso");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setRequired(true);

        typeField = new Select<>();
        typeField.setLabel("Tipo de recurso");
        typeField.setItems(ResourceType.values());
        typeField.setItemLabelGenerator(type -> switch (type) {
            case FOOD -> "Alimentos";
            case MEDICINE -> "Medicina";
            case CLOTHING -> "Ropa";
            case SHELTER -> "Refugio";
            case TOOLS -> "Herramientas";
            case FUEL -> "Combustible";
            case SANITATION -> "Higiene";
            case COMMUNICATION -> "Comunicación";
            case TRANSPORTATION -> "Transporte";
            case BUILDING -> "Construcción";
            case MONETARY -> "Donaciones";
            case STATIONERY -> "Papelería";
            case LOGISTICS -> "Logística";
            case OTHER -> "Otros";
        });
        typeField.setRequiredIndicatorVisible(true);

        quantityField = new NumberField("Cantidad");
        quantityField.setRequiredIndicatorVisible(true);
        quantityField.setRequired(true);

        unitField = new TextField("Unidad de medida");
        unitField.setRequiredIndicatorVisible(true);
        unitField.setRequired(true);

        storageField = new Select<>();
        storageField.setLabel("Almacén");
        storageField.setItemLabelGenerator(storageAux ->
                String.format("%s - %s", storageAux.getName(), storageAux.isFull() ? "Lleno" : "Disponible")
        );

        List<StorageDTO> storages = storageService.getStorages();
        storageField.setItems(storages);

        formLayout.add(nameField, typeField, quantityField, unitField, storageField);
        return formLayout;
    }

    protected Component getButtons() {
        HorizontalLayout buttonLayout = new HorizontalLayout();

        Button cancelButton = new Button("Cancelar", event -> close());
        Button saveButton = new Button(resource == null ? "Guardar" : "Actualizar", event -> saveResource());

        buttonLayout.add(cancelButton);

        if (resource != null) {
            Button deleteButton = new Button("Eliminar", event -> deleteResource());
            deleteButton.getStyle().set("color", "var(--lumo-error-text-color)");
            buttonLayout.add(deleteButton);
        }

        buttonLayout.add(saveButton);

        return buttonLayout;
    }

    //=============================== Edit Resource =========================================
    protected void saveResource() {
        String name = nameField.getValue();
        ResourceType type = typeField.getValue();
        Double quantity = quantityField.getValue();
        String unit = unitField.getValue();
        StorageDTO selectedStorage = storageField.getValue();

        if (name.isEmpty() || type == null || quantity == null || unit.isEmpty()) {
            Notification.show("Por favor, completa todos los campos.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        ResourceDTO resourceToSave = new ResourceDTO(
                name,
                type,
                quantity,
                unit,
                selectedStorage != null ? selectedStorage.getId() : null,
                selectedCatastrophe.getId()
        );

        try {
            if (resource == null) {
                // Agregar nuevo recurso
                resourceService.addResource(resourceToSave);
                Notification.show("Recurso añadido con éxito.", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                // Actualizar recurso existente
                resourceService.updateResource(resource.getId(), resourceToSave);
                Notification.show("Recurso actualizado con éxito.", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            close();
        } catch (Exception e) {
            String action = resource == null ? "añadir" : "actualizar";
            Notification.show("Error al " + action + " el recurso: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    protected void deleteResource() {
        if (resource == null) return;

        try {
            resourceService.deleteResource(resource.getId());
            Notification.show("Recurso eliminado con éxito.", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
        } catch (Exception e) {
            Notification.show("Error al eliminar el recurso: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}