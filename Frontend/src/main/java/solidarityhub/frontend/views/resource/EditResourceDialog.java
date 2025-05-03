package solidarityhub.frontend.views.resource;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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

public class EditResourceDialog {
    private final ResourceService resourceService;
    private final StorageService storageService;
    private final Grid<ResourceDTO> resourceGrid;
    private final CatastropheDTO selectedCatastrophe;
    private final ResourceDTO resource;

    public EditResourceDialog(ResourceService resourceService, StorageService storageService, 
                            Grid<ResourceDTO> resourceGrid, CatastropheDTO selectedCatastrophe, 
                            ResourceDTO resource) {
        this.resourceService = resourceService;
        this.storageService = storageService;
        this.resourceGrid = resourceGrid;
        this.selectedCatastrophe = selectedCatastrophe;
        this.resource = resource;
    }

    public void openEditResourceDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setHeight("600px");

        H3 title = new H3("Editar recurso");
        title.setWidthFull();
        title.getStyle().set("text-align", "center");

        TextField nameField = new TextField("Nombre del recurso");
        nameField.setValue(resource.getName());

        Select<ResourceType> typeField = new Select<>();
        typeField.setLabel("Tipo de recurso");
        typeField.setItems(ResourceType.values());
        typeField.setValue(resource.getType());
        typeField.setItemLabelGenerator(type -> {
            return switch (type) {
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
                default -> "Desconocido";
            };
        });

        NumberField quantityField = new NumberField("Cantidad");
        quantityField.setValue(resource.getQuantity());
        
        TextField unitField = new TextField("Unidad de medida");
        unitField.setValue(resource.getUnit());

        Select<StorageDTO> storageField = new Select<>();
        storageField.setLabel("Almacén");
        storageField.setItemLabelGenerator(storageAux -> 
            String.format("%s - %s", storageAux.getName(), storageAux.isFull() ? "Lleno" : "Disponible")
        );

        List<StorageDTO> storages = storageService.getStorages();
        storageField.setItems(storages);
        if (resource.getStorageId() != null) {
            storageField.setValue(storages.stream()
                    .filter(s -> s.getId() == (resource.getStorageId()))
                    .findFirst()
                    .orElse(null));
        }

        nameField.setRequiredIndicatorVisible(true);
        typeField.setRequiredIndicatorVisible(true);
        quantityField.setRequiredIndicatorVisible(true);
        unitField.setRequiredIndicatorVisible(true);

        dialog.addOpenedChangeListener(dialogEvent -> {
            if (!dialogEvent.isOpened()) {
                ResourceView resourceView = (ResourceView) resourceGrid.getParent().get();
                if (resourceView != null) {
                    resourceView.refreshGrid();
                }
            }
        });

        Button cancelButton = new Button("Cancelar", event -> dialog.close());

        Button deleteButton = new Button("Eliminar", event -> {
            try {
                resourceService.deleteResource(resource.getId());
                Notification.show("Recurso eliminado con éxito.", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
            } catch (Exception e) {
                Notification.show("Error al eliminar el recurso: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        deleteButton.getStyle().set("color", "var(--lumo-error-text-color)");

        Button updateButton = new Button("Actualizar", event -> {
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

            ResourceDTO updatedResource = new ResourceDTO(
                    name,
                    type,
                    quantity,
                    unit,
                    selectedStorage != null ? selectedStorage.getId() : null,
                    selectedCatastrophe.getId()
            );

            try {
                resourceService.updateResource(resource.getId(), updatedResource);
                Notification.show("Recurso actualizado con éxito.", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
            } catch (Exception e) {
                Notification.show("Error al actualizar el recurso: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        HorizontalLayout nameTypeLayout = new HorizontalLayout(nameField, typeField);
        nameTypeLayout.setWidthFull();
        nameTypeLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        HorizontalLayout quantityUnitLayout = new HorizontalLayout(quantityField, unitField);
        quantityUnitLayout.setWidthFull();
        quantityUnitLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        HorizontalLayout storageFieldLayout = new HorizontalLayout(storageField);
        storageFieldLayout.setWidthFull();
        storageFieldLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, deleteButton, updateButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout dialogLayout = new VerticalLayout(title, nameTypeLayout, quantityUnitLayout, storageFieldLayout, buttonLayout);
        dialog.add(dialogLayout);

        dialog.open();
    }
}