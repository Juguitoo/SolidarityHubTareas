package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.Notification;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.Volunteer;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class NotificationDTO {
    private int id;
    private String title;
    private String body;
    private LocalDateTime creationDateTime;
    private boolean seen;
    private Integer taskId;
    private String volunteerDni;

    /**
     * Constructs a DTO from a Notification entity
     */
    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.body = notification.getBody();
        this.creationDateTime = notification.getCreationDateTime();
        this.seen = notification.isSeen();

        if (notification.getTask() != null) {
            this.taskId = notification.getTask().getId();
        }

        if (notification.getVolunteer() != null) {
            this.volunteerDni = notification.getVolunteer().getDni();
        }
    }

    /**
     * Constructs a DTO with all fields
     */
    public NotificationDTO(int id, String title, String body, LocalDateTime creationDateTime,
                           boolean seen, Integer taskId, String volunteerDni) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.creationDateTime = creationDateTime;
        this.seen = seen;
        this.taskId = taskId;
        this.volunteerDni = volunteerDni;
    }

    /**
     * Converts this DTO to a Notification entity
     * Note: Requires Task and Volunteer entities to be loaded separately
     */
    public Notification toEntity(Task task, Volunteer volunteer) {
        Notification notification = new Notification(title, body, task, volunteer);
        notification.setSeen(seen);
        return notification;
    }

    /**
     * Updates an existing Notification entity with the data from this DTO
     */
    public void updateEntity(Notification notification, Task task, Volunteer volunteer) {
        notification.setTitle(this.title);
        notification.setBody(this.body);
        notification.setSeen(this.seen);
        notification.setTask(task);
        notification.setVolunteer(volunteer);
    }
}
