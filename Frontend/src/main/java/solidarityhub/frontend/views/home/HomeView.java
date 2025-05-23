package solidarityhub.frontend.views.home;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import org.pingu.domain.enums.EmergencyLevel;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.service.FormatService;
import solidarityhub.frontend.service.NeedService;
import solidarityhub.frontend.service.TaskService;
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.catastrophe.EditCatastropheDialog;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@PageTitle("Inicio")
@Route("home")
public class HomeView extends VerticalLayout implements BeforeEnterObserver {

    private final CatastropheService catastropheService;
    private final TaskService taskService;
    private final NeedService needService;
    private final FormatService formatService;
    private CatastropheDTO selectedCatastrophe;
    protected static Translator translator;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public HomeView() {
        this.catastropheService = new CatastropheService();
        this.taskService = new TaskService();
        this.needService = new NeedService();
        this.formatService = FormatService.getInstance();

        initializeTranslator();
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

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
        if (!catastropheService.isCatastropheSelected(event, selectedCatastrophe)) {
            return;
        }

        buildView();
    }

    private void buildView(){
        removeAll();
        setWidthFull();
        addClassName("home-view");

        HeaderComponent header = new HeaderComponent(selectedCatastrophe.getName());

        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.addClassName("home-view-content");
        mainContent.setWidthFull();
        mainContent.setAlignItems(Alignment.START);

        // Columna izquierda
        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.addClassName("column");
        leftColumn.setPadding(false);

        leftColumn.add(
            getAdminCard(),
            getRecentTasksCard(),
            getNeedsOverviewCard()
        );

        // Columna derecha
        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.addClassName("column");
        rightColumn.setPadding(false);

        rightColumn.add(
                getCatastropheCard(),
                getQuickStatsCard(),
                new ResourceSummaryView(selectedCatastrophe)
        );

        mainContent.add(leftColumn, rightColumn);

        add(header, mainContent);
    }

    //===============================Get Components=========================================
    //Admin Card
    private Component getAdminCard(){
        VerticalLayout adminCard = new VerticalLayout();
        adminCard.addClassName("admin-card");

        adminCard.add(getWelcomeAdmin(), getAdminPreferences());

        return adminCard;
    }

    private Component getWelcomeAdmin() {
        HorizontalLayout welcomeCard = new HorizontalLayout();

        HorizontalLayout welcomeHeader = new HorizontalLayout();
        welcomeHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        welcomeHeader.setWidthFull();

        Image adminIcon = new Image("images/profile.png", "Admin Icon");
        adminIcon.addClassName("admin-icon-large");

        VerticalLayout welcomeText = new VerticalLayout();
        welcomeText.setPadding(false);
        welcomeText.setSpacing(false);

        H2 welcomeTitle = new H2(translator.get("welcome_back"));
        Span adminName = new Span("Perro Sanchez");
        adminName.addClassName("admin-name-large");
        Span adminRole = new Span("Presidente del Gobierno");
        adminRole.addClassName("admin-role");

        welcomeText.add(welcomeTitle, adminName, adminRole);

//        HorizontalLayout statusLayout = new HorizontalLayout();
//        statusLayout.setAlignItems(FlexComponent.Alignment.CENTER);
//        statusLayout.setSpacing(true);
//
//        Span statusIndicator = new Span("●");
//        statusIndicator.addClassName("status-online");
//        Span statusText = new Span(translator.get("online_now"));
//        statusText.addClassName("status-text");
//
//        statusLayout.add(statusIndicator, statusText);

        welcomeHeader.add(adminIcon, welcomeText);
        welcomeCard.add(welcomeHeader);

        return welcomeCard;
    }

