package solidarityhub.frontend.views.resources.donation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
import org.pingu.domain.enums.DonationStatus;
import org.pingu.domain.enums.DonationType;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.DonationDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.DonationService;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@PageTitle("Donaciones")
public class DonationView extends VerticalLayout {

    private final DonationService donationService;
    private final CatastropheDTO selectedCatastrophe;

    private final Grid<DonationDTO> donationGrid;
    private ListDataProvider<DonationDTO> donationDataProvider;

    private Grid.Column<DonationDTO> typeColumn;
    private Grid.Column<DonationDTO> statusColumn;
    private Grid.Column<DonationDTO> donorColumn;

    public static String typeFilterValue = "";
    public static String statusFilterValue = "";
    public static String yearFilterValue = "";
    public static String quantityFilterValue = "";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Translator translator;

    public DonationView(CatastropheDTO catastrophe) {
        this.donationService = new DonationService();
        this.selectedCatastrophe = catastrophe;

        this.donationGrid = new Grid<>(DonationDTO.class, false);

        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        }else{
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());

        setSizeFull();
        addClassName("donations-view");
        setPadding(false);

        buildView();
    }

    private void buildView() {
        removeAll();

        add(getButtons(), donationGrid);
        populateDonationGrid();
    }

    //===============================Grid Methods=========================================
    private List<DonationDTO> getDonationList() {
        if (selectedCatastrophe != null) {
            return donationService.getDonations(typeFilterValue, statusFilterValue, quantityFilterValue, yearFilterValue, selectedCatastrophe.getId());
        } else {
            return Collections.emptyList();
        }
    }

    private void populateDonationGrid() {
        this.donationDataProvider = new ListDataProvider<>(getDonationList());

        if (donationDataProvider.getItems().isEmpty()) {
            donationGrid.setVisible(false);
            add(new Span(translator.get("no_donations")));
        } else {
            donationGrid.setVisible(true);
            donationGrid.setDataProvider(donationDataProvider);

            getGridColumns();
        }

        donationGrid.addItemDoubleClickListener(event -> {
            DonationDTO donation = event.getItem();
            AddDonationDialog dialog = new AddDonationDialog(selectedCatastrophe, donation);
            dialog.open();
            dialog.addOpenedChangeListener(dialogEvent -> {
                if (!dialogEvent.isOpened()) {
                    refreshDonations();
                }
            });
        });

        donationGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        donationGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private void refreshDonations() {
        donationService.clearCache();
        donationGrid.setItems(getDonationList());
        donationGrid.getDataProvider().refreshAll();
    }

    //===============================Get Components=========================================
    private Component getButtons() {
        Button addDonationButton = new Button(translator.get("add_donation_button"), new Icon("vaadin", "plus"));
        addDonationButton.addClassName("add-resource-button");

        Button filterButton = new Button(translator.get("filter_donations"), new Icon("vaadin", "filter"));
        filterButton.addClassName("filter-button");

        Button clearFiltersButton = new Button(translator.get("clear_filters"), new Icon("vaadin", "trash"));
        clearFiltersButton.addClassName("clear-filters-button");

        addDonationButton.addClickListener(e -> {
            AddDonationDialog dialog = new AddDonationDialog(selectedCatastrophe);
            dialog.open();
            dialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    refreshDonations();
                }
            });
        });

        filterButton.addClickListener(e -> {
            CompletableFuture<List<String>> filters = openFilterDialog();
            filters.thenAccept(filterValues -> {
                if (!filterValues.isEmpty()) {
                    typeFilterValue = filterValues.get(0);
                    statusFilterValue = filterValues.get(1);
                    quantityFilterValue = filterValues.get(2);
                    yearFilterValue = filterValues.get(3);
                    refreshDonations();
                }
            });

        });

        clearFiltersButton.addClickListener(e -> {
            typeFilterValue = "";
            statusFilterValue = "";
            quantityFilterValue = "";
            yearFilterValue = "";
            refreshDonations();
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(clearFiltersButton, filterButton, addDonationButton);
        buttonLayout.setAlignItems(Alignment.CENTER);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.START);

        return buttonLayout;
    }

    private void getGridColumns() {
        donationGrid.addColumn(DonationDTO::getDescription).setHeader(translator.get("donation_description")).setAutoWidth(true).setSortable(true);

        typeColumn = donationGrid.addColumn(donation -> formatDonationType(donation.getType())).setAutoWidth(true).setHeader(translator.get("donation_type")).setSortable(true);

        donationGrid.addColumn(donation ->
                        donation.getDate() != null ? donation.getDate().format(DATE_FORMATTER) : "").setHeader(translator.get("donation_date"))
                .setSortable(true).setAutoWidth(true);

        statusColumn = donationGrid.addColumn(donation -> formatDonationStatus(donation.getStatus()))
                .setAutoWidth(true).setHeader(translator.get("donation_status")).setSortable(true);

        donorColumn = donationGrid.addColumn(donation -> donation.getDonorName() != null ? donation.getDonorName() : "Anonimo")
                .setAutoWidth(true).setHeader(translator.get("donation_donor")).setSortable(true);

        donationGrid.addColumn(DonationDTO::getCantidad).setHeader(translator.get("donation_amount"))
                .setAutoWidth(true).setSortable(true);
    }
    //===============================Format Methods=========================================
    private String formatDonationType(DonationType type) {
        if (type == null) {
            return "";
        }

        return switch (type) {
            case FINANCIAL -> translator.get("donation_type_financial");
            case MATERIAL -> translator.get("donation_type_material");
            case SERVICE -> translator.get("donation_type_service");
        };
    }

    private String formatDonationStatus(DonationStatus status) {
        if (status == null) {
            return "";
        }

        return switch (status) {
            case COMPLETED -> translator.get("donation_status_completed");
            case IN_PROGRESS -> translator.get("donation_status_in_progress");
            case SCHEDULED -> translator.get("donation_status_scheduled");
            case CANCELLED -> translator.get("donation_status_cancelled");
        };
    }

    private CompletableFuture<List<String>> openFilterDialog() {
        FilterDonationsDialog filterDonationsDialog = new FilterDonationsDialog();
        filterDonationsDialog.setWidth("550px");
        filterDonationsDialog.setHeight("480px");
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        filterDonationsDialog.addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                List<String> filters = filterDonationsDialog.getSelectedFilters();
                future.complete(filters);
            }
        });
        filterDonationsDialog.open();

        return future;
    }
}