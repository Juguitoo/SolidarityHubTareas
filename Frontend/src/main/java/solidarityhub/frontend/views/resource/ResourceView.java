package solidarityhub.frontend.views.resource;


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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.service.ResourceService;
import solidarityhub.frontend.service.StorageService;
import solidarityhub.frontend.views.catastrophe.CatastropheSelectionView;
import solidarityhub.frontend.views.headerComponent;

import java.util.List;


@PageTitle("Recursos")
@Route("/resources/supplies")
public class ResourceView extends VerticalLayout implements BeforeEnterObserver {

    private final ResourceService resourceService;
    private final StorageService storageService;
    private CatastropheDTO selectedCatastrophe;
    private Grid<ResourceDTO> resourceGrid;

    @Autowired
    public ResourceView(ResourceService resourceService, StorageService storageService) {
        this.resourceService = resourceService;
        setSizeFull();
        addClassName("resources-view");
        this.storageService = storageService;
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
        // Limpiar la vista actual
        removeAll();

        // Header
        headerComponent title = new headerComponent("Recursos para la catástrofe: " + selectedCatastrophe.getName());

        // Filtros
        Select<String> typeFilter = new Select<>();
        typeFilter.setLabel("Filtrar por tipo");
        typeFilter.setItems("Todos los tipos", "Alimentos", "Medicina", "Ropa", "Refugio", "Herramientas",
                "Combustible", "Higiene", "Comunicación", "Transporte", "Construcción", "Donaciones",
                "Papelería", "Logística", "Otros");
        typeFilter.setValue("Todos los tipos");

        // Crear tabs para cada tipo de recurso
        Tab tab1 = new Tab("DONACIONES");
        Tab tab2 = new Tab("RECURSOS");
        Tab tab3 = new Tab("VOLUNTARIOS");
        Tab tab4 = new Tab("ALOJAMIENTOS");

        // Agregar tabs al TabSheet
        Tabs tabs = new Tabs(tab2, tab1, tab3, tab4);
        tabs.setWidthFull();

        // Agregar listener para la navegación entre tabs
        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(tab1)) {
                // Navegación a la vista de donaciones
                UI.getCurrent().navigate("resources/donations");
            } else if (event.getSelectedTab().equals(tab3)) {
                UI.getCurrent().navigate("resources/volunteers");
            } else if (event.getSelectedTab().equals(tab4)) {
                UI.getCurrent().navigate("resources/lodging");
            }
        });

        Button applyFilterButton = new Button("Aplicar", e -> applyFilter(typeFilter.getValue()));

        // Botón para registrar nuevo recurso
        Button addResourceButton = new Button("Registrar nuevo recurso", new Icon("vaadin", "plus"));
        addResourceButton.addClickListener(e -> {
            // Lógica para registrar un nuevo suministro
            AddResourceDialog addResourceDialog = new AddResourceDialog(resourceService, storageService, resourceGrid );
            addResourceDialog.openAddResourceDialog();
        });
        addResourceButton.addClassName("add-resource-button");

        HorizontalLayout filterLayout = new HorizontalLayout(typeFilter, applyFilterButton, addResourceButton);
        filterLayout.setAlignItems(Alignment.BASELINE);
        filterLayout.setWidthFull();
        filterLayout.setJustifyContentMode(JustifyContentMode.START);

        // Grid para mostrar los recursos
        resourceGrid = new Grid<>(ResourceDTO.class, false);
        resourceGrid.addColumn(ResourceDTO::getId).setHeader("ID");
        resourceGrid.addColumn(ResourceDTO::getType).setHeader("Tipo");
        resourceGrid.addColumn(ResourceDTO::getName).setHeader("Nombre");
        resourceGrid.addColumn(ResourceDTO::getCantidad).setHeader("Cantidad");
        resourceGrid.addColumn(ResourceDTO::getStorage).setHeader("Almacén");
        //resourceGrid.addComponentColumn(this::createStatusBadge).setHeader("Estado");

        resourceGrid.setItems(loadResources());


        // Agregar componentes a la vista
        add(title, tabs, filterLayout, resourceGrid);
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

    private List<ResourceDTO> loadResources() {
        return resourceService.getResourcesByCatastropheId(selectedCatastrophe.getId());
    }
    private void applyFilter(String type) {
        if ("Todos los tipos".equals(type)) {
            resourceGrid.setItems(loadResources());
        } else {
            resourceGrid.setItems(resourceService.getResourcesByType(selectedCatastrophe.getId(), type));
        }
    }

}
