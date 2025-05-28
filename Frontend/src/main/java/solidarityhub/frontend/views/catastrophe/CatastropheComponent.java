package solidarityhub.frontend.views.catastrophe;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;
import lombok.Getter;
import lombok.Setter;
import org.pingu.domain.enums.EmergencyLevel;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.i18n.Translator;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

@Getter
public class CatastropheComponent extends VerticalLayout {

    private String name;
    private String description;
    private String date;
    private final EmergencyLevel emergencyLevel;
    private static Translator translator = new Translator();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Setter
    private Consumer<Void> onEditAction;

    public CatastropheComponent(CatastropheDTO catastropheDTO) {
        this.name = catastropheDTO.getName();
        this.description = catastropheDTO.getDescription();
        this.date = catastropheDTO.getStartDate() != null ? catastropheDTO.getStartDate().format(DATE_FORMATTER) : "Fecha no disponible";
        this.emergencyLevel = catastropheDTO.getEmergencyLevel();

        translator.initializeTranslator();
        creatComponent();
    }

    private void creatComponent() {
        addClassName("catastrophe-card");
        addClassName(getEmergencyLevelClass(emergencyLevel));

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.add(getCatastropheNameComponent(), getStartDateComponent());
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout contentLayout = new HorizontalLayout(getCatastropheDescriptionComponent());
        contentLayout.setWidthFull();
        contentLayout.setFlexGrow(1, getCatastropheDescriptionComponent());

        HorizontalLayout footer = new HorizontalLayout(getCatastropheIcon(), getCatastropheEmergencyLevelComponent());

        getContextMenu();

        add(header, contentLayout, footer);
    }

    //===============================Get Components=========================================
    public Component getCatastropheNameComponent() {
        H2 catastropheNameTitle = new H2(name);
        catastropheNameTitle.addClassName("catastrophe-title");
        return catastropheNameTitle;
    }

    public Component getStartDateComponent() {
        Span startDateTimeSpan = new Span(translator.get("start_date_catastrophe") + date);
        startDateTimeSpan.addClassName("catastrophe-date");
        return startDateTimeSpan;
    }

    public Component getCatastropheDescriptionComponent() {
        Div taskDescriptionSpan = new Div(description);
        taskDescriptionSpan.addClassName("catastrophe-description");
        return taskDescriptionSpan;
    }

    private Component getCatastropheIcon() {
        if (emergencyLevel == null) {
            Icon fallbackIcon = VaadinIcon.EXCLAMATION_CIRCLE.create();
            fallbackIcon.setSize("24px");
            return fallbackIcon;
        }

        Icon icon;
        switch (emergencyLevel) {
            case LOW:
                icon = VaadinIcon.WARNING.create();
                icon.setColor("#4CAF50");
                break;
            case MEDIUM:
                icon = VaadinIcon.WARNING.create();
                icon.setColor("#FFC107");
                break;
            case HIGH:
                icon = VaadinIcon.WARNING.create();
                icon.setColor("#FF5722");
                break;
            case VERYHIGH:
                icon = VaadinIcon.WARNING.create();
                icon.setColor("#F44336");
                break;
            default:
                icon = VaadinIcon.EXCLAMATION_CIRCLE.create();
                break;
        }

        icon.setSize("24px");
        return icon;
    }

    private Component getCatastropheEmergencyLevelComponent() {
        Span levelSpan = new Span("Sin definir");
        if (emergencyLevel != null) {
            levelSpan = new Span(translator.get("emergency_level") + formatEmergencyLevel(emergencyLevel));
            levelSpan.addClassName(getEmergencyLevelClass(emergencyLevel) + "-text");
        }
        return levelSpan;
    }

    private void getContextMenu() {
        ContextMenu contextMenu = new ContextMenu(this);
        contextMenu.setOpenOnClick(false);
        contextMenu.addClassName("catastrophe-context-menu");

        contextMenu.addItem("Editar", event -> {
            if (onEditAction != null) {
                onEditAction.accept(null);
            }
        });
    }

    //===============================Set Methods=========================================
    private static String formatEmergencyLevel(EmergencyLevel level) {
        if (level == null) return translator.get("unknown_emergency_level");

        return switch (level) {
            case LOW -> translator.get("low_emergency_level");
            case MEDIUM -> translator.get("medium_emergency_level");
            case HIGH -> translator.get("high_emergency_level");
            case VERYHIGH -> translator.get("very_high_emergency_level");
        };
    }

    private static String getEmergencyLevelClass(EmergencyLevel level) {
        if (level == null) return "emergency-unknown";

        return switch (level) {
            case LOW -> "emergency-low";
            case MEDIUM -> "emergency-medium";
            case HIGH -> "emergency-high";
            case VERYHIGH -> "emergency-very-high";
        };
    }
}