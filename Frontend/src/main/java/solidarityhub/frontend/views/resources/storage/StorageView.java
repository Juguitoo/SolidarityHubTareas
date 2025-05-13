package solidarityhub.frontend.views.resources.storage;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.StorageDTO;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.StorageService;

import java.util.*;

public class StorageView extends VerticalLayout {
    private final StorageService storageService;
    protected final CatastropheService catastropheService;

    protected CatastropheDTO selectedCatastrophe;

    private final Grid<StorageDTO> storageGrid;
    private ListDataProvider<StorageDTO> storageDataProvider;

    private Grid.Column<StorageDTO> nameColumn;
    private Grid.Column<StorageDTO> statusColumn;

    private String nameFilterValue = "";
    private final Set<Boolean> fullStatusFilterValues = new HashSet<>();

    public StorageView(CatastropheDTO catastrophe) {
        this.storageService = new StorageService();
        this.catastropheService = new CatastropheService();

        this.selectedCatastrophe = catastrophe;

        this.storageGrid = new Grid<>(StorageDTO.class, false);
        buildView();
    }

    private void buildView() {
        removeAll();

        setSizeFull();
        addClassName("storage-view");
        setPadding(false);

        add(storageGrid);
        populateStorageGrid();
    }

    //===============================Grid Methods=========================================
    private List<StorageDTO> getStorageList() {
        if (selectedCatastrophe != null) {
            return storageService.getStorages();
        } else {
            return Collections.emptyList();
        }
    }

    private void populateStorageGrid() {
        this.storageDataProvider = new ListDataProvider<>(getStorageList());

        if (storageDataProvider.getItems().isEmpty()) {
            storageGrid.setVisible(false);
            add(new Span("No hay almacenes disponibles para esta catástrofe."));
        } else {
            storageGrid.setVisible(true);
            storageGrid.setDataProvider(storageDataProvider);

            getGridColumns();
            getGridFilter();
        }

        storageGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        storageGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    //===============================Get Components=========================================
    private void getGridColumns() {
        nameColumn = storageGrid.addColumn(StorageDTO::getName)
                .setHeader("Nombre")
                .setAutoWidth(true);

        statusColumn = storageGrid.addColumn(this::getStorageStatusText)
                .setHeader("Estado")
                .setAutoWidth(true);
    }

    private void getGridFilter() {
        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder("Buscar almacén");
        nameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        nameFilter.setClearButtonVisible(true);
        nameFilter.addValueChangeListener(event -> {
            nameFilterValue = event.getValue();
            applyAllFilters();
        });
        nameColumn.setHeader(getGridFilterHeader("Nombre", nameFilter));

        MultiSelectComboBox<String> statusFilter = new MultiSelectComboBox<>();
        statusFilter.setPlaceholder("Filtrar por estado");
        statusFilter.setItems("Almacén lleno", "Almacén disponible");
        statusFilter.addValueChangeListener(event -> {
            fullStatusFilterValues.clear();
            Set<String> selectedStatuses = event.getValue();
            if (selectedStatuses.contains("Almacén lleno")) {
                fullStatusFilterValues.add(true);
            }
            if (selectedStatuses.contains("Almacén disponible")) {
                fullStatusFilterValues.add(false);
            }
            applyAllFilters();
        });
        statusColumn.setHeader(getGridFilterHeader("Estado", statusFilter));
    }

    private String getStorageStatusText(StorageDTO storage) {
        return storage.isFull() ? "Almacén lleno" : "Almacén disponible";
    }

    private Component getGridFilterHeader(String headerText, Component filterComponent) {
        HorizontalLayout filterHeader = new HorizontalLayout();
        Span filterTitle = new Span(headerText);
        filterHeader.setAlignItems(Alignment.CENTER);
        filterTitle.addClassName("grid__filter-title");
        filterHeader.add(filterTitle, filterComponent);

        filterHeader.addClassName("grid__filter-header");

        return filterHeader;
    }

    //=============================== Other Methods =========================================
    private void applyAllFilters() {
        storageDataProvider.clearFilters();

        // Filtro por nombre
        if (!nameFilterValue.isEmpty()) {
            storageDataProvider.addFilter(storage ->
                    StringUtils.containsIgnoreCase(storage.getName(), nameFilterValue));
        }

        // Filtro por estado (lleno/disponible)
        if (!fullStatusFilterValues.isEmpty()) {
            storageDataProvider.addFilter(storage ->
                    fullStatusFilterValues.contains(storage.isFull()));
        }
    }
}