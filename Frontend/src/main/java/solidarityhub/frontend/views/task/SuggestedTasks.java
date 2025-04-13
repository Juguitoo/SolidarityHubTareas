package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@PageTitle("Suggested Tasks")
@Route("suggested-tasks")
public class SuggestedTasks extends VerticalLayout {

    public SuggestedTasks() {

        Div header = new Div();
        header.addClassName("header");

        Button backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("tasks")));
        backButton.addClassName("back-button");

        H1 title = new H1("Tareas sugeridas");
        title.addClassName("title");

        header.add(backButton, title);

        add(header, getSuggestedTasks());
    }

    private HorizontalLayout getSuggestedTasks() {
        HorizontalLayout suggestedTasksLayout = new HorizontalLayout();
        suggestedTasksLayout.addClassName("centered-container");
        CheckboxGroup<String> suggestedTasks = new CheckboxGroup<>();

        List<String> options = List.of("Opción 1", "Opción 2", "Opción 3");
        suggestedTasks.setItems(options);

        suggestedTasks.setThemeName("vertical");
        suggestedTasks.addClassName("centered-checkbox-group");

        suggestedTasksLayout.add(suggestedTasks);
        suggestedTasksLayout.setAlignItems(Alignment.CENTER);
        suggestedTasksLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        return suggestedTasksLayout;
    }
}
