package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
