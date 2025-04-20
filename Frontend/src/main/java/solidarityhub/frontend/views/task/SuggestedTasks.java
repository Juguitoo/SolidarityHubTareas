package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
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
import solidarityhub.frontend.views.headerComponent;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Suggested Tasks")
@Route("suggested-tasks")
public class SuggestedTasks extends VerticalLayout {
    private final TaskService taskService;
    private final CatastropheDTO selectedCatastrophe;

    @Autowired
    public SuggestedTasks(TaskService taskService) {
        this.taskService = taskService;
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        //Header
        headerComponent header = new headerComponent("Tareas sugeridas", "tasks");

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(header, getSuggestedTasks());
    }

    private VerticalLayout getButtons() {
        VerticalLayout buttonsLayout = new VerticalLayout();;
        buttonsLayout.addClassName("suggested-tasks-buttons");

        buttonsLayout.setSpacing(true);
        buttonsLayout.setPadding(false);
        buttonsLayout.setSizeUndefined();
        buttonsLayout.setAlignItems(Alignment.CENTER);

        Button acceptButton = new Button(new Icon("vaadin", "check"));
        acceptButton.getStyle().set("color", "green");
        acceptButton.addClickListener(event -> {getUI().ifPresent(ui -> ui.navigate("tasks"));});

        Button cancelButton = new Button(new Icon("vaadin", "close"));
        cancelButton.getStyle().set("color", "red");
        cancelButton.addClickListener(event -> {getUI().ifPresent(ui -> ui.navigate("tasks"));});

        buttonsLayout.add(acceptButton, cancelButton);
        return buttonsLayout;
    }

    private VerticalLayout getSuggestedTasks() {
        VerticalLayout suggestedTasksLayout = new VerticalLayout();
        suggestedTasksLayout.addClassName("suggested-tasks-layout");

        List<HorizontalLayout> suggestedTasks = new ArrayList<>();
        try {
            List<TaskComponent> tasks = taskService.getTasksByCatastrophe(selectedCatastrophe.getId()).stream()
                    .map(TaskComponent::new).toList();
            tasks.forEach(task -> {
                VerticalLayout buttons = getButtons();
                HorizontalLayout suggestedTask = new HorizontalLayout(task, buttons);
                suggestedTask.setWidthFull();
                suggestedTask.setAlignItems(Alignment.CENTER);
                suggestedTask.setSpacing(true);

                suggestedTasks.add(suggestedTask);
                suggestedTask.addClassName("suggested-task");
                suggestedTask.setFlexGrow(1, task);
            });

            if (suggestedTasks.isEmpty()) {
                suggestedTasksLayout.add(new Span("No hay tareas sugeridas para esta catástrofe."));
            } else {
                suggestedTasks.forEach(suggestedTasksLayout::add);
            }
        } catch (Exception e) {
            suggestedTasksLayout.add(new Span("Error al cargar tareas: " + e.getMessage()));
        }

        suggestedTasksLayout.setAlignItems(Alignment.CENTER);
        suggestedTasksLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        return suggestedTasksLayout;
    }
}
