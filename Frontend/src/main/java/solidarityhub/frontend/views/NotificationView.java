package solidarityhub.frontend.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
    private static Translator translator;
    private final NotificationService notificationService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public NotificationView(NotificationService notificationService) {
        this.notificationService = notificationService;
        initializeTranslator();
        buildView();
    }

    private void buildView() {
        addClassName("notifications-view");
        HeaderComponent header = new HeaderComponent(translator.get("notifications_title"), "window.history.back()");

        add(header);

        // Fetch notifications
        List<NotificationDTO> notifications = notificationService.getAllNotifications();

        if (notifications.isEmpty()) {
            add(getEmptyState());
        } else {
            // Sort notifications by creation date (newest first)
            notifications.sort(Comparator.comparing(NotificationDTO::getCreationDateTime).reversed());

            VerticalLayout notificationsContainer = new VerticalLayout();
            notificationsContainer.setPadding(true);
            notificationsContainer.setSpacing(true);

            for (NotificationDTO notification : notifications) {
                notificationsContainer.add(createNotificationCard(notification));
            }

            add(notificationsContainer);
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
            if (card.getParent().get().getChildren().count() == 0) {
                removeAll();
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
        suggestedTasksButton.addClickListener(e -> UI.getCurrent().navigate("suggested-tasks"));

        emptyState.add(infoIcon, noNotificationsTitle, helpText, suggestedTasksButton);
        return emptyState;
    }
}