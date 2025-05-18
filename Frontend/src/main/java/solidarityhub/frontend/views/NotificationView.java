package solidarityhub.frontend.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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

        buildView();
    }

    private void buildView() {
        addClassName("notifications-view");
        HeaderComponent header = new HeaderComponent(translator.get("notifications_title"), "window.history.back()");

        add(header, getEmptyState());
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

    private Component getEmptyState() {
        VerticalLayout emptyState = new VerticalLayout();
        emptyState.setAlignItems(Alignment.CENTER);
        emptyState.setJustifyContentMode(JustifyContentMode.START);
        emptyState.setSizeFull();

        Icon infoIcon = VaadinIcon.INFO_CIRCLE_O.create();
        infoIcon.addClassName("info-icon");

        H2 noNotificationsTitle = new H2(translator.get("no_notifications"));
        noNotificationsTitle.addClassName("no-notifications-title");

        Paragraph helpText = new Paragraph(translator.get("notifications_help_text"));
        helpText.addClassName("help-text");

        Button suggestedTasksButton = new Button(translator.get("go_to_suggested_tasks"), new Icon(VaadinIcon.ARROW_RIGHT));
        suggestedTasksButton.addClickListener(e -> UI.getCurrent().navigate("suggested-tasks"));

        emptyState.add(infoIcon, noNotificationsTitle, helpText, suggestedTasksButton);
        return emptyState;
    }
}