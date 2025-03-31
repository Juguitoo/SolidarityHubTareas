package solidarityhub.backend.model;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
}
