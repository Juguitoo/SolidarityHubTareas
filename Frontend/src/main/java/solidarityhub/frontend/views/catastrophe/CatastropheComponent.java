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
import solidarityhub.frontend.i18n.Translator;

import java.util.Locale;
import java.util.function.Consumer;

@Getter
public class CatastropheComponent extends VerticalLayout {

    private String name;
    private String description;
    private String date;
    private final String emergencyLevel;
    private static Translator translator;

    @Setter
    private Consumer<Void> onEditAction;

    public CatastropheComponent(String name, String description, String date, String emergencyLevel) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.emergencyLevel = emergencyLevel;

        initializeTranslator();
        creatComponent();
    }

    private void initializeTranslator() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());
    }

    private void creatComponent() {
        addClassName("catastrophe-card");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.add(getCatastropheNameComponent(), getStartDateComponent());
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
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
        Span startDateTimeSpan = new Span(date.replace('T', ' '));
        startDateTimeSpan.addClassName("catastrophe-date");
        return startDateTimeSpan;
    }

    public Component getCatastropheDescriptionComponent() {
        Div taskDescriptionSpan = new Div(description);
        taskDescriptionSpan.addClassName("catastrophe-description");
        return taskDescriptionSpan;
    }

    private Component getCatastropheIcon() {
        Component imageComponent;
        if (emergencyLevel.toLowerCase().contains(translator.get("low_emergency_level").toLowerCase())) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.setColor("#4CAF50");
            icon.setSize("24px");
            imageComponent = icon;
        } else if (emergencyLevel.toLowerCase().contains(translator.get("medium_emergency_level").toLowerCase())) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.setColor("#FFC107");
            icon.setSize("24px");
            imageComponent = icon;
        } else if (emergencyLevel.toLowerCase().contains(translator.get("high_emergency_level").toLowerCase()) &&
                !emergencyLevel.toLowerCase().contains(translator.get("very_high_emergency_level").toLowerCase())) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.setColor("#FF5722");
            icon.setSize("24px");
            imageComponent = icon;
        } else if (emergencyLevel.toLowerCase().contains(translator.get("very_high_emergency_level").toLowerCase())) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.setColor("#F44336");
            icon.setSize("24px");
            imageComponent = icon;
        } else {
            Icon fallbackIcon = VaadinIcon.EXCLAMATION_CIRCLE.create();
            fallbackIcon.setSize("24px");
            imageComponent = fallbackIcon;
        }
        return imageComponent;
    }

    private Component getCatastropheEmergencyLevelComponent() {
        Span levelSpan = new Span("Sin definir");
        if (!emergencyLevel.isEmpty()) {
            levelSpan = new Span(emergencyLevel);
            levelSpan.addClassName("catastrophe-level");
            if (emergencyLevel.toLowerCase().contains(translator.get("high_emergency_level").toLowerCase()) &&
                    !emergencyLevel.toLowerCase().contains(translator.get("very_high_emergency_level").toLowerCase())) {
                levelSpan.getElement().getStyle().set("color", "#FF5722");
                levelSpan.getElement().getStyle().set("font-weight", "bold");
            } else if (emergencyLevel.toLowerCase().contains(translator.get("very_high_emergency_level").toLowerCase())) {
                levelSpan.getElement().getStyle().set("color", "#F44336");
                levelSpan.getElement().getStyle().set("font-weight", "bold");
                levelSpan.getElement().getStyle().set("text-transform", "uppercase");
            }
        }
        return levelSpan;
    }

    private void getContextMenu() {
        ContextMenu contextMenu = new ContextMenu(this);
        contextMenu.setOpenOnClick(false); // Solo se abrirá con click derecho

        contextMenu.addItem("Editar", event -> {
            if (onEditAction != null) {
                onEditAction.accept(null);
            }
        });

        getStyle().set("cursor", "pointer");
        getElement().addEventListener("mouseover", e ->
                getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)"));
        getElement().addEventListener("mouseout", e ->
                getStyle().set("box-shadow", "none"));
    }

    //===============================Set Methods=========================================
    public void setName(String name) {
        this.name = name;
        getElement().getChildren()
                .filter(child -> child.getTag().equals("span"))
                .filter(child -> child.hasAttribute("class"))
                .filter(child -> child.getAttribute("class").contains("catastrophe-title"))
                .findFirst()
                .ifPresent(element -> element.setText(name));
    }

    public void setDescription(String description) {
        this.description = description;
        getElement().getChildren()
                .filter(child -> child.getTag().equals("span"))
                .filter(child -> child.hasAttribute("class"))
                .filter(child -> child.getAttribute("class").contains("catastrophe-description"))
                .findFirst()
                .ifPresent(element -> element.setText(description));
    }

    public void setDate(String date) {
        this.date = date;
        getElement().getChildren()
                .filter(child -> child.getTag().equals("span"))
                .filter(child -> child.hasAttribute("class"))
                .filter(child -> child.getAttribute("class").contains("catastrophe-date"))
                .findFirst()
                .ifPresent(element -> element.setText(date));
    }
}