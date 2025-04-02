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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import solidarityhub.frontend.views.catastrophe.CatastropheView;
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

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        getElement().setAttribute("class", "main-layout");
        addDrawerContent();

    }



    private void addDrawerContent() {
        // Contenedor principal del drawer
        drawerContent = new VerticalLayout();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.setSizeFull();
        drawerContent.getElement().setAttribute("class", "drawer-content");

        // Logo y nombre de la aplicación
        Header header = new Header();
        header.getElement().setAttribute("class", "drawer-header");

        // Crear el diseño del botón logo
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.setSpacing(true);
        logoLayout.getElement().setAttribute("class", "logo-layout");

        // Botón de menú hamburguesa
        Image logoImage = new Image("icons/logo.png", "Logo");
        logoImage.getElement().setAttribute("class", "logo-principal");
        logoButton = new Button(logoImage);
        logoButton.getElement().setAttribute("class", "logo-button");
        logoButton.addClickListener(e -> toggleDrawerMinimized());

        // Nombre de la aplicación
        appName = new Span("SolidarityHub");
        appName.getElement().setAttribute("class", "app-name");

        logoLayout.add(logoButton, appName);
        header.add(logoLayout);

        // Crear contenedor perfil simplificado
        Div profileDiv = new Div();
        profileDiv.getElement().setAttribute("class", "profile-section");

        // Información usuario (simplificada sin Avatar)
        // Quitamos "Voluntario" como solicitado por el usuario
        profileDiv.getElement().setAttribute("class", "profile-section");

        // Crear navegación
        sideNav = createNavigation();
        Scroller scroller = new Scroller(sideNav);
        scroller.getElement().setAttribute("class", "nav-scroller");

        // Añadir sección de footer simplificada
        Footer footer = new Footer();
        footer.getElement().setAttribute("class", "drawer-footer");

        Span version = new Span("v1.0");
        version.getElement().setAttribute("class", "version-info");
        footer.add(version);

        // Añadir todo al contenedor principal
        drawerContent.add(header, profileDiv, scroller, footer);
        addToDrawer(drawerContent);
    }

    private void toggleDrawerMinimized() {
        minimized = !minimized;
        if (minimized) {
            // Colapsar el menú
            drawerContent.getElement().setAttribute("class", "drawer-content drawer-minimized");

            // Ajustar el ancho del drawer y forzar recalculo del layout
            getElement().executeJs(
                    "document.documentElement.style.setProperty('--drawer-width', '50px');" +
                            "this.shadowRoot.querySelector('[part=\"drawer\"]').style.width = '50px';" +
                            "this.shadowRoot.querySelector('[part=\"drawer\"]').style.minWidth = '50px';" +
                            "this.shadowRoot.querySelector('[part=\"drawer\"]').style.maxWidth = '50px';"
            );

            appName.setVisible(false);

            // Ocultar texto de los items de navegación
            sideNav.getItems().forEach(item -> {
                item.getElement().executeJs("this.shadowRoot.querySelector('[part=\"item\"]').style.justifyContent = 'center';");
                item.setLabel("");
            });
        } else {
            // Expandir el menú
            drawerContent.getElement().setAttribute("class", "drawer-content");

            // Restaurar el ancho del drawer y forzar recalculo del layout
            getElement().executeJs(
                    "document.documentElement.style.setProperty('--drawer-width', '260px');" +
                            "this.shadowRoot.querySelector('[part=\"drawer\"]').style.width = '260px';" +
                            "this.shadowRoot.querySelector('[part=\"drawer\"]').style.minWidth = '260px';" +
                            "this.shadowRoot.querySelector('[part=\"drawer\"]').style.maxWidth = '260px';"
            );

            appName.setVisible(true);

            // Restaurar estilo y texto
            sideNav.getItems().forEach(item -> {
                item.getElement().executeJs("this.shadowRoot.querySelector('[part=\"item\"]').style.justifyContent = 'flex-start';");
            });
            updateNavigationTexts();
        }

        // Forzar el redimensionamiento del contenido principal
        UI.getCurrent().getPage().executeJs(
                "setTimeout(function() { window.dispatchEvent(new Event('resize')); }, 100);"
        );
    }

    private void updateNavigationTexts() {
        // Hay que identificar los items por su posición ya que getIcon() no está disponible
        int index = 0;
        for (SideNavItem item : sideNav.getItems()) {
            String text;
            switch (index) {
                case 0:
                    text = "Catástrofes";
                    break;
                case 1:
                    text = "Tareas";
                    break;
                case 2:
                    text = "Mapa";
                    break;
                case 3:
                    text = "Recursos";
                    break;
                case 4:
                    text = "Dashboard";
                    break;
                case 5:
                    text = "Contacto";
                    break;
                default:
                    text = "";
            }
            item.setLabel(text);
            index++;
        }
    }

    public SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.getElement().setAttribute("class", "side-nav");

        SideNavItem homeLink = new SideNavItem("Catástrofes", CatastropheView.class);
        homeLink.setPrefixComponent(new Icon(VaadinIcon.CLOUD));
        homeLink.getElement().setAttribute("class", "nav-item");

        SideNavItem taskLink = new SideNavItem("Tareas", TaskView.class);
        taskLink.setPrefixComponent(new Icon(VaadinIcon.TASKS));
        taskLink.getElement().setAttribute("class", "nav-item");

        SideNavItem mapLink = new SideNavItem("Mapa", "http://localhost:8080/map");
        mapLink.setPrefixComponent(new Icon(VaadinIcon.MAP_MARKER));
        mapLink.getElement().setAttribute("class", "nav-item");

        SideNavItem resourcesLink = new SideNavItem("Recursos", "http://localhost:8083/recursos");
        resourcesLink.setPrefixComponent(new Icon(VaadinIcon.TOOLBOX));
        resourcesLink.getElement().setAttribute("class", "nav-item");

        SideNavItem dashboardLink = new SideNavItem("Dashboard", "http://localhost:8080/dashboard");
        dashboardLink.setPrefixComponent(new Icon(VaadinIcon.DASHBOARD));
        dashboardLink.getElement().setAttribute("class", "nav-item");

        SideNavItem contactLink = new SideNavItem("Contacto", "http://localhost:8080/contact");
        contactLink.setPrefixComponent(new Icon(VaadinIcon.PHONE));
        contactLink.getElement().setAttribute("class", "nav-item");

        nav.addItem(homeLink, taskLink, mapLink, resourcesLink, dashboardLink, contactLink);

        return nav;
    }

}