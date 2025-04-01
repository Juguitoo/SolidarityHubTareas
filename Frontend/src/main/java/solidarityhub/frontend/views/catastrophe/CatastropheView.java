package solidarityhub.frontend.views.catastrophe;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
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
        addClassName("catastrophe-view");

        // Área de bienvenida
        H2 welcomeTitle = new H2("Bienvenido Facundo");
        welcomeTitle.addClassName("welcome-title");

        // Sección de catástrofes
        VerticalLayout catastrophesSection = createCatastrophesSection();

        // Añadir componentes a la vista principal
        add(welcomeTitle, catastrophesSection);
    }



    private VerticalLayout createCatastrophesSection() {
        // Título de la sección
        H3 sectionTitle = new H3("Catastrofes");
        sectionTitle.addClassName("section-title");

        // Tarjetas de catástrofes
        VerticalLayout cardsLayout = new VerticalLayout();
        cardsLayout.addClassName("cards-layout");

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
        createButton.addClassName("create-button");
        createButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(AddCatastrophe.class)));

        // Crear y devolver la sección completa
        VerticalLayout section = new VerticalLayout(sectionTitle, cardsLayout, createButton);
        section.addClassName("catastrophes-section");

        return section;
    }


}
