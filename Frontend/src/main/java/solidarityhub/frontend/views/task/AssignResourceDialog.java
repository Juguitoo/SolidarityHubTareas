package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.*;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.ResourceAssignmentService;
import solidarityhub.frontend.service.ResourceService;
import java.util.*;

public class AssignResourceDialog extends Dialog {
    private final ResourceService resourceService;
    private final CatastropheService catastropheService;

    private final ResourceAssignmentService assignmentService;
    private final TaskDTO selectedTask;
    private final CatastropheDTO selectedCatastrophe;
    private static Translator translator = new Translator();

    private List<ResourceAssignmentDTO> resourcesToAssign = new ArrayList<>();
    private Grid<ResourceAssignmentDTO> resourcesGrid;

    private ComboBox<ResourceQuantityDTO> resourceComboBox;
    private NumberField quantityField;

    public AssignResourceDialog(CatastropheDTO selectedCatastrophe) {
        this.resourceService = new ResourceService();
        this.catastropheService = new CatastropheService();

        this.assignmentService = new ResourceAssignmentService();
        this.selectedCatastrophe = selectedCatastrophe;
        this.selectedTask = null;

        resourcesToAssign.addAll(getAssignedResourcesFromSession());

        translator.initializeTranslator();
        buildView();
    }

    public AssignResourceDialog(TaskDTO selectedTask, CatastropheDTO selectedCatastrophe) {
        this.catastropheService = new CatastropheService();
        this.assignmentService = new ResourceAssignmentService();

        this.selectedCatastrophe = selectedCatastrophe;
        this.resourceService = new ResourceService();
        this.selectedTask = selectedTask;

        Set<ResourceAssignmentDTO> uniqueResources = new HashSet<>();
        uniqueResources.addAll(getAssignedResourcesFromSession());
        uniqueResources.addAll(assignmentService.getAssignmentsByTask(selectedTask.getId()));

        this.resourcesToAssign = new ArrayList<>(uniqueResources);

        translator.initializeTranslator();
        buildView();
    }

    private void buildView() {
        setWidth("600px");
        setHeaderTitle(translator.get("assign_resource_title"));

        add(createForm(), createGrid());

        getFooter().add(createButtons());
    }

