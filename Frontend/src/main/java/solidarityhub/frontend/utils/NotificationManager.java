package solidarityhub.frontend.utils;

import com.vaadin.flow.component.UI;
import solidarityhub.frontend.service.NotificationService;
import solidarityhub.frontend.views.HeaderComponent;
import solidarityhub.frontend.views.MainLayout;

/**
 * Clase utilitaria para gestionar las actualizaciones del indicador de notificaciones
 * en toda la aplicación de forma centralizada.
 */
public class NotificationManager {

    /**
     * Actualiza el indicador de notificaciones en todos los HeaderComponents
     * de la UI actual.
     */
    public static void updateNotificationIndicator() {
        UI current = UI.getCurrent();
        if (current != null) {
            current.access(() -> {
                try {
                    NotificationService notificationService = new NotificationService();
                    boolean hasUnread = notificationService.hasUnreadNotifications();
                    HeaderComponent.updateAllNotificationButtons(hasUnread);
                } catch (Exception e) {
                    System.err.println("Error updating notification indicator: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Actualiza el indicador de notificaciones de forma asíncrona.
     * Útil cuando se llama desde el backend o hilos separados.
     */
    public static void updateNotificationIndicatorAsync(UI ui) {
        if (ui != null && !ui.isClosing()) {
            ui.access(() -> {
                try {
                    NotificationService notificationService = new NotificationService();
                    boolean hasUnread = notificationService.hasUnreadNotifications();
                    HeaderComponent.updateAllNotificationButtons(hasUnread);
                } catch (Exception e) {
                    System.err.println("Error updating notification indicator async: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Fuerza la actualización del indicador verificando el estado actual
     * desde el servidor. También actualiza el MainLayout si está disponible.
     */
    public static void forceRefreshNotificationIndicator() {
        UI current = UI.getCurrent();
        if (current != null) {
            current.access(() -> {
                try {
                    // Actualizar a través de HeaderComponent
                    NotificationService notificationService = new NotificationService();
                    boolean hasUnread = notificationService.hasUnreadNotifications();
                    HeaderComponent.updateAllNotificationButtons(hasUnread);

                    // Si hay un MainLayout disponible, también forzar su actualización
                    if (current.getChildren().findFirst().orElse(null) instanceof MainLayout) {
                        MainLayout mainLayout = (MainLayout) current.getChildren().findFirst().orElse(null);
                        if (mainLayout != null) {
                            mainLayout.forceUpdateNotificationIndicator();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error forcing notification refresh: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Método de conveniencia para actualizar después de acciones específicas
     * como marcar notificaciones como leídas.
     */
    public static void updateAfterNotificationAction() {
        // Pequeño delay para asegurar que la base de datos se haya actualizado
        UI current = UI.getCurrent();
        if (current != null) {
            current.access(() -> {
                try {
                    // Esperar un momento para que se complete la operación en BD
                    Thread.sleep(100);
                    forceRefreshNotificationIndicator();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.err.println("Error in updateAfterNotificationAction: " + e.getMessage());
                }
            });
        }
    }
}