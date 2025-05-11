package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import solidarityhub.backend.config.FcmService;
import solidarityhub.backend.model.Notification;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.repository.NotificationRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private FcmService fcmService;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set the user and password fields using reflection
        ReflectionTestUtils.setField(notificationService, "user", "test@example.com");
        ReflectionTestUtils.setField(notificationService, "password", "password");
    }

    @Test
    void testNotifyEmail() {
        // Arrange
        String receiver = "recipient@example.com";
        Notification notification = createTestNotification();
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        notificationService.notifyEmail(receiver, notification);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testNotifyEmail_Exception() {
        // Arrange
        String receiver = "recipient@example.com";
        Notification notification = createTestNotification();
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act - this should not throw an exception despite the mail sender failing
        notificationService.notifyEmail(receiver, notification);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testNotifyApp() {
        // Arrange
        String token = "device-token";
        String title = "Test Title";
        String body = "Test Body";
        when(fcmService.sendNotification(anyString(), anyString(), anyString())).thenReturn("success");

        // Act
        notificationService.notifyApp(token, title, body);

        // Assert
        verify(fcmService, times(1)).sendNotification(token, title, body);
    }

    @Test
    void testNotifyApp_Exception() {
        // Arrange
        String token = "device-token";
        String title = "Test Title";
        String body = "Test Body";
        when(fcmService.sendNotification(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("FCM error"));

        // Act - this should not throw an exception despite the FCM service failing
        notificationService.notifyApp(token, title, body);

        // Assert
        verify(fcmService, times(1)).sendNotification(token, title, body);
    }

    @Test
    void testSave() {
        // Arrange
        Notification notification = createTestNotification();
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.save(notification);

        // Assert
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void testSave_Exception() {
        // Arrange
        Notification notification = createTestNotification();
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("Database error"));

        // Act - this should not throw an exception despite the repository failing
        notificationService.save(notification);

        // Assert
        verify(notificationRepository, times(1)).save(notification);
    }

    // Helper method
    private Notification createTestNotification() {
        Task task = new Task();
        try {
            java.lang.reflect.Field idField = Task.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(task, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Volunteer volunteer = new Volunteer();
        try {
            java.lang.reflect.Field dniField = Volunteer.class.getDeclaredField("dni");
            dniField.setAccessible(true);
            dniField.set(volunteer, "12345678A");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Notification("Test Title", "Test Body", task, volunteer);
    }
}