    //===============================Task actions=========================================
    private Component createForm() {
        FormLayout formLayout = new FormLayout();


        List<ResourceQuantityDTO> availableResources = resourceService.getResourcesAndAvailableQuantities(selectedCatastrophe.getId());
        resourceComboBox = new ComboBox<>(translator.get("select_resource"));
        resourceComboBox.setItems(availableResources);
        resourceComboBox.setItemLabelGenerator(r -> r.getResource().getName() + " (" + r.getAvailableQuantity() + " " + r.getResource().getUnit() + ")");
        resourceComboBox.setRequired(true);
        resourceComboBox.setWidthFull();

        Span unitSpan = new Span();

        resourceComboBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                ResourceDTO selectedResource = event.getValue().getResource();
                unitSpan.setText(selectedResource.getUnit());

                Double available = assignmentService.getAvailableQuantity(selectedResource.getId());

                // Fix: Validate available quantity before setting max
                if (available != null && available > 0) {
                    quantityField.setMax(available);
                } else {
                    quantityField.setMax(1000.0); // Set reasonable default
                    quantityField.clear(); // Clear any existing value
                }
            }
        });

        quantityField = new NumberField(translator.get("quantity"));
        quantityField.setStepButtonsVisible(true);
        quantityField.setMin(0.5);
        quantityField.setStep(0.5);
        quantityField.setRequiredIndicatorVisible(true);

        Icon plusIcon = VaadinIcon.PLUS.create();
        Button addButton = new Button(plusIcon);
        addButton.addClickListener(e -> {
            if (validateForm()) {
                addResourceToGrid();
            } else {
                Notification.show(translator.get("check_fields"), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        formLayout.add(resourceComboBox, quantityField, unitSpan, addButton);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2),
                new FormLayout.ResponsiveStep("500px", 4)
        );

        return formLayout;
    }

    private Component createGrid() {
        HorizontalLayout gridContainer = new HorizontalLayout();
        gridContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        gridContainer.setPadding(false);

        if (resourcesToAssign.isEmpty()) {
            return new Span(translator.get("no_resources_assigned"));
        }

        resourcesGrid = new Grid<>(ResourceAssignmentDTO.class, false);
        resourcesGrid.addColumn(ResourceAssignmentDTO::getResourceName).setHeader(translator.get("resource_name"));
        resourcesGrid.addColumn(ResourceAssignmentDTO::getQuantity).setHeader(translator.get("quantity"));
        resourcesGrid.addColumn(ResourceAssignmentDTO::getUnits).setHeader(translator.get("unit"));

        resourcesGrid.setItems(resourcesToAssign);

        gridContainer.add(resourcesGrid, getDeleteButton());
        return gridContainer;

    }

    private Component getDeleteButton() {
        Icon minusIcon = VaadinIcon.MINUS.create();
        Button deleteResourceButton = new Button(minusIcon);
        deleteResourceButton.addClickListener(e -> {
            Set<ResourceAssignmentDTO> selectedItems = resourcesGrid.getSelectedItems();
            resourcesToAssign.removeAll(selectedItems);
            refreshGrid();
        });

        return deleteResourceButton;
    }

    private Component createButtons() {
        HorizontalLayout buttonLayout = new HorizontalLayout();

        Button cancelButton = new Button(translator.get("cancel_button"), event -> close());

        Button saveButton = new Button(translator.get("assign_button"), event -> {
            saveAssignedResource();
            close();
        });

        buttonLayout.add(cancelButton, saveButton);

        return buttonLayout;
    }

    //===============================Save Assigned Resources=========================================
    private void addResourceToGrid() {
        ResourceDTO selectedResource = resourceComboBox.getValue().getResource();
        double quantity = quantityField.getValue();

        if (selectedResource == null || quantity <= 0) {
            return;
        }

        boolean updated = false;

        for (ResourceAssignmentDTO existing : resourcesToAssign) {
            if (existing.getResourceId() == selectedResource.getId()) {
                existing.setQuantity(existing.getQuantity() + quantity);
                updated = true;
                break;
            }
        }

        if (!updated) {
            ResourceAssignmentDTO assignment;

            if (selectedTask != null) {
                assignment = new ResourceAssignmentDTO(
                        selectedTask.getId(),
                        selectedResource.getId(),
                        quantity,
                        selectedResource.getUnit()
                );
                assignment.setResourceName(selectedResource.getName());
            } else {
                assignment = new ResourceAssignmentDTO(
                        0,
                        selectedResource.getId(),
                        quantity,
                        selectedResource.getUnit()
                );
                assignment.setResourceName(selectedResource.getName());
            }
            VaadinSession.getCurrent().setAttribute(selectedResource.getName(), selectedResource.getId());
            resourcesToAssign.add(assignment);
        }

        refreshGrid();

        resourceComboBox.clear();
        quantityField.clear();
    }

    private void saveAssignedResource() {
        VaadinSession.getCurrent().setAttribute("assignedResources", resourcesToAssign);
    }

    private void refreshGrid() {
        if (resourcesGrid != null) {
            resourcesGrid.setItems(resourcesToAssign);
            resourcesGrid.deselectAll();
            resourcesGrid.getDataProvider().refreshAll();
        } else {
            removeAll();
            getFooter().removeAll();
            buildView();
        }
    }

    @SuppressWarnings("unchecked")
    private List<ResourceAssignmentDTO> getAssignedResourcesFromSession() {
        List<ResourceAssignmentDTO> resources = (List<ResourceAssignmentDTO>)
                VaadinSession.getCurrent().getAttribute("assignedResources");

        return resources != null ? resources : new ArrayList<>();
    }

    private boolean validateForm() {
        return resourceComboBox.getValue() != null &&
                quantityField.getValue() != null &&
                quantityField.getValue() > 0;
    }
}
