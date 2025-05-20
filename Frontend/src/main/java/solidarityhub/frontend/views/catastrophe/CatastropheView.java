package solidarityhub.frontend.views.catastrophe;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.EmptyLayout;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.i18n.Translator;
import org.pingu.domain.enums.EmergencyLevel;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.TaskService;
import solidarityhub.frontend.views.HeaderComponent;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@PageTitle("Catastrofes")
@Route(value = "select-catastrophe", layout = EmptyLayout.class)
@RouteAlias(value = "", layout = EmptyLayout.class)
public class CatastropheView extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Logger LOGGER = Logger.getLogger(CatastropheView.class.getName());
    private static Translator translator;

    private final CatastropheService catastropheService;
    private final TaskService taskService;

    public CatastropheView() {
        this.catastropheService = new CatastropheService();
        this.taskService = new TaskService();

        VaadinSession.getCurrent().setAttribute("cache", true);
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());

        buildView();
    }

    private void buildView() {
        addClassName("catastrophe-view");
        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        HeaderComponent header = new HeaderComponent(translator.get("select_catastrophe_title"));

        Paragraph subtitle = new Paragraph(translator.get("select_catastrophe_subtitle"));
        subtitle.addClassName("selection-subtitle");

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidthFull();
        buttonsLayout.add(getAddCatastropheButton());

        add(header, subtitle, buttonsLayout);

        VerticalLayout catastrophesContainer = new VerticalLayout();
        catastrophesContainer.addClassName("catastrophes-container");

        try {
            List<CatastropheDTO> catastrophes = catastropheService.getAllCatastrophes();

            if (catastrophes.isEmpty()) {
                add(new H3(translator.get("no_catastrophes_found")));
            } else {
                catastrophes.sort(Comparator.comparing((CatastropheDTO c) -> getEmergencyLevelWeight(c.getEmergencyLevel())).reversed());

                for (CatastropheDTO catastrophe : catastrophes) {
                    CatastropheComponent catastropheComp = createCatastropheComponent(catastrophe);
                    catastropheComp.addClickListener(e -> {
                        selectCatastrophe(catastrophe);
                        UI.getCurrent().navigate("home");
                    });
                    catastrophesContainer.add(catastropheComp);
                }
                add(catastrophesContainer);
            }
        } catch (Exception e) {
            Notification.show(translator.get("error_loading_catastrophes") + ": " + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            LOGGER.log(Level.SEVERE, translator.get("error_loading_catastrophes"), e);
        }
    }

    private static void selectCatastrophe(CatastropheDTO catastrophe) {
        // Guardar la catástrofe seleccionada en la sesión
        VaadinSession.getCurrent().setAttribute("selectedCatastrophe", catastrophe);
        Notification.show(translator.get("selected_catastrophe") + catastrophe.getName(),
                3000, Notification.Position.BOTTOM_START);
    }

    //===============================Get Components=========================================
    private Component getAddCatastropheButton() {
        Button addCatastropheButton = new Button(translator.get("add_catastrophe"));
        addCatastropheButton.addClassName("add-catastrophe-button");
        addCatastropheButton.addClickListener(e -> UI.getCurrent().navigate("add-catastrophe"));
        return addCatastropheButton;
    }

    private CatastropheComponent createCatastropheComponent(CatastropheDTO catastrophe) {
        CatastropheComponent catastropheComp = new CatastropheComponent(catastrophe);

        catastropheComp.setOnEditAction(v -> openEditDialog(catastrophe));

        return catastropheComp;
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

    private void openEditDialog(CatastropheDTO catastrophe) {
        try {
            EditCatastropheDialog dialog = new EditCatastropheDialog(catastrophe, catastropheService);
            dialog.open();
        } catch (Exception e) {
            Notification.show("Error al abrir el diálogo de edición: " + e.getMessage(),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }


}