package solidarityhub.frontend.views.resource.donation;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.DonationDTO;
import solidarityhub.frontend.model.enums.DonationStatus;
import solidarityhub.frontend.model.enums.DonationType;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.DonationService;
import solidarityhub.frontend.views.catastrophe.CatastropheSelectionView;
import solidarityhub.frontend.views.HeaderComponent;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Donaciones")
@Route("resources/donations")
@Menu(order = 2, icon = LineAwesomeIconUrl.HAND_HOLDING_HEART_SOLID)
public class DonationView extends VerticalLayout implements BeforeEnterObserver {

    private final DonationService donationService;
    private final CatastropheService catastropheService;
    private CatastropheDTO selectedCatastrophe;
    private final Tabs donationTabs;
    private final Div donationsContainer;
    private Grid<DonationDTO> donationGrid;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    public DonationView(DonationService donationService, CatastropheService catastropheService) {
        this.donationService = donationService;
        this.catastropheService = catastropheService;

        // Add CSS class
        addClassName("donations-view");

        // Create tabs
        Tab donacionesTab = new Tab("DONACIONES");
        Tab suministrosTab = new Tab("RECURSOS");
        Tab voluntariosTab = new Tab("VOLUNTARIOS");
        Tab alojamientosTab = new Tab("ALOJAMIENTOS");

        donationTabs = new Tabs(suministrosTab, donacionesTab, voluntariosTab, alojamientosTab);
        donationTabs.addClassName("donation-tabs");

        // Set the active tab
        donationTabs.setSelectedTab(donacionesTab);

        // Container for donations
        donationsContainer = new Div();
        donationsContainer.setSizeFull();

        // Set up tab change listener
        donationTabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab().equals(donacionesTab)) {
                // Stay on current view
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
                createDonationsGrid()
        );
    }

    private HorizontalLayout createFilterSection() {
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.addClassName("filter-section");

        // Create type filter
        Select<String> typeFilter = new Select<>();
        typeFilter.setLabel("Filtrar por tipo:");
        typeFilter.setItems("Todas", "Económica", "Material", "Servicio");
        typeFilter.setValue("Todas");

        typeFilter.addValueChangeListener(event -> {
            String selectedType = event.getValue();
            applyTypeFilter(selectedType);
        });



        filterLayout.add(typeFilter);
        return filterLayout;
    }


    private void applyTypeFilter(String typeFilter) {
        if ("Todas".equals(typeFilter)) {
            refreshDonations(); // Mostrar todas las donaciones
        } else {
            DonationType type = null;
            switch (typeFilter) {
                case "Económica":
                    type = DonationType.FINANCIAL;
                    break;
                case "Material":
                    type = DonationType.MATERIAL;
                    break;
                case "Servicio":
                    type = DonationType.SERVICE;
                    break;
            }

            if (type != null) {
                // Filtrar donaciones por tipo
                DonationType finalType = type;
                List<DonationDTO> filteredDonations = donationService.getDonationsByCatastrophe(selectedCatastrophe.getId())
                        .stream()
                        .filter(donation -> donation.getType() == finalType)
                        .collect(Collectors.toList());

                donationGrid.setItems(filteredDonations);
            }
        }
    }


    private Button createAddDonationButton() {
        Button addDonationButton = new Button("Registrar nueva donación", new Icon(VaadinIcon.PLUS));
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

    private Grid<DonationDTO> createDonationsGrid() {
        donationGrid = new Grid<>(DonationDTO.class, false);
        donationGrid.addClassName("donation-grid");

        // Configure grid columns
        donationGrid.addColumn(DonationDTO::getCode).setHeader("Código").setAutoWidth(true);
        donationGrid.addColumn(donation -> donation.getDate().format(DATE_FORMATTER)).setHeader("Fecha").setAutoWidth(true);
        donationGrid.addColumn(donation -> formatDonationType(donation.getType())).setHeader("Tipo").setAutoWidth(true);
        donationGrid.addColumn(DonationDTO::getDescription).setHeader("Descripción").setAutoWidth(true);
        donationGrid.addColumn(donation -> formatDonationStatus(donation.getStatus())).setHeader("Estado").setAutoWidth(true);

        // Set up grid items
        refreshDonations();

        // Add double-click listener for editing
        donationGrid.addItemDoubleClickListener(event -> {
            DonationDTO donation = event.getItem();
            DonationFormDialog dialog = new DonationFormDialog(donationService, catastropheService, selectedCatastrophe, donation);
            dialog.open();
            dialog.addOpenedChangeListener(dialogEvent -> {
                if (!dialogEvent.isOpened()) {
                    refreshDonations();
                }
            });
        });

        return donationGrid;
    }

    private void refreshDonations() {
        if (selectedCatastrophe != null) {
            List<DonationDTO> donations = donationService.getDonationsByCatastrophe(selectedCatastrophe.getId());
            donationGrid.setItems(donations);
            if (donations.isEmpty()) {
                Notification.show("No hay donaciones para esta catástrofe",
                        3000, Notification.Position.MIDDLE);
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

    private String formatDonationStatus(DonationStatus status) {
        if (status == null) {
            return "";
        }

        switch (status) {
            case COMPLETED:
                return "Completada";
            case IN_PROGRESS:
                return "En proceso";
            case SCHEDULED:
                return "Programada";
            case CANCELLED:
                return "Cancelada";
            default:
                return status.toString();
        }
    }
}
