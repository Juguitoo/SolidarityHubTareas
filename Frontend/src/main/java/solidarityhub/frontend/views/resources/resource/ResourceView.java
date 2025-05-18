package solidarityhub.frontend.views.resources.resource;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import org.pingu.domain.enums.ResourceType;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.dto.StorageDTO;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.ResourceService;
import solidarityhub.frontend.service.StorageService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;


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

    public static String quantityFilterValue = "";
    public static String typeFilterValue = "";
    public static String storageFilterValue = "";

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

            return resourceService.getResources(typeFilterValue, quantityFilterValue, storageFilterValue, selectedCatastrophe.getId());
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

        Button filterButton = new Button("Filtrar recursos", new Icon("vaadin", "filter"));
        filterButton.addClassName("filter-button");

        Button clearFiltersButton = new Button("Limpiar filtros", new Icon("vaadin", "trash"));
        clearFiltersButton.addClassName("clear-filters-button");

        AddResourceDialog addResourceDialog = new AddResourceDialog(selectedCatastrophe);
        addResourceButton.addClickListener(e -> {
            addResourceDialog.open();
            addResourceDialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    refreshResources();
                }
            });
        });

        filterButton.addClickListener(e -> {
            CompletableFuture<List<String>> filters = openFilterDialog();
            filters.thenAccept(filterValues -> {
                if (!filterValues.isEmpty()) {
                    typeFilterValue = filterValues.get(0);
                    storageFilterValue = filterValues.get(1);
                    quantityFilterValue = filterValues.get(2);
                    refreshResources();
                }
            });

        });

        clearFiltersButton.addClickListener(e -> {
            typeFilterValue = "";
            storageFilterValue = "";
            quantityFilterValue = "";
            refreshResources();
        });

        HorizontalLayout filterLayout = new HorizontalLayout(clearFiltersButton, filterButton, addResourceButton);
        filterLayout.setAlignItems(Alignment.CENTER);
        filterLayout.setWidthFull();
        filterLayout.setJustifyContentMode(JustifyContentMode.START);

        return filterLayout;
    }

    private void getGridColumns(){
        nameColumn = resourceGrid.addColumn(ResourceDTO::getName).setHeader("Nombre").setAutoWidth(true).setSortable(true);

        typeColumn = resourceGrid.addColumn(resource -> translateResourceType(resource.getType())).setHeader("Tipo").setAutoWidth(true).setSortable(true);

        resourceGrid.addColumn(ResourceDTO::getCantidad).setHeader("Cantidad").setAutoWidth(true);

        storageColumn = resourceGrid.addColumn(resource -> {
                    if (resource.getStorageId() != null) {
                        return storageDTOMap.get(resource.getStorageId()).getName();
                    } else {
                        return "No asignado";
                    }
                }).setHeader("Almacen").setAutoWidth(true).setSortable(true);

        statusColumn = resourceGrid.addColumn(new ComponentRenderer<>(this::getResourceStatus)).setHeader("Estado").setAutoWidth(true);
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

    //=============================== Other Methods =========================================
    private CompletableFuture<List<String>> openFilterDialog() {
        FilterResourcesDialog filterResourcesDialog = new FilterResourcesDialog();
        filterResourcesDialog.setWidth("550px");
        filterResourcesDialog.setHeight("480px");
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        filterResourcesDialog.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                List<String> filters = filterResourcesDialog.getSelectedFilters();
                future.complete(filters);
            }
        });
        filterResourcesDialog.open();

        return future;
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