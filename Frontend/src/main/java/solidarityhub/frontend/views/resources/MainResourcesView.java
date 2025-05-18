package solidarityhub.frontend.views.resources;

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
import solidarityhub.frontend.views.resources.donation.DonationView;
import solidarityhub.frontend.views.resources.resource.ResourceView;
import solidarityhub.frontend.views.resources.storage.StorageView;

@PageTitle("Recursos")
@Route("resources")
public class MainResourcesView extends VerticalLayout implements BeforeEnterObserver {

    private final CatastropheService catastropheService;

    private CatastropheDTO selectedCatastrophe;

    public MainResourcesView() {
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

        addClassName("main-resource-view");
        setSizeFull();

        HeaderComponent title = new HeaderComponent("Recursos: " + selectedCatastrophe.getName());

        add(title, getTabs());
    }

    private Component getTabs(){
        ResourceView resourceView = new ResourceView(selectedCatastrophe);
        DonationView donationView = new DonationView(selectedCatastrophe);
        StorageView storageView = new StorageView(selectedCatastrophe);

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        Tab resouseTab = new Tab("Recursos");
        Tab donationsTab = new Tab("Donaciones");
        Tab storageTab = new Tab("Almacenes");
        Tab volunteerTab = new Tab("Voluntarios");

        tabSheet.add(resouseTab, resourceView);
        tabSheet.add(donationsTab, donationView);
        tabSheet.add(storageTab, storageView);


        return tabSheet;
    }
}
