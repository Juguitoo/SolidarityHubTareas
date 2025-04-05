package solidarityhub.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender sender;

    //private final String host = "smtp.gmail.com";

    @Value("${spring.mail.username}")
    private String user;

    @Value("${spring.mail.password}")
    private String password;

    public boolean notifyEmail(String receiver, String subject, String body) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(user);
            message.setTo(receiver);
            message.setSubject(subject);
            message.setText(body);

            sender.send(message);
            return true;
        } catch (Exception e) {
            System.out.println("NotificationService: Error al enviar el correo: " + e.getMessage());
            return false;
        }
    }
}
