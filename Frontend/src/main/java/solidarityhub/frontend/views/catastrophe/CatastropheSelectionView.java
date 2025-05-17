package solidarityhub.frontend.views.catastrophe;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.i18n.Translator;
import org.pingu.domain.enums.EmergencyLevel;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.TaskService;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;


@PageTitle("Catastrofes")
@Route("")
public class CatastropheSelectionView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Logger LOGGER = Logger.getLogger(CatastropheSelectionView.class.getName());
    private static Translator translator;

    @Autowired
    public CatastropheSelectionView(CatastropheService catastropheService, TaskService taskService) {
        VaadinSession.getCurrent().setAttribute("cache", true);
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());

        // Configuración de la vista
        addClassName("catastrophe-selection-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        // Crear componentes de encabezado
        H1 title = new H1(translator.get("select_catastrophe_title"));
        title.addClassName("selection-title");
        title.getStyle().set("margin-top", "20px");

        Paragraph subtitle = new Paragraph(translator.get("select_catastrophe_subtitle"));
        subtitle.addClassName("selection-subtitle");

        // Contenedor para las tarjetas de catástrofes
        FlexLayout cardsContainer = new FlexLayout();
        cardsContainer.addClassName("catastrophes-container");
        cardsContainer.getStyle().set("margin-top", "10px");

        try {
            // Obtener las catástrofes
            List<CatastropheDTO> catastrophes = catastropheService.getAllCatastrophes();

            if (catastrophes.isEmpty()) {
                add(title, subtitle, new H3(translator.get("no_catastrophes_found")));

                Button addCatastropheButton = new Button(translator.get("add_catastrophe"));
                addCatastropheButton.addClassName("add-catastrophe-button");
                addCatastropheButton.addClickListener(e -> UI.getCurrent().navigate("add-catastrophe"));
                add(addCatastropheButton);
            } else {
                // Añadir primero los componentes de título y subtítulo
                add(title, subtitle);

                // Ordenar las catástrofes por nivel de emergencia (MUY ALTO primero)
                catastrophes.sort(Comparator.comparing((CatastropheDTO c) -> {
                    // Ordenar de mayor a menor nivel de emergencia
                    return getEmergencyLevelWeight(c.getEmergencyLevel());
                }).reversed());

                // Luego agregar las tarjetas de catástrofes ordenadas
                for (CatastropheDTO catastrophe : catastrophes) {
                    cardsContainer.add(createCatastropheCard(catastrophe, taskService));
                }
                add(cardsContainer);

                Button addCatastropheButton = new Button(translator.get("add_new_catastrophe"));
                addCatastropheButton.addClassName("add-catastrophe-button");
                addCatastropheButton.addClickListener(e -> UI.getCurrent().navigate("add-catastrophe"));
                add(addCatastropheButton);
            }
        } catch (Exception e) {
            Notification.show(translator.get("error_loading_catastrophes") + ": " + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            LOGGER.log(Level.SEVERE, translator.get("error_loading_catastrophes"), e);
        }
    }

    private static int getEmergencyLevelWeight(EmergencyLevel level) {
        if (level == null) return 0;

        return switch (level) {
            case VERYHIGH -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    private static VerticalLayout createCatastropheCard(CatastropheDTO catastrophe, TaskService taskService) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("catastrophe-selection-card");

        // Aplicar estilo según nivel de emergencia
        String emergencyClass = getEmergencyLevelClass(catastrophe.getEmergencyLevel());
        card.addClassName(emergencyClass);

        H3 name = new H3(catastrophe.getName());
        name.addClassName("catastrophe-card-title");

        Paragraph description = new Paragraph(catastrophe.getDescription());
        description.addClassName("catastrophe-card-description");

        Paragraph date = new Paragraph(translator.get("start_date_catastrophe") +
                (catastrophe.getStartDate() != null ? catastrophe.getStartDate().format(DATE_FORMATTER) : "Fecha no disponible"));
        date.addClassName("catastrophe-card-date");

        // Nivel de emergencia
        Paragraph level = new Paragraph(translator.get("emergency_level") + formatEmergencyLevel(catastrophe.getEmergencyLevel()));
        level.addClassName("catastrophe-card-level");
        level.addClassName(emergencyClass + "-text");

        card.add(name, description, date, level);
        card.setPadding(true);
        card.setSpacing(false);

        // Al hacer clic, se selecciona esta catástrofe
        card.addClickListener(e -> {
            taskService.taskCache.clear();
            selectCatastrophe(catastrophe);
            UI.getCurrent().navigate("home");
        });

        return card;
    }

    private static void selectCatastrophe(CatastropheDTO catastrophe) {
        // Guardar la catástrofe seleccionada en la sesión
        VaadinSession.getCurrent().setAttribute("selectedCatastrophe", catastrophe);
        Notification.show(translator.get("selected_catastrophe") + catastrophe.getName(),
                3000, Notification.Position.BOTTOM_START);
    }

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