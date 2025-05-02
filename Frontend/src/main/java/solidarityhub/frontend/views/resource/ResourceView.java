package solidarityhub.frontend.views.resource;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.service.ResourceService;
import solidarityhub.frontend.service.StorageService;
import solidarityhub.frontend.views.catastrophe.CatastropheSelectionView;
import solidarityhub.frontend.views.HeaderComponent;

import java.util.Collections;
import java.util.List;


@PageTitle("Recursos")
@Route("resources")
public class ResourceView extends VerticalLayout implements BeforeEnterObserver {

    private final ResourceService resourceService;
    private final StorageService storageService;
    private CatastropheDTO selectedCatastrophe;
    private Grid<ResourceDTO> resourceGrid;
    private ListDataProvider<ResourceDTO> resourceDataProvider;

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

        // Header
        HeaderComponent title = new HeaderComponent("Recursos para la catástrofe: " + selectedCatastrophe.getName());

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

        add(title, tabs, getFilters(), resourceGrid);
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
        System.out.println(resourceDataProvider.getItems());

        if (resourceDataProvider.getItems().isEmpty()) {
            resourceGrid.setVisible(false);
            add(new Span("No hay recursos disponibles para esta catástrofe."));
        } else {
            resourceGrid.setVisible(true);
            resourceGrid.setDataProvider(resourceDataProvider);
            resourceGrid.addColumn(ResourceDTO::getName).setHeader("Nombre");
            resourceGrid.addColumn(ResourceDTO::getType).setHeader("Tipo");
            resourceGrid.addColumn(ResourceDTO::getCantidad).setHeader("Cantidad");
            resourceGrid.addColumn(resource -> {
                if (resource.getStorageId() != null) {
                    var storage = storageService.getStorageById(resource.getStorageId());
                    return storage != null ? storage.getName() : "No disponible";
                } else {
                    return "No asignado";
                }
            }).setHeader("Almacén");
            //resourceGrid.addComponentColumn(this::createStatusBadge).setHeader("Estado");
        }
    }

    //===============================Get Components=========================================
    private Component getFilters() {
        Select<String> typeFilter = new Select<>();
        typeFilter.setLabel("Filtrar por tipo");
        typeFilter.setItems("Todos los tipos", "Alimentos", "Medicina", "Ropa", "Refugio", "Herramientas",
                "Combustible", "Higiene", "Comunicación", "Transporte", "Construcción", "Donaciones",
                "Papelería", "Logística", "Otros");
        typeFilter.setValue("Todos los tipos");

        // Botón para registrar nuevo recurso
        Button addResourceButton = new Button("Registrar nuevo recurso", new Icon("vaadin", "plus"));
        addResourceButton.addClickListener(e -> {
            // Lógica para registrar un nuevo suministro
            AddResourceDialog addResourceDialog = new AddResourceDialog(resourceService, storageService, resourceGrid, selectedCatastrophe);
            addResourceDialog.openAddResourceDialog();
        });
        addResourceButton.addClassName("add-resource-button");

        HorizontalLayout filterLayout = new HorizontalLayout(typeFilter, addResourceButton);
        filterLayout.setAlignItems(Alignment.BASELINE);
        filterLayout.setWidthFull();
        filterLayout.setJustifyContentMode(JustifyContentMode.START);

        return filterLayout;
    }


    // a implementar más adelante
    private Span createStatusBadge(String status) {
        Span badge = new Span();
        badge.addClassName("status-badge");
        switch (status) {
            case "Stock bajo":
                badge.getElement().getThemeList().add("error");
                break;
            case "Stock medio":
                badge.getElement().getThemeList().add("warning");
                break;
            case "Stock alto":
                badge.getElement().getThemeList().add("success");
                break;
        }
        return badge;
    }
}
