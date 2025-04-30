package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Setter
    private String title;
    @Setter
    private String body;

    private LocalDateTime creationDateTime;

    @Setter
    private boolean seen;

    @Setter
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "task_id")
    private Task task;

    @Setter
    @ManyToOne
    @JoinColumn(name = "volunteer_dni")
    private Volunteer volunteer;

    public Notification(String title, String body, Task task, Volunteer volunteer) {
        this.title = title;
        this.body = body;
        this.task = task;
        this.volunteer = volunteer;
        this.seen = false;
        this.creationDateTime = LocalDateTime.now();
    }
}
