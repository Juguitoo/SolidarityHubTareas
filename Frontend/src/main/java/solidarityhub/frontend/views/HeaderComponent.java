package solidarityhub.frontend.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;

public class HeaderComponent extends Div {
    public Button backButton;

    public HeaderComponent(String titleText, String navigateUrl) {
        addClassName("header");

        backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate(navigateUrl)));
        backButton.addClassName("back-button");

        H1 title = new H1(titleText);
        title.addClassName("title");

        add(backButton, title);
    }

    public HeaderComponent(String titleText) {
        addClassName("header");

        H1 title = new H1(titleText);
        title.addClassName("title");

        Icon bellIcon = new Icon("vaadin", "bell-o");
        bellIcon.addClassNames("notification-task__icon");

        Button suggestedTasksButton = new Button(bellIcon);
        suggestedTasksButton.addClickListener(e -> UI.getCurrent().navigate("suggested-tasks"));
        suggestedTasksButton.addClassName("notification-task__button");

        add(title, suggestedTasksButton);
    }
}
