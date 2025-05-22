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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final FcmService fcmService;

    private final JavaMailSender sender;

    private final Set<String> recentNotifications = ConcurrentHashMap.newKeySet();


    @Value("SolidarityHub <${spring.mail.username}>")
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
        try {
            // CREAR CLAVE ÚNICA PARA EVITAR DUPLICADOS
            String notificationKey = createNotificationKey(notification);

            // VERIFICAR SI YA EXISTE UNA NOTIFICACIÓN SIMILAR RECIENTE
            if (recentNotifications.contains(notificationKey)) {
                System.out.println("⚠️ Notificación duplicada detectada, omitiendo: " + notification.getTitle());
                return;
            }

            // VERIFICAR EN BASE DE DATOS SI YA EXISTE
            if (isDuplicateNotification(notification)) {
                System.out.println("⚠️ Notificación duplicada en BD detectada, omitiendo: " + notification.getTitle());
                return;
            }

            notificationRepository.save(notification);

            // AGREGAR A CACHE TEMPORAL (SE LIMPIA DESPUÉS DE 5 MINUTOS)
            recentNotifications.add(notificationKey);
            CompletableFuture.delayedExecutor(5, TimeUnit.MINUTES).execute(() -> {
                recentNotifications.remove(notificationKey);
            });

            System.out.println("✓ Notificación guardada: " + notification.getTitle());

        } catch (Exception e) {
            System.out.println("NotificationService: Error al guardar la notificación: " + e.getMessage());
        }
    }
    private String createNotificationKey(Notification notification) {
        StringBuilder key = new StringBuilder();
        key.append(notification.getTitle()).append("|");
        key.append(notification.getBody()).append("|");

        if (notification.getTask() != null) {
            key.append(notification.getTask().getId()).append("|");
        }

        if (notification.getVolunteer() != null) {
            key.append(notification.getVolunteer().getDni());
        }

        return key.toString();
    }

    private boolean isDuplicateNotification(Notification notification) {
        // BUSCAR NOTIFICACIONES SIMILARES EN LOS ÚLTIMOS 10 MINUTOS
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        List<Notification> recentSimilar = notificationRepository.findRecentSimilarNotifications(
                notification.getTitle(),
                notification.getBody(),
                tenMinutesAgo
        );

        return !recentSimilar.isEmpty();
    }
}
