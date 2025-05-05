package solidarityhub.frontend.views.resource.donation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
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

    private Grid.Column<DonationDTO> codeColumn;
    private Grid.Column<DonationDTO> typeColumn;
    private Grid.Column<DonationDTO> descriptionColumn;
    private Grid.Column<DonationDTO> statusColumn;
    private Grid.Column<DonationDTO> donorColumn;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DonationView(CatastropheDTO catastrophe) {
        this.donationService = new DonationService();
        this.catastropheService = new CatastropheService();
        this.selectedCatastrophe = catastrophe;

        // Crear grid
        this.donationGrid = new Grid<>(DonationDTO.class, false);

        setSizeFull();
        addClassName("donations-view");
        setPadding(false);

        // Construir la vista inmediatamente
        buildView();
    }

    private void buildView() {
        removeAll();

        add(getButtons(), donationGrid);
        populateDonationGrid();

        setSizeFull();
    }

    private Component getButtons() {
        // Botón para registrar nueva donación
        Button addDonationButton = new Button("Registrar nueva donación", new Icon("vaadin", "plus"));
        addDonationButton.addClassName("add-resource-button");

        addDonationButton.addClickListener(e -> {
            // Abrir diálogo de creación de donación
            DonationFormDialog dialog = new DonationFormDialog(donationService, catastropheService, selectedCatastrophe);
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

            configureGridColumns();
            configureGridFilters();
        }
    }

    private void configureGridColumns() {
        codeColumn = donationGrid.addColumn(DonationDTO::getCode)
                .setHeader("Código")
                .setAutoWidth(true);

        typeColumn = donationGrid.addColumn(donation -> formatDonationType(donation.getType()))
                .setHeader("Tipo")
                .setAutoWidth(true);

        descriptionColumn = donationGrid.addColumn(DonationDTO::getDescription)
                .setHeader("Descripción")
                .setAutoWidth(true);

        Grid.Column<DonationDTO> dateColumn = donationGrid.addColumn(donation ->
                        donation.getDate() != null ? donation.getDate().format(DATE_FORMATTER) : "")
                .setHeader("Fecha")
                .setSortable(true)
                .setAutoWidth(true);

        statusColumn = donationGrid.addColumn(donation -> formatDonationStatus(donation.getStatus()))
                .setHeader("Estado")
                .setAutoWidth(true);

        donorColumn = donationGrid.addColumn(donation ->
                        donation.getVolunteerName() != null ? donation.getVolunteerName() : donation.getVolunteerDni())
                .setHeader("Donante")
                .setAutoWidth(true);

        Grid.Column<DonationDTO> cantidadColumn = donationGrid.addColumn(DonationDTO::getCantidad)
                .setHeader("Cantidad")
                .setAutoWidth(true);

        // Configurar evento de doble clic para editar
        donationGrid.addItemDoubleClickListener(event -> {
            DonationDTO donation = event.getItem();
            DonationFormDialog dialog = new DonationFormDialog(
                    donationService, catastropheService, selectedCatastrophe, donation);
            dialog.open();
            dialog.addOpenedChangeListener(dialogEvent -> {
                if (!dialogEvent.isOpened()) {
                    refreshDonations();
                }
            });
        });
    }

    private void configureGridFilters() {
        HeaderRow filterRow = donationGrid.appendHeaderRow();

        // Filtro por código
        TextField codeFilter = new TextField();
        codeFilter.setPlaceholder("Buscar código");
        codeFilter.setValueChangeMode(ValueChangeMode.LAZY);
        codeFilter.addValueChangeListener(event -> {
            donationDataProvider.clearFilters();
            if (!event.getValue().isEmpty()) {
                donationDataProvider.setFilter(donation ->
                        StringUtils.containsIgnoreCase(donation.getCode(), event.getValue()));
            }
        });
        filterRow.getCell(codeColumn).setComponent(codeFilter);

        // Filtro por tipo
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
        filterRow.getCell(typeColumn).setComponent(typeFilter);

        // Filtro por estado
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
        filterRow.getCell(statusColumn).setComponent(statusFilter);

        // Filtro por donante
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
        filterRow.getCell(donorColumn).setComponent(donorFilter);
    }

    private void refreshDonations() {
        donationService.clearCache();
        List<DonationDTO> donations = donationService.getDonationsByCatastrophe(selectedCatastrophe.getId());
        donationDataProvider = new ListDataProvider<>(donations);
        donationGrid.setItems(donations);
        donationGrid.getDataProvider().refreshAll();
    }

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