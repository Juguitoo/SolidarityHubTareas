package solidarityhub.frontend.views.resources.storage;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.StorageDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.StorageService;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StorageView extends VerticalLayout {
    private final StorageService storageService;
    protected final CatastropheService catastropheService;

    protected CatastropheDTO selectedCatastrophe;

    private final Grid<StorageDTO> storageGrid;
    private ListDataProvider<StorageDTO> storageDataProvider;

    private Grid.Column<StorageDTO> nameColumn;
    private Grid.Column<StorageDTO> statusColumn;

    private final Translator translator;

    public StorageView(CatastropheDTO catastrophe) {
        this.storageService = new StorageService();
        this.catastropheService = new CatastropheService();

        this.selectedCatastrophe = catastrophe;

        this.storageGrid = new Grid<>(StorageDTO.class, false);

        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        }else{
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());

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
            add(new Span(translator.get("no_storages")));
        } else {
            storageGrid.setVisible(true);
            storageGrid.setDataProvider(storageDataProvider);

            getGridColumns();
        }

        storageGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        storageGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    //===============================Get Components=========================================
    private void getGridColumns() {
        nameColumn = storageGrid.addColumn(StorageDTO::getName)
                .setHeader(translator.get("storage_name"))
                .setAutoWidth(true);

        statusColumn = storageGrid.addColumn(this::getStorageStatusText)
                .setHeader(translator.get("storage_status"))
                .setAutoWidth(true);
    }

    private String getStorageStatusText(StorageDTO storage) {
        return storage.isFull() ? translator.get("storage_full") : translator.get("storage_available");
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
}