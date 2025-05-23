package solidarityhub.frontend.views.resources.resource;

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
import com.vaadin.flow.router.PageTitle;
import org.apache.commons.lang3.StringUtils;
import solidarityhub.frontend.dto.StorageDTO;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.ResourceDTO;
import org.pingu.domain.enums.ResourceType;
import solidarityhub.frontend.service.ResourceService;
import solidarityhub.frontend.service.StorageService;
import java.util.*;
import java.util.List;


@PageTitle("Recursos")
public class ResourceView extends VerticalLayout{

    private final ResourceService resourceService;
    private final StorageService storageService;
    protected final CatastropheService catastropheService;

    protected CatastropheDTO selectedCatastrophe;

    private final Grid<ResourceDTO> resourceGrid;
    private ListDataProvider<ResourceDTO> resourceDataProvider;

    private Grid.Column<ResourceDTO> nameColumn;
    private Grid.Column<ResourceDTO> typeColumn;
    private Grid.Column<ResourceDTO> storageColumn;
    private Grid.Column<ResourceDTO> statusColumn;

    private String nameFilterValue = "";
    private Set<ResourceType> typeFilterValues = new HashSet<>();
    private Set<String> storageFilterValues = new HashSet<>();
    private Set<String> statusFilterValues = new HashSet<>();

    private final HashMap<Integer, StorageDTO> storageDTOMap = new HashMap<>();
    private final List<String> storageNames = new ArrayList<>();


    public ResourceView(CatastropheDTO catastrophe) {
        this.resourceService = new ResourceService();
        this.storageService = new StorageService();
        this.catastropheService = new CatastropheService();

        this.selectedCatastrophe = catastrophe;

        this.resourceGrid = new Grid<>(ResourceDTO.class, false);
        for (StorageDTO s : storageService.getStorages()){
            storageDTOMap.put(s.getId(), s);
            storageNames.add(s.getName());
        }
        buildView();
    }

    private void buildView() {
        removeAll();

        setSizeFull();
        addClassName("resources-view");
        setPadding(false);

        add(getButtons(), resourceGrid);
        populateResourceGrid();
    }

    //=============================== Grid Methods =========================================
    private List<ResourceDTO> getResourceList() {
        if (selectedCatastrophe != null) {
            return resourceService.getResourcesByCatastropheId(selectedCatastrophe.getId());
        } else {
            return Collections.emptyList();
        }
    }

