package solidarityhub.frontend.views.catastrophe;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
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
public class CatastropheComponent extends Div {

    private String name;
    private String description;
    private String date;
    @Setter
    private String emergencyLevel;
    private static Translator translator;

    // Callbacks para las acciones
    private Consumer<Void> onEditAction;
    private Consumer<Void> onDeleteAction;

    public CatastropheComponent() {
        this("Sin nombre", "Sin descripción", "Sin fecha", "");
    }

    public CatastropheComponent(String name, String description, String date, String emergencyLevel) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.emergencyLevel = emergencyLevel;

        initializeTranslator();
        setupComponent();
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

    /**
     * Establecer callback para la acción de editar
     */
    public void setOnEditAction(Consumer<Void> callback) {
        this.onEditAction = callback;
    }

    /**
     * Establecer callback para la acción de eliminar
     */
    public void setOnDeleteAction(Consumer<Void> callback) {
        this.onDeleteAction = callback;
    }

    private void setupComponent() {
        // Estilo del contenedor principal
        addClassName("catastrophe-card");

        // Crear el componente para la imagen según el nivel de emergencia
        Component imageComponent;
        if (emergencyLevel.toLowerCase().contains(translator.get("low_emergency_level").toLowerCase())) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.setColor("#4CAF50"); // verde
            icon.setSize("24px");
            imageComponent = icon;
        } else if (emergencyLevel.toLowerCase().contains(translator.get("medium_emergency_level").toLowerCase())) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.setColor("#FFC107"); // amarillo
            icon.setSize("24px");
            imageComponent = icon;
        } else if (emergencyLevel.toLowerCase().contains(translator.get("high_emergency_level").toLowerCase()) &&
                !emergencyLevel.toLowerCase().contains(translator.get("very_high_emergency_level").toLowerCase())) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.setColor("#FF5722"); // naranja
            icon.setSize("24px");
            imageComponent = icon;
        } else if (emergencyLevel.toLowerCase().contains(translator.get("very_high_emergency_level").toLowerCase())) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.setColor("#F44336"); // rojo
            icon.setSize("24px");
            imageComponent = icon;
        } else {
            Icon fallbackIcon = VaadinIcon.EXCLAMATION_CIRCLE.create();
            fallbackIcon.setSize("24px");
            imageComponent = fallbackIcon;
        }

        VerticalLayout textContent = new VerticalLayout();
        textContent.setPadding(false);
        textContent.setSpacing(false);
        textContent.setMargin(false);

        Span titleSpan = new Span(name);
        titleSpan.addClassName("catastrophe-title");

        Span descSpan = new Span(description);
        descSpan.addClassName("catastrophe-description");

        textContent.add(titleSpan, descSpan);

        // Fecha en la esquina superior derecha
        Span dateSpan = new Span(date);
        dateSpan.addClassName("catastrophe-date");

        // Nivel de emergencia
        if (!emergencyLevel.isEmpty()) {
            Span levelSpan = new Span(emergencyLevel);
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
            textContent.add(levelSpan);
        }

        // Layout para imagen y texto
        HorizontalLayout contentLayout = new HorizontalLayout(imageComponent, textContent);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.setSpacing(true);
        contentLayout.setWidthFull();

        add(contentLayout, dateSpan);

        // Menú contextual (click derecho)
        ContextMenu contextMenu = new ContextMenu(this);
        contextMenu.setOpenOnClick(false); // Solo se abrirá con click derecho

        contextMenu.addItem("Editar", event -> {
            if (onEditAction != null) {
                onEditAction.accept(null);
            }
        });



        // Estilo al pasar el cursor
        getStyle().set("cursor", "pointer");
        getElement().addEventListener("mouseover", e ->
                getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)"));
        getElement().addEventListener("mouseout", e ->
                getStyle().set("box-shadow", "none"));
    }

    // Getters y setters
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