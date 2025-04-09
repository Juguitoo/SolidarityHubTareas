package solidarityhub.frontend.views.catastrophe;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Componente reutilizable que representa una catástrofe en la interfaz.
 * Muestra información como nombre, descripción, fecha y una imagen o ícono.
 */

public class CatastropheComponent extends Div {

    private String name;
    private String description;
    private String date;
    private String emergencyLevel;

    /**
     * Constructor básico con valores por defecto
     */
    public CatastropheComponent() {
        this("Sin nombre", "Sin descripción", "Sin fecha", "");
    }

    /**
     * Constructor con todos los parámetros
     *
     * @param name Nombre de la catástrofe
     * @param description Descripción breve
     * @param date Fecha en formato texto
     * @param emergencyLevel Nivel de emergencia (texto descriptivo)
     */
    public CatastropheComponent(String name, String description, String date, String emergencyLevel) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.emergencyLevel = emergencyLevel;

        setupComponent();
    }

    /**
     * Configura el aspecto visual del componente
     */
    private void setupComponent() {
        // Estilo del contenedor principal
        addClassName("catastrophe-card");

        // Crear el componente para la imagen según el nivel de emergencia
        Component imageComponent;
        if (emergencyLevel.contains("bajo")) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.setColor("#4CAF50"); // verde
            icon.setSize("24px");
            imageComponent = icon;
        } else if (emergencyLevel.contains("medio")) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.setColor("#FFC107"); // amarillo
            icon.setSize("24px");
            imageComponent = icon;
        } else if (emergencyLevel.contains("alto")) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.setColor("#FF5722"); // naranja
            icon.setSize("24px");
            imageComponent = icon;
        } else if (emergencyLevel.contains("MUY ALTO")) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.setColor("#F44336"); // rojo
            icon.setSize("24px");
            imageComponent = icon;
        } else {
            Icon fallbackIcon = VaadinIcon.EXCLAMATION_CIRCLE.create();
            fallbackIcon.setSize("24px");
            imageComponent = fallbackIcon;
        }

        // Contenido de texto
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
            if (emergencyLevel.contains("alto")) {
                levelSpan.getElement().getStyle().set("color", "#FF5722");
                levelSpan.getElement().getStyle().set("font-weight", "bold");
            } else if (emergencyLevel.contains("MUY ALTO")) {
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

        // Añadir todos los componentes
        add(contentLayout, dateSpan);

        // Agregar eventos de hover para mejorar la experiencia de usuario
        addClickListener(event -> {
            // Aquí se puede implementar la acción de clic en la catástrofe
            getElement().getStyle().set("cursor", "pointer");
        });

        // Efecto hover
        getElement().addEventListener("mouseover", e ->
                getStyle().set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)"));
        getElement().addEventListener("mouseout", e ->
                getStyle().set("box-shadow", "none"));
    }

    // Getters y setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        getElement().getChildren()
                .filter(child -> child.getTag().equals("span"))
                .filter(child -> child.hasAttribute("class"))
                .filter(child -> child.getAttribute("class").contains("catastrophe-title"))
                .findFirst()
                .ifPresent(element -> element.setText(name));
    }

    public String getDescription() {
        return description;
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

    public String getDate() {
        return date;
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
    public String getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(String emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
        // Aquí habría que reimplementar la parte del nivel de emergencia
        // Para una implementación completa
    }

}
