package solidarityhub.frontend.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;

public class headerComponent extends Div {
    public Button backButton;

    public headerComponent(String titleText, String navigateUrl) {
        addClassName("header");

        backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate(navigateUrl)));
        backButton.addClassName("back-button");

        H1 title = new H1(titleText);
        title.addClassName("title");

        add(backButton, title);
    }

    public headerComponent(String titleText) {
        addClassName("header");

        H1 title = new H1(titleText);
        title.addClassName("title");

        Icon lightbulbIcon = new Icon("vaadin", "lightbulb");
        lightbulbIcon.addClassNames("suggested-tasks__icon");

        Button suggestedTasksButton = new Button(lightbulbIcon);
        suggestedTasksButton.addClickListener(e -> UI.getCurrent().navigate("suggested-tasks"));
        suggestedTasksButton.addClassNames("suggested-tasks__button");

        add(title, suggestedTasksButton);
    }
}
