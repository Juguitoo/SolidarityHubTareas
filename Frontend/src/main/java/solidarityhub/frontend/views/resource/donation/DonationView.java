package solidarityhub.frontend.views.resource.donation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.DonationDTO;
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.model.enums.DonationStatus;
import solidarityhub.frontend.model.enums.DonationType;
import solidarityhub.frontend.model.enums.ResourceType;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.DonationService;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@PageTitle("Donaciones")
public class DonationView extends VerticalLayout {

    private final DonationService donationService;
    private final CatastropheService catastropheService;

    private CatastropheDTO selectedCatastrophe;

    private final Grid<DonationDTO> donationGrid;
    private ListDataProvider<DonationDTO> donationDataProvider;

    private Grid.Column<DonationDTO> descriptionColumn;
    private Grid.Column<DonationDTO> typeColumn;
    private Grid.Column<DonationDTO> dateColumn;
    private Grid.Column<DonationDTO> statusColumn;
    private Grid.Column<DonationDTO> amountColumn;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DonationView(CatastropheDTO catastrophe) {
        this.donationService = new DonationService();
        this.catastropheService = new CatastropheService();

        this.selectedCatastrophe = catastrophe;

        donationGrid = new Grid<>(DonationDTO.class, false);
        buildView();
    }

    private void buildView() {
        removeAll();

        setSizeFull();
        addClassName("donations-view");
        setPadding(false);

        add(getAddDonationButton(), donationGrid);
        populateDonationGrid();
    }

    private List<DonationDTO> getDonationList() {
        if (selectedCatastrophe != null) {
            return donationService.getDonationsByCatastrophe(selectedCatastrophe.getId());
        } else {
            return Collections.emptyList();
        }
    }

    private void populateDonationGrid() {
        this.donationDataProvider = new ListDataProvider<>(getDonationList());

        if (donationDataProvider.getItems().isEmpty()) {
            donationGrid.setVisible(false);
            add(new Span("No hay donaciones disponibles para esta catástrofe."));
        } else {
            donationGrid.setVisible(true);
            donationGrid.setDataProvider(donationDataProvider);

            getGridColumns();
            getGridFilter();
        }
    }

    //===============================Get Components=========================================
    private Button getAddDonationButton() {
        Button addDonationButton = new Button("Registrar nueva donación", new Icon(VaadinIcon.PLUS));
        addDonationButton.addClassName("add-resource-button");
        addDonationButton.addClickListener(e -> {
            DonationFormDialog dialog = new DonationFormDialog(donationService, catastropheService, selectedCatastrophe);
            dialog.open();
            dialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    //refreshDonations();
                }
            });
        });

        return addDonationButton;
    }

    private void getGridColumns() {
        donationGrid.addClassName("donation-grid");

        descriptionColumn = donationGrid.addColumn(DonationDTO::getDescription).setHeader("Descripción").setAutoWidth(true);
        amountColumn = donationGrid.addColumn(DonationDTO::getAmount).setHeader("Cantidad").setSortable(true).setAutoWidth(true);
        typeColumn = donationGrid.addColumn(donation -> formatDonationType(donation.getType())).setAutoWidth(true);
        dateColumn = donationGrid.addColumn(donation -> donation.getDate().format(DATE_FORMATTER)).setHeader("Fecha").setAutoWidth(true);
        statusColumn = donationGrid.addColumn(donation -> formatDonationStatus(donation.getStatus())).setHeader("Estado").setAutoWidth(true);

        donationGrid.addItemDoubleClickListener(event -> {
            DonationDTO donation = event.getItem();
            DonationFormDialog dialog = new DonationFormDialog(donationService, catastropheService, selectedCatastrophe, donation);
            dialog.open();
            dialog.addOpenedChangeListener(dialogEvent -> {
                if (!dialogEvent.isOpened()) {
                    //refreshDonations();
                }
            });
        });

    }

    private void getGridFilter() {
        MultiSelectComboBox<DonationType> typeFilter = new MultiSelectComboBox<>();
        typeFilter.setPlaceholder("Filtrar por tipo");
        typeFilter.setItems(DonationType.values());
        typeFilter.setItemLabelGenerator(this::translateDonationType);
        typeFilter.addValueChangeListener(event -> {
            donationDataProvider.clearFilters();
            Set<DonationType> selectedTypes = event.getValue();
            if(!selectedTypes.isEmpty()) {
                donationDataProvider.addFilter(resource ->
                        selectedTypes.contains(resource.getType())
                );
            }
        });

        HorizontalLayout filterHeader = new HorizontalLayout();
        Span filterTitle = new Span("Tipo");
        filterHeader.setAlignItems(Alignment.CENTER);
        filterHeader.add(filterTitle, typeFilter);

        typeColumn.setHeader(filterHeader);

    }

    private String translateDonationType (DonationType type) {
        if (type == null) return "";
        return switch (type) {
            case FINANCIAL -> "Económica";
            case MATERIAL -> "Material";
            case SERVICE -> "Servicio";
            default -> "Desconocido";
        };
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
