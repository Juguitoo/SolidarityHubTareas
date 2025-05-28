package solidarityhub.frontend.views.resources.resource;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.server.VaadinSession;
import org.pingu.domain.enums.ResourceType;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.dto.StorageDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.ResourceAssignmentService;
import solidarityhub.frontend.service.ResourceService;
import solidarityhub.frontend.service.StorageService;

import java.util.*;
import java.util.concurrent.CompletableFuture;


@PageTitle("Recursos")
public class ResourceView extends VerticalLayout{

    private final ResourceService resourceService;
    private final StorageService storageService;
    protected final CatastropheService catastropheService;
    protected final ResourceAssignmentService resourceAssignmentService;

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
    private final Translator translator = new Translator();


    public ResourceView(CatastropheDTO catastrophe) {
        this.resourceService = new ResourceService();
        this.storageService = new StorageService();
        this.catastropheService = new CatastropheService();
        this.resourceAssignmentService = new ResourceAssignmentService();

        this.selectedCatastrophe = catastrophe;

        this.resourceGrid = new Grid<>(ResourceDTO.class, false);
        for (StorageDTO s : storageService.getStorages()){
            storageDTOMap.put(s.getId(), s);
            storageNames.add(s.getName());
        }

        translator.initializeTranslator();

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
            add(new Span(translator.get("no_resources")));
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
        Button addResourceButton = new Button(translator.get("add_resource_button"), new Icon("vaadin", "plus"));
        addResourceButton.addClassName("add-resource-button");

        Button filterButton = new Button(translator.get("filter_resources"), new Icon("vaadin", "filter"));
        filterButton.addClassName("filter-button");

        Button clearFiltersButton = new Button(translator.get("clear_filters"), new Icon("vaadin", "trash"));
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
        nameColumn = resourceGrid.addColumn(ResourceDTO::getName).setHeader(translator.get("resource_name")).setAutoWidth(true).setSortable(true);

        typeColumn = resourceGrid.addColumn(resource -> translateResourceType(resource.getType())).setHeader(translator.get("resource_type")).setAutoWidth(true).setSortable(true);

        resourceGrid.addColumn(ResourceDTO::getCantidad).setHeader(translator.get("resource_quantity")).setAutoWidth(true);

        storageColumn = resourceGrid.addColumn(resource -> {
                    if (resource.getStorageId() != null) {
                        return storageDTOMap.get(resource.getStorageId()).getName();
                    } else {
                        return translator.get("no_assigned");
                    }
                }).setHeader(translator.get("resource_storage")).setAutoWidth(true).setSortable(true);

        resourceGrid.addColumn(resource -> {
            Double assigned = resourceAssignmentService.getTotalAssignedQuantity(resource.getId());
            if (assigned == null || assigned == 0) {
                return "0";
            }
            return assigned + " " + resource.getUnit();
        }).setHeader(translator.get("assigned_quantity")).setAutoWidth(true);

        // Add available quantity column
        resourceGrid.addColumn(resource -> {
            Double assigned = resourceAssignmentService.getTotalAssignedQuantity(resource.getId());
            if (assigned == null) {
                assigned = 0.0;
            }
            double available = resource.getQuantity() - assigned;
            return available + " " + resource.getUnit();
        }).setHeader(translator.get("available_quantity")).setAutoWidth(true);

        storageColumn = resourceGrid.addColumn(resource -> {
            if (resource.getStorageId() != null) {
                return storageDTOMap.get(resource.getStorageId()).getName();
            } else {
                return translator.get("no_assigned");
            }
        }).setHeader(translator.get("resource_storage")).setAutoWidth(true).setSortable(true);

        statusColumn = resourceGrid.addColumn(new ComponentRenderer<>(this::getResourceStatus)).setHeader(translator.get("resource_status")).setAutoWidth(true);
    }

    private Span getResourceStatus(ResourceDTO resource) {
        Span badge = new Span();
        badge.setText(translator.get("resource_no_stock"));
        badge.addClassName("resource__status");

        double quantity = resource.getQuantity();
        int min = 10;
        int max = 50;

        if (quantity < min) {
            badge.setText(translator.get("resource_low_stock"));
            badge.removeClassNames("status--medium", "status--high");
            badge.addClassName("status--low");
        } else if (quantity < max) {
            badge.setText(translator.get("resource_medium_stock"));
            badge.removeClassNames("status--low", "status--high");
            badge.addClassName("status--medium");
        } else {
            badge.setText(translator.get("resource_high_stock"));
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