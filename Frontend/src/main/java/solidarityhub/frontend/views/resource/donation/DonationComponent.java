package solidarityhub.frontend.views.resource.donation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import solidarityhub.frontend.dto.DonationDTO;
import solidarityhub.frontend.model.enums.DonationStatus;
import solidarityhub.frontend.model.enums.DonationType;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DonationComponent extends Div {

    private final DonationDTO donation;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DonationComponent(DonationDTO donation) {
        this.donation = donation;

        addClassName("donation-card");

        add(
                createHeader(),
                createContent(),
                createFooter()
        );
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("donation-header");
        header.setWidthFull();

        Span title = new Span(donation.getCode());
        title.addClassName("donation-title");

        Span date = new Span(donation.getDate().format(DATE_FORMATTER));
        date.addClassName("donation-date");

        header.add(title, date);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        return header;
    }

    private Component createContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        HorizontalLayout typeRow = new HorizontalLayout();
        Span type = new Span(formatDonationType(donation.getType()));
        type.addClassName("donation-type");
        typeRow.add(type);

        Span description = new Span(donation.getDescription());
        description.addClassName("donation-description");

        content.add(typeRow, description);

        return content;
    }

    private Component createFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.addClassName("donation-footer");
        footer.setWidthFull();

        if (donation.getType() == DonationType.FINANCIAL) {
            Span amount = new Span(String.format(Locale.getDefault(), "%.2f €", donation.getAmount()));
            amount.addClassName("donation-amount");
            footer.add(amount);
        } else {
            footer.add(new Span());
        }

        Span status = new Span(formatDonationStatus(donation.getStatus()));
        status.addClassName("donation-status");

        // Add specific class based on status
        switch (donation.getStatus()) {
            case COMPLETED:
                status.addClassName("status-completed");
                break;
            case IN_PROGRESS:
                status.addClassName("status-in-progress");
                break;
            case SCHEDULED:
                status.addClassName("status-scheduled");
                break;
            case CANCELLED:
                status.addClassName("status-cancelled");
                break;
        }

        footer.add(status);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);

        return footer;
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
