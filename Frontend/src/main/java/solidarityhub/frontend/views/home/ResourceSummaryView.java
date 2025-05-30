package solidarityhub.frontend.views.home;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.VaadinSession;
import org.pingu.domain.enums.ResourceType;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.ResourceSummaryDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.FormatService;
import solidarityhub.frontend.service.ResourceSummaryService;

import java.util.List;
import java.util.Locale;

public class ResourceSummaryView extends VerticalLayout {
    private final ResourceSummaryService resourceSummaryService;
    private final CatastropheDTO catastrophe;
    private static Translator translator = new Translator();
    private final FormatService formatService;

    public ResourceSummaryView(CatastropheDTO catastrophe) {
        this.resourceSummaryService = new ResourceSummaryService();
        this.catastrophe = catastrophe;
        this.formatService = FormatService.getInstance();

        translator.initializeTranslator();
        buildView();
    }

    private void buildView() {
        addClassName("resource-summary-view");
        setPadding(true);
        setSpacing(true);

        H3 title = new H3(translator.get("resource_summary"));
        title.addClassName("section-title");

        add(title);

        List<ResourceSummaryDTO> summaries = resourceSummaryService.getResourceSummary(catastrophe.getId());


        if (summaries.isEmpty()) {
            add(new Span(translator.get("no_resources")));
        } else {
            for (ResourceSummaryDTO summary : summaries) {
                add(createResourceSummaryCard(summary));
            }
        }
    }

    private Component createResourceSummaryCard(ResourceSummaryDTO summary) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("resource-summary-card");
        card.setPadding(true);
        card.setSpacing(true);

        // Header with type and count
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span typeName = new Span(formatService.formatResourceType(summary.getType()));
        typeName.addClassName("resource-type-name");

        Span count = new Span(summary.getCount() + " " + translator.get("items"));
        count.addClassName("resource-count");

        header.add(typeName, count);

        // Quantity info
        HorizontalLayout quantityInfo = new HorizontalLayout();
        quantityInfo.setWidthFull();
        quantityInfo.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span totalLabel = new Span(translator.get("total") + ":");
        Span totalValue = new Span(String.format("%.2f", summary.getTotalQuantity()));

        Span availableLabel = new Span(translator.get("available") + ":");
        Span availableValue = new Span(String.format("%.2f", summary.getAvailableQuantity()));

        quantityInfo.add(totalLabel, totalValue, availableLabel, availableValue);

        // Usage progress bar
        VerticalLayout progressLayout = new VerticalLayout();
        progressLayout.setPadding(false);
        progressLayout.setSpacing(false);

        ProgressBar usageProgress = new ProgressBar();
        usageProgress.setMin(0);
        usageProgress.setMax(100);
        double usagePercentage = summary.getUsagePercentage();
        double safeValue = Math.max(0.0, Math.min(100.0, usagePercentage));
        usageProgress.setValue(safeValue);
        usageProgress.setWidthFull();

        Span usageLabel = new Span(String.format("%.1f%% " + translator.get("used"),
                summary.getUsagePercentage()));
        usageLabel.addClassName("usage-label");

        progressLayout.add(usageProgress, usageLabel);

        card.add(header, quantityInfo, progressLayout);

        return card;
    }


}
