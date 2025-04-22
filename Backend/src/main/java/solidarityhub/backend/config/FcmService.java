package solidarityhub.backend.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    public String sendNotification(String token, String title, String body) {
        Message message = Message.builder()
            .setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
            .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending message";
        }
    }
}