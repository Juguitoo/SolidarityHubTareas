package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.dto.NeedDTO;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.enums.DayMoment;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.backend.model.enums.WeekDay;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Volunteer extends Person {

    @OneToMany(mappedBy = "volunteer", cascade = CascadeType.ALL)
    private List<ScheduleAvailability> scheduleAvailabilities;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<TaskType> taskTypes;

    @ManyToMany
    @JoinTable(name = "volunteer_tasks",
            joinColumns = @JoinColumn(name = "volunteer_dni"),
            inverseJoinColumns = @JoinColumn(name = "task_id"))
    private List<Task> tasks;

    @OneToMany(mappedBy = "volunteer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Donation> donations;

    @OneToMany(mappedBy = "volunteer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Certificate> certificates;

    @OneToMany(mappedBy = "volunteer", fetch = FetchType.EAGER)
    private List<Notification> notifications;

    @OneToOne(cascade = CascadeType.ALL)
    private GPSCoordinates location;

    public Volunteer(String dni, String firstName, String lastName, String email,
                     int phone, String address, String password, List<TaskType> taskTypes,
                     List<ScheduleAvailability> scheduleAvailabilities) {
        super(dni, firstName, lastName, email, phone, address, password);
        this.tasks = new ArrayList<>();
        this.donations = new ArrayList<>();
        this.certificates = new ArrayList<>();
        this.scheduleAvailabilities = scheduleAvailabilities;
        this.taskTypes = taskTypes;
        for (ScheduleAvailability s : this.scheduleAvailabilities) {
            s.setVolunteer(this);
        }
    }

    public int isAvailable(LocalDateTime startTimeTask, LocalDateTime endTimeTask) {
        int available = 0;
        WeekDay startWeekDay = WeekDay.valueOf(String.valueOf(startTimeTask.getDayOfWeek()));
        WeekDay endWeekDay = WeekDay.valueOf(String.valueOf(endTimeTask.getDayOfWeek()));
        for (ScheduleAvailability schedule : this.scheduleAvailabilities) {
            if (ScheduleAvailability.weekDayToInt(schedule.getWeekDay()) >= ScheduleAvailability.weekDayToInt(startWeekDay) &&
            ScheduleAvailability.weekDayToInt(schedule.getWeekDay()) <= ScheduleAvailability.weekDayToInt(endWeekDay)) {
                if (schedule.getDayMoment() == DayMoment.MORNING) {
                    if (startTimeTask.getHour() >= 8 && endTimeTask.getHour() <= 12) {
                        available++;
                    }
                } else if (schedule.getDayMoment() == DayMoment.AFTERNOON) {
                    if (startTimeTask.getHour() >= 12 && endTimeTask.getHour() <= 18) {
                        available++;
                    }
                } else
                    available++;
                }

            }
            return available;
        }

    public double getDistance(TaskDTO taskDTO) {
        if (taskDTO.getNeeds().isEmpty()) {return 0.0;}
        double totalLatitude = 0.0;
        double totalLongitude = 0.0;
        for (NeedDTO need : taskDTO.getNeeds()) {
            totalLongitude += need.getLocation().getLongitude();
            totalLatitude += need.getLocation().getLatitude();
        }
        double averageLatitude = totalLatitude / taskDTO.getNeeds().size();
        double averageLongitude = totalLongitude / taskDTO.getNeeds().size();

        return Math.sqrt(Math.pow(this.getLocation().getLatitude() - averageLatitude, 2) +
                Math.pow(this.getLocation().getLongitude() - averageLongitude, 2));
    }
}
