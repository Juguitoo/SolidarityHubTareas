package solidarityhub.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.NotificationDTO;
import solidarityhub.backend.model.Notification;
import solidarityhub.backend.repository.NotificationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/solidarityhub/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllNotifications() {
        List<NotificationDTO> dtos = new ArrayList<>();
        notificationRepository.findBySeenOrderByCreationDateTimeDesc(false).forEach(n -> {
            dtos.add(convertToDTO(n));
        });
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNotification(@PathVariable Integer id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        if (notification.isPresent()) {
            return ResponseEntity.ok(convertToDTO(notification.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/volunteer/{dni}")
    public ResponseEntity<?> getNotificationsForVolunteer(@PathVariable String dni) {
        List<NotificationDTO> dtos = new ArrayList<>();
        notificationRepository.findByVolunteerDniAndSeen(dni, false).forEach(n -> {
            dtos.add(convertToDTO(n));
        });
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/count")
    public ResponseEntity<?> getUnreadNotificationsCount() {
        int count = notificationRepository.countBySeenAndVolunteerDniIsNull(false);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable int id) {
        Optional<Notification> notificationOpt = notificationRepository.findById(id);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setSeen(true);
            notificationRepository.save(notification);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable int id) {
        Optional<Notification> notificationOpt = notificationRepository.findById(id);
        if (notificationOpt.isPresent()) {
            notificationRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setBody(notification.getBody());
        dto.setCreationDateTime(notification.getCreationDateTime());
        dto.setSeen(notification.isSeen());

        if (notification.getTask() != null) {
            dto.setTaskId(notification.getTask().getId());
        }

        if (notification.getVolunteer() != null) {
            dto.setVolunteerDni(notification.getVolunteer().getDni());
        }

        return dto;
    }
}
