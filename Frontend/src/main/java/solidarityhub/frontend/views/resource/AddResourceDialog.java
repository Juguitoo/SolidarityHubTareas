package solidarityhub.frontend.views.resource;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.model.Storage;
import solidarityhub.frontend.service.ResourceService;

public class AddResourceDialog {

    private final ResourceService resourceService;
    private final Grid<ResourceDTO> resourceGrid;

    public AddResourceDialog(ResourceService resourceService, Grid<ResourceDTO> resourceGrid) {
        this.resourceService = resourceService;
        this.resourceGrid = resourceGrid;
    }

    public void openAddResourceDialog() {
        // Crear el diálogo
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setHeight("500px");

        // Título del diálogo
        H3 title = new H3("Añadir nuevo recurso");

        // Campos del formulario
        TextField nameField = new TextField("Nombre del recurso");
        TextField typeField = new TextField("Tipo de recurso");
        NumberField quantityField = new NumberField("Cantidad");
        TextField unitField = new TextField("Unidad de medida");
        TextField storageField = new TextField("Almacén");

        // Botón para guardar
        Button saveButton = new Button("Guardar", event -> {
            // Lógica para guardar el recurso
            String name = nameField.getValue();
            String type = typeField.getValue();
            Double quantity = quantityField.getValue();
            String unit = unitField.getValue();
            String cantidad = String.valueOf(quantity) + " " + unit;
            Storage storage = new Storage();
            storage.setName(storageField.getValue());

            if (name.isEmpty() || type.isEmpty() || quantity == null || unit.isEmpty() ) {
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
        HorizontalLayout quantityUnitLayout = new HorizontalLayout(quantityField, unitField);

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
