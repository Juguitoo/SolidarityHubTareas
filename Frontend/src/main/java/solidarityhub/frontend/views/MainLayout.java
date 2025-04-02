package solidarityhub.frontend.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.views.catastrophe.CatastropheSelectionView;
import solidarityhub.frontend.views.task.TaskView;

import java.util.List;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private boolean minimized = false;
    private H1 viewTitle;
    private SideNav sideNav;
    private VerticalLayout drawerContent;
    private Span appName;
    private Button logoButton;
    private Div selectedCatastropheInfo;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        getElement().setAttribute("class", "main-layout");
        addDrawerContent();

        // Configurar un listener para actualizar el indicador de catástrofe seleccionada
        // cuando cambia de ruta o se refresca la página
        UI.getCurrent().addAfterNavigationListener(event -> updateSelectedCatastropheInfo());
    }

    private void addDrawerContent() {
        drawerContent = new VerticalLayout();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.setSizeFull();
        drawerContent.getElement().setAttribute("class", "drawer-content");

        Header header = new Header();
        header.getElement().setAttribute("class", "drawer-header");

        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.setSpacing(true);
        logoLayout.getElement().setAttribute("class", "logo-layout");

        Image logoImage = new Image("icons/logo.png", "Logo");
        logoImage.getElement().setAttribute("class", "logo-principal");
        logoButton = new Button(logoImage);
        logoButton.getElement().setAttribute("class", "logo-button");
        logoButton.addClickListener(e -> toggleDrawerMinimized());

        appName = new Span("SolidarityHub");
        appName.getElement().setAttribute("class", "app-name");

        logoLayout.add(logoButton, appName);
        header.add(logoLayout);

        // Div para mostrar la catástrofe seleccionada
        selectedCatastropheInfo = new Div();
        selectedCatastropheInfo.addClassName("selected-catastrophe-info");
        updateSelectedCatastropheInfo(); // Inicializar con la información actual

        sideNav = createNavigation();
        Scroller scroller = new Scroller(sideNav);
        scroller.getElement().setAttribute("class", "nav-scroller");

        Footer footer = new Footer();
        footer.getElement().setAttribute("class", "drawer-footer");
        Span version = new Span("v1.0");
        version.getElement().setAttribute("class", "version-info");
        footer.add(version);

        drawerContent.add(header, selectedCatastropheInfo, scroller, footer);
        addToDrawer(drawerContent);
    }

    private void updateSelectedCatastropheInfo() {
        CatastropheDTO selectedCatastrophe =
                (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        selectedCatastropheInfo.removeAll();

        if (selectedCatastrophe != null) {
            VerticalLayout infoLayout = new VerticalLayout();
            infoLayout.setSpacing(false);
            infoLayout.setPadding(false);

            // Título "Catástrofe seleccionada:"
            H4 title = new H4("Catástrofe seleccionada:");
            title.addClassName("selected-catastrophe-title");

            // Nombre de la catástrofe
            Paragraph catastropheName = new Paragraph(selectedCatastrophe.getName());
            catastropheName.addClassName("selected-catastrophe-name");

            // Botón para cambiar de catástrofe
            Button changeButton = new Button("Cambiar", VaadinIcon.EXCHANGE.create());
            changeButton.addClassName("change-catastrophe-button");
            changeButton.addClickListener(e -> UI.getCurrent().navigate(CatastropheSelectionView.class));

            infoLayout.add(title, catastropheName, changeButton);
            selectedCatastropheInfo.add(infoLayout);
            selectedCatastropheInfo.setVisible(true);
        } else {
            // Si no hay catástrofe seleccionada, mostrar un mensaje o redirigir
            Button selectButton = new Button("Seleccionar catástrofe", VaadinIcon.PLUS.create());
            selectButton.addClassName("select-catastrophe-button");
            selectButton.addClickListener(e -> UI.getCurrent().navigate(CatastropheSelectionView.class));

            selectedCatastropheInfo.add(new H4("No hay catástrofe seleccionada"), selectButton);
            selectedCatastropheInfo.setVisible(true);
        }
    }

    private void toggleDrawerMinimized() {
        minimized = !minimized;

        if (minimized) {
            // Primero guardar los tooltips
            sideNav.getItems().forEach(item -> {
                item.getElement().setAttribute("title", item.getLabel());
            });

            // Luego colapsar el drawer
            drawerContent.getElement().setAttribute("class", "drawer-content drawer-minimized");
            getElement().executeJs("document.documentElement.style.setProperty('--drawer-width', '55px');");

            // Ocultar nombre de la app
            appName.setVisible(false);

            // Ocultar info de catástrofe seleccionada
            selectedCatastropheInfo.setVisible(false);

            // Vaciar las etiquetas para evitar que ocupen espacio
            UI.getCurrent().access(() -> {
                sideNav.getItems().forEach(item -> item.setLabel(""));
            });

        } else {
            // Expandir el drawer
            drawerContent.getElement().setAttribute("class", "drawer-content");
            getElement().executeJs("document.documentElement.style.setProperty('--drawer-width', '260px');");

            // Mostrar nombre de la app
            appName.setVisible(true);

            // Mostrar info de catástrofe seleccionada
            selectedCatastropheInfo.setVisible(true);

            // Restaurar las etiquetas
            updateNavigationTexts();
        }

        // Notificar al navegador para recalcular tamaños
        UI.getCurrent().getPage().executeJs(
                "setTimeout(function() { window.dispatchEvent(new Event('resize')); }, 100);"
        );
    }

    private void updateNavigationTexts() {
        String[] labels = {"Tareas", "Mapa", "Recursos", "Dashboard", "Contacto"};
        int index = 0;
        for (SideNavItem item : sideNav.getItems()) {
            item.setLabel(labels[index++]);
        }
    }

    public SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.getElement().setAttribute("class", "side-nav");

        nav.addItem(
                createNavItem("Tareas", VaadinIcon.TASKS, TaskView.class),
                createNavItem("Mapa", VaadinIcon.MAP_MARKER, "http://localhost:8080/map"),
                createNavItem("Recursos", VaadinIcon.TOOLBOX, "http://localhost:8083/recursos"),
                createNavItem("Dashboard", VaadinIcon.DASHBOARD, "http://localhost:8080/dashboard"),
                createNavItem("Contacto", VaadinIcon.PHONE, "http://localhost:8080/contact")
        );

        return nav;
    }

    private SideNavItem createNavItem(String label, VaadinIcon icon, Class<? extends Component> view) {
        SideNavItem item = new SideNavItem(label, view);
        item.setPrefixComponent(new Icon(icon));
        item.getElement().setAttribute("class", "nav-item");

        // Añadir tooltip usando el atributo title
        item.getElement().setAttribute("title", label);

        // Configurar el texto del elemento
        item.setLabel(label);
        return item;
    }

    private SideNavItem createNavItem(String label, VaadinIcon icon, String url) {
        SideNavItem item = new SideNavItem(label, url);
        item.setPrefixComponent(new Icon(icon));
        item.getElement().setAttribute("class", "nav-item");
        // Añadir tooltip usando el atributo title
        item.getElement().setAttribute("title", label);

        // Configurar el texto del elemento
        item.setLabel(label);
        return item;
    }
}
