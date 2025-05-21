package solidarityhub.frontend.views.survey;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import solidarityhub.frontend.dto.SurveyDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.SurveyService;
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.MainLayout;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@PageTitle("Encuestas")
@Route(value = "surveys", layout = MainLayout.class)
public class SurveyView extends VerticalLayout {
    
    private Translator translator;
    
    private final SurveyService surveyService;
    private Grid<SurveyDTO> grid;
    
    @Autowired
    public SurveyView(SurveyService surveyService) {
        this.surveyService = surveyService;
        
        // Configuración del idioma
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        this.translator = new Translator(UI.getCurrent().getLocale());
        
        // Inicializar el grid primero
        grid = new Grid<>(SurveyDTO.class, false);
        grid.addClassName("surveys-grid");
        grid.setSizeFull();
        grid.setMinHeight("300px");
        grid.setHeight("100%");
        
        // Configurar el grid
        configureGrid();
        
        // Construir la vista
        buildView();
        
        // Cargar datos
        loadSurveys();
    }
    
    private void buildView() {
        removeAll();
        
        addClassName("surveys-container");
        setSizeFull();

        // Cabecera
        HeaderComponent header = new HeaderComponent(translator.get("survey_view_title"));

        Component actionButtons = getActionButtons();
        Component surveysGrid = getSurveysGrid();

        // Establecer crecimiento flexible para el grid
        setFlexGrow(1, surveysGrid);

        // Añadir componentes
        add(
            header,
            actionButtons,
            surveysGrid
        );
    }
    
    private Component getActionButtons() {
        HorizontalLayout actionButtonsLayout = new HorizontalLayout();
        actionButtonsLayout.addClassName("action-buttons__layout");
        
        Button createSurveyButton = new Button(translator.get("create_survey_button"), new Icon(VaadinIcon.PLUS));
        createSurveyButton.addClickListener(e -> openCreateSurveyDialog());
        createSurveyButton.addClassName("surveys-button");
        
        actionButtonsLayout.setAlignItems(Alignment.CENTER);
        actionButtonsLayout.add(createSurveyButton);
        
        return actionButtonsLayout;
    }
    
    private Component getSurveysGrid() {
        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.addClassName("surveys-grid-layout");
        gridLayout.setSizeFull();
        gridLayout.setFlexGrow(1, grid);
        
        gridLayout.setHeight("70vh");
        gridLayout.add(grid);
        
        return gridLayout;
    }
    
    private void configureGrid() {
        // Configurar columnas
        grid.addColumn(SurveyDTO::getName)
            .setHeader(translator.get("survey_title"))
            .setFlexGrow(2)
            .setSortable(true);
            
        grid.addColumn(survey -> {
            // Aquí habría que obtener el creador desde el backend
            return "Admin"; // Valor temporal
        }).setHeader(translator.get("survey_creator")).setFlexGrow(1);
        
        grid.addColumn(survey -> {
            if (survey.getCreatedDate() != null) {
                return survey.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            return "";
        }).setHeader(translator.get("survey_creation_date")).setFlexGrow(1);
        
        grid.addColumn(survey -> {
            if (survey.getQuestions() != null) {
                return survey.getQuestions().size();
            }
            return 0;
        }).setHeader(translator.get("survey_questions_count")).setFlexGrow(1);
        
        grid.addColumn(SurveyDTO::getAverageQualification)
            .setHeader(translator.get("survey_average_rating"))
            .setFlexGrow(1);
            
        // Agregar columna de acciones
        grid.addComponentColumn(survey -> {
            HorizontalLayout buttonsLayout = new HorizontalLayout();
            buttonsLayout.setSpacing(true);
            
            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addClassName("edit-button");
            editButton.getElement().setAttribute("title", translator.get("edit_survey"));
            editButton.addClickListener(e -> editSurvey(survey));
            
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addClassName("delete-button");
            deleteButton.getElement().setAttribute("title", translator.get("delete_survey"));
            deleteButton.addClickListener(e -> deleteSurvey(survey));
            
            buttonsLayout.add(editButton, deleteButton);
            return buttonsLayout;
        }).setHeader(translator.get("actions")).setFlexGrow(1);
    }
    
    private void loadSurveys() {
        try {
            List<SurveyDTO> surveys = surveyService.getSurveys();
            
            if (surveys.isEmpty()) {
                // Mostrar mensaje de "No hay encuestas" pero manteniendo el grid visible con altura
                grid.setItems(Collections.emptyList());
                
                // Opcional: crear un componente para mostrar mensaje de vacío
                Component emptyState = createEmptyState();
                grid.setHeight("400px"); // Mantener altura aunque esté vacío
                
            } else {
                grid.setItems(surveys);
            }
        } catch (Exception e) {
            Notification.show(
                translator.get("error_loading_surveys") + e.getMessage(),
                3000,
                Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    // "Métod_o opcional para crear un componente visual de "no hay datos"
    private Component createEmptyState() {
        VerticalLayout emptyStateLayout = new VerticalLayout();
        emptyStateLayout.setAlignItems(Alignment.CENTER);
        emptyStateLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        
        Icon icon = VaadinIcon.CLIPBOARD_TEXT.create();
        icon.setSize("48px");
        icon.setColor("var(--lumo-primary-color)");
        
        Span noSurveysText = new Span(translator.get("no_surveys_found"));
        noSurveysText.getStyle().set("font-size", "16px");
        
        emptyStateLayout.add(icon, noSurveysText);
        return emptyStateLayout;
    }
    
    private void refreshGrid() {
        loadSurveys();
        grid.getDataProvider().refreshAll();
    }
    
    private void openCreateSurveyDialog() {
        CreateSurveyDialog dialog = new CreateSurveyDialog(translator);
        dialog.addSaveListener(survey -> refreshGrid());
        dialog.open();
    }
    
    private void editSurvey(SurveyDTO survey) {
        EditSurveyDialog dialog = new EditSurveyDialog(translator, survey);
        dialog.addSaveListener(s -> refreshGrid());
        dialog.open();
    }
    
    private void deleteSurvey(SurveyDTO survey) {
        try {
            surveyService.deleteSurvey(survey.getId());
            refreshGrid();
            Notification.show(
                translator.get("survey_deleted_success"),
                3000,
                Notification.Position.BOTTOM_START
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show(
                translator.get("error_deleting_survey") + e.getMessage(),
                3000,
                Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}