package solidarityhub.frontend.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.i18n.Translator;

import java.util.Locale;
import java.util.Objects;

public class HeaderComponent extends Div {
    private static Translator translator;
    public Button backButton;

    public HeaderComponent(String titleText, String navigateUrl) {
        initializeTranslator();
        addClassName("header");

        backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> {
            if(Objects.equals(navigateUrl, "window.history.back()")){
                UI.getCurrent().getPage().executeJs("window.history.back()");
            } else {
                UI.getCurrent().navigate(navigateUrl);
            }
        });
        backButton.addClassName("back-button");
        backButton.getElement().setAttribute("title", translator.get("back_button_tooltip")); // Tooltip traducido

        H1 title = new H1(titleText);
        title.addClassName("title");

        add(backButton, title);
    }

    public HeaderComponent(String titleText) {
        initializeTranslator();
        addClassName("header");

        H1 title = new H1(titleText);
        title.addClassName("title");

        Icon bellIcon = new Icon("vaadin", "bell-o");
        bellIcon.addClassName("notification__icon");

        Button notificationButton = new Button(bellIcon);
        notificationButton.addClassName("notification__button");
        notificationButton.addClickListener(e -> UI.getCurrent().navigate("notifications"));
        notificationButton.getElement().setAttribute("title", translator.get("notifications_button_tooltip"));

        add(title, notificationButton);
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