    private Component getAdminPreferences() {
        VerticalLayout preferencesCard = new VerticalLayout();
        preferencesCard.addClassName("preferences-card");

        H3 preferencesTitle = new H3(translator.get("home_preferences"));
        preferencesTitle.addClassName("card-title");

        HorizontalLayout preferencesContent = new HorizontalLayout();
        preferencesContent.setWidthFull();
        preferencesContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        preferencesContent.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout themeSection = new HorizontalLayout();
        themeSection.setAlignItems(FlexComponent.Alignment.CENTER);

        Span themeLabel = new Span(translator.get("theme"));
        themeLabel.addClassName("preference-label");

        themeSection.add(themeLabel, getThemeBtn());

        HorizontalLayout languageSection = new HorizontalLayout();
        languageSection.setAlignItems(FlexComponent.Alignment.CENTER);

        Span languageLabel = new Span(translator.get("language"));
        languageLabel.addClassName("preference-label");

        ComboBox<String> languageSelector = getLanguageSelector();

        languageSection.add(languageLabel, languageSelector);

        preferencesContent.add(themeSection, languageSection);
        preferencesCard.add(preferencesTitle, preferencesContent);

        return preferencesCard;
    }

    //Catastrophe Card
    private Component getCatastropheCard() {
        VerticalLayout catastropheCard = new VerticalLayout();
        catastropheCard.addClassName("catastrophe-info-card");
        catastropheCard.addClassName(getEmergencyLevelClass(selectedCatastrophe.getEmergencyLevel()));

        HorizontalLayout catastropheHeader = new HorizontalLayout();
        catastropheHeader.setWidthFull();
        catastropheHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        catastropheHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H3 catastropheName = new H3(selectedCatastrophe.getName());
        catastropheName.addClassName("catastrophe-name");

        Button editButton = new Button(translator.get("edit_catastrophe"), VaadinIcon.EDIT.create());
        editButton.addClassName("edit-catastrophe-btn");
        editButton.addClickListener(e -> {
            Dialog editCatastropheDialog = new EditCatastropheDialog(selectedCatastrophe);
            editCatastropheDialog.open();
        });

        catastropheHeader.add(catastropheName, editButton);

        VerticalLayout catastropheInfo = new VerticalLayout();
        catastropheInfo.setPadding(false);

        Div catastropheDescription = new Div();
        catastropheDescription.setText(selectedCatastrophe.getDescription());
        catastropheDescription.addClassName("catastrophe-description");

        HorizontalLayout catastropheData = new HorizontalLayout();
        catastropheData.addClassName("catastrophe-data");
        catastropheData.setWidthFull();
        catastropheData.setAlignItems(FlexComponent.Alignment.CENTER);

        Span emergencyInfo = new Span(translator.get("emergency_level") + formatService.formatEmergencyLevel(selectedCatastrophe.getEmergencyLevel()));
        emergencyInfo.addClassName("catastrophe-data__label");

        Span dateInfo = new Span(translator.get("start_date") + selectedCatastrophe.getStartDate().format(DATE_FORMATTER));
        dateInfo.addClassName("catastrophe-data__label");

        catastropheData.add(emergencyInfo, dateInfo);

        catastropheInfo.add(catastropheDescription, catastropheData);
        catastropheCard.add(catastropheHeader, catastropheInfo);

        return catastropheCard;
    }

    //QuickStats Card
    private Component getQuickStatsCard() {
        VerticalLayout statsCard = new VerticalLayout();
        statsCard.addClassName("quick-stats-card");
        statsCard.setPadding(true);
        statsCard.setSpacing(true);

        H3 statsTitle = new H3(translator.get("quick_overview"));
        statsTitle.addClassName("card-title");

        HorizontalLayout statsContent = new HorizontalLayout();
        statsContent.setWidthFull();
        statsContent.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);

