package solidarityhub.frontend.views.resources.donation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.server.VaadinSession;
import org.pingu.domain.enums.DonationStatus;
import org.pingu.domain.enums.DonationType;
import solidarityhub.frontend.i18n.Translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilterDonationsDialog extends Dialog {

    private Select<DonationType> donationTypeFilter = new Select<>();
    private Select<DonationStatus> statusFilter = new Select<>();
    private NumberField quantityFilter = new NumberField();
    private IntegerField yearFilter = new IntegerField();

    private final Translator translator = new Translator();

    public FilterDonationsDialog() {
        translator.initializeTranslator();

        buildView();
        loadFiltersData();
    }

    public void buildView() {
        setHeaderTitle(translator.get("filter_donation_title"));

        add(getFilters());
        getFooter().add(getButtons());
    }

    private void loadFiltersData() {
        if (DonationView.quantityFilterValue != null && !DonationView.quantityFilterValue.isEmpty()) {
            quantityFilter.setValue(Double.parseDouble(DonationView.quantityFilterValue));
        }
        if (DonationView.typeFilterValue != null && !DonationView.typeFilterValue.isEmpty()) {
            donationTypeFilter.setValue(DonationType.valueOf(DonationView.typeFilterValue));
        }
        if (DonationView.statusFilterValue != null && !DonationView.statusFilterValue.isEmpty()) {
            statusFilter.setValue(DonationStatus.valueOf(DonationView.statusFilterValue));
        }
        if (DonationView.yearFilterValue != null && !DonationView.yearFilterValue.isEmpty()) {
            yearFilter.setValue(Integer.parseInt(DonationView.yearFilterValue));
        }
    }

    private Component getFilters() {
        VerticalLayout filters = new VerticalLayout();

        donationTypeFilter.setItems(DonationType.values());
        donationTypeFilter.setItemLabelGenerator(this::formatDonationType);
        donationTypeFilter.setWidthFull();
        donationTypeFilter.setLabel(translator.get("filter_donation_type_title") + ":");
        donationTypeFilter.setHelperText(translator.get("filter_donation_type_helper"));

        HorizontalLayout typeFilterLayout = new HorizontalLayout();
        typeFilterLayout.setWidthFull();
        typeFilterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        typeFilterLayout.add(donationTypeFilter, getClearFilter(donationTypeFilter));

        statusFilter.setItems(DonationStatus.values());
        statusFilter.setItemLabelGenerator(this::formatDonationStatus);
        statusFilter.setWidthFull();
        statusFilter.setLabel(translator.get("filter_donation_status_title") + ":");
        statusFilter.setHelperText(translator.get("filter_donation_status_helper"));

        HorizontalLayout statusFilterLayout = new HorizontalLayout();
        statusFilterLayout.setWidthFull();
        statusFilterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        statusFilterLayout.add(statusFilter, getClearFilter(statusFilter));

        quantityFilter.setLabel(translator.get("filter_donation_quantity_title") + ":");
        quantityFilter.setWidthFull();
        quantityFilter.setHelperText(translator.get("filter_donation_quantity_helper"));

        HorizontalLayout quantityFilterLayout = new HorizontalLayout();
        quantityFilterLayout.setWidthFull();
        quantityFilterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        quantityFilterLayout.add(quantityFilter, getClearFilter(quantityFilter));

        yearFilter.setLabel(translator.get("filter_donation_year_title") + ":");
        yearFilter.setWidthFull();
        yearFilter.setHelperText(translator.get("filter_donation_year_helper"));

        HorizontalLayout yearFilterLayout = new HorizontalLayout();
        yearFilterLayout.setWidthFull();
        yearFilterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        yearFilterLayout.add(yearFilter, getClearFilter(yearFilter));

        filters.add(typeFilterLayout, statusFilterLayout, quantityFilterLayout, yearFilterLayout);
        filters.setAlignItems(FlexComponent.Alignment.START);
        filters.setSpacing(false);
        filters.getStyle().set("row-gap", "0px");
        return filters;
    }

    public HorizontalLayout getButtons() {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();

        Button applyButton = new Button(translator.get("apply_filters"));
        applyButton.addClickListener(event -> {
            close();
        });

        Button clearButton = new Button(translator.get("clear_filters"));
        clearButton.addClickListener(event -> {
            donationTypeFilter.clear();
            statusFilter.clear();
            quantityFilter.clear();
            yearFilter.clear();
        });
        HorizontalLayout clearButtonLayout = new HorizontalLayout(clearButton);
        clearButtonLayout.setWidthFull();
        clearButtonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        buttons.add(clearButtonLayout, applyButton);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return buttons;
    }

    public List<String> getSelectedFilters() {
        List<String> filters = new ArrayList<>();
        if(donationTypeFilter.getValue() == null) {
            filters.add("");
        } else {
            filters.add(donationTypeFilter.getValue().toString());
        }
        if(statusFilter.getValue() == null) {
            filters.add("");
        } else {
            filters.add(String.valueOf(statusFilter.getValue()));
        }
        if(quantityFilter.getValue() == null) {
            filters.add("");
        } else {
            filters.add(quantityFilter.getValue().toString());
        }
        if(yearFilter.getValue() == null) {
            filters.add("");
        } else {
            filters.add(yearFilter.getValue().toString());
        }
        return filters;
    }

    public Button getClearFilter(Component component) {
        Button clearButton = new Button(new Icon("vaadin", "close-big"));
        clearButton.addClickListener(event -> {
            if (component instanceof Select) {
                ((Select<?>) component).clear();
            } else if (component instanceof NumberField) {
                ((NumberField) component).clear();
            } else if (component instanceof IntegerField) {
                ((IntegerField) component).clear();
            }
        });
        clearButton.getStyle().set("margin-top", "15px");
        return clearButton;
    }

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
}
