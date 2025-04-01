package solidarityhub.frontend.views.catastrophe;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import solidarityhub.frontend.service.CatastropheService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@PageTitle("Catastrofes")
@Route("catastrophe")
@Menu(order = 0, icon = LineAwesomeIconUrl.CLOUD_RAIN_SOLID)
public class CatastropheView extends VerticalLayout {

    private final CatastropheService catastropheService;

    public CatastropheView(CatastropheService catastropheService) {
        this.catastropheService = catastropheService;
        // Configuración general
        addClassName("catastrophe-view");

        // Área de bienvenida
        H2 welcomeTitle = new H2("Bienvenido Facundo");
        welcomeTitle.addClassName("welcome-title");

        // Sección de catástrofes
        VerticalLayout catastrophesSection = createCatastrophesSection();

        // Botón de añadir con símbolo +
        Button addCatastropheButton = new Button(new Icon("lumo", "plus"));
        addCatastropheButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("addcatastrophe")));
        addCatastropheButton.addClassName("add-task-button");

        // Añadir componentes a la vista principal
        add(welcomeTitle, catastrophesSection, addCatastropheButton);

    }

    private VerticalLayout createCatastrophesSection() {
        // Título de la sección
        H3 sectionTitle = new H3("Catastrofes");
        sectionTitle.addClassName("section-title");

        // Tarjetas de catástrofes
        VerticalLayout cardsLayout = new VerticalLayout();
        cardsLayout.addClassName("cards-layout");

        // Crear instancias del componente reutilizable
        List<CatastropheComponent> catastropheComponents = new ArrayList<>();
        try {
            catastropheComponents = catastropheService.getAllCatastrophes().stream()
                    .map(catastrophe -> new CatastropheComponent(
                            catastrophe.getName(),
                            catastrophe.getDescription(),
                            formatDate(catastrophe.getStartDate().atStartOfDay()),
                            ""))
                    .toList();

            if (catastropheComponents.isEmpty()) {
                cardsLayout.add(new Span("No hay catástrofes disponibles."));
            } else {
                catastropheComponents.forEach(cardsLayout::add);
            }
        } catch (Exception e) {
            cardsLayout.add(new Span("Error cargando catástrofes: " + e.getMessage()));
            e.printStackTrace();
        }

        // Botón para ver todas las catástrofes
        Button viewAllButton = new Button("Ver todas las catástrofes");
        viewAllButton.addClassName("button");
        viewAllButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("morecatastrophes")));

        // Crear y devolver la sección completa
        VerticalLayout section = new VerticalLayout(sectionTitle, cardsLayout, viewAllButton);
        section.addClassName("catastrophes-section");

        return section;
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) {
            return "Sin fecha";
        }
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}
