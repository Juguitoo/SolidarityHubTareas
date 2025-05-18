package solidarityhub.frontend.views.task.suggested;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.NeedService;
import solidarityhub.frontend.service.TaskService;
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.task.TaskComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@PageTitle("Suggested Tasks")
@Route("suggested-tasks")
public class SuggestedTasks extends VerticalLayout {
    private static Translator translator;
    private final TaskService taskService;
    private final NeedService needService;
    private final CatastropheDTO selectedCatastrophe;
    private List<TaskDTO> suggestedTasks;

    @Autowired
    public SuggestedTasks(TaskService taskService, NeedService needService) {
        initializeTranslator();

        this.taskService = taskService;
        this.needService = needService;
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        buildView();
    }

    private void buildView() {
        addClassName("suggested-tasks-view");

        HeaderComponent header = new HeaderComponent(translator.get("suggested_tasks_button"), "tasks");

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        add(header, getSuggestedTasks());
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
                    task.editButton.setEnabled(false);
                    Div clickableTask = clickableTaskComponent(task);
                    suggestedTasksDiv.add(clickableTask);
                    clickableTask.addClassName("suggested-task");
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
            getUI().ifPresent(ui -> ui.navigate("editSuggestedTask"));
        });

        clickableTask.getStyle().set("cursor", "pointer");
        clickableTask.getElement().setAttribute("title", translator.get("click_to_edit"));
        return clickableTask;
    }
}