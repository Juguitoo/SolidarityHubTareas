package solidarityhub.frontend.views.survey;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import solidarityhub.frontend.dto.SurveyDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.SurveyService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractSurveyDialog extends Dialog {
    
    protected final Translator translator;
    protected final SurveyService surveyService;
    
    protected TextField surveyNameField;
    protected VerticalLayout questionsLayout;
    protected Button addQuestionButton;
    protected List<QuestionComponent> questionComponents = new ArrayList<>();
    protected SurveyDTO survey;
    
    private final List<Consumer<SurveyDTO>> saveListeners = new ArrayList<>();
    
    public AbstractSurveyDialog(Translator translator) {
        this.translator = translator;
        this.surveyService = new SurveyService();
        
        setWidth("650px");
        setHeight("auto");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        
        // Configuración básica del diálogo
        setClassName("survey-dialog");
        
        // Crear componentes base
        createBasicComponents();
        
        // Agregar al diálogo
        add(createDialogContent());
    }
    
    protected void createBasicComponents() {
        // Crear campo para el nombre de la encuesta
        surveyNameField = new TextField(translator.get("survey_name_field"));
        surveyNameField.setWidthFull();
        surveyNameField.setRequired(true);
        surveyNameField.setErrorMessage(translator.get("survey_name_required"));
        
        // Crear layout para las preguntas
        questionsLayout = new VerticalLayout();
        questionsLayout.setPadding(false);
        questionsLayout.setSpacing(true);
        questionsLayout.addClassName("questions-layout");
        
        // Crear botón para añadir preguntas
        addQuestionButton = new Button(translator.get("add_question_button"), new Icon(VaadinIcon.PLUS));
        addQuestionButton.addClickListener(e -> addQuestionField());
        addQuestionButton.addClassName("add-question-button");
    }
    
    protected Component createDialogContent() {
        // Título del diálogo
        H3 title = new H3(getDialogTitle());
        title.addClassName("dialog-title");
        
        // Formulario
        Component form = createForm();
        
        // Botones de acción
        HorizontalLayout buttons = createActionButtons();
        
        // Layout principal
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.add(title, form, buttons);
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        
        return mainLayout;
    }
    
    protected Component createForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.addClassName("survey-form");
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );
        
        formLayout.add(surveyNameField);
        
        H3 questionsTitle = new H3(translator.get("questions_section_title"));
        questionsTitle.addClassName("questions-title");
        
        VerticalLayout questionsSection = new VerticalLayout();
        questionsSection.add(questionsTitle, questionsLayout, addQuestionButton);
        questionsSection.setPadding(false);
        questionsSection.setSpacing(true);
        
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.add(formLayout, questionsSection);
        formContainer.setPadding(false);
        formContainer.setSpacing(true);
        
        return formContainer;
    }
    
    protected HorizontalLayout createActionButtons() {
        Button saveButton = new Button(translator.get("save_button"), e -> saveForm());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClassName("save-button");
        
        Button cancelButton = new Button(translator.get("cancel_button"), e -> close());
        cancelButton.addClassName("cancel-button");
        
        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.addClassName("action-buttons");
        
        return buttons;
    }
    
    protected void addQuestionField() {
        QuestionComponent component = new QuestionComponent("", translator);
        questionComponents.add(component);
        questionsLayout.add(component);
        
        component.addDeleteListener(() -> {
            questionsLayout.remove(component);
            questionComponents.remove(component);
        });
    }
    
    protected void saveForm() {
        if (surveyNameField.isEmpty()) {
            Notification.show(
                    translator.get("survey_name_required_message"),
                    3000,
                    Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        if (questionComponents.isEmpty()) {
            Notification.show(
                    translator.get("questions_required_message"),
                    3000,
                    Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        // Actualizar el objeto Survey
        updateSurveyFromForm();
        
        // Validar que haya al menos una pregunta válida
        Map<Integer, String> questions = createQuestionsMap();
        
        if (questions.isEmpty()) {
            Notification.show(
                    translator.get("valid_questions_required_message"),
                    3000,
                    Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        // Actualizar preguntas y calificaciones
        survey.setQuestions(questions);
        survey.setQualifications(createQualificationsMap(questions));
        
        // Guardar la encuesta
        try {
            saveSurvey();
            
            // Cerrar el diálogo
            close();
            
            // Notificar a los listeners
            notifySaveListeners();
            
            Notification.show(
                    translator.get("survey_saved_success"),
                    3000,
                    Notification.Position.BOTTOM_START
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
        } catch (Exception e) {
            Notification.show(
                    translator.get("error_saving_survey") + e.getMessage(),
                    3000,
                    Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    protected void updateSurveyFromForm() {
        survey.setName(surveyNameField.getValue());
        
        // Asegurar que se establecen las fechas si es una nueva encuesta
        if (survey.getCreatedDate() == null) {
            survey.setCreatedDate(LocalDateTime.now());
        }
        
        survey.setUpdatedAt(LocalDateTime.now());
    }
    
    protected Map<Integer, String> createQuestionsMap() {
        Map<Integer, String> questions = new HashMap<>();
        for (int i = 0; i < questionComponents.size(); i++) {
            String questionText = questionComponents.get(i).getQuestionText();
            if (!questionText.trim().isEmpty()) {
                questions.put(i + 1, questionText);
            }
        }
        return questions;
    }
    
    protected Map<Integer, Double> createQualificationsMap(Map<Integer, String> questions) {
        Map<Integer, Double> qualifications = new HashMap<>();
        for (Integer questionId : questions.keySet()) {
            qualifications.put(questionId, 0.0);
        }
        return qualifications;
    }
    
    protected abstract String getDialogTitle();
    
    protected abstract void saveSurvey() throws Exception;
    
    public Registration addSaveListener(Consumer<SurveyDTO> listener) {
        saveListeners.add(listener);
        return () -> saveListeners.remove(listener);
    }
    
    protected void notifySaveListeners() {
        for (Consumer<SurveyDTO> listener : saveListeners) {
            listener.accept(survey);
        }
    }
    
    // Clase interna para representar un componente de pregunta
    protected static class QuestionComponent extends HorizontalLayout {
        private final TextField questionField;
        private final List<Runnable> deleteListeners = new ArrayList<>();
        
        public QuestionComponent(String initialValue, Translator translator) {
            setPadding(false);
            setMargin(false);
            setWidthFull();
            addClassName("question-component");
            
            questionField = new TextField();
            questionField.setValue(initialValue);
            questionField.setPlaceholder(translator.get("question_field_placeholder"));
            questionField.setWidthFull();
            questionField.addClassName("question-field");

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> deleteListeners.forEach(Runnable::run));
            deleteButton.addClassName("delete-button");
            
            add(questionField, deleteButton);
            setFlexGrow(1, questionField);
        }
        
        public String getQuestionText() {
            return questionField.getValue();
        }
        
        public void addDeleteListener(Runnable listener) {
            deleteListeners.add(listener);
        }
    }
}