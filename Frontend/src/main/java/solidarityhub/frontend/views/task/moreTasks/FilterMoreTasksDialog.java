package solidarityhub.frontend.views.task.moreTasks;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.server.VaadinSession;
import org.pingu.domain.enums.EmergencyLevel;
import org.pingu.domain.enums.Priority;
import org.pingu.domain.enums.Status;
import org.pingu.domain.enums.TaskType;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.FormatService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilterMoreTasksDialog extends Dialog {
    private final Translator translator = new Translator();
    private final FormatService formatService;

    private final Select<Status> statusFilter = new Select<>();
    private final Select<Priority> priorityFilter = new Select<>();
    private final Select<TaskType> typeFilter = new Select<>();
    private final Select<EmergencyLevel> emergencyLevelFilter = new Select<>();


    public FilterMoreTasksDialog() {
        translator.initializeTranslator();
        this.formatService = FormatService.getInstance();

        buildView();
    }

    public void buildView() {
        setHeaderTitle(translator.get("filter_tasks"));

        add(getFilters());
        getFooter().add(getButtons());
    }

    private Component getFilters() {
        VerticalLayout filters = new VerticalLayout();

        statusFilter.setItems(Status.values());
        statusFilter.setItemLabelGenerator(formatService::formatTaskStatus);
        statusFilter.setWidthFull();
        statusFilter.setLabel(translator.get("filter_status") + ":");
        statusFilter.setHelperText(translator.get("filter_status_helper"));

        HorizontalLayout statusFilterLayout = new HorizontalLayout();
        statusFilterLayout.setWidthFull();
        statusFilterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        statusFilterLayout.add(statusFilter, getClearFilter(statusFilter));

        priorityFilter.setItems(Priority.values());
        priorityFilter.setItemLabelGenerator(formatService::formatPriority);
        priorityFilter.setWidthFull();
        priorityFilter.setLabel(translator.get("filter_priority") + ":");
        priorityFilter.setHelperText(translator.get("filter_priority_helper"));

        HorizontalLayout priorityFilterLayout = new HorizontalLayout();
        priorityFilterLayout.setWidthFull();
        priorityFilterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        priorityFilterLayout.add(priorityFilter, getClearFilter(priorityFilter));

        typeFilter.setItems(TaskType.values());
        typeFilter.setItemLabelGenerator(formatService::formatTaskType);
        typeFilter.setWidthFull();
        typeFilter.setLabel(translator.get("filter_type") + ":");
        typeFilter.setHelperText(translator.get("filter_type_helper"));

        HorizontalLayout typeFilterLayout = new HorizontalLayout();
        typeFilterLayout.setWidthFull();
        typeFilterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        typeFilterLayout.add(typeFilter, getClearFilter(typeFilter));

        emergencyLevelFilter.setItems(EmergencyLevel.values());
        emergencyLevelFilter.setItemLabelGenerator(formatService::formatEmergencyLevel);
        emergencyLevelFilter.setWidthFull();
        emergencyLevelFilter.setLabel(translator.get("filter_emergency_level") + ":");
        emergencyLevelFilter.setHelperText(translator.get("filter_emergency_level_helper"));

        HorizontalLayout emergencyLevelFilterLayout = new HorizontalLayout();
        emergencyLevelFilterLayout.setWidthFull();
        emergencyLevelFilterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        emergencyLevelFilterLayout.add(emergencyLevelFilter, getClearFilter(emergencyLevelFilter));

        filters.add(statusFilterLayout, priorityFilterLayout, typeFilterLayout, emergencyLevelFilterLayout);
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
            statusFilter.clear();
            priorityFilter.clear();
            typeFilter.clear();
            emergencyLevelFilter.clear();
        });
        HorizontalLayout clearButtonLayout = new HorizontalLayout(clearButton);
        clearButtonLayout.setWidthFull();
        clearButtonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        buttons.add(clearButtonLayout, applyButton);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return buttons;
    }



    public List<String> getSelectedFilters(){
        List<String> filters = new ArrayList<>();
        if (statusFilter.getValue() == null) {
            filters.add("");
        } else {
            filters.add(statusFilter.getValue().toString());
        }
        if (priorityFilter.getValue() == null) {
            filters.add("");
        } else {
            filters.add(priorityFilter.getValue().toString());
        }
        if (typeFilter.getValue() == null) {
            filters.add("");
        } else {
            filters.add(typeFilter.getValue().toString());
        }
        if (emergencyLevelFilter.getValue() == null) {
            filters.add("");
        } else {
            filters.add(emergencyLevelFilter.getValue().toString());
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
            }
        });
        clearButton.getStyle().set("margin-top", "15px");
        return clearButton;
    }

}
