package solidarityhub.frontend.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
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

    private H1 viewTitle;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("SolidarityHub");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }


    public static SideNav createNavigation() {
        SideNav nav = new SideNav();

        SideNavItem homeLink = new SideNavItem("Catástrofes",
                CatastropheView.class, VaadinIcon.CLOUD.create());
        SideNavItem mapLink = new SideNavItem("Mapa",
                "http://localhost:8080/map", VaadinIcon.MAP_MARKER.create());
        SideNavItem taskLink = new SideNavItem("Tareas",
                TaskView.class, VaadinIcon.TASKS.create());
        SideNavItem dashboardLink = new SideNavItem("Dashboard",
                "http://localhost:8080/dashboar", VaadinIcon.DASHBOARD.create());
        SideNavItem resourcesLink = new SideNavItem("Recursos",
                "http://localhost:8083/recursos", VaadinIcon.TOOLBOX.create());
        SideNavItem contactLink = new SideNavItem("Contacto",
                "http://localhost:8080/contact", VaadinIcon.PHONE.create());

        nav.addItem(homeLink,taskLink,mapLink,resourcesLink,dashboardLink, contactLink);
        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}
