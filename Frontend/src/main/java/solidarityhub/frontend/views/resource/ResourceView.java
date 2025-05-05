package solidarityhub.frontend.views.resource;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
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
import solidarityhub.frontend.model.enums.ResourceType;
import solidarityhub.frontend.service.ResourceService;
import solidarityhub.frontend.service.StorageService;

import java.util.*;


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
    }

    //===============================Get Components=========================================
    private Component getButtons() {
        // Botón para registrar nuevo recurso
        Button addResourceButton = new Button("Registrar nuevo recurso", new Icon("vaadin", "plus"));
        AddResourceDialog addResourceDialog = new AddResourceDialog(resourceService, storageService, resourceGrid, selectedCatastrophe);
        addResourceButton.addClickListener(e -> addResourceDialog.openAddResourceDialog());
        addResourceButton.addClassName("add-resource-button");

        HorizontalLayout filterLayout = new HorizontalLayout(addResourceButton);
        filterLayout.setAlignItems(Alignment.CENTER);
        filterLayout.setWidthFull();
        filterLayout.setJustifyContentMode(JustifyContentMode.START);

        return filterLayout;
    }

    private void getGridColumns(){
        nameColumn = resourceGrid.addColumn(ResourceDTO::getName)
                .setHeader("Nombre").setAutoWidth(true);

        typeColumn = resourceGrid.addColumn(resource -> translateResourceType(resource.getType()))
                .setHeader("Tipo").setAutoWidth(true);

        Grid.Column<ResourceDTO> quantityColumn = resourceGrid.addColumn(ResourceDTO::getCantidad)
                .setHeader("Cantidad").setAutoWidth(true);

        storageColumn = resourceGrid.addColumn(resource -> {
                    if (resource.getStorageId() != null) {
                        return storageDTOMap.get(resource.getStorageId()).getName();
                    } else {
                        return "No asignado";
                    }
                }).setHeader("Almacén").setAutoWidth(true);

        statusColumn = resourceGrid.addColumn(new ComponentRenderer<>(resource -> {
                    double quantity = resource.getQuantity();
                    int min = 10;
                    int max = 50;
                    return getResourceStatus(quantity, min, max);
                }))
                .setHeader("Estado").setSortable(true).setAutoWidth(true);

        resourceGrid.addItemDoubleClickListener(event -> {
            ResourceDTO resource = event.getItem();
            EditResourceDialog dialog = new EditResourceDialog(
                    resourceService,
                    storageService,
                    resourceGrid,
                    selectedCatastrophe,
                    resource);
            dialog.openEditResourceDialog();
        });
    }

    private void getGridFilter(){
        HeaderRow filterRow = resourceGrid.appendHeaderRow();

        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder("Buscar recurso");
        nameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        nameFilter.addValueChangeListener(event -> {
            resourceDataProvider.clearFilters();
            if (!event.getValue().isEmpty()) {
                resourceDataProvider.setFilter(resource ->
                        StringUtils.containsIgnoreCase(resource.getName(), event.getValue()));
            }
        });
        filterRow.getCell(nameColumn).setComponent(nameFilter);

        MultiSelectComboBox<ResourceType> typeFilter = new MultiSelectComboBox<>();
        typeFilter.setPlaceholder("Filtrar por tipo");
        typeFilter.setItems(ResourceType.values());
        typeFilter.setItemLabelGenerator(this::translateResourceType);
        typeFilter.addValueChangeListener(event -> {
            resourceDataProvider.clearFilters();
            Set<ResourceType> selectedTypes = event.getValue();
            if(!selectedTypes.isEmpty()) {
                resourceDataProvider.addFilter(resource ->
                        selectedTypes.contains(resource.getType())
                );
            }
        });
        filterRow.getCell(typeColumn).setComponent(typeFilter);

        MultiSelectComboBox<String> storageFilter = new MultiSelectComboBox<>();
        storageFilter.setPlaceholder("Filtrar por almacén");
        storageFilter.setItems(storageNames);
        storageFilter.addValueChangeListener(event -> {
            resourceDataProvider.clearFilters();
            Set<String> selectedStorages = event.getValue();
            if (!selectedStorages.isEmpty()) {
                resourceDataProvider.addFilter(resource -> {
                    String storageName = resource.getStorageId() != null ?
                            storageDTOMap.get(resource.getStorageId()).getName() :
                            "No asignado";
                    return selectedStorages.contains(storageName);
                });
            }
        });
        filterRow.getCell(storageColumn).setComponent(storageFilter);

        MultiSelectComboBox<String> statusFilter = new MultiSelectComboBox<>();
        statusFilter.setPlaceholder("Filtrar por estado");
        statusFilter.setItems("Stock bajo", "Stock medio", "Stock alto", "Stock no disponible");
        statusFilter.addValueChangeListener(event -> {
            resourceDataProvider.clearFilters();
            Set<String> selectedStatuses = event.getValue();
            if (!selectedStatuses.isEmpty()) {
                resourceDataProvider.addFilter(resource -> {
                    double quantity = resource.getQuantity();
                    int min = 10;
                    int max = 50;
                    String status = getResourceStatus(quantity, min, max).getText();
                    return selectedStatuses.contains(status);
                });
            }
        });
        filterRow.getCell(statusColumn).setComponent(statusFilter);
    }

    private Span getResourceStatus(double quantity, int min, int max) {
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

    public void refreshGrid() {
        List<ResourceDTO> resources = getResourceList();
        resourceDataProvider = new ListDataProvider<>(resources);
        resourceGrid.setItems(resources);
        resourceGrid.getDataProvider().refreshAll();
    }
}