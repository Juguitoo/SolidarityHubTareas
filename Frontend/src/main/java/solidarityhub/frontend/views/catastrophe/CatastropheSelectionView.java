package solidarityhub.frontend.views.catastrophe;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.model.enums.EmergencyLevel;
import solidarityhub.frontend.service.CatastropheService;

import java.time.format.DateTimeFormatter;
import java.util.List;


@PageTitle("Catastrofes")
@Route("catastrophe")
@Menu(order = 0, icon = LineAwesomeIconUrl.CLOUD_RAIN_SOLID)
public class CatastropheSelectionView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final CatastropheService catastropheService;

    public CatastropheSelectionView(CatastropheService catastropheService) {
        this.catastropheService = catastropheService;

        addClassName("catastrophe-selection-view");
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Selecciona una Catástrofe");
        title.addClassName("selection-title");

        Paragraph subtitle = new Paragraph("Elige la catástrofe en la que quieres trabajar");
        subtitle.addClassName("selection-subtitle");

        FlexLayout cardsContainer = new FlexLayout();
        cardsContainer.addClassName("catastrophes-container");

        try {
            List<CatastropheDTO> catastrophes = catastropheService.getAllCatastrophes();

            if (catastrophes.isEmpty()) {
                add(title, subtitle, new H3("No hay catástrofes disponibles"));

                Button addCatastropheButton = new Button("Añadir una catástrofe");
                addCatastropheButton.addClassName("add-catastrophe-button");
                addCatastropheButton.addClickListener(e -> UI.getCurrent().navigate("add-catastrophe"));
                add(addCatastropheButton);
            } else {
                for (CatastropheDTO catastrophe : catastrophes) {
                    cardsContainer.add(createCatastropheCard(catastrophe));
                }

                add(title, subtitle, cardsContainer);

                Button addCatastropheButton = new Button("+ Añadir Nueva Catástrofe");
                addCatastropheButton.addClassName("add-catastrophe-button");
                addCatastropheButton.addClickListener(e -> UI.getCurrent().navigate("add-catastrophe"));
                add(addCatastropheButton);
            }
        } catch (Exception e) {
            Notification.show("Error al cargar las catástrofes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VerticalLayout createCatastropheCard(CatastropheDTO catastrophe) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("catastrophe-selection-card");

        // Aplicar estilo según nivel de emergencia
        String emergencyClass = getEmergencyLevelClass(catastrophe.getEmergencyLevel());
        card.addClassName(emergencyClass);

        H3 name = new H3(catastrophe.getName());
        name.addClassName("catastrophe-card-title");

        Paragraph description = new Paragraph(catastrophe.getDescription());
        description.addClassName("catastrophe-card-description");

        Paragraph date = new Paragraph("Inicio: " +
                (catastrophe.getStartDate() != null ? catastrophe.getStartDate().format(DATE_FORMATTER) : "Fecha no disponible"));
        date.addClassName("catastrophe-card-date");

        // Nivel de emergencia
        Paragraph level = new Paragraph("Nivel: " + formatEmergencyLevel(catastrophe.getEmergencyLevel()));
        level.addClassName("catastrophe-card-level");
        level.addClassName(emergencyClass + "-text");

        card.add(name, description, date, level);

        // Al hacer clic, se selecciona esta catástrofe
        card.addClickListener(e -> {
            selectCatastrophe(catastrophe);
            UI.getCurrent().navigate("tasks");
        });

        return card;
    }

    private void selectCatastrophe(CatastropheDTO catastrophe) {
        // Guardar la catástrofe seleccionada en la sesión
        VaadinSession.getCurrent().setAttribute("selectedCatastrophe", catastrophe);
        Notification.show("Catástrofe seleccionada: " + catastrophe.getName(),
                3000, Notification.Position.BOTTOM_START);
    }

    private String formatEmergencyLevel(EmergencyLevel level) {
        if (level == null) return "Desconocido";

        return switch (level) {
            case LOW -> "Bajo";
            case MEDIUM -> "Medio";
            case HIGH -> "Alto";
            case VERYHIGH -> "MUY ALTO";
        };
    }

    private String getEmergencyLevelClass(EmergencyLevel level) {
        if (level == null) return "emergency-unknown";

        return switch (level) {
            case LOW -> "emergency-low";
            case MEDIUM -> "emergency-medium";
            case HIGH -> "emergency-high";
            case VERYHIGH -> "emergency-very-high";
        };
    }
}
