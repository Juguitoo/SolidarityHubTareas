package solidarityhub.frontend.views.resource.donation;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
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
import solidarityhub.frontend.dto.DonationDTO;
import solidarityhub.frontend.model.enums.DonationType;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.DonationService;
import solidarityhub.frontend.views.catastrophe.CatastropheSelectionView;
import solidarityhub.frontend.views.HeaderComponent;

import java.util.List;

@PageTitle("Donaciones")
@Route("resources/donations/cards")
public class DonationCardView extends VerticalLayout implements BeforeEnterObserver {

    private final DonationService donationService;
    private final CatastropheService catastropheService;
    private CatastropheDTO selectedCatastrophe;
    private final Tabs donationTabs;
    private final FlexLayout cardsContainer;

    @Autowired
    public DonationCardView(DonationService donationService, CatastropheService catastropheService) {
        this.donationService = donationService;
        this.catastropheService = catastropheService;

        // Add CSS class
        addClassName("donations-view");

        // Create tabs
        Tab donacionesTab = new Tab("Donaciones");
        Tab suministrosTab = new Tab("Suministros");
        Tab voluntariosTab = new Tab("Voluntarios");
        Tab alojamientosTab = new Tab("Alojamientos");

        donationTabs = new Tabs(donacionesTab, suministrosTab, voluntariosTab, alojamientosTab);
        donationTabs.addClassName("donation-tabs");

        // Set the active tab
        donationTabs.setSelectedTab(donacionesTab);

        // Container for donation cards
        cardsContainer = new FlexLayout();
        cardsContainer.setWidthFull();
        cardsContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        // Set up tab change listener
        donationTabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(donacionesTab)) {
                // Switch to grid view
                UI.getCurrent().navigate("resources/donations");
            } else if (event.getSelectedTab().equals(suministrosTab)) {
                UI.getCurrent().navigate("resources");
            } else if (event.getSelectedTab().equals(voluntariosTab)) {
                UI.getCurrent().navigate("resources/volunteers");
            } else if (event.getSelectedTab().equals(alojamientosTab)) {
                UI.getCurrent().navigate("resources/lodging");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Verify if a catastrophe is selected
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        // If no catastrophe is selected, redirect to selection page
        if (selectedCatastrophe == null) {
            Notification.show("Por favor, selecciona una catástrofe primero",
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            if (event != null) {
                event.forwardTo(CatastropheSelectionView.class);
            }
            return;
        }

        // Build the view with the selected catastrophe
        buildView();
    }

    private void buildView() {
        // Clear previous components
        removeAll();

        // Add header
        HeaderComponent header = new HeaderComponent("Recursos: " + selectedCatastrophe.getName());

        // Add components
        add(
                header,
                donationTabs,
                createFilterSection(),
                createAddDonationButton(),
                createViewToggle(),
                cardsContainer
        );

        // Load donations
        refreshDonations();
    }

    private HorizontalLayout createFilterSection() {
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.addClassName("filter-section");

        // Create type filter
        Select<String> typeFilter = new Select<>();
        typeFilter.setLabel("Filtrar por tipo:");
        typeFilter.setItems("Todos", "Económica", "Material", "Servicio");
        typeFilter.setValue("Todos");

        typeFilter.addValueChangeListener(event -> {
            String selectedType = event.getValue();
            if ("Todos".equals(selectedType)) {
                refreshDonations();
            } else {
                DonationType type = null;
                if ("Económica".equals(selectedType)) {
                    type = DonationType.FINANCIAL;
                } else if ("Material".equals(selectedType)) {
                    type = DonationType.MATERIAL;
                } else if ("Servicio".equals(selectedType)) {
                    type = DonationType.SERVICE;
                }

                if (type != null) {
                    filterDonationsByType(type);
                }
            }
        });

        Button applyButton = new Button("Aplicar");
        applyButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);

        filterLayout.add(typeFilter, applyButton);
        return filterLayout;
    }

    private Button createAddDonationButton() {
        Button addDonationButton = new Button("+ Registrar nueva donación", new Icon(VaadinIcon.PLUS));
        addDonationButton.addClassName("add-donation-button");
        addDonationButton.addClickListener(e -> {
            // Open the donation form dialog
            DonationFormDialog dialog = new DonationFormDialog(donationService, catastropheService, selectedCatastrophe);
            dialog.open();
            dialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    refreshDonations();
                }
            });
        });

        return addDonationButton;
    }

    private HorizontalLayout createViewToggle() {
        HorizontalLayout toggleLayout = new HorizontalLayout();
        toggleLayout.setWidthFull();
        toggleLayout.setJustifyContentMode(JustifyContentMode.END);

        Button gridViewButton = new Button("Vista de tabla", new Icon(VaadinIcon.TABLE));
        gridViewButton.addClickListener(e -> UI.getCurrent().navigate("resources/donations"));

        toggleLayout.add(gridViewButton);
        return toggleLayout;
    }

    private void refreshDonations() {
        if (selectedCatastrophe != null) {
            List<DonationDTO> donations = donationService.getDonationsByCatastrophe(selectedCatastrophe.getId());
            cardsContainer.removeAll();

            if (donations.isEmpty()) {
                Notification.show("No hay donaciones para esta catástrofe",
                        3000, Notification.Position.MIDDLE);
                return;
            }

            for (DonationDTO donation : donations) {
                DonationComponent card = new DonationComponent(donation);
                card.setWidth("31%");
                card.setMinWidth("300px");

                // Add click listener to open edit dialog
                card.addClickListener(e -> {
                    DonationFormDialog dialog = new DonationFormDialog(
                            donationService, catastropheService, selectedCatastrophe, donation);
                    dialog.open();
                    dialog.addOpenedChangeListener(event -> {
                        if (!event.isOpened()) {
                            refreshDonations();
                        }
                    });
                });

                cardsContainer.add(card);
            }
        }
    }

    private void filterDonationsByType(DonationType type) {
        if (selectedCatastrophe != null) {
            List<DonationDTO> donations = donationService.getDonationsByCatastrophe(selectedCatastrophe.getId())
                    .stream()
                    .filter(donation -> donation.getType() == type)
                    .toList();

            cardsContainer.removeAll();

            if (donations.isEmpty()) {
                Notification.show("No hay donaciones de tipo " + formatDonationType(type) + " para esta catástrofe",
                        3000, Notification.Position.MIDDLE);
                return;
            }

            for (DonationDTO donation : donations) {
                DonationComponent card = new DonationComponent(donation);
                card.setWidth("31%");
                card.setMinWidth("300px");

                // Add click listener to open edit dialog
                card.addClickListener(e -> {
                    DonationFormDialog dialog = new DonationFormDialog(
                            donationService, catastropheService, selectedCatastrophe, donation);
                    dialog.open();
                    dialog.addOpenedChangeListener(event -> {
                        if (!event.isOpened()) {
                            refreshDonations();
                        }
                    });
                });

                cardsContainer.add(card);
            }
        }
    }

    private String formatDonationType(DonationType type) {
        if (type == null) {
            return "";
        }

        switch (type) {
            case FINANCIAL:
                return "Económica";
            case MATERIAL:
                return "Material";
            case SERVICE:
                return "Servicio";
            default:
                return type.toString();
        }
    }
}
