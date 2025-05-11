package solidarityhub.frontend.views.task;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.model.enums.Priority;
import solidarityhub.frontend.service.TaskService;
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.catastrophe.CatastropheSelectionView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@PageTitle("Ver más tareas")
@Route("moretasks")
public class MoreTasks extends VerticalLayout implements BeforeEnterObserver {
    private static Translator translator;

    private final TaskService taskService;
    private CatastropheDTO selectedCatastrophe;
    private ListDataProvider<TaskDTO> tasksDataProvider;
    private final Grid<TaskDTO> taskGrid;

    public MoreTasks() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        }else{
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());

        this.taskService = new TaskService();

        this.taskGrid = new Grid<>(TaskDTO.class, false);
        taskGrid.addClassName("moreTasks_grid");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");

        if (selectedCatastrophe == null) {
            Notification.show(translator.get("select_catastrophe_warning"),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            event.forwardTo(CatastropheSelectionView.class);
            return;
        }

        buildView();
    }

    private void buildView() {
        removeAll();
        addClassName("moreTasks_Container");

        //Header
        HeaderComponent header = new HeaderComponent(
            translator.get("task_view_title") + selectedCatastrophe.getName(), "tasks"
        );


        add(header, getFilters(), taskGrid);
        populateTaskGrid();
    }

    //===============================Get Grid Items=========================================
    private List<TaskDTO> getTasksList() {
        if (selectedCatastrophe != null) {
            return taskService.getTasksByCatastrophe(selectedCatastrophe.getId());
        } else {
            return Collections.emptyList();
        }
    }

    private void populateTaskGrid() {
        this.tasksDataProvider = new ListDataProvider<>(getTasksList());

        if (tasksDataProvider.getItems().isEmpty()) {
            taskGrid.setVisible(false);
            add(new Span(translator.get("no_tasks")));
        } else {
            taskGrid.setVisible(true);
            taskGrid.setDataProvider(tasksDataProvider);
            taskGrid.addColumn(TaskDTO::getName).setHeader(translator.get("more_tasks_name"));
            taskGrid.addColumn(task -> task.getDescription().length() > 50 ? task.getDescription().substring(0, 50) + "..." : task.getDescription()).setHeader(translator.get("more_tasks_description"));
            taskGrid.addColumn(TaskDTO::getPriority).setHeader(translator.get("more_tasks_priority"));
            taskGrid.addColumn(task -> formatDate(task.getStartTimeDate())).setHeader(translator.get("more_tasks_start_date"));
            taskGrid.addColumn(task -> formatDate(task.getEstimatedEndTimeDate())).setHeader(translator.get("more_tasks_end_date"));
            taskGrid.addColumn(task -> task.getNeeds().size()).setHeader(translator.get("more_tasks_needs"));
            taskGrid.addColumn(task -> task.getVolunteers().size()).setHeader(translator.get("more_tasks_volunteers"));
        }

        taskGrid.addItemClickListener(event -> {
            if (event.getClickCount() == 2) {
                TaskDTO selectedTask = event.getItem();
                UI.getCurrent().navigate("editTask", QueryParameters.simple(
                        Collections.singletonMap("id", String.valueOf(selectedTask.getId()))));
            }
        });
    }

    //===============================Get Components=========================================
    private Component getFilters() {
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setAlignItems(Alignment.BASELINE);
        filterLayout.setSpacing(true);

        TextField searchField = getSearchField();
        Button searchButton = getSearchButton(searchField);

        filterLayout.add(getPriorityFilter());

        return filterLayout;
    }

    private MultiSelectComboBox<Priority> getPriorityFilter() {
        MultiSelectComboBox<Priority> filter = new MultiSelectComboBox<>(translator.get("more_tasks_priority_filter"));
        filter.setItems(Priority.LOW, Priority.MODERATE, Priority.URGENT);
        filter.setItemLabelGenerator(priority -> switch (priority) {
            case LOW -> translator.get("low_priority");
            case MODERATE -> translator.get("moderate_priority");
            case URGENT -> translator.get("urgent_priority");
        });
        filter.addValueChangeListener(event -> applyPriorityFilter(event.getValue()));
        return filter;
    }

    private TextField getSearchField() {
        TextField searchField = new TextField(translator.get("more_tasks_search_task"));
        searchField.addKeyPressListener(Key.ENTER, e -> applySearchFilter(searchField.getValue()));
        return searchField;
    }

    private Button getSearchButton(TextField searchField) {
        Button searchButton = new Button(translator.get("more_tasks_search_task_button"), e -> applySearchFilter(searchField.getValue()));
        searchButton.addClassName("search-button"); // Agregar la clase CSS
        return searchButton;
    }

    //===============================Manage Filters=========================================
    private void applySearchFilter(String searchTerm) {
        String term = searchTerm.trim().toLowerCase();
        tasksDataProvider.clearFilters();
        if (!term.isEmpty()) {
            tasksDataProvider.addFilter(task -> task.getName().toLowerCase().contains(term));
        }
        tasksDataProvider.refreshAll();
    }

    private void applyPriorityFilter(Set<Priority> selectedPriorities) {
        tasksDataProvider.clearFilters();
        if (!selectedPriorities.isEmpty()) {
            tasksDataProvider.addFilter(task -> selectedPriorities.contains(task.getPriority()));
        }
        tasksDataProvider.refreshAll();
    }


    private String formatDate(LocalDateTime taskDate) {
        if (taskDate == null) {
            return translator.get("more_tasks_no_available");
        }
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}
