package solidarityhub.frontend.views.resources.storage;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.dto.StorageDTO;
import solidarityhub.frontend.model.enums.ResourceType;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.ResourceService;
import solidarityhub.frontend.service.StorageService;
import solidarityhub.frontend.views.resources.resource.AddResourceDialog;
import solidarityhub.frontend.views.resources.resource.EditResourceDialog;

import java.util.*;

public class StorageView extends VerticalLayout {
    private final ResourceService resourceService;
    private final StorageService storageService;
    protected final CatastropheService catastropheService;

    protected CatastropheDTO selectedCatastrophe;

    private final Grid<StorageDTO> storageGrid;
    private ListDataProvider<StorageDTO> storageDataProvider;

    private Grid.Column<StorageDTO> nameColumn;
    private Grid.Column<StorageDTO> typeColumn;
    private Grid.Column<StorageDTO> storageColumn;
    private Grid.Column<StorageDTO> statusColumn;

    public StorageView(CatastropheDTO catastrophe) {
        this.resourceService = new ResourceService();
        this.storageService = new StorageService();
        this.catastropheService = new CatastropheService();

        this.selectedCatastrophe = catastrophe;

        this.storageGrid = new Grid<>(StorageDTO.class, false);
        buildView();
    }

    private void buildView() {
        removeAll();

        setSizeFull();
        addClassName("resources-view");
        setPadding(false);

        add(getButtons(), storageGrid);
        populateStorageGrid();
    }

    //===============================Grid Methods=========================================
    private List<StorageDTO> getStorageList() {
        if (selectedCatastrophe != null) {
            return storageService.getStorages();
        } else {
            return Collections.emptyList();
        }
    }

    private void populateStorageGrid() {
        this.storageDataProvider = new ListDataProvider<>(getStorageList());

        if (storageDataProvider.getItems().isEmpty()) {
            storageGrid.setVisible(false);
            add(new Span("No hay recursos disponibles para esta catástrofe."));
        } else {
            storageGrid.setVisible(true);
            storageGrid.setDataProvider(storageDataProvider);

//            getGridColumns();
//            getGridFilter();
        }

        storageGrid.addItemDoubleClickListener(event -> {
//            StorageDTO storage = event.getItem();
//            EditResourceDialog dialog = new EditResourceDialog(selectedCatastrophe, storage);
//            dialog.open();
//            dialog.addOpenedChangeListener(dialogEvent -> {
//                if (!dialogEvent.isOpened()) {
//                    refreshStorage();
//                }
//            });
        });

        storageGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        storageGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private void refreshStorage() {
        resourceService.clearCache();
        storageGrid.setItems(getStorageList());
        storageGrid.getDataProvider().refreshAll();
    }

    //===============================Get Components=========================================
    private Component getButtons() {
        Button addResourceButton = new Button("Registrar nuevo recurso", new Icon("vaadin", "plus"));
        addResourceButton.addClassName("add-resource-button");

        AddResourceDialog addResourceDialog = new AddResourceDialog(selectedCatastrophe);
        addResourceButton.addClickListener(e -> {
            addResourceDialog.open();
            addResourceDialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    refreshStorage();
                }
            });
        });

        HorizontalLayout filterLayout = new HorizontalLayout(addResourceButton);
        filterLayout.setAlignItems(Alignment.CENTER);
        filterLayout.setWidthFull();
        filterLayout.setJustifyContentMode(JustifyContentMode.START);

        return filterLayout;
    }

