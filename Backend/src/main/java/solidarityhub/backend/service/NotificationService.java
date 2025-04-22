package solidarityhub.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import solidarityhub.backend.config.FcmService;

@Service
public class NotificationService {

    private final FcmService fcmService;

    private final JavaMailSender sender;

    @Value("${spring.mail.username}")
    private String user;

    @Value("${spring.mail.password}")
    private String password;

    @Autowired
    public NotificationService(JavaMailSender sender, FcmService fcmService) {
        this.sender = sender;
        this.fcmService = fcmService;
    }

    @Async
    public void notifyEmail(String receiver, String subject, String body) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(user);
            message.setTo(receiver);
            message.setSubject(subject);
            message.setText(body);

            sender.send(message);
        } catch (Exception e) {
            System.out.println("NotificationService: Error al enviar el correo: " + e.getMessage());
        }
    }

    @Async
    public void notifyApp(String token, String title, String body) {
        try{
            fcmService.sendNotification(token, title, body);
        } catch (Exception e) {
            System.out.println("NotificationService: Error al enviar el correo: " + e.getMessage());
        }
    }
}
