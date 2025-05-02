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
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.dto.StorageDTO;
import solidarityhub.frontend.model.Storage;
import solidarityhub.frontend.model.enums.ResourceType;
import solidarityhub.frontend.service.ResourceService;
import solidarityhub.frontend.service.StorageService;

import java.util.List;

public class AddResourceDialog {

    private final ResourceService resourceService;
    private final StorageService storageService;
    private final Grid<ResourceDTO> resourceGrid;

    public AddResourceDialog(ResourceService resourceService, StorageService storageService, Grid<ResourceDTO> resourceGrid) {
        this.resourceService = resourceService;
        this.storageService = storageService;
        this.resourceGrid = resourceGrid;
    }

    public void openAddResourceDialog() {
        // Crear el diálogo
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setHeight("600px");

        // Título del diálogo
        H3 title = new H3("Añadir nuevo recurso");

        // Campos del formulario
        TextField nameField = new TextField("Nombre del recurso");

        // Desplegable de tipo de recurso
        Select<ResourceType> typeField = new Select<>();
        typeField.setLabel("Tipo de recurso");
        typeField.setItems(ResourceType.values());
        typeField.setItemLabelGenerator(type -> {
            switch (type) {
                case FOOD: return "Alimentos";
                case MEDICINE: return "Medicina";
                case CLOTHING: return "Ropa";
                case SHELTER: return "Refugio";
                case TOOLS: return "Herramientas";
                case FUEL: return "Combustible";
                case SANITATION: return "Higiene";
                case COMMUNICATION: return "Comunicación";
                case TRANSPORTATION: return "Transporte";
                case BUILDING: return "Construcción";
                case MONETARY: return "Donaciones";
                case STATIONERY: return "Papelería";
                case LOGISTICS: return "Logística";
                case OTHER: return "Otros";
                default: return "Desconocido";
            }
        });

        NumberField quantityField = new NumberField("Cantidad");
        TextField unitField = new TextField("Unidad de medida");

        // Crear el desplegable de selección de almacén
        Select<StorageDTO> storageField = new Select<>();
        storageField.setLabel("Almacén");
        storageField.setItemLabelGenerator(storageAux -> {
            //String address = convertCoordinatesToAddress(storageAux.getLatitude(), storageAux.getLongitude());
            return String.format("%s - %s - %s", storageAux.getName(), "address", storageAux.isFull() ? "Lleno" : "Disponible");
        });

        // Cargar los almacenes en el desplegable
        List<StorageDTO> storages = storageService.getStorages();
        storageField.setItems(storages);

        // Establecer los campos obligatorios
        nameField.setRequiredIndicatorVisible(true);
        typeField.setRequiredIndicatorVisible(true);
        quantityField.setRequiredIndicatorVisible(true);
        unitField.setRequiredIndicatorVisible(true);

        // Botón para guardar
        Button saveButton = new Button("Guardar", event -> {
            // Lógica para guardar el recurso
            String name = nameField.getValue();
            ResourceType type = typeField.getValue();
            Double quantity = quantityField.getValue();
            String unit = unitField.getValue();
            String cantidad = String.valueOf(quantity) + " " + unit;
            Storage storage = new Storage();
            storage.setName(storageField.getValue().getName());

            if (name.isEmpty() || type == null || quantity == null || unit.isEmpty() ) {
                Notification.show("Por favor, completa todos los campos.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            ResourceDTO newResource = new ResourceDTO();
            newResource.setName(name);
            newResource.setType(type);
            newResource.setQuantity(quantity);
            newResource.setUnit(unit);
            newResource.setCantidad(cantidad);
            newResource.setStorage(storage);

            try {
                resourceService.addResource(newResource);
                Notification.show("Recurso añadido con éxito.", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
                resourceGrid.setItems(resourceService.getResources()); // Actualizar la tabla
            } catch (Exception e) {
                Notification.show("Error al añadir el recurso: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Botón para cancelar
        Button cancelButton = new Button("Cancelar", event -> dialog.close());

        HorizontalLayout nameTypeLayout = new HorizontalLayout(nameField, typeField);
        nameTypeLayout.setWidthFull();
        HorizontalLayout quantityUnitLayout = new HorizontalLayout(quantityField, unitField);
        quantityUnitLayout.setWidthFull();

        // Layout de botones
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Agregar componentes al diálogo
        VerticalLayout dialogLayout = new VerticalLayout(title, nameTypeLayout, quantityUnitLayout, storageField, buttonLayout);
        dialog.add(dialogLayout);

        // Abrir el diálogo
        dialog.open();
    }

}


