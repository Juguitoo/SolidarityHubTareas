package solidarityhub.frontend.views;



import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.NotificationDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.NotificationService;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Route("notifications")
@PageTitle("Notificaciones")
public class NotificationView extends VerticalLayout {
    private static Translator translator = new Translator();
    private final NotificationService notificationService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private VerticalLayout notificationsContainer;
    private List<NotificationDTO> currentNotifications;

    public NotificationView(NotificationService notificationService) {
        this.notificationService = notificationService;
        translator.initializeTranslator();
        buildView();
    }

    private void buildView() {
        addClassName("notifications-view");
        HeaderComponent header = new HeaderComponent(translator.get("notifications_title"), "window.history.back()");

        add(header);

        // Fetch notifications
        currentNotifications = notificationService.getAllNotifications();

        if (currentNotifications.isEmpty()) {
            add(getEmptyState());
        } else {
            // Sort notifications by creation date (newest first)
            currentNotifications.sort(Comparator.comparing(NotificationDTO::getCreationDateTime).reversed());

            // Add mark all as read button
            add(getMarkAllAsReadButton());

            notificationsContainer = new VerticalLayout();
            notificationsContainer.setPadding(true);
            notificationsContainer.setSpacing(true);

            for (NotificationDTO notification : currentNotifications) {
                notificationsContainer.add(createNotificationCard(notification));
            }

            add(notificationsContainer);
        }
    }

    private Component getMarkAllAsReadButton() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonLayout.setPadding(true);
        buttonLayout.addClassName("mark-all-button-layout");

        Button markAllAsReadButton = new Button(translator.get("mark_all_as_read"), VaadinIcon.CHECK_CIRCLE.create());
        markAllAsReadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        markAllAsReadButton.addClassName("mark-all-button");

        markAllAsReadButton.addClickListener(e -> markAllAsRead());

        buttonLayout.add(markAllAsReadButton);
        return buttonLayout;
    }

    private void markAllAsRead() {
        if (currentNotifications == null || currentNotifications.isEmpty()) {
            return;
        }

        try {
            // Llamada directa sin hilos adicionales
            boolean success = notificationService.markAllAsRead();

            if (success) {
                // Show success notification
                Notification.show(
                        translator.get("all_notifications_marked_read"),
                        2000,
                        Notification.Position.BOTTOM_START
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Clear the view immediately and show empty state
                removeAll();

                // Re-add header
                HeaderComponent header = new HeaderComponent(translator.get("notifications_title"), "window.history.back()");
                add(header);
                add(getEmptyState());
            } else {
                // Show error notification
                Notification.show(
                        translator.get("error_marking_notifications"),
                        3000,
                        Notification.Position.BOTTOM_START
                ).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            Notification.show(
                    translator.get("error_marking_notifications") + ": " + e.getMessage(),
                    3000,
                    Notification.Position.BOTTOM_START
            ).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private Component createNotificationCard(NotificationDTO notification) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("notification-card");
        card.setPadding(true);
        card.setSpacing(false);

        // Determine icon based on notification type
        Icon icon;
        if (notification.getTitle().contains("recurso")) {
            icon = VaadinIcon.PACKAGE.create();
            card.addClassName("resource-notification");
        } else if (notification.getTitle().contains("almacén")) {
            icon = VaadinIcon.STORAGE.create();
            card.addClassName("storage-notification");
        } else if (notification.getTitle().contains("tarea")) {
            icon = VaadinIcon.TASKS.create();
            card.addClassName("task-notification");
        } else {
            icon = VaadinIcon.BELL.create();
        }

        // Header with title and date
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setAlignItems(Alignment.CENTER);
        titleLayout.add(icon, new H2(notification.getTitle()));

        Span dateSpan = new Span(notification.getCreationDateTime().format(DATE_FORMATTER));
        dateSpan.addClassName("notification-date");

        header.add(titleLayout, dateSpan);
        header.expand(titleLayout);

        // Body
        Paragraph body = new Paragraph(notification.getBody());
        body.addClassName("notification-body");

        // Actions if related to a task
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        if (notification.getTaskId() != null) {
            Button viewTaskButton = new Button(translator.get("view_task"), VaadinIcon.ARROW_RIGHT.create());
            viewTaskButton.addClickListener(e -> UI.getCurrent().navigate("editTask",
                    QueryParameters.simple(Collections.singletonMap("id", String.valueOf(notification.getTaskId())))));
            actions.add(viewTaskButton);
        }

        Button markAsReadButton = new Button(translator.get("mark_as_read"));
        markAsReadButton.addClickListener(e -> {
            notificationService.markAsRead(notification.getId());
            card.removeFromParent();

            // If no more notifications, show empty state
            if (notificationsContainer.getChildren().count() == 0) {
                removeAll();
                HeaderComponent headerComponent = new HeaderComponent(translator.get("notifications_title"), "window.history.back()");
                add(headerComponent);
                add(getEmptyState());
            }
        });

        actions.add(markAsReadButton);

        card.add(header, body, actions);
        return card;
    }

    private void initializeTranslator() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());
    }

    private Component getEmptyState() {
        VerticalLayout emptyState = new VerticalLayout();
        emptyState.setAlignItems(Alignment.CENTER);
        emptyState.setJustifyContentMode(JustifyContentMode.START);
        emptyState.setSizeFull();

        Icon infoIcon = VaadinIcon.INFO_CIRCLE_O.create();
        infoIcon.addClassName("info-icon");

        H2 noNotificationsTitle = new H2(translator.get("no_notifications"));
        noNotificationsTitle.addClassName("no-notifications-title");

        Paragraph helpText = new Paragraph(translator.get("notifications_help_text"));
        helpText.addClassName("help-text");

        Button suggestedTasksButton = new Button(translator.get("go_to_suggested_tasks"), new Icon(VaadinIcon.ARROW_RIGHT));
        suggestedTasksButton.addClickListener(e -> UI.getCurrent().navigate("tasks/suggested-tasks"));

        emptyState.add(infoIcon, noNotificationsTitle, helpText, suggestedTasksButton);
        return emptyState;
    }
}