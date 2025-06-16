package solidarityhub.frontend.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.i18n.Translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class HeaderComponent extends Div {
    private static Translator translator = new Translator();

    public Button backButton;
    public Button notificationButton;

    public HeaderComponent(String titleText, String navigateUrl) {
        initializeTranslator();
        addClassName("header");

        add(getBackButton(navigateUrl), getTitle(titleText), getNotificationButton());
    }

    public HeaderComponent(String titleText) {
        initializeTranslator();
        addClassName("header");

        add(getTitle(titleText), getNotificationButton());
    }

    private Component getTitle(String titleText) {
        H1 title = new H1(titleText);
        title.addClassName("title");
        return title;
    }

    private Component getNotificationButton() {
        Icon bellIcon = new Icon("vaadin", "bell-o");
        bellIcon.addClassName("notification__icon");

        Button notificationButton = new Button(bellIcon);
        notificationButton.addClassName("notification__button");
        notificationButton.addClickListener(e -> UI.getCurrent().navigate("notifications"));
        notificationButton.getElement().setAttribute("title", translator.get("notifications_button_tooltip"));

        this.notificationButton = notificationButton;
        return notificationButton;
    }

    private Component getBackButton(String navigateUrl) {
        Button backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> {
            if (Objects.equals(navigateUrl, "window.history.back()")) {
                UI.getCurrent().getPage().executeJs("window.history.back()");
            } else {
                UI.getCurrent().navigate(navigateUrl);
            }
        });
        backButton.addClassName("back-button");
        backButton.getElement().setAttribute("title", translator.get("back_button_tooltip"));
        this.backButton = backButton;
        return backButton;
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

    public static void updateAllNotificationButtons(boolean hasUnreadNotifications) {
        UI.getCurrent().accessSynchronously(() -> {
            findAllHeaderComponents(UI.getCurrent()).forEach(header -> {
                if (header.notificationButton != null) {
                    if (hasUnreadNotifications) {
                        header.notificationButton.addClassName("notification-indicator");
                    } else {
                        header.notificationButton.removeClassName("notification-indicator");
                    }
                }
            });
        });
    }

    private static List<HeaderComponent> findAllHeaderComponents(Component root) {
        List<HeaderComponent> headers = new ArrayList<>();

        if (root instanceof HeaderComponent) {
            headers.add((HeaderComponent) root);
        }

        root.getChildren().forEach(child -> {
            headers.addAll(findAllHeaderComponents(child));
        });

        return headers;
    }

}