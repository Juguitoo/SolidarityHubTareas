package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.service.NeedService;
import solidarityhub.frontend.service.TaskService;
import solidarityhub.frontend.views.headerComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@PageTitle("Suggested Tasks")
@Route("suggested-tasks")
public class SuggestedTasks extends VerticalLayout {
    private final TaskService taskService;
    private final NeedService needService;
    private final CatastropheDTO selectedCatastrophe;
    private List<TaskDTO> suggestedTasks;

    @Autowired
    public SuggestedTasks(TaskService taskService, NeedService needService) {
        this.taskService = taskService;
        this.needService = needService;
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        //Header
        headerComponent header = new headerComponent("Tareas sugeridas", "tasks");

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(header, getSuggestedTasks());
    }

    private VerticalLayout getSuggestedTasks() {
        VerticalLayout suggestedTasksLayout = new VerticalLayout();
        suggestedTasksLayout.addClassName("suggested-tasks-layout");

        List<Div> suggestedTasksDiv = new ArrayList<>();
        if (!taskService.suggestedTasksCache.isEmpty()) {
            suggestedTasks = taskService.suggestedTasksCache.stream()
                    .filter(task -> Objects.equals(task.getCatastropheId(), selectedCatastrophe.getId()))
                    .toList();
        }else if (needService.getNeedsWithoutTaskCount(selectedCatastrophe.getId()) > 2) {
            try {
                suggestedTasks = taskService.getSuggestedTasks(selectedCatastrophe.getId());
                List<TaskComponent> tasks = suggestedTasks.stream()
                        .map(TaskComponent::new).toList();
                tasks.forEach(task -> {
                    task.editButton.setEnabled(false);
                    Div clickableTask = clickableTaskComponent(task);
                    suggestedTasksDiv.add(clickableTask);
                    clickableTask.addClassName("suggested-task");
                });
            } catch (Exception e) {
                suggestedTasksLayout.add(new Span("Error al cargar tareas: " + e.getMessage()));
            }
        }

        if (suggestedTasksDiv.isEmpty()) {
            suggestedTasksLayout.add(new Span("No hay tareas sugeridas para esta catástrofe."));
        } else {
            suggestedTasksDiv.forEach(suggestedTasksLayout::add);
        }

        suggestedTasksLayout.setAlignItems(Alignment.CENTER);
        suggestedTasksLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        return suggestedTasksLayout;
    }

    public Div clickableTaskComponent(TaskComponent task) {
        Div clickableTask = new Div(task);

        clickableTask.addClickListener(event -> {
            TaskDTO selectedTask = suggestedTasks.stream().filter(t -> Objects.equals(t.getName(), task.getTaskName())).findFirst().orElse(null);
            VaadinSession.getCurrent().setAttribute("selectedSuggestedTask", selectedTask);
            getUI().ifPresent(ui -> ui.navigate("editSuggestedTask"));
        });

        clickableTask.getStyle().set("cursor", "pointer");
        return clickableTask;
    }
}
