package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.NeedService;
import solidarityhub.frontend.service.TaskService;
import solidarityhub.frontend.views.HeaderComponent;

import java.util.*;

@PageTitle("Suggested Tasks")
@Route("tasks/suggested-tasks")
public class SuggestedTasks extends VerticalLayout implements BeforeEnterObserver {
    private static final Translator translator = new Translator();
    private final TaskService taskService;
    private final CatastropheService catastropheService;
    private CatastropheDTO selectedCatastrophe;
    private List<TaskDTO> suggestedTasks;

    public SuggestedTasks() {
        translator.initializeTranslator();

        this.catastropheService = new CatastropheService();
        this.taskService = new TaskService();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
        if (!catastropheService.isCatastropheSelected(event, selectedCatastrophe)) {
            return;
        }

        buildView();
    }

    private void buildView() {
        addClassName("suggested-tasks-view");

        HeaderComponent header = new HeaderComponent(translator.get("suggested_tasks_button"), "window.history.back()");

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
        } else {
            try {
                suggestedTasks = taskService.getSuggestedTasks(selectedCatastrophe.getId());
                List<TaskComponent> tasks = suggestedTasks.stream()
                        .map(TaskComponent::new).toList();
                tasks.forEach(task -> {
                    task.editButton.setVisible(false);
                    Div clickableTask = clickableTaskComponent(task);
                    suggestedTasksDiv.add(clickableTask);
                });
            } catch (Exception e) {
                suggestedTasksLayout.add(new Span(translator.get("error_loading_suggested_tasks") + ": " + e.getMessage()));
            }
        }

        if (suggestedTasksDiv.isEmpty()) {
            suggestedTasksLayout.add(new Span(translator.get("no_suggested_tasks")));
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
            getUI().ifPresent(ui -> ui.navigate("tasks/editSuggestedTask"));
        });

        clickableTask.addClassName("suggested-task");
        clickableTask.getElement().setAttribute("title", translator.get("click_to_edit"));
        return clickableTask;
    }
}