package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Volunteer extends Person {

    @ManyToMany
    @JoinTable(name = "volunteer_skills",
            joinColumns = @JoinColumn(name = "volunteer_dni"),
            inverseJoinColumns = @JoinColumn(name = "skill_name"))
    private List<Skill> skills;

    @OneToMany(mappedBy = "volunteer", cascade = CascadeType.ALL)
    private List<ScheduleAvailability> scheduleAvailabilities;

    @ManyToMany
    @JoinTable(name = "volunteer_preferences",
            joinColumns = @JoinColumn(name = "volunteer_dni"),
            inverseJoinColumns = @JoinColumn(name = "preference_name"))
    private List<Preference> preferences;

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

    public Volunteer(String dni, String firstName, String lastName, String email,
                     int phone, String address, String password, List<Skill> skills,
                     List<ScheduleAvailability> scheduleAvailabilities, List<Preference> preferences) {
        super(dni, firstName, lastName, email, phone, address, password);
        this.tasks = new ArrayList<>();
        this.donations = new ArrayList<>();
        this.certificates = new ArrayList<>();
        this.skills = skills;
        this.scheduleAvailabilities = scheduleAvailabilities;
        this.preferences = preferences;
        for (ScheduleAvailability s : this.scheduleAvailabilities) {
            s.setVolunteer(this);
        }
    }
}
