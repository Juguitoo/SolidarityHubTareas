package solidarityhub.frontend.views.catastrophe;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
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
    private String imageUrl;

    /**
     * Constructor básico con valores por defecto
     */
    public CatastropheComponent() {
        this("Sin nombre", "Sin descripción", "Sin fecha", null);
    }

    /**
     * Constructor con todos los parámetros
     *
     * @param name Nombre de la catástrofe
     * @param description Descripción breve
     * @param date Fecha en formato texto
     * @param imageUrl URL de la imagen (opcional)
     */
    public CatastropheComponent(String name, String description, String date, String imageUrl) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.imageUrl = imageUrl;

        setupComponent();
    }

    /**
     * Configura el aspecto visual del componente
     */
    private void setupComponent() {
        // Estilo del contenedor principal
        addClassName("catastrophe-card");



        // Crear el componente para la imagen
        Component imageComponent;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Image image = new Image(imageUrl, "Imagen de catástrofe");
            image.setWidth("48px");
            image.setHeight("48px");
            imageComponent = image;
        } else {
            Icon fallbackIcon = VaadinIcon.PICTURE.create();
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        // Aquí habría que reimplementar la parte de la imagen
        // Para una implementación completa
    }
}
