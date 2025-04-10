package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import solidarityhub.backend.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
}
