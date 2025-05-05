package solidarityhub.frontend.views.resource;

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

public class AddResourceDialog extends Dialog{

    private final ResourceService resourceService;
    private final StorageService storageService;
    private final CatastropheDTO selectedCatastrophe;

    private TextField nameField;
    private Select<ResourceType> typeField;
    private NumberField quantityField;
    private TextField unitField;
    private Select<StorageDTO> storageField;

    public AddResourceDialog(CatastropheDTO selectedCatastrophe) {
        this.resourceService = new ResourceService();
        this.storageService = new StorageService();
        this.selectedCatastrophe = selectedCatastrophe;

        buildView();
    }

    public void buildView() {
        setHeaderTitle("Añadir nuevo recurso");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(getForms());

        add(dialogLayout);
        getFooter().add(getButtons());
    }

    private Component getForms(){
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
        storageField.setItemLabelGenerator(storageAux -> String.format("%s - %s - %s", storageAux.getName(), "address", storageAux.isFull() ? "Lleno" : "Disponible"));

        List<StorageDTO> storages = storageService.getStorages();
        storageField.setItems(storages);

        formLayout.add(nameField, typeField, quantityField, unitField, storageField);
        return formLayout;
    }

    private Component getButtons(){
        HorizontalLayout buttonLayout = new HorizontalLayout();

        Button saveButton = new Button("Guardar", event -> {
            String name = nameField.getValue();
            ResourceType type = typeField.getValue();
            Double quantity = quantityField.getValue();
            String unit = unitField.getValue();
            StorageDTO selectedStorage = storageField.getValue();

            if (name.isEmpty() || type == null || quantity == null || unit.isEmpty() ) {
                Notification.show("Por favor, completa todos los campos.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            ResourceDTO newResource = new ResourceDTO(
                    name,
                    type,
                    quantity,
                    unit,
                    // Si se selecciona un almacén, se asigna el ID del almacén al recurso
                    selectedStorage != null ? selectedStorage.getId() : null,
                    selectedCatastrophe.getId()
            );

            try {
                resourceService.addResource(newResource);
                Notification.show("Recurso añadido con éxito.", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                close();
            } catch (Exception e) {
                Notification.show("Error al añadir el recurso: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelButton = new Button("Cancelar", event -> close());

        buttonLayout.add(cancelButton, saveButton);

        return buttonLayout;
    }

}