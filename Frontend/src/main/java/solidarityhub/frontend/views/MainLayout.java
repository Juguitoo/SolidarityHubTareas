package solidarityhub.frontend.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;

import java.util.Locale;

@Layout
public class MainLayout extends AppLayout {

    private boolean minimized = false;
    private SideNav sideNav;
    private VerticalLayout drawerContent;
    private Span appName;
    private Button logoButton;
    private Button minimizeButton;
    private Div selectedCatastropheInfo;
    private ComboBox<String> languageSelector;
    private HorizontalLayout footerLayout;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        getElement().setAttribute("class", "main-layout");
        addDrawerContent();
        UI.getCurrent().addAfterNavigationListener(e -> updateSelectedCatastropheInfo());
    }

    private void addDrawerContent() {
        drawerContent = new VerticalLayout();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(true);
        drawerContent.setSizeFull();
        drawerContent.getElement().setAttribute("class", "drawer-content");

        Header header = new Header();
        header.getElement().setAttribute("class", "drawer-header");

        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.setSpacing(true);
        logoLayout.getElement().setAttribute("class", "logo-layout");

        Image logoImage = new Image("icons/LogoNegroSinFondo.png", "Logo");
        logoImage.getElement().setAttribute("class", "logo-principal");
        logoButton = new Button(logoImage);
        logoButton.getElement().setAttribute("class", "logo-button");
        logoButton.addClickListener(e -> toggleDrawerMinimized());

        appName = new Span("SolidarityHub");
        appName.getElement().setAttribute("class", "app-name");

        logoLayout.add(logoButton, appName);
        header.add(logoLayout);

        selectedCatastropheInfo = new Div();
        selectedCatastropheInfo.addClassName("selected-catastrophe-info");
        updateSelectedCatastropheInfo();

        sideNav = createNavigation();
        Scroller scroller = new Scroller(sideNav);
        scroller.getElement().setAttribute("class", "nav-scroller");

        languageSelector = getLanguageSelector();


        minimizeButton = new Button(VaadinIcon.ANGLE_DOUBLE_LEFT.create());
        minimizeButton.getElement().setAttribute("class", "minimize-button");
        minimizeButton.addClickListener(e -> toggleDrawerMinimized());
        minimizeButton.getStyle().set("margin-right", "25px");
        Footer footer = new Footer();
        footer.setWidthFull();
        footer.getElement().setAttribute("class", "drawer-footer");

        footerLayout = new HorizontalLayout(languageSelector, minimizeButton);
        footerLayout.setSpacing(true);
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        footer.add(footerLayout);

        drawerContent.add(header, selectedCatastropheInfo, scroller, footer);
        addToDrawer(drawerContent);
    }

    private void updateSelectedCatastropheInfo() {
        CatastropheDTO selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
        selectedCatastropheInfo.removeAll();
        if (minimized) {
            selectedCatastropheInfo.setVisible(false);
            return;
        }
        if (selectedCatastrophe != null) {
            VerticalLayout infoLayout = new VerticalLayout();
            infoLayout.setSpacing(false);
            infoLayout.setPadding(false);
            H4 title = new H4("Catástrofe seleccionada:");
            title.addClassName("selected-catastrophe-title");
            Paragraph catastropheName = new Paragraph(selectedCatastrophe.getName());
            catastropheName.addClassName("selected-catastrophe-name");
            Button changeButton = new Button("Cambiar", VaadinIcon.EXCHANGE.create());
            changeButton.addClassName("change-catastrophe-button");
            changeButton.addClickListener(e -> UI.getCurrent().getPage().setLocation("http://localhost:8083/catastrophes"));
            infoLayout.add(title, catastropheName, changeButton);
            selectedCatastropheInfo.add(infoLayout);
            selectedCatastropheInfo.setVisible(true);
        } else {
            Button selectButton = new Button("Seleccionar catástrofe", VaadinIcon.PLUS.create());
            selectButton.addClassName("select-catástrofe-button");
            selectButton.addClickListener(e -> UI.getCurrent().getPage().setLocation("http://localhost:8083/catastrophes"));
            selectedCatastropheInfo.add(new H4("No hay catástrofe seleccionada"), selectButton);
            selectedCatastropheInfo.setVisible(true);
        }
    }

    private ComboBox<String> getLanguageSelector() {
        ComboBox<String> languageSelector = new ComboBox<>();
        languageSelector.setAllowCustomValue(false);
        languageSelector.addClassName("language-selector");
        languageSelector.setItems("Español", "Valencià", "English");
        languageSelector.setValue(getCurrentLanguage());

        languageSelector.addValueChangeListener(event -> {
            String selected = event.getValue();
            Locale newLocale = switch (selected) {
                case "English" -> new Locale("en");
                case "Valencià" -> new Locale("va");
                default -> new Locale("es");
            };

            VaadinSession.getCurrent().setAttribute(Locale.class, newLocale);
            UI.getCurrent().setLocale(newLocale);
                UI.getCurrent().getPage().reload();
        });
        return languageSelector;
    }

    private String getCurrentLanguage() {
        Locale current = UI.getCurrent().getLocale();
        return switch (current.getLanguage()) {
            case "en" -> "English";
            case "va" -> "Valencià";
            case "es" -> "Español";
            default -> "";
            };
    }

    private void toggleDrawerMinimized() {
        minimized = !minimized;
        if (minimized) {
            sideNav.getItems().forEach(item -> item.getElement().setAttribute("title", item.getLabel()));
            drawerContent.getElement().setAttribute("class", "drawer-content drawer-minimized");
            getElement().executeJs(
                    "document.documentElement.style.setProperty('--drawer-width', '55px');" +
                            "document.querySelector('vaadin-app-layout::part(content)').style.marginLeft = '55px';"
            );
            appName.setVisible(false);
            selectedCatastropheInfo.setVisible(false);
            languageSelector.setVisible(false);

            UI.getCurrent().access(() -> sideNav.getItems().forEach(item -> item.setLabel("")));
            minimizeButton.setIcon(VaadinIcon.ANGLE_DOUBLE_RIGHT.create());

            minimizeButton.getElement().getStyle().set("margin-right", "0px");
            footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        } else {
            drawerContent.getElement().setAttribute("class", "drawer-content");
            getElement().executeJs(
                    "document.documentElement.style.setProperty('--drawer-width', '260px');" +
                            "document.querySelector('vaadin-app-layout::part(content)').style.marginLeft = '260px';"
            );
            appName.setVisible(true);
            updateSelectedCatastropheInfo();
            selectedCatastropheInfo.setVisible(true);
            languageSelector.setVisible(true);
            updateNavigationTexts();
            minimizeButton.setIcon(VaadinIcon.ANGLE_DOUBLE_LEFT.create());

            minimizeButton.getElement().getStyle().set("margin-right", "25px");
            footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        }
        UI.getCurrent().getPage().executeJs(
                "setTimeout(function() { window.dispatchEvent(new Event('resize')); }, 100);"
        );
    }



    private void updateNavigationTexts() {
        String[] labels = {"Inicio", "Tareas", "Mapa", "Dashboard", "Recursos",
                "Crear recurso", "Crear donación", "Crear almacén", "Encuestas",
                "Contacto", "Sobre nosotros", "Log Out"
        };
        int i = 0;
        for (SideNavItem item : sideNav.getItems()) {
            item.setLabel(labels[i++]);
        }
    }


    //TODO Probar rutas con grupo cohesionado
    public SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.getElement().setAttribute("class", "side-nav");
        nav.addItem(
                createNavItem("Inicio", VaadinIcon.HOME, "http://localhost:8083/home"),
                createNavItem("Tareas", VaadinIcon.TASKS, "http://localhost:8083/tasks"),
                createNavItem("Mapa", VaadinIcon.MAP_MARKER, "http://localhost:8080/map"),
                createNavItem("Dashboard", VaadinIcon.DASHBOARD, "http://localhost:8080/dashboard"),
                createNavItem("Recursos", VaadinIcon.TOOLBOX, "http://localhost:8083/resources"),
                createNavItem("Crear recurso", VaadinIcon.PLUS_CIRCLE, "http://localhost:8080/new-resource"),
                createNavItem("Crear donación", VaadinIcon.MONEY, "http://localhost:8080/new-donation"),
                createNavItem("Crear almacén", VaadinIcon.DATABASE, "http://localhost:8080/new-storage"),
                createNavItem("Encuestas", VaadinIcon.CLIPBOARD_CHECK, "http://localhost:8083/surveys"),
                createNavItem("Contacto", VaadinIcon.PHONE, "http://localhost:8080/contact"),
                createNavItem("Sobre nosotros", VaadinIcon.INFO_CIRCLE, "http://localhost:8080/about-us"),
                createNavItem("Log Out", VaadinIcon.SIGN_OUT, "http://localhost:8080/logout")
        );
        return nav;
    }

    private SideNavItem createNavItem(String label, VaadinIcon icon, String url) {
        SideNavItem item = new SideNavItem(label, url);
        item.setPrefixComponent(icon.create());
        item.getElement().setAttribute("class", "nav-item");
        item.getElement().setAttribute("title", label);
        item.setLabel(label);
        return item;
    }
}