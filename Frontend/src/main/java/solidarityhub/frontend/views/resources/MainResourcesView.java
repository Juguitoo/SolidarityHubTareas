package solidarityhub.frontend.views.resources;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.resources.donation.DonationView;
import solidarityhub.frontend.views.resources.resource.ResourceView;
import solidarityhub.frontend.views.resources.storage.StorageView;

import java.util.Locale;

@PageTitle("Recursos")
@Route("resources")
public class MainResourcesView extends VerticalLayout implements BeforeEnterObserver {

    private final CatastropheService catastropheService;

    private CatastropheDTO selectedCatastrophe;
    private final Translator translator = new Translator();

    public MainResourcesView() {
        this.catastropheService = new CatastropheService();

        translator.initializeTranslator();

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

        HeaderComponent title = new HeaderComponent(translator.get("resources_title") + ": " + selectedCatastrophe.getName());

        add(title, getTabs());
    }

    private Component getTabs(){
        ResourceView resourceView = new ResourceView(selectedCatastrophe);
        DonationView donationView = new DonationView(selectedCatastrophe);
        StorageView storageView = new StorageView(selectedCatastrophe);

        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        Tab resouseTab = new Tab(translator.get("resources_title"));
        Tab donationsTab = new Tab(translator.get("donations_title"));
        Tab storageTab = new Tab(translator.get("storage_title"));
        Tab volunteerTab = new Tab(translator.get("volunteers_title"));

        tabSheet.add(resouseTab, resourceView);
        tabSheet.add(donationsTab, donationView);
        tabSheet.add(storageTab, storageView);


        return tabSheet;
    }
}
