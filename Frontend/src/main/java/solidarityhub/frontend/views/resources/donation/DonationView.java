package solidarityhub.frontend.views.resources.donation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import org.apache.commons.lang3.StringUtils;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.DonationDTO;
import solidarityhub.frontend.model.enums.DonationStatus;
import solidarityhub.frontend.model.enums.DonationType;
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
    private final CatastropheDTO selectedCatastrophe;

    private final Grid<DonationDTO> donationGrid;
    private ListDataProvider<DonationDTO> donationDataProvider;

    private Grid.Column<DonationDTO> typeColumn;
    private Grid.Column<DonationDTO> statusColumn;
    private Grid.Column<DonationDTO> donorColumn;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DonationView(CatastropheDTO catastrophe) {
        this.donationService = new DonationService();
        this.catastropheService = new CatastropheService();
        this.selectedCatastrophe = catastrophe;

        this.donationGrid = new Grid<>(DonationDTO.class, false);

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
            getGridFilters();
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
        Button addDonationButton = new Button("Registrar nueva donación", new Icon("vaadin", "plus"));
        addDonationButton.addClassName("add-resource-button");

        addDonationButton.addClickListener(e -> {
            AddDonationDialog dialog = new AddDonationDialog(selectedCatastrophe);
            dialog.open();
            dialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    refreshDonations();
                }
            });
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(addDonationButton);
        buttonLayout.setAlignItems(Alignment.CENTER);
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.START);

        return buttonLayout;
    }

    private void getGridColumns() {
        donationGrid.addColumn(DonationDTO::getDescription).setHeader("Descripción").setAutoWidth(true);

        typeColumn = donationGrid.addColumn(donation -> formatDonationType(donation.getType())).setAutoWidth(true);

        donationGrid.addColumn(donation ->
                        donation.getDate() != null ? donation.getDate().format(DATE_FORMATTER) : "")
                .setSortable(true).setAutoWidth(true);

        statusColumn = donationGrid.addColumn(donation -> formatDonationStatus(donation.getStatus()))
                .setAutoWidth(true);

        donorColumn = donationGrid.addColumn(donation -> donation.getVolunteerName() != null ? donation.getVolunteerName() : donation.getVolunteerDni())
                .setAutoWidth(true);

        donationGrid.addColumn(DonationDTO::getCantidad)
                .setAutoWidth(true);
    }

    private void getGridFilters() {
        MultiSelectComboBox<DonationType> typeFilter = new MultiSelectComboBox<>();
        typeFilter.setPlaceholder("Filtrar por tipo");
        typeFilter.setItems(DonationType.values());
        typeFilter.setItemLabelGenerator(this::formatDonationType);
        typeFilter.addValueChangeListener(event -> {
            donationDataProvider.clearFilters();
            Set<DonationType> selectedTypes = event.getValue();
            if (!selectedTypes.isEmpty()) {
                donationDataProvider.addFilter(donation ->
                        selectedTypes.contains(donation.getType()));
            }
        });
        typeColumn.setHeader(getGridFilterHeader("Tipo", typeFilter, typeColumn));

        MultiSelectComboBox<DonationStatus> statusFilter = new MultiSelectComboBox<>();
        statusFilter.setPlaceholder("Filtrar por estado");
        statusFilter.setItems(DonationStatus.values());
        statusFilter.setItemLabelGenerator(this::formatDonationStatus);
        statusFilter.addValueChangeListener(event -> {
            donationDataProvider.clearFilters();
            Set<DonationStatus> selectedStatuses = event.getValue();
            if (!selectedStatuses.isEmpty()) {
                donationDataProvider.addFilter(donation ->
                        selectedStatuses.contains(donation.getStatus()));
            }
        });
        statusColumn.setHeader(getGridFilterHeader("Estado", statusFilter, statusColumn));

        TextField donorFilter = new TextField();
        donorFilter.setPlaceholder("Buscar donante");
        donorFilter.setValueChangeMode(ValueChangeMode.LAZY);
        donorFilter.addValueChangeListener(event -> {
            donationDataProvider.clearFilters();
            if (!event.getValue().isEmpty()) {
                donationDataProvider.setFilter(donation ->
                        (donation.getVolunteerName() != null &&
                                StringUtils.containsIgnoreCase(donation.getVolunteerName(), event.getValue())) ||
                                (donation.getVolunteerDni() != null &&
                                        StringUtils.containsIgnoreCase(donation.getVolunteerDni(), event.getValue())));
            }
        });
        donorColumn.setHeader(getGridFilterHeader("Donante", donorFilter, donorColumn));
    }

    private Component getGridFilterHeader(String headerText, Component filterComponent, Grid.Column<DonationDTO> column) {
        HorizontalLayout filterHeader = new HorizontalLayout();
        Span filterTitle = new Span(headerText);
        filterHeader.setAlignItems(Alignment.CENTER);
        filterHeader.add(filterTitle, filterComponent);

        column.setHeader(filterHeader);

        return filterHeader;
    }

    //===============================Format Methods=========================================
    private String formatDonationType(DonationType type) {
        if (type == null) {
            return "";
        }

        return switch (type) {
            case FINANCIAL -> "Económica";
            case MATERIAL -> "Material";
            case SERVICE -> "Servicio";
            default -> type.toString();
        };
    }

    private String formatDonationStatus(DonationStatus status) {
        if (status == null) {
            return "";
        }

        return switch (status) {
            case COMPLETED -> "Completada";
            case IN_PROGRESS -> "En proceso";
            case SCHEDULED -> "Programada";
            case CANCELLED -> "Cancelada";
            default -> status.toString();
        };
    }

    }