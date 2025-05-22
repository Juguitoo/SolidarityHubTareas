package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import solidarityhub.backend.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findBySeenOrderByCreationDateTimeDesc(boolean seen);

    List<Notification> findByVolunteerDniAndSeen(String volunteerDni, boolean seen);

    int countBySeenAndVolunteerDniIsNull(boolean seen);

    // Método optimizado con query nativa para máximo rendimiento
    @Modifying
    @Transactional
    @Query(value = "UPDATE notification SET seen = true WHERE seen = false", nativeQuery = true)
    int markAllUnreadAsReadNative();

    // Método JPQL como respaldo
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.seen = true WHERE n.seen = false")
    int markAllUnreadAsRead();
}
