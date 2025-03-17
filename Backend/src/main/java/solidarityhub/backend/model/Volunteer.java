package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Volunteer extends Person {

    @ManyToMany
    @JoinTable(name = "volunteer_skills",
            joinColumns = @JoinColumn(name = "volunteer_dni"),
            inverseJoinColumns = @JoinColumn(name = "skill_name"))
    private Set<Skill> skills;

    @OneToMany(mappedBy = "volunteer", cascade = CascadeType.ALL)
    private Set<ScheduleAvailability> scheduleAvailabilities;

    @ManyToMany
    @JoinTable(name = "volunteer_preferences",
            joinColumns = @JoinColumn(name = "volunteer_dni"),
            inverseJoinColumns = @JoinColumn(name = "preference_name"))
    private Set<Preference> preferences;

    @ManyToMany
    @JoinTable(name = "volunteer_tasks",
            joinColumns = @JoinColumn(name = "volunteer_dni"),
            inverseJoinColumns = @JoinColumn(name = "task_id"))
    private Set<Task> tasks;

    @OneToMany(mappedBy = "volunteer", cascade = CascadeType.ALL)
    private Set<Donation> donations;

    @OneToMany(mappedBy = "volunteer", cascade = CascadeType.ALL)
    private Set<Certificate> certificates;

    @OneToMany(mappedBy = "volunteer")
    private Set<Notification> notifications;

    public Volunteer(String dNI, String firstName, String lastName, String email,
                     int phone, String address, String password, Set<Skill> skills,
                     Set<ScheduleAvailability> scheduleAvailabilities, Set<Preference> preferences) {
        super(dNI, firstName, lastName, email, phone, address, password);
        this.tasks = new HashSet<>();
        this.donations = new HashSet<>();
        this.certificates = new HashSet<>();
        this.skills = skills;
        this.scheduleAvailabilities = scheduleAvailabilities;
        this.preferences = preferences;
        for (ScheduleAvailability s : this.scheduleAvailabilities) {
            s.setVolunteer(this);
        }
    }
}
