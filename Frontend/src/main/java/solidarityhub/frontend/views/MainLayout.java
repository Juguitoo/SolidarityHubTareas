package solidarityhub.frontend.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.views.catastrophe.CatastropheView;
import solidarityhub.frontend.views.home.HomeView;
import solidarityhub.frontend.views.resources.MainResourcesView;
import solidarityhub.frontend.views.task.TaskView;

import java.util.Locale;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {
    private static Translator translator;

    private boolean minimized = false;
    private SideNav sideNav;
    private VerticalLayout drawerContent;
    private Span appName;
    private Button logoButton;
    private Div selectedCatastropheInfo;
    private ComboBox<String> languageSelector;

    public MainLayout() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());

        setPrimarySection(Section.DRAWER);
        getElement().setAttribute("class", "main-layout");

        // Agregar selector de idiomas a la barra superior
        addToNavbar(createTopBar());

        // Agregar contenido del drawer
        addDrawerContent();

        UI.getCurrent().addAfterNavigationListener(event -> updateSelectedCatastropheInfo());
    }

    private Component createTopBar() {
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.addClassName("top-bar");
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);

        // Selector de idioma
        languageSelector = new ComboBox<>();
        languageSelector.setAllowCustomValue(false);
        languageSelector.addClassName("language-selector");
        languageSelector.setItems("Español", "Valencià", "English");
        languageSelector.setValue(getIdiomaActual());

        languageSelector.addValueChangeListener(event -> {
            String selected = event.getValue();
            Locale newLocale = switch (selected) {
                case "English" -> new Locale("en");
                case "Valencià" -> new Locale("va");
                default -> new Locale("es");
            };

            // Guardamos el locale en sesión
            VaadinSession.getCurrent().setAttribute(Locale.class, newLocale);

            // Establecemos el nuevo locale para la UI actual
            UI.getCurrent().setLocale(newLocale);

            // Recargamos la vista actual
            UI.getCurrent().getPage().reload();
        });

        topBar.add(getThemeBtn(), languageSelector);
        return topBar;
    }

    private Component getThemeBtn(){
        Span themeIcon = new Span();
        themeIcon.setText("🌙");

        Button toggleTheme = new Button(themeIcon);
        toggleTheme.getElement().setAttribute("aria-label", "Cambiar tema");

        toggleTheme.addClickListener(event -> UI.getCurrent().getPage().executeJs("""
            const html = document.documentElement;
            const current = html.getAttribute('data-theme');
            const next = current === 'dark' ? 'light' : 'dark';
            html.setAttribute('data-theme', next);
            localStorage.setItem('theme', next);
            const icon = next === 'dark' ? '🌙' : '🌞';
            document.getElementById('theme-icon').textContent = icon;"""
    ));
        themeIcon.setId("theme-icon");
        return toggleTheme;
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
        updateSelectedCatastropheInfo();

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

        if (minimized) {
            selectedCatastropheInfo.setVisible(false);
            return;
        }

        if (selectedCatastrophe != null) {
            VerticalLayout infoLayout = new VerticalLayout();
            infoLayout.setSpacing(false);
            infoLayout.setPadding(false);

            // Título "Catástrofe seleccionada:"
            H4 title = new H4(translator.get("selected_catastrophe"));
            title.addClassName("selected-catastrophe-title");

            // Nombre de la catástrofe
            Paragraph catastropheName = new Paragraph(selectedCatastrophe.getName());
            catastropheName.addClassName("selected-catastrophe-name");

            // Botón para cambiar de catástrofe
            Button changeButton = new Button(translator.get("change_catastrophe"), VaadinIcon.EXCHANGE.create());
            changeButton.addClassName("change-catastrophe-button");
            changeButton.addClickListener(e -> UI.getCurrent().navigate(CatastropheView.class));

            infoLayout.add(title, catastropheName, changeButton);
            selectedCatastropheInfo.add(infoLayout);
            selectedCatastropheInfo.setVisible(true);
        } else {
            // Si no hay catástrofe seleccionada, mostrar un mensaje o redirigir
            Button selectButton = new Button(translator.get("select_catastrophe_title"), VaadinIcon.PLUS.create());
            selectButton.addClassName("select-catastrophe-button");
            selectButton.addClickListener(e -> UI.getCurrent().navigate(CatastropheView.class));
            H4 no_catastrophe = new H4(translator.get("no_selected_catastrophe"));
            no_catastrophe.addClassName("no-catastrophe-title");

            selectedCatastropheInfo.add(no_catastrophe, selectButton);
            selectedCatastropheInfo.setVisible(true);
        }
    }

    private void toggleDrawerMinimized() {
        minimized = !minimized;

        if (minimized) {
            // Primero guardar los tooltips
            sideNav.getItems().forEach(item -> item.getElement().setAttribute("title", item.getLabel()));

            // Luego colapsar el drawer
            drawerContent.getElement().setAttribute("class", "drawer-content drawer-minimized");
            getElement().executeJs(
                    "document.documentElement.style.setProperty('--drawer-width', '55px');" +
                            "document.querySelector('vaadin-app-layout::part(content)').style.marginLeft = '55px';"
            );

            // Ocultar nombre de la app
            appName.setVisible(false);

            // Ocultar info de catástrofe seleccionada
            selectedCatastropheInfo.setVisible(false);

            // Vaciar las etiquetas para evitar que ocupen espacio
            UI.getCurrent().access(() -> sideNav.getItems().forEach(item -> item.setLabel("")));

        } else {
            // Expandir el drawer
            drawerContent.getElement().setAttribute("class", "drawer-content");
            getElement().executeJs(
                    "document.documentElement.style.setProperty('--drawer-width', '260px');" +
                            "document.querySelector('vaadin-app-layout::part(content)').style.marginLeft = '260px';"
            );

            // Mostrar nombre de la app
            appName.setVisible(true);
            updateSelectedCatastropheInfo();
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
        String[] labels = {translator.get("label_tasks"), translator.get("label_map"), translator.get("label_resources"), translator.get("label_dashboard"), translator.get("label_contact")};
        int index = 0;
        for (SideNavItem item : sideNav.getItems()) {
            item.setLabel(labels[index++]);
        }
    }

    public SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.getElement().setAttribute("class", "side-nav");

        nav.addItem(
                createNavItem("Inicio", VaadinIcon.HOME, HomeView.class),
                createNavItem(translator.get("label_tasks"), VaadinIcon.TASKS, TaskView.class),
                createNavItem(translator.get("label_map"), VaadinIcon.MAP_MARKER, "http://localhost:8080/map"),
                createNavItem(translator.get("label_resources"), VaadinIcon.TOOLBOX, MainResourcesView.class),
                createNavItem(translator.get("label_dashboard"), VaadinIcon.DASHBOARD, "http://localhost:8080/dashboard"),
                createNavItem(translator.get("label_contact"), VaadinIcon.PHONE, "http://localhost:8080/contact")
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

    private String getIdiomaActual() {
        Locale current = UI.getCurrent().getLocale();
        return switch (current.getLanguage()) {
            case "en" -> "English";
            case "va" -> "Valencià";
            default -> "Español";
        };
    }
}