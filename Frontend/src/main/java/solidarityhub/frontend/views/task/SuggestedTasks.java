package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.service.TaskService;

import java.util.List;

@PageTitle("Suggested Tasks")
@Route("suggested-tasks")
public class SuggestedTasks extends VerticalLayout {
    private final TaskService taskService;
    private CatastropheDTO selectedCatastrophe;

    @Autowired
    public SuggestedTasks(TaskService taskService) {
        this.taskService = taskService;
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        Div header = new Div();
        header.addClassName("header");

        Button backButton = new Button(new Icon("vaadin", "arrow-left"));
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("tasks")));
        backButton.addClassName("back-button");

        H1 title = new H1("Tareas sugeridas");
        title.addClassName("title");

        header.add(backButton, title);

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(header, getSuggestedTasks(), getButtons());
    }

    private Component getButtons() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.addClassName("buttons-layout");

        Button acceptButton = new Button("Confirmar");
        acceptButton.addClickListener(event -> {
            // Handle acceptance of suggested tasks
            getUI().ifPresent(ui -> ui.navigate("tasks"));
        });

        Button cancelButton = new Button("Rechazar");
        cancelButton.addClickListener(event -> {
            // Handle cancellation
            getUI().ifPresent(ui -> ui.navigate("tasks"));
        });

        buttonsLayout.add(cancelButton, acceptButton);
        return buttonsLayout;
    }

    private HorizontalLayout getSuggestedTasks() {
        HorizontalLayout suggestedTasksLayout = new HorizontalLayout();
        suggestedTasksLayout.addClassName("centered-container");
        CheckboxGroup<TaskComponent> suggestedTasksCheckBox = new CheckboxGroup<>();

        List<TaskComponent> suggestedTasks;
        try {
            suggestedTasks = taskService.getTasksByCatastrophe(selectedCatastrophe.getId()).stream()
                    .map(TaskComponent::new).toList();

            if (suggestedTasks.isEmpty()) {
                suggestedTasksLayout.add(new Span("No hay tareas pendientes para esta catástrofe."));
            } else {
                suggestedTasksCheckBox.setItems(suggestedTasks);
                suggestedTasksCheckBox.setRenderer(getTaskRenderer());
            }
        } catch (Exception e) {
            suggestedTasksCheckBox.add(new Span("Error al cargar tareas: " + e.getMessage()));
        }

        suggestedTasksCheckBox.setThemeName("vertical");
        suggestedTasksCheckBox.addClassName("centered-checkbox-group");

        suggestedTasksLayout.add(suggestedTasksCheckBox);
        suggestedTasksLayout.setAlignItems(Alignment.CENTER);
        suggestedTasksLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        return suggestedTasksLayout;
    }

    private ComponentRenderer<Component, TaskComponent> getTaskRenderer() {
        return new ComponentRenderer<>(t -> {
            VerticalLayout card = new VerticalLayout();
            card.addClassName("task-card");
            HorizontalLayout header = new HorizontalLayout();
            header.setWidthFull();
            header.setJustifyContentMode(JustifyContentMode.BETWEEN);
            header.setAlignItems(Alignment.CENTER);
            header.add(t.getTaskName(), t.getStartDateTime());

            HorizontalLayout footer = new HorizontalLayout();
            footer.setWidthFull();
            footer.setJustifyContentMode(JustifyContentMode.BETWEEN);
            footer.setAlignItems(Alignment.CENTER);
            footer.add(t.getPriorityLevel(), t.getSettings());

            card.add(
                    header, new HorizontalLayout(t.getImg(), t.getTaskDescription()),
                    footer
            );
            return card;
        });
    }
}
