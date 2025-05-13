package solidarityhub.frontend.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.i18n.Translator;

import java.util.Locale;

@Route("notifications")
@PageTitle("Notificaciones")
public class NotificationView extends VerticalLayout {
    private static Translator translator;

    public NotificationView() {
        initializeTranslator();

        // Usar el título traducido para el componente de cabecera
        HeaderComponent header = new HeaderComponent(translator.get("notifications_title"), "window.history.back()");

        // Contenido para mostrar cuando no hay notificaciones
        VerticalLayout emptyState = new VerticalLayout();
        emptyState.setAlignItems(Alignment.CENTER);
        emptyState.setJustifyContentMode(JustifyContentMode.CENTER);
        emptyState.setSizeFull();

        Icon infoIcon = VaadinIcon.INFO_CIRCLE_O.create();
        infoIcon.setSize("48px");
        infoIcon.setColor("var(--lumo-contrast-50pct)");

        H2 noNotificationsTitle = new H2(translator.get("no_notifications"));
        noNotificationsTitle.getStyle().set("margin-top", "1rem");
        noNotificationsTitle.getStyle().set("color", "var(--lumo-contrast-70pct)");

        Paragraph helpText = new Paragraph(translator.get("notifications_help_text"));
        helpText.getStyle().set("text-align", "center");
        helpText.getStyle().set("max-width", "500px");
        helpText.getStyle().set("color", "var(--lumo-contrast-50pct)");

        Button suggestedTasksButton = new Button(
                translator.get("go_to_suggested_tasks"),
                new Icon(VaadinIcon.ARROW_RIGHT)
        );
        suggestedTasksButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        suggestedTasksButton.addClickListener(e -> UI.getCurrent().navigate("suggested-tasks"));

        emptyState.add(infoIcon, noNotificationsTitle, helpText, suggestedTasksButton);

        add(header, emptyState);
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
}