package solidarityhub.frontend.views.survey;

import solidarityhub.frontend.dto.SurveyDTO;
import solidarityhub.frontend.i18n.Translator;

public class CreateSurveyDialog extends AbstractSurveyDialog {
    
    public CreateSurveyDialog() {
        super();
        initializeDialog();
    }
    
    private void initializeDialog() {
        this.survey = new SurveyDTO();
        surveyNameField.setValue("");
        questionsLayout.removeAll();
        questionComponents.clear();
        
        // Añadir al menos un campo de pregunta
        addQuestionField();
    }
    
    @Override
    protected String getDialogTitle() {
        return translator.get("create_survey_title");
    }
    
    @Override
    protected void saveSurvey() throws Exception {
        // Inicializar campos adicionales para nueva encuesta
        survey.setNumberOfAnswers(0);
        survey.setAverageQualification(0.0);
        
        // Guardar la encuesta usando el servicio
        surveyService.addSurvey(survey);
    }
}