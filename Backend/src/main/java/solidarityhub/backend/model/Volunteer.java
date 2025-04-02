package solidarityhub.backend.model;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.enums.DayMoment;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.backend.model.enums.WeekDay;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

    public void notifyEmail(String subject, String message) {
        // Configuración del servidor de correo
        String host = "smtp.example.com";
        final String user = getEmail();
        final String password = "";

        // Propiedades del correo
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.port", "587");

        // Autenticación
        Session session = Session.getInstance(properties, new jakarta.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try {
            // Crear el mensaje
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(user));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(this.getEmail()));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(message);

            // Enviar el mensaje
            Transport.send(mimeMessage);
            System.out.println("Correo enviado exitosamente");

        } catch (MessagingException e) {
            e.printStackTrace();
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

    public double getDistance(Task task) {
        if (task.getNeeds().isEmpty()) {return 0.0;};
        double totalLatitude = 0.0;
        double totalLongitude = 0.0;
        for (Need need : task.getNeeds()) {
            totalLongitude += need.getLocation().getLongitude();
            totalLatitude += need.getLocation().getLatitude();
        }
        double averageLatitude = totalLatitude / task.getNeeds().size();
        double averageLongitude = totalLongitude / task.getNeeds().size();

        return Math.sqrt(Math.pow(this.getLocation().getLatitude() - averageLatitude, 2) +
                Math.pow(this.getLocation().getLongitude() - averageLongitude, 2));
    }
}
