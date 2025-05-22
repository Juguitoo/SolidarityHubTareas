package solidarityhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import solidarityhub.backend.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findBySeenOrderByCreationDateTimeDesc(boolean seen);

    List<Notification> findByVolunteerDniAndSeen(String volunteerDni, boolean seen);

    int countBySeenAndVolunteerDniIsNull(boolean seen);

    @Modifying
    @Transactional
    @Query(value = "UPDATE notification SET seen = true WHERE seen = false", nativeQuery = true)
    int markAllUnreadAsReadNative();

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.seen = true WHERE n.seen = false")
    int markAllUnreadAsRead();

    // AGREGAR ESTE MÉTODO PARA DETECTAR DUPLICADOS
    @Query("SELECT n FROM Notification n WHERE n.title = :title AND n.body = :body AND n.creationDateTime > :since")
    List<Notification> findRecentSimilarNotifications(
            @Param("title") String title,
            @Param("body") String body,
            @Param("since") LocalDateTime since
    );

    // MÉTODO PARA LIMPIAR NOTIFICACIONES ANTIGAS (OPCIONAL)
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.creationDateTime < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}