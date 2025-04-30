package solidarityhub.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import solidarityhub.backend.config.FcmService;
import solidarityhub.backend.model.Notification;
import solidarityhub.backend.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final FcmService fcmService;

    private final JavaMailSender sender;

    @Value("${spring.mail.username}")
    private String user;

    @Value("${spring.mail.password}")
    private String password;

    @Autowired
    public NotificationService(JavaMailSender sender, FcmService fcmService, NotificationRepository notificationRepository) {
        this.sender = sender;
        this.fcmService = fcmService;
        this.notificationRepository = notificationRepository;
    }

    @Async
    public void notifyEmail(String receiver, Notification notification) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(user);
            message.setTo(receiver);
            message.setSubject(notification.getTitle());
            message.setText(notification.getBody());

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
            System.out.println("NotificationService: Error al notificar a la app: " + e.getMessage());
        }
    }

    @Async
    public void save(Notification notification) {
        try {;
            notificationRepository.save(notification);
        } catch (Exception e) {
            System.out.println("NotificationService: Error al guardar la notificación: " + e.getMessage());
        }
    }
}
