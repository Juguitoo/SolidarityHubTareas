package solidarityhub.frontend.views.volunteer;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.pingu.domain.enums.TaskType;
import solidarityhub.frontend.dto.VolunteerDTO;
import solidarityhub.frontend.service.FormatService;

import java.time.LocalDateTime;

public class VolunteerInfo extends HorizontalLayout {
    private final VolunteerDTO volunteer;
    private final String activeTab;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final boolean checkAvailability;
    private final FormatService formatService;

    public VolunteerInfo(VolunteerDTO volunteer, String activeTab) {
        this(volunteer, activeTab, null, null, false);
    }

    public VolunteerInfo(VolunteerDTO volunteer, String activeTab,
                         LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this(volunteer, activeTab, startDateTime, endDateTime, true);
    }

    private VolunteerInfo(VolunteerDTO volunteer, String activeTab,
                          LocalDateTime startDateTime, LocalDateTime endDateTime,
                          boolean checkAvailability) {
        this.formatService = FormatService.getInstance();
        this.volunteer = volunteer;
        this.activeTab = activeTab;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.checkAvailability = checkAvailability;

        setPadding(false);
        setSpacing(false);
        setMargin(false);
        addClassName("listBox__item");

        if (checkAvailability) {
            // Set background color based on availability
            boolean isAvailable = volunteer.isAvailable() > 0;
            if (isAvailable) {
                addClassName("availability-highlight-available");
            } else {
                addClassName("availability-highlight-unavailable");
            }
        }

        buildUI();
    }

    private void buildUI() {
        // Layout para toda la información del voluntario
        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setPadding(false);
        infoLayout.setSpacing(false);
        infoLayout.setMargin(false);

        // Nombre y email
        Span nameSpan = new Span(volunteer.getFirstName() + " " + volunteer.getLastName());
        nameSpan.addClassName("volunteerContent--name");

        Span emailSpan = new Span(volunteer.getEmail());
        emailSpan.addClassName("volunteerContent--email");

        infoLayout.add(nameSpan, emailSpan);

        // Añadir las habilidades debajo del email
        if (volunteer.getTaskTypes() != null && !volunteer.getTaskTypes().isEmpty()) {
            HorizontalLayout skillsLayout = new HorizontalLayout();
            skillsLayout.setPadding(false);
            skillsLayout.setSpacing(true);
            skillsLayout.setMargin(false);

            for (TaskType taskType : volunteer.getTaskTypes()) {
                Span skillSpan = new Span(formatService.formatTaskType(taskType));
                skillSpan.addClassName("listBox__item-detail");
                skillsLayout.add(skillSpan);
            }

            infoLayout.add(skillsLayout);
        }

        // Layout para la disponibilidad (a la derecha)
        if (checkAvailability) {
            boolean isAvailable = volunteer.isAvailable() > 0;
            Span availabilitySpan = new Span(isAvailable ? "Disponible" : "No disponible");
            availabilitySpan.addClassName(isAvailable ? "volunteer-available" : "volunteer-unavailable");

            // Añadir los dos componentes principales al layout
            add(infoLayout, availabilitySpan);

            // Hacer que el layout de info ocupe el espacio disponible
            setFlexGrow(1, infoLayout);
        } else {
            add(infoLayout);
        }
    }
}