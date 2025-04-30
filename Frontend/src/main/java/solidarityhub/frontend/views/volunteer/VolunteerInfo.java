package solidarityhub.frontend.views.volunteer;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.frontend.dto.VolunteerDTO;

import java.util.List;
import java.util.stream.Collectors;

public class VolunteerInfo extends HorizontalLayout {
    private final VolunteerDTO volunteer;
    private final String activeTab;

    public VolunteerInfo(VolunteerDTO volunteer, String activeTab) {
        this.volunteer = volunteer;
        this.activeTab = activeTab;

        setPadding(false);
        setSpacing(false);
        setMargin(false);
        addClassName("listBox__item");

        buildUI();
    }

    private void buildUI() {
        VerticalLayout volunteerInfo = getVolunteerInfoLayout();
        volunteerInfo.setPadding(false);
        add(volunteerInfo);

        HorizontalLayout skillsContainer = new HorizontalLayout();

        if (activeTab.equals("Habilidades")) {
            if (volunteer.getTaskTypes() != null && !volunteer.getTaskTypes().isEmpty()) {
                skillsContainer.addClassName("skills-container");
                skillsContainer.setPadding(false);
                skillsContainer.setMargin(false);

                for (TaskType taskType : volunteer.getTaskTypes()) {
                    Span skillSpan = new Span(formatTaskType(taskType));
                    skillSpan.addClassName("listBox__item-detail");
                    skillsContainer.add(skillSpan);
                }
            }
        }else{
            remove(skillsContainer);
        }
        add(skillsContainer);
    }

    private VerticalLayout getVolunteerInfoLayout() {
        VerticalLayout volunteerInfo = new VerticalLayout();
        volunteerInfo.setPadding(false);
        volunteerInfo.setSpacing(false);
        volunteerInfo.setMargin(false);
        volunteerInfo.addClassName("volunteerInfo");

        Span nameSpan = new Span(volunteer.getFirstName() + " " + volunteer.getLastName());
        nameSpan.addClassName("volunteerContent--name");
        Span emailSpan = new Span(volunteer.getEmail());
        emailSpan.addClassName("volunteerContent--email");

        volunteerInfo.add(nameSpan, emailSpan);
        return volunteerInfo;
    }

    private String formatTaskTypeList(List<TaskType> taskTypes) {
        if (taskTypes == null || taskTypes.isEmpty()) {
            return "Ninguna";
        }

        return taskTypes.stream()
                .map(this::formatTaskType)
                .collect(Collectors.joining(", "));
    }


    private String formatTaskType(TaskType taskType) {
        if (taskType == null) {
            return "No especificado";
        }

        return switch (taskType) {
            case MEDICAL -> "Médica";
            case POLICE -> "Policía";
            case FIREFIGHTERS -> "Bomberos";
            case CLEANING -> "Limpieza";
            case FEED -> "Alimentación";
            case PSYCHOLOGICAL -> "Psicológica";
            case BUILDING -> "Construcción";
            case CLOTHING -> "Ropa";
            case REFUGE -> "Refugio";
            case OTHER -> "Otra";
            case SEARCH -> "Búsqueda";
            case LOGISTICS -> "Logística";
            case COMMUNICATION -> "Comunicación";
            case MOBILITY -> "Movilidad";
            case PEOPLEMANAGEMENT -> "Gestión de personas";
            case SAFETY -> "Seguridad";
        };
    }
}