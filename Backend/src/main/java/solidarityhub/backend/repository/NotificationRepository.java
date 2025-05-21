package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import solidarityhub.backend.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findBySeenOrderByCreationDateTimeDesc(boolean seen);

    List<Notification> findByVolunteerDniAndSeen(String volunteerDni, boolean seen);

    int countBySeenAndVolunteerDniIsNull(boolean seen);
}