//    private void getGridColumns(){
//        nameColumn = storageGrid.addColumn(StorageDTO::getName).setAutoWidth(true);
//
//        typeColumn = storageGrid.addColumn(resource -> translateResourceType(resource.getType())).setAutoWidth(true);
//
//        storageGrid.addColumn(ResourceDTO::getCantidad).setHeader("Cantidad").setAutoWidth(true);
//
//        storageColumn = storageGrid.addColumn(resource -> {
//            if (resource.getStorageId() != null) {
//                return storageDTOMap.get(resource.getStorageId()).getName();
//            } else {
//                return "No asignado";
//            }
//        }).setAutoWidth(true);
//
//        statusColumn = storageGrid.addColumn(new ComponentRenderer<>(resource -> {
//            double quantity = resource.getQuantity();
//            int min = 10;
//            int max = 50;
//            return getStorageStatus(quantity, min, max);
//        })).setAutoWidth(true);
//    }
//
//    private void getGridFilter(){
//        TextField nameFilter = new TextField();
//        nameFilter.setPlaceholder("Buscar recurso");
//        nameFilter.setValueChangeMode(ValueChangeMode.LAZY);
//        nameFilter.addValueChangeListener(event -> {
//            storageDataProvider.clearFilters();
//            if (!event.getValue().isEmpty()) {
//                storageDataProvider.setFilter(resource ->
//                        StringUtils.containsIgnoreCase(resource.getName(), event.getValue()));
//            }
//        });
//        nameColumn.setHeader(getGridFilterHeader("Nombre", nameFilter));
//
//        MultiSelectComboBox<ResourceType> typeFilter = new MultiSelectComboBox<>();
//        typeFilter.setPlaceholder("Filtrar por tipo");
//        typeFilter.setItems(ResourceType.values());
//        typeFilter.setItemLabelGenerator(this::translateResourceType);
//        typeFilter.addValueChangeListener(event -> {
//            storageDataProvider.clearFilters();
//            Set<ResourceType> selectedTypes = event.getValue();
//            if(!selectedTypes.isEmpty()) {
//                storageDataProvider.addFilter(resource ->
//                        selectedTypes.contains(resource.getType())
//                );
//            }
//        });
//        typeColumn.setHeader(getGridFilterHeader("Tipo", typeFilter));
//
//        MultiSelectComboBox<String> storageFilter = new MultiSelectComboBox<>();
//        storageFilter.setPlaceholder("Filtrar por almacén");
//        storageFilter.setItems(storageNames);
//        storageFilter.addValueChangeListener(event -> {
//            storageDataProvider.clearFilters();
//            Set<String> selectedStorages = event.getValue();
//            if (!selectedStorages.isEmpty()) {
//                storageDataProvider.addFilter(resource -> {
//                    String storageName = resource.getStorageId() != null ?
//                            storageDTOMap.get(resource.getStorageId()).getName() :
//                            "No asignado";
//                    return selectedStorages.contains(storageName);
//                });
//            }
//        });
//        storageColumn.setHeader(getGridFilterHeader("Almacén", storageFilter));
//
//        MultiSelectComboBox<String> statusFilter = new MultiSelectComboBox<>();
//        statusFilter.setPlaceholder("Filtrar por estado");
//        statusFilter.setItems("Stock bajo", "Stock medio", "Stock alto", "Stock no disponible");
//        statusFilter.addValueChangeListener(event -> {
//            storageDataProvider.clearFilters();
//            Set<String> selectedStatuses = event.getValue();
//            if (!selectedStatuses.isEmpty()) {
//                storageDataProvider.addFilter(resource -> {
//                    double quantity = resource.getQuantity();
//                    int min = 10;
//                    int max = 50;
//                    String status = getStorageStatus(quantity, min, max).getText();
//                    return selectedStatuses.contains(status);
//                });
//            }
//        });
//        statusColumn.setHeader(getGridFilterHeader("Estado", statusFilter));
//    }

    private Span getStorageStatus(double quantity, int min, int max) {
        Span badge = new Span();
        badge.setText("Stock no disponible");
        badge.addClassName("resource__status");

        if (quantity < min) {
            badge.setText("Stock bajo");
            badge.removeClassNames("status--medium", "status--high");
            badge.addClassName("status--low");
        } else if (quantity < max) {
            badge.setText("Stock medio");
            badge.removeClassNames("status--low", "status--high");
            badge.addClassName("status--medium");
        } else {
            badge.setText("Stock Alto");
            badge.removeClassNames("status--medium", "status--low");
            badge.addClassName("status--high");
        }

        return badge;
    }

    private Component getGridFilterHeader(String headerText, Component filterComponent) {
        HorizontalLayout filterHeader = new HorizontalLayout();
        Span filterTitle = new Span(headerText);
        filterHeader.setAlignItems(Alignment.CENTER);
        filterTitle.addClassName("grid__filter-title");
        filterHeader.add(filterTitle, filterComponent);

        filterHeader.addClassName("grid__filter-header");

        return filterHeader;
    }


    private String translateResourceType(ResourceType type) {
        if (type == null) return "";
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
        };
    }
}
