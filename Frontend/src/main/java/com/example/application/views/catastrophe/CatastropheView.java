package com.example.application.views.catastrophe;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;


@PageTitle("Catastrofes")
@Route("catastrophe")
@Menu(order = 0, icon = LineAwesomeIconUrl.CLOUD_RAIN_SOLID)
public class CatastropheView extends VerticalLayout {


    public CatastropheView(){
        // Configuración general
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Área de bienvenida
        H2 welcomeTitle = new H2("Bienvenido Facundo");
        welcomeTitle.getStyle().set("margin-bottom", "20px");
        welcomeTitle.getStyle().set("font-weight", "bold");

        // Sección de catástrofes
        VerticalLayout catastrophesSection = createCatastrophesSection();

        // Añadir componentes a la vista principal
        add(welcomeTitle, catastrophesSection);
        setHorizontalComponentAlignment(Alignment.CENTER, welcomeTitle);
        setHorizontalComponentAlignment(Alignment.CENTER, catastrophesSection);
        setFlexGrow(1, catastrophesSection);
    }



    private VerticalLayout createCatastrophesSection() {
        // Título de la sección
        H3 sectionTitle = new H3("Catastrofes");
        sectionTitle.getStyle().set("margin-top", "0");
        sectionTitle.getStyle().set("margin-bottom", "10px");

        // Tarjetas de catástrofes
        VerticalLayout cardsLayout = new VerticalLayout();
        cardsLayout.setPadding(false);
        cardsLayout.setSpacing(true);

        // Crear instancias del componente reutilizable
        CatastropheComponent card1 = new CatastropheComponent(
                "Terremoto San Juan",
                "Magnitud 6.4 en escala Richter",
                "07/03/25",
                null
        );

        CatastropheComponent card2 = new CatastropheComponent(
                "Inundaciones Litoral",
                "Evacuados: 2500 personas",
                "07/03/25",
                null
        );

        // Añadir las tarjetas al layout
        cardsLayout.add(card1, card2);

        // Botón para crear nueva catástrofe
        Button createButton = new Button("Crear catastrofe");
        createButton.getStyle().set("background-color", "lightblue");
        createButton.getStyle().set("color", "black");
        createButton.getStyle().set("border-radius", "4px");
        createButton.getStyle().set("margin-top", "20px");

        createButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(AddCatastrophe.class)));

        // Crear y devolver la sección completa
        VerticalLayout section = new VerticalLayout(sectionTitle, cardsLayout, createButton);
        section.setPadding(true);
        section.setSpacing(false);
        section.setMaxWidth("600px");
        section.setWidth("100%");

        return section;
    }

    private Div createCatastropheCard(String date) {
        // Contenedor principal de la tarjeta
        Div card = new Div();
        card.getStyle().set("background-color", "#f0f0f0");
        card.getStyle().set("border-radius", "8px");
        card.getStyle().set("padding", "10px");
        card.getStyle().set("width", "100%");
        card.getStyle().set("box-sizing", "border-box");

        // Icono placeholder para imagen
        Icon imageIcon = VaadinIcon.PICTURE.create();
        imageIcon.setSize("24px");

        // Contenido de texto
        Div textContent = new Div();
        textContent.getStyle().set("display", "inline-block");
        textContent.getStyle().set("margin-left", "10px");

        Span titleSpan = new Span("Nombre");
        titleSpan.getStyle().set("font-weight", "bold");
        titleSpan.getStyle().set("display", "block");

        Span descSpan = new Span("Descripción");
        descSpan.getStyle().set("color", "gray");
        descSpan.getStyle().set("display", "block");

        textContent.add(titleSpan, descSpan);

        // Fecha en la esquina superior derecha
        Span dateSpan = new Span(date);
        dateSpan.getStyle().set("position", "absolute");
        dateSpan.getStyle().set("right", "15px");
        dateSpan.getStyle().set("top", "10px");
        dateSpan.getStyle().set("font-size", "0.8em");
        dateSpan.getStyle().set("color", "gray");

        // Añadir componentes a la tarjeta
        card.getStyle().set("position", "relative");
        card.add(imageIcon, textContent, dateSpan);

        return card;
    }
}
