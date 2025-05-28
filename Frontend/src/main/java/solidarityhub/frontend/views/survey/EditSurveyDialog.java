package solidarityhub.frontend.views.survey;

import solidarityhub.frontend.dto.SurveyDTO;
import solidarityhub.frontend.i18n.Translator;

import java.util.Map;

public class EditSurveyDialog extends AbstractSurveyDialog {
    
    public EditSurveyDialog(SurveyDTO survey) {
        super();
        this.survey = survey;
        initializeDialog();
    }
    
    private void initializeDialog() {
        surveyNameField.setValue(survey.getName());
        
        questionsLayout.removeAll();
        questionComponents.clear();
        
        if (survey.getQuestions() != null && !survey.getQuestions().isEmpty()) {
            // Ordenar las preguntas por su índice (ID)
            survey.getQuestions().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        QuestionComponent component = new QuestionComponent(entry.getValue(), translator);
                        questionComponents.add(component);
                        questionsLayout.add(component);
                        
                        component.addDeleteListener(() -> {
                            questionsLayout.remove(component);
                            questionComponents.remove(component);
                        });
                    });
        } else {
            // Añadir al menos un campo de pregunta vacío
            addQuestionField();
        }
    }
    
    @Override
    protected String getDialogTitle() {
        return translator.get("edit_survey_title");
    }
    
    @Override
    protected void saveSurvey() throws Exception {
        // Aquí no inicializamos numberOfAnswers ni averageQualification
        // ya que queremos mantener los valores que ya tiene la encuesta
        
        // Guardar la encuesta usando el servicio
        surveyService.updateSurvey(survey.getId(), survey);
    }
}