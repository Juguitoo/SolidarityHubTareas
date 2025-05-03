package solidarityhub.frontend.views.resource;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.resource.donation.DonationView;

@PageTitle("Recursos")
@Route("resources")
public class MainResourceView extends VerticalLayout implements BeforeEnterObserver {

    private final CatastropheService catastropheService;

    private CatastropheDTO selectedCatastrophe;

    public MainResourceView() {
        this.catastropheService = new CatastropheService();

        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        if (!catastropheService.isCatastropheSelected(event, selectedCatastrophe)) {
            return;
        }

        buildView();
    }

    private void buildView() {
        removeAll();

        setSizeFull();

        HeaderComponent title = new HeaderComponent("Recursos para la catástrofe: " + selectedCatastrophe.getName());

        add(title, getTabs());
    }

    private Component getTabs(){
        ResourceView resourceView = new ResourceView(selectedCatastrophe);
        DonationView donationView = new DonationView(selectedCatastrophe);

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        Tab resouseTab = new Tab("RECURSOS");
        Tab donationsTab = new Tab("DONACIONES");
        Tab volunteerTab = new Tab("VOLUNTARIOS");
        Tab accommodationTab = new Tab("ALOJAMIENTOS");

        tabSheet.add(resouseTab, resourceView);
        tabSheet.add(donationsTab, donationView);


        return tabSheet;
    }
}
