package solidarityhub.frontend.views.resource;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.apache.commons.lang3.StringUtils;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.model.enums.ResourceType;
import solidarityhub.frontend.service.ResourceService;
import solidarityhub.frontend.service.StorageService;
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.catastrophe.CatastropheSelectionView;
import java.util.Collections;
import java.util.List;
import java.util.Set;


@PageTitle("Recursos")
@Route("resources")
public class ResourceView extends VerticalLayout implements BeforeEnterObserver {

    private final ResourceService resourceService;
    private final StorageService storageService;
    private CatastropheDTO selectedCatastrophe;

    private Grid<ResourceDTO> resourceGrid;
    private ListDataProvider<ResourceDTO> resourceDataProvider;

    private Grid.Column<ResourceDTO> nameColumn;
    private Grid.Column<ResourceDTO> typeColumn;
    private Grid.Column<ResourceDTO> quantityColumn;
    private Grid.Column<ResourceDTO> storageColumn;
    private Grid.Column<ResourceDTO> statusColumn;


    public ResourceView() {
        this.resourceService = new ResourceService();
        this.storageService = new StorageService();

        this.resourceGrid = new Grid<>(ResourceDTO.class, false);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Verificar si hay una catástrofe seleccionada
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        // Si no hay catástrofe seleccionada, redireccionar a la pantalla de selección
        if (selectedCatastrophe == null) {
            Notification.show("Por favor, selecciona una catástrofe primero",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            if (event != null) {
                event.forwardTo(CatastropheSelectionView.class);
            }
            return;
        }

        // Construir la vista con la catástrofe seleccionada
        buildView();
    }

    private void buildView() {
        removeAll();

        setSizeFull();
        addClassName("resources-view");

        HeaderComponent title = new HeaderComponent("Recursos para la catástrofe: " + selectedCatastrophe.getName());

        add(title, getTabs(), getButtons(), resourceGrid);
        populateResourceGrid();
    }

    private List<ResourceDTO> getResourceList() {
        if (selectedCatastrophe != null) {
//            return resourceService.getResourcesByCatastropheId(selectedCatastrophe.getId());
            return resourceService.getResources();
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
    private Component getTabs(){
        Tab resouseTab = new Tab("RECURSOS");
        Tab donationsTab = new Tab("DONACIONES");
        Tab volunteerTab = new Tab("VOLUNTARIOS");
        Tab accommodationTab = new Tab("ALOJAMIENTOS");

        Tabs tabs = new Tabs(resouseTab, donationsTab, volunteerTab, accommodationTab);
        tabs.setWidthFull();

        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(donationsTab)) {
                UI.getCurrent().navigate("resources/donations");
            } else if (event.getSelectedTab().equals(volunteerTab)) {
                UI.getCurrent().navigate("resources/volunteers");
            } else if (event.getSelectedTab().equals(accommodationTab)) {
                UI.getCurrent().navigate("resources/lodging");
            }
        });

        return tabs;
    }

    private Component getButtons() {
        // Botón para registrar nuevo recurso
        Button addResourceButton = new Button("Registrar nuevo recurso", new Icon("vaadin", "plus"));
        addResourceButton.addClickListener(e -> {
            // Lógica para registrar un nuevo suministro
            AddResourceDialog addResourceDialog = new AddResourceDialog(resourceService, storageService, resourceGrid, selectedCatastrophe);
            addResourceDialog.openAddResourceDialog();
        });
        addResourceButton.addClassName("add-resource-button");

        HorizontalLayout filterLayout = new HorizontalLayout(//typeFilter,
            addResourceButton);
        filterLayout.setAlignItems(Alignment.BASELINE);
        filterLayout.setWidthFull();
        filterLayout.setJustifyContentMode(JustifyContentMode.START);

        return filterLayout;
    }

    private void getGridColumns(){
        nameColumn = resourceGrid.addColumn(ResourceDTO::getName)
                .setHeader("Nombre").setSortable(true).setAutoWidth(true);

        typeColumn = resourceGrid.addColumn(resource -> translateResourceType(resource.getType()))
                .setHeader("Tipo").setSortable(true).setAutoWidth(true);

        quantityColumn = resourceGrid.addColumn(ResourceDTO::getCantidad)
                .setHeader("Cantidad").setSortable(true).setAutoWidth(true);

        storageColumn = resourceGrid.addColumn(resource -> {
                    if (resource.getStorageId() != null) {
                        var storage = storageService.getStorageById(resource.getStorageId());
                        return storage != null ? storage.getName() : "No disponible";
                    } else {
                        return "No asignado";
                    }
                })
                .setHeader("Almacén").setSortable(true).setAutoWidth(true);

        statusColumn = resourceGrid.addColumn(new ComponentRenderer<>(resource -> {
                    double quantity = resource.getQuantity();
                    int min = 10;
                    int max = 50;
                    return getResourceStatus(quantity, min, max);
                }))
                .setHeader("Estado").setSortable(true).setAutoWidth(true);
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

        TextField quantityFilter = new TextField();
        quantityFilter.setPlaceholder("Filtrar por cantidad");
        quantityFilter.addValueChangeListener(event -> {
            resourceDataProvider.clearFilters();
            if (!event.getValue().isEmpty()) {
                resourceDataProvider.setFilter(resource ->
                        StringUtils.containsIgnoreCase(resource.getCantidad(), event.getValue()));
            }
        });
        filterRow.getCell(quantityColumn).setComponent(quantityFilter);

        TextField storageFilter = new TextField();
        storageFilter.setPlaceholder("Filtrar por almacén");
        storageFilter.addValueChangeListener(event -> {
            resourceDataProvider.clearFilters();
            if (!event.getValue().isEmpty()) {
                resourceDataProvider.addFilter(resource -> {
                    String storageName = resource.getStorageId() != null ?
                            storageService.getStorageById(resource.getStorageId()).getName() :
                            "No asignado";
                    return StringUtils.containsIgnoreCase(storageName, event.getValue());
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
            default -> "Desconocido";
        };
    }

}