        try {
            // Estadísticas de tareas
            int todoTasks = taskService.getToDoTasksCount(selectedCatastrophe.getId());
            long inProgressTasks = taskService.getInProgressTasksCount(selectedCatastrophe.getId());
            long finishedTasks = taskService.getFinishedTasksCount(selectedCatastrophe.getId());

            // Estadísticas de necesidades
            int pendingNeeds = needService.getNeedsWithoutTaskCount(selectedCatastrophe.getId());

            statsContent.add(
                    createStatItem(String.valueOf(todoTasks), translator.get("pending_tasks"), "stat-todo"),
                    createStatItem(String.valueOf(inProgressTasks), translator.get("active_tasks"), "stat-progress"),
                    createStatItem(String.valueOf(finishedTasks), translator.get("completed_tasks"), "stat-completed"),
                    createStatItem(String.valueOf(pendingNeeds), translator.get("pending_needs"), "stat-needs")
            );
        } catch (Exception e) {
            Span errorMessage = new Span(translator.get("error_loading_stats"));
            errorMessage.addClassName("error-text");
            statsContent.add(errorMessage);
        }

        statsCard.add(statsTitle, statsContent);
        return statsCard;
    }

    private Component createStatItem(String value, String label, String className) {
        VerticalLayout statItem = new VerticalLayout();
        statItem.addClassName("stat-item");
        statItem.addClassName(className);
        statItem.setPadding(false);
        statItem.setSpacing(false);
        statItem.setAlignItems(FlexComponent.Alignment.CENTER);

        Span statValue = new Span(value);
        statValue.addClassName("stat-value");

        Span statLabel = new Span(label);
        statLabel.addClassName("stat-label");

        statItem.add(statValue, statLabel);
        return statItem;
    }

    //Tasks Card
    private Component getRecentTasksCard() {
        VerticalLayout tasksCard = new VerticalLayout();
        tasksCard.addClassName("recent-tasks-card");
        tasksCard.setPadding(true);
        tasksCard.setSpacing(true);

        HorizontalLayout tasksHeader = new HorizontalLayout();
        tasksHeader.setWidthFull();
        tasksHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        tasksHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        H3 tasksTitle = new H3(translator.get("recent_tasks"));
        tasksTitle.addClassName("card-title");

        Button viewAllTasks = new Button(translator.get("view_all"), VaadinIcon.ARROW_RIGHT.create());
        viewAllTasks.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewAllTasks.addClickListener(e -> UI.getCurrent().navigate("tasks/moretasks"));

        tasksHeader.add(tasksTitle, viewAllTasks);

        // Obtener tareas recientes
        VerticalLayout tasksContent = new VerticalLayout();
        tasksContent.setPadding(false);
        tasksContent.setSpacing(true);

        try {
            List<TaskDTO> recentTasks = taskService.getTasksByCatastrophe(selectedCatastrophe.getId())
                    .stream()
                    .sorted((t1, t2) -> t2.getStartTimeDate().compareTo(t1.getStartTimeDate()))
                    .limit(3)
                    .toList();

            if (recentTasks.isEmpty()) {
                Span noTasks = new Span(translator.get("no_recent_tasks"));
                noTasks.addClassName("empty-state-text");
                tasksContent.add(noTasks);
            } else {
                for (TaskDTO task : recentTasks) {
                    tasksContent.add(createTaskSummary(task));
                }
            }
        } catch (Exception e) {
            Span errorMessage = new Span(translator.get("error_loading_recent_tasks"));
            errorMessage.addClassName("error-text");
            tasksContent.add(errorMessage);
        }

        tasksCard.add(tasksHeader, tasksContent);
        return tasksCard;
    }

    private Component createTaskSummary(TaskDTO task) {
        HorizontalLayout taskSummary = new HorizontalLayout();
        taskSummary.addClassName("task-summary");
        taskSummary.setWidthFull();
        taskSummary.setAlignItems(FlexComponent.Alignment.CENTER);
        taskSummary.setPadding(true);
        taskSummary.setSpacing(true);

        // Indicador de estado
        Span statusIndicator = new Span();
        statusIndicator.addClassName("task-status-indicator");
        switch (task.getStatus()) {
            case TO_DO -> statusIndicator.addClassName("status-todo");
            case IN_PROGRESS -> statusIndicator.addClassName("status-in-progress");
            case FINISHED -> statusIndicator.addClassName("status-finished");
        }

        VerticalLayout taskInfo = new VerticalLayout();
        taskInfo.setPadding(false);
        taskInfo.setSpacing(false);
        taskInfo.setFlexGrow(1);

        Span taskName = new Span(task.getName());
        taskName.addClassName("task-name");

        Span taskMeta = new Span(formatService.formatTaskStatus(task.getStatus()));

        taskInfo.add(taskName, taskMeta);

        Icon arrow = VaadinIcon.ANGLE_RIGHT.create();
        arrow.addClassName("task-arrow");

        taskSummary.add(statusIndicator, taskInfo, arrow);

        // Hacer clickeable
        taskSummary.addClickListener(e ->
                UI.getCurrent().navigate("editTask", QueryParameters.simple(
                        java.util.Collections.singletonMap("id", String.valueOf(task.getId()))
                ))
        );

        return taskSummary;
    }

    //Needs Card
    private Component getNeedsOverviewCard() {
        VerticalLayout needsCard = new VerticalLayout();
        needsCard.addClassName("needs-overview-card");
        needsCard.setPadding(true);
        needsCard.setSpacing(true);

        HorizontalLayout needsHeader = new HorizontalLayout();
        needsHeader.setWidthFull();
        needsHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        needsHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        H3 needsTitle = new H3(translator.get("needs_overview"));
        needsTitle.addClassName("card-title");

        Button addTaskButton = new Button(translator.get("create_task"), VaadinIcon.PLUS.create());
        addTaskButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        addTaskButton.addClickListener(e -> UI.getCurrent().navigate("tasks/addtask"));

        needsHeader.add(needsTitle, addTaskButton);

        // Contenido de necesidades
        VerticalLayout needsContent = new VerticalLayout();
        needsContent.setPadding(false);
        needsContent.setSpacing(true);

        try {
            List<NeedDTO> pendingNeeds = needService.getNeedsWithoutTask(selectedCatastrophe.getId());

            if (!pendingNeeds.isEmpty()) {
                // Título con contador
                HorizontalLayout summaryHeader = new HorizontalLayout();
                summaryHeader.setAlignItems(FlexComponent.Alignment.CENTER);
                summaryHeader.addClassName("needs-summary-header");

                Icon warningIcon = VaadinIcon.WARNING.create();
                warningIcon.addClassName("needs-warning-icon");

                Span summaryText = new Span(
                        pendingNeeds.size() + " " + translator.get("needs_without_task")
                );
                summaryText.addClassName("needs-warning-text");

                summaryHeader.add(warningIcon, summaryText);
                needsContent.add(summaryHeader);

                // Lista de necesidades (máximo 5)
                int maxToShow = Math.min(pendingNeeds.size(), 5);
                for (int i = 0; i < maxToShow; i++) {
                    NeedDTO need = pendingNeeds.get(i);

                    // Creamos el item de necesidad directamente aquí
                    HorizontalLayout needItem = new HorizontalLayout();
                    needItem.addClassName("need-item");
                    needItem.setWidthFull();
                    needItem.setAlignItems(FlexComponent.Alignment.CENTER);
                    needItem.setPadding(true);
                    needItem.setSpacing(true);

                    // Indicador de tipo de tarea
                    Span typeIndicator = new Span();
                    typeIndicator.addClassName("need-type-indicator");
                    typeIndicator.setText(formatService.formatTaskType(need.getTaskType()).substring(0, 1).toUpperCase());

                    VerticalLayout needInfo = new VerticalLayout();
                    needInfo.setPadding(false);
                    needInfo.setSpacing(false);
                    needInfo.setFlexGrow(1);

                    Span needDescription = new Span(need.getDescription());
                    needDescription.addClassName("need-description");

                    Span needType = new Span(formatService.formatTaskType(need.getTaskType()));
                    needType.addClassName("need-type-text");

                    // Indicador de urgencia
                    Span urgencyIndicator = new Span(formatService.formatUrgencyLevel(need.getUrgency()));
                    urgencyIndicator.addClassName("need-urgency");
                    switch (need.getUrgency()) {
                        case URGENT -> urgencyIndicator.addClassName("urgency-urgent");
                        case MODERATE -> urgencyIndicator.addClassName("urgency-moderate");
                        case LOW -> urgencyIndicator.addClassName("urgency-low");
                    }

                    needInfo.add(needDescription, needType);
                    needItem.add(typeIndicator, needInfo, urgencyIndicator);

                    needsContent.add(needItem);
                }

                // Mostrar enlace "Ver más" si hay más de 5
                if (pendingNeeds.size() > 5) {
                    Button viewMoreButton = new Button(
                            "+" + (pendingNeeds.size() - 5) + " " + translator.get("more_needs"),
                            VaadinIcon.ARROW_RIGHT.create()
                    );
                    viewMoreButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
                    viewMoreButton.addClickListener(e -> UI.getCurrent().navigate("addtask"));
                    needsContent.add(viewMoreButton);
                }
            } else {
                Span noNeeds = new Span(translator.get("all_needs_assigned"));
                noNeeds.addClassName("success-text");
                needsContent.add(noNeeds);
            }
        } catch (Exception e) {
            Span errorMessage = new Span(translator.get("error_loading_needs"));
            errorMessage.addClassName("error-text");
            needsContent.add(errorMessage);
        }

        needsCard.add(needsHeader, needsContent);
        return needsCard;
    }

    //===============================Helper Methods=========================================
    private Button getThemeBtn() {
        Span themeIcon = new Span();
        themeIcon.setText("🌙");

        Button toggleTheme = new Button(themeIcon);
        toggleTheme.addClassName("theme-toggle-btn");

        toggleTheme.addClickListener(event -> UI.getCurrent().getPage().executeJs("""
                const html = document.documentElement;
                const current = html.getAttribute('data-theme');
                const next = current === 'dark' ? 'light' : 'dark';
                html.setAttribute('data-theme', next);
                localStorage.setItem('theme', next);
                const icon = next === 'dark' ? '🌙' : '🌞';
                $0.textContent = icon;""", themeIcon.getElement()
        ));

        themeIcon.setId("theme-icon");
        return toggleTheme;
    }

    private ComboBox<String> getLanguageSelector() {
        ComboBox<String> languageSelector = new ComboBox<>();
        languageSelector.setAllowCustomValue(false);
        languageSelector.addClassName("language-selector");
        languageSelector.setItems("Español", "Valencià", "English");
        languageSelector.setValue(getCurrentLanguage());

        languageSelector.addValueChangeListener(event -> {
            String selected = event.getValue();
            Locale newLocale = switch (selected) {
                case "English" -> new Locale("en");
                case "Valencià" -> new Locale("va");
                default -> new Locale("es");
            };

            VaadinSession.getCurrent().setAttribute(Locale.class, newLocale);
            UI.getCurrent().setLocale(newLocale);
            UI.getCurrent().getPage().reload();
        });
        return languageSelector;
    }

    private String getCurrentLanguage() {
        Locale current = UI.getCurrent().getLocale();
        return switch (current.getLanguage()) {
            case "en" -> "English";
            case "va" -> "Valencià";
            default -> "Español";
        };
    }

    private String getEmergencyLevelClass(EmergencyLevel level) {
        if (level == null) return "emergency-unknown";

        return switch (level) {
            case LOW -> "emergency-low-card";
            case MEDIUM -> "emergency-medium-card";
            case HIGH -> "emergency-high-card";
            case VERYHIGH -> "emergency-very-high-card";
        };
    }
}