    private void populateResourceGrid() {
        this.resourceDataProvider = new ListDataProvider<>(getResourceList());

        if (resourceDataProvider.getItems().isEmpty()) {
            resourceGrid.setVisible(false);
            add(new Span("No hay recursos disponibles para esta catástrofe."));
        } else {
            resourceGrid.setVisible(true);
            resourceGrid.setDataProvider(resourceDataProvider);

            getGridColumns();
            getGridFilter();
        }

        resourceGrid.addItemDoubleClickListener(event -> {
            ResourceDTO resource = event.getItem();
            EditResourceDialog dialog = new EditResourceDialog(selectedCatastrophe, resource);
            dialog.open();
            dialog.addOpenedChangeListener(dialogEvent -> {
                if (!dialogEvent.isOpened()) {
                    refreshResources();
                }
            });
        });

        resourceGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        resourceGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private void refreshResources() {
        resourceService.clearCache();
        resourceGrid.setItems(getResourceList());
        resourceGrid.getDataProvider().refreshAll();
    }

    //=============================== Get Components =========================================
    private Component getButtons() {
        Button addResourceButton = new Button("Registrar nuevo recurso", new Icon("vaadin", "plus"));
        addResourceButton.addClassName("add-resource-button");

        AddResourceDialog addResourceDialog = new AddResourceDialog(selectedCatastrophe);
        addResourceButton.addClickListener(e -> {
            addResourceDialog.open();
            addResourceDialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    refreshResources();
                }
            });
        });

        HorizontalLayout filterLayout = new HorizontalLayout(addResourceButton);
        filterLayout.setAlignItems(Alignment.CENTER);
        filterLayout.setWidthFull();
        filterLayout.setJustifyContentMode(JustifyContentMode.START);

        return filterLayout;
    }

    private void getGridColumns(){
        nameColumn = resourceGrid.addColumn(ResourceDTO::getName).setAutoWidth(true);

        typeColumn = resourceGrid.addColumn(resource -> translateResourceType(resource.getType())).setAutoWidth(true);

        resourceGrid.addColumn(ResourceDTO::getCantidad).setHeader("Cantidad").setAutoWidth(true);

        storageColumn = resourceGrid.addColumn(resource -> {
                    if (resource.getStorageId() != null) {
                        return storageDTOMap.get(resource.getStorageId()).getName();
                    } else {
                        return "No asignado";
                    }
                }).setAutoWidth(true);

        statusColumn = resourceGrid.addColumn(new ComponentRenderer<>(this::getResourceStatus)).setAutoWidth(true);
    }

    private void getGridFilter() {
        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder("Buscar recurso");
        nameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        nameFilter.setClearButtonVisible(true);
        nameFilter.addValueChangeListener(event -> {
            nameFilterValue = event.getValue();
            applyAllFilters();
        });
        nameColumn.setHeader(getGridFilterHeader("Nombre", nameFilter));

        MultiSelectComboBox<ResourceType> typeFilter = new MultiSelectComboBox<>();
        typeFilter.setPlaceholder("Filtrar por tipo");
        typeFilter.setItems(ResourceType.values());
        typeFilter.setItemLabelGenerator(this::translateResourceType);
        typeFilter.addValueChangeListener(event -> {
            typeFilterValues = event.getValue();
            applyAllFilters();
        });
        typeColumn.setHeader(getGridFilterHeader("Tipo", typeFilter));

        MultiSelectComboBox<String> storageFilter = new MultiSelectComboBox<>();
        storageFilter.setPlaceholder("Filtrar por almacén");
        storageFilter.setItems(storageNames);
        storageFilter.addValueChangeListener(event -> {
            storageFilterValues = event.getValue();
            applyAllFilters();
        });
        storageColumn.setHeader(getGridFilterHeader("Almacén", storageFilter));

        MultiSelectComboBox<String> statusFilter = new MultiSelectComboBox<>();
        statusFilter.setPlaceholder("Filtrar por estado");
        statusFilter.setItems("Stock bajo", "Stock medio", "Stock alto", "Stock no disponible");
        statusFilter.addValueChangeListener(event -> {
            statusFilterValues = event.getValue();
            applyAllFilters();
        });
        statusColumn.setHeader(getGridFilterHeader("Estado", statusFilter));
    }

    private Span getResourceStatus(ResourceDTO resource) {
        Span badge = new Span();
        badge.setText("Stock no disponible");
        badge.addClassName("resource__status");

        double quantity = resource.getQuantity();
        int min = 10;
        int max = 50;

        if (quantity < min) {
            badge.setText("Stock bajo");
            badge.removeClassNames("status--medium", "status--high");
            badge.addClassName("status--low");
        } else if (quantity < max) {
            badge.setText("Stock medio");
            badge.removeClassNames("status--low", "status--high");
            badge.addClassName("status--medium");
        } else {
            badge.setText("Stock alto");
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

    //=============================== Other Methods =========================================
    private void applyAllFilters() {
        resourceDataProvider.clearFilters();

        if (!nameFilterValue.isEmpty()) {
            resourceDataProvider.addFilter(resource ->
                    StringUtils.containsIgnoreCase(resource.getName(), nameFilterValue));
        }

        if (!typeFilterValues.isEmpty()) {
            resourceDataProvider.addFilter(resource ->
                    typeFilterValues.contains(resource.getType()));
        }

        if (!storageFilterValues.isEmpty()) {
            resourceDataProvider.addFilter(resource -> {
                String storageName = resource.getStorageId() != null ?
                        storageDTOMap.get(resource.getStorageId()).getName() :
                        "No asignado";
                return storageFilterValues.contains(storageName);
            });
        }

        if (!statusFilterValues.isEmpty()) {
            resourceDataProvider.addFilter(resource -> {
                return statusFilterValues.contains(getResourceStatus(resource).getText());
            });
        }
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