package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.ResourceAssignmentDTO;
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.ResourceAssignmentService;
import solidarityhub.frontend.service.ResourceService;

import java.util.List;
import java.util.Locale;

public class AssignResourceDialog extends Dialog {
    private final ResourceService resourceService;
    private final ResourceAssignmentService assignmentService;
    private final TaskDTO selectedTask;
    private static Translator translator;

    private ComboBox<ResourceDTO> resourceComboBox;
    private NumberField quantityField;
    private TextField unitField;
    private Span availableQuantitySpan;

    public AssignResourceDialog(TaskDTO selectedTask) {
        this.resourceService = new ResourceService();
        this.assignmentService = new ResourceAssignmentService();
        this.selectedTask = selectedTask;

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

    private void buildView() {
        setHeaderTitle(translator.get("assign_resource_title"));

        VerticalLayout content = new VerticalLayout();
        content.add(createForm());

        add(content);

        getFooter().add(createButtons());
    }

    private Component createForm() {
        FormLayout formLayout = new FormLayout();

        List<ResourceDTO> resources = resourceService.getResourcesByCatastropheId(selectedTask.getCatastropheId());

        resourceComboBox = new ComboBox<>(translator.get("select_resource"));
        resourceComboBox.setItems(resources);
        resourceComboBox.setItemLabelGenerator(resource ->
                resource.getName() + " (" + resource.getCantidad() + ")");
        resourceComboBox.setRequired(true);
        resourceComboBox.setWidthFull();

        resourceComboBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                ResourceDTO selectedResource = event.getValue();
                unitField.setValue(selectedResource.getUnit());

                // Get available quantity
                Double available = assignmentService.getAvailableQuantity(selectedResource.getId());
                availableQuantitySpan.setText(translator.get("available_quantity") + ": " +
                        available + " " + selectedResource.getUnit());

                // Set max value for quantity field
                quantityField.setMax(available);
            } else {
                availableQuantitySpan.setText("");
                unitField.clear();
            }
        });

        quantityField = new NumberField(translator.get("quantity"));
        quantityField.setMin(0.01);
        quantityField.setStep(0.01);
        quantityField.setRequiredIndicatorVisible(true);

        unitField = new TextField(translator.get("unit"));
        unitField.setReadOnly(true);

        availableQuantitySpan = new Span();

        formLayout.add(resourceComboBox, quantityField, unitField, availableQuantitySpan);

        return formLayout;
    }

    private Component createButtons() {
        HorizontalLayout buttonLayout = new HorizontalLayout();

        Button cancelButton = new Button(translator.get("cancel_button"), event -> close());

        Button saveButton = new Button(translator.get("assign_button"), event -> {
            if (validateForm()) {
                assignResource();
            } else {
                Notification.show(translator.get("check_fields"), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        buttonLayout.add(cancelButton, saveButton);

        return buttonLayout;
    }

    private boolean validateForm() {
        return resourceComboBox.getValue() != null &&
                quantityField.getValue() != null &&
                quantityField.getValue() > 0;
    }

    private void assignResource() {
        ResourceDTO selectedResource = resourceComboBox.getValue();
        double quantity = quantityField.getValue();

        ResourceAssignmentDTO assignment = assignmentService.assignResourceToTask(
                selectedTask.getId(),
                selectedResource.getId(),
                quantity,
                unitField.getValue()
        );

        if (assignment != null) {
            Notification.show(
                    translator.get("resource_assigned_success"),
                    3000,
                    Notification.Position.BOTTOM_START
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
        } else {
            Notification.show(
                    translator.get("resource_assigned_error"),
                    3000,
                    Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
