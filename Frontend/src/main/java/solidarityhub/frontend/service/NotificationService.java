package solidarityhub.frontend.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.NotificationDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class NotificationService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public NotificationService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/solidarityhub/notifications";

        // Configurar timeout más corto para respuestas rápidas
        restTemplate.getRequestFactory();
    }

    public List<NotificationDTO> getAllNotifications() {
        try {
            ResponseEntity<NotificationDTO[]> response = restTemplate.exchange(
                    baseUrl, HttpMethod.GET, null, NotificationDTO[].class);

            NotificationDTO[] notifications = response.getBody();
            if (notifications != null) {
                return Arrays.asList(notifications);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public boolean hasUnreadNotifications() {
        List<NotificationDTO> notifications = getAllNotifications();
        return notifications.stream().anyMatch(notification -> !notification.isSeen());
    }

    public void markAsRead(int notificationId) {
        try {
            restTemplate.put(baseUrl + "/" + notificationId + "/read", null);
        } catch (Exception e) {
            // Handle exception silently for individual notifications
        }
    }

    public boolean markAllAsRead() {
        try {
            // Llamada simple y directa
            ResponseEntity<Void> response = restTemplate.exchange(
                    baseUrl + "/mark-all-read",
                    HttpMethod.PUT,
                    null,
                    Void.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}