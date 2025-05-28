package solidarityhub.frontend.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
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
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.i18n.Translator;

import java.util.Locale;

@Layout
public class MainLayout extends AppLayout {

    private boolean minimized = false;

    private SideNav sideNav;
    private VerticalLayout drawerContent;
    private Span appName;
    private Div selectedCatastropheInfo;
    private HorizontalLayout footerLayout;
    private Button minimizeButton;
    private Button logOutButton;

    private Translator translator = new Translator();

    public MainLayout() {
        translator.initializeTranslator();

        setPrimarySection(Section.DRAWER);
        addClassName("main-layout");
        addDrawerContent();

        UI.getCurrent().getPage().executeJs(
                "return localStorage.getItem('drawerMinimized') === 'true';"
        ).then(Boolean.class, savedState -> {
            if (savedState != null && savedState) {
                minimized = false;
                toggleDrawerMinimized();
            }
        });

        UI.getCurrent().addAfterNavigationListener(e -> updateSelectedCatastropheInfo());
    }

    private void initializeTranslator() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());
    }

    //===============================Menu=========================================
    private void addDrawerContent() {
        drawerContent = new VerticalLayout();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.setSizeFull();
        drawerContent.addClassName("drawer-content");

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.addClassName("drawer-header");

        Image logoImage = new Image("icons/LogoAzulSinFondo.png", "Logo");
        logoImage.addClassName("logo-principal");
        Button logoButton = new Button(logoImage);
        logoButton.addClassName("logo-button");
        logoButton.addClickListener(e -> UI.getCurrent().navigate("http://localhost:8083/home"));

        appName = new Span("SolidarityHub");
        appName.addClassName("app-name");

        headerLayout.add(logoButton, appName);

        selectedCatastropheInfo = new Div();
        selectedCatastropheInfo.addClassName("selected-catastrophe-info");
        updateSelectedCatastropheInfo();

        sideNav = createNavigation();
        Scroller scroller = new Scroller(sideNav);
        scroller.addClassName("nav-scroller");

        //Footer
        footerLayout = new HorizontalLayout();
        footerLayout.addClassName("drawer-footer");
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        minimizeButton = new Button(VaadinIcon.ANGLE_DOUBLE_LEFT.create());
        minimizeButton.addClassName("minimize-button");
        minimizeButton.addClickListener(e -> toggleDrawerMinimized());
        minimizeButton.getStyle().set("margin-right", "25px");

        logOutButton = new Button("Log Out", VaadinIcon.SIGN_OUT.create());
        logOutButton.addClassName("log-out-button");
        logOutButton.addClickListener(e -> UI.getCurrent().getPage().setLocation("http://localhost:8080/logout"));

        footerLayout.add(logOutButton, minimizeButton);

        drawerContent.add(headerLayout, selectedCatastropheInfo, scroller, footerLayout);
        addToDrawer(drawerContent);
    }

    private void updateNavigationTexts() {
        String[] labels = {translator.get("label_home"), translator.get("label_tasks"), translator.get("label_map"), translator.get("label_dashboard"), translator.get("label_resources"),
                translator.get("label_surveys"), translator.get("label_contact"), translator.get("label_aboutus")
        };
        int i = 0;
        for (SideNavItem item : sideNav.getItems()) {
            item.setLabel(labels[i++]);
        }
    }

    public SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addClassName("side-nav");
        nav.addItem(
                createNavItem(translator.get("label_home"), VaadinIcon.HOME, "http://localhost:8083/home"),
                createNavItem(translator.get("label_tasks"), VaadinIcon.TASKS, "http://localhost:8083/tasks"),
                createNavItem(translator.get("label_map"), VaadinIcon.MAP_MARKER, "http://localhost:8080/map"),
                createNavItem(translator.get("label_dashboard"), VaadinIcon.DASHBOARD, "http://localhost:8080/dashboard"),
                createNavItem(translator.get("label_resources"), VaadinIcon.TOOLBOX, "http://localhost:8083/resources"),
                createNavItem(translator.get("label_surveys"), VaadinIcon.CLIPBOARD_CHECK, "http://localhost:8083/surveys"),
                createNavItem(translator.get("label_contact"), VaadinIcon.PHONE, "http://localhost:8080/contact"),
                createNavItem(translator.get("label_aboutus"), VaadinIcon.INFO_CIRCLE, "http://localhost:8080/about-us")
        );
        return nav;
    }

    private SideNavItem createNavItem(String label, VaadinIcon icon, String url) {
        SideNavItem item = new SideNavItem(label, url);
        item.setMatchNested(true);
        item.addClassName("nav-item");

        Icon iconComponent = icon.create();
        iconComponent.addClassName("nav-item__icon");
        item.setPrefixComponent(iconComponent);

        item.getElement().setAttribute("title", label);
        item.setLabel(label);

        return item;
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
            infoLayout.addClassName("selected-catastrophe-info-layout");
            infoLayout.setSpacing(false);
            infoLayout.setPadding(false);

            H4 title = new H4(translator.get("selected_catastrophe") + " ");
            title.addClassName("selected-catastrophe-title");

            Paragraph catastropheName = new Paragraph(selectedCatastrophe.getName());
            catastropheName.addClassName("selected-catastrophe-name");

            Button changeButton = new Button(translator.get("change_catastrophe"), VaadinIcon.EXCHANGE.create());
            changeButton.addClassName("change-catastrophe-button");
            changeButton.addClickListener(e -> UI.getCurrent().getPage().setLocation("http://localhost:8083/"));

            infoLayout.add(title, catastropheName, changeButton);
            selectedCatastropheInfo.add(infoLayout);
            selectedCatastropheInfo.setVisible(true);
        } else {
            Button selectButton = new Button(translator.get("select_catastrophe_button"), VaadinIcon.PLUS.create());
            selectButton.addClassName("change-catastrophe-button");
            selectButton.addClickListener(e -> UI.getCurrent().getPage().setLocation("http://localhost:8083/"));
            selectedCatastropheInfo.add(new H4("No hay catástrofe seleccionada"), selectButton);
            selectedCatastropheInfo.setVisible(true);
        }
    }

    private void toggleDrawerMinimized() {
        minimized = !minimized;

        UI.getCurrent().getPage().executeJs(
                "localStorage.setItem('drawerMinimized', $0)", minimized
        );

        if (minimized) {
            sideNav.getItems().forEach(item -> item.getElement().setAttribute("title", item.getLabel()));
            drawerContent.getElement().setAttribute("class", "drawer-content drawer-minimized");
            getElement().executeJs(
                    "document.documentElement.style.setProperty('--drawer-width', '55px');" +
                            "document.querySelector('vaadin-app-layout::part(content)').style.marginLeft = '55px';"
            );
            appName.setVisible(false);
            selectedCatastropheInfo.setVisible(false);

            UI.getCurrent().access(() -> sideNav.getItems().forEach(item -> item.setLabel("")));

            minimizeButton.setIcon(VaadinIcon.ANGLE_DOUBLE_RIGHT.create());
            minimizeButton.getElement().getStyle().set("margin-right", "0px");
            logOutButton.setVisible(false);
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
            updateNavigationTexts();

            minimizeButton.setIcon(VaadinIcon.ANGLE_DOUBLE_LEFT.create());
            minimizeButton.getElement().getStyle().set("margin-right", "25px");
            logOutButton.setVisible(true);
            footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        }
        UI.getCurrent().getPage().executeJs(
                "setTimeout(function() { window.dispatchEvent(new Event('resize')); }, 100);"
        );
    }

}