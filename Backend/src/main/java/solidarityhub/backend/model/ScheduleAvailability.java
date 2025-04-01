package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.enums.DayMoment;
import solidarityhub.backend.model.enums.WeekDay;

@Getter
@Entity
@NoArgsConstructor
public class ScheduleAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "volunteer_dni")
    @Setter
    private Volunteer volunteer;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayMoment dayMoment;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeekDay weekDay;

    public ScheduleAvailability(DayMoment dayMoment, WeekDay weekDay) {
        this.dayMoment = dayMoment;
        this.weekDay = weekDay;
    }
}
