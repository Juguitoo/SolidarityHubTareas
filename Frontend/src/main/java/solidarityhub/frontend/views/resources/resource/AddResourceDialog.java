package solidarityhub.frontend.views.resources.resource;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.dto.StorageDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.model.enums.ResourceType;
import solidarityhub.frontend.service.ResourceService;
import solidarityhub.frontend.service.StorageService;

import java.util.List;
import java.util.Locale;

public class AddResourceDialog extends Dialog {

    protected final ResourceService resourceService;
    protected final StorageService storageService;
    protected final CatastropheDTO selectedCatastrophe;
    protected static Translator translator;

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

        initializeTranslator();
        buildView();
    }

    private void initializeTranslator() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());
    }

    public void buildView() {
        setHeaderTitle(resource == null ?
                translator.get("add_resource_button") :
                translator.get("edit_resource_button"));
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
        nameField = new TextField(translator.get("resource_name"));
        nameField.setRequiredIndicatorVisible(true);
        nameField.setRequired(true);

        typeField = new Select<>();
        typeField.setLabel(translator.get("resource_type"));
        typeField.setItems(ResourceType.values());
        typeField.setItemLabelGenerator(this::translateResourceType);
        typeField.setRequiredIndicatorVisible(true);

        quantityField = new NumberField(translator.get("resource_quantity"));
        quantityField.setRequiredIndicatorVisible(true);
        quantityField.setRequired(true);

        unitField = new TextField(translator.get("unit"));
        unitField.setRequiredIndicatorVisible(true);
        unitField.setRequired(true);

        storageField = new Select<>();
        storageField.setLabel(translator.get("resource_storage"));
        storageField.setItemLabelGenerator(storageAux ->
                String.format("%s - %s", storageAux.getName(),
                        storageAux.isFull() ? translator.get("storage_full") : translator.get("storage_available"))
        );

        List<StorageDTO> storages = storageService.getStorages();
        storageField.setItems(storages);

        formLayout.add(nameField, typeField, quantityField, unitField, storageField);
        return formLayout;
    }

    protected Component getButtons() {
        HorizontalLayout buttonLayout = new HorizontalLayout();

        Button cancelButton = new Button(translator.get("cancel_button"), event -> close());
        Button saveButton = new Button(
                resource == null ? translator.get("save_button") : translator.get("edit_button"),
                event -> saveResource()
        );

        buttonLayout.add(cancelButton);

        if (resource != null) {
            Button deleteButton = new Button(translator.get("delete_button"), event -> deleteResource());
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
            Notification.show(translator.get("check_fields"), 3000, Notification.Position.MIDDLE)
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
                Notification.show(translator.get("resource_added_success"), 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                // Actualizar recurso existente
                resourceService.updateResource(resource.getId(), resourceToSave);
                Notification.show(translator.get("resource_updated_success"), 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            close();
        } catch (Exception e) {
            String action = resource == null ? translator.get("add_action") : translator.get("update_action");
            Notification.show(translator.get("error_prefix") + " " + action + " " +
                                    translator.get("resource_lowercase") + ": " + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    protected void deleteResource() {
        if (resource == null) return;

        try {
            resourceService.deleteResource(resource.getId());
            Notification.show(translator.get("resource_deleted_success"), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
        } catch (Exception e) {
            Notification.show(translator.get("error_deleting_resource") + ": " + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String translateResourceType(ResourceType type) {
        if (type == null) return "";
        return switch (type) {
            case FOOD -> translator.get("resource_type_food");
            case MEDICINE -> translator.get("resource_type_medicine");
            case CLOTHING -> translator.get("resource_type_clothing");
            case SHELTER -> translator.get("resource_type_shelter");
            case TOOLS -> translator.get("resource_type_tools");
            case FUEL -> translator.get("resource_type_fuel");
            case SANITATION -> translator.get("resource_type_sanitation");
            case COMMUNICATION -> translator.get("resource_type_communication");
            case TRANSPORTATION -> translator.get("resource_type_transportation");
            case BUILDING -> translator.get("resource_type_building");
            case MONETARY -> translator.get("resource_type_monetary");
            case STATIONERY -> translator.get("resource_type_stationery");
            case LOGISTICS -> translator.get("resource_type_logistics");
            case OTHER -> translator.get("resource_type_other");
        };
    }
}