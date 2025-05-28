package solidarityhub.frontend.views.resources.resource;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.server.VaadinSession;
import org.pingu.domain.enums.ResourceType;
import solidarityhub.frontend.dto.StorageDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.StorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilterResourcesDialog extends Dialog {
    private StorageService storageService;

    private Select<ResourceType> resourceTypeFilter = new Select<>();
    private Select<StorageDTO> storageFilter = new Select<>();
    private NumberField quantityFilter = new NumberField();

    private List<StorageDTO> storageList = new ArrayList<>();
    private final Translator translator = new Translator();

    public FilterResourcesDialog() {
        storageService = new StorageService();

        translator.initializeTranslator();

        buildView();
        loadFiltersData();
    }

    public void buildView() {
        setHeaderTitle(translator.get("filter_resource_title"));

        add(getFilters());
        getFooter().add(getButtons());
    }

    private void loadFiltersData() {
        if (ResourceView.quantityFilterValue != null && !ResourceView.quantityFilterValue.isEmpty()) {
            quantityFilter.setValue(Double.parseDouble(ResourceView.quantityFilterValue));
        }
        if (ResourceView.typeFilterValue != null && !ResourceView.typeFilterValue.isEmpty()) {
            resourceTypeFilter.setValue(ResourceType.valueOf(ResourceView.typeFilterValue));
        }
        if (ResourceView.storageFilterValue != null && !ResourceView.storageFilterValue.isEmpty()) {
            storageList.forEach(storageDTO -> {
                if (storageDTO.getId() == Integer.parseInt(ResourceView.storageFilterValue)) {
                    storageFilter.setValue(storageDTO);
                }
            });
        }
    }

    private Component getFilters() {
        VerticalLayout filters = new VerticalLayout();

        resourceTypeFilter.setItems(ResourceType.values());
        resourceTypeFilter.setItemLabelGenerator(this::translateResourceType);
        resourceTypeFilter.setWidthFull();
        resourceTypeFilter.setLabel(translator.get("filter_resource_type_title") + ":");
        resourceTypeFilter.setHelperText(translator.get("filter_resource_type_helper"));

        HorizontalLayout typeFilterLayout = new HorizontalLayout();
        typeFilterLayout.setWidthFull();
        typeFilterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        typeFilterLayout.add(resourceTypeFilter, getClearFilter(resourceTypeFilter));

        storageList = storageService.getStorages();
        storageFilter.setItems(storageList);
        storageFilter.setItemLabelGenerator(StorageDTO::getName);
        storageFilter.setWidthFull();
        storageFilter.setLabel(translator.get("filter_resource_storage_title") + ":");
        storageFilter.setHelperText(translator.get("filter_resource_storage_helper"));

        HorizontalLayout storageFilterLayout = new HorizontalLayout();
        storageFilterLayout.setWidthFull();
        storageFilterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        storageFilterLayout.add(storageFilter, getClearFilter(storageFilter));

        quantityFilter.setLabel(translator.get("filter_resource_quantity_title") + ":");
        quantityFilter.setWidthFull();
        quantityFilter.setHelperText(translator.get("filter_resource_quantity_helper"));

        HorizontalLayout quantityFilterLayout = new HorizontalLayout();
        quantityFilterLayout.setWidthFull();
        quantityFilterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        quantityFilterLayout.add(quantityFilter, getClearFilter(quantityFilter));

        filters.add(typeFilterLayout, storageFilterLayout, quantityFilterLayout);
        filters.setAlignItems(FlexComponent.Alignment.START);
        filters.setSpacing(false);
        filters.getStyle().set("row-gap", "0px");
        return filters;
    }

    public HorizontalLayout getButtons() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();

        Button applyButton = new Button(translator.get("apply_filters"));
        applyButton.addClickListener(event -> {
            close();
        });

        Button clearButton = new Button(translator.get("clear_filters"));
        clearButton.addClickListener(event -> {
            resourceTypeFilter.clear();
            storageFilter.clear();
            quantityFilter.clear();
        });
        HorizontalLayout clearButtonLayout = new HorizontalLayout(clearButton);
        clearButtonLayout.setWidthFull();
        clearButtonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        buttons.add(clearButtonLayout, applyButton);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return buttons;
    }

    public List<String> getSelectedFilters() {
        List<String> filters = new ArrayList<>();
        if(resourceTypeFilter.getValue() == null) {
            filters.add("");
        } else {
            filters.add(resourceTypeFilter.getValue().toString());
        }
        if(storageFilter.getValue() == null) {
            filters.add("");
        } else {
            filters.add(String.valueOf(storageFilter.getValue().getId()));
        }
        if(quantityFilter.getValue() == null) {
            filters.add("");
        } else {
            filters.add(quantityFilter.getValue().toString());
        }
        return filters;
    }

    public Button getClearFilter(Component component) {
        Button clearButton = new Button(new Icon("vaadin", "close-big"));
        clearButton.addClickListener(event -> {
            if (component instanceof Select) {
                ((Select<?>) component).clear();
            } else if (component instanceof NumberField) {
                ((NumberField) component).clear();
            }
        });
        clearButton.getStyle().set("margin-top", "15px");
        return clearButton;
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
