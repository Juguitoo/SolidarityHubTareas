package solidarityhub.frontend.service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.pingu.domain.enums.*;
import solidarityhub.frontend.i18n.Translator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatService {

    private Translator translator;
    private static FormatService instance;

    private FormatService() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        }else{
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());
    }

    public static FormatService getInstance() {
        if (instance == null) {
            instance = new FormatService();
        }else{
            instance.updateLocale();
        }
        return instance;
    }

    public void updateLocale(){
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        }else{
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());
    }

    public String formatEmergencyLevel(EmergencyLevel level) {
        if (level == null) return translator.get("unknown_emergency_level");

        return switch (level) {
            case LOW -> translator.get("low_emergency_level");
            case MEDIUM -> translator.get("medium_emergency_level");
            case HIGH -> translator.get("high_emergency_level");
            case VERYHIGH -> translator.get("very_high_emergency_level");
        };
    }

    public String formatTaskStatus(Status status) {
        if (status == null) return "";

        return switch (status) {
            case TO_DO -> translator.get("status_todo");
            case IN_PROGRESS -> translator.get("status_in_progress");
            case FINISHED -> translator.get("status_finished");
        };
    }

    public String formatTaskType(TaskType taskType) {
        if (taskType == null) {
            return translator.get("task_type_not_specified");
        }

        return switch (taskType) {
            case MEDICAL -> translator.get("task_type_medical");
            case POLICE -> translator.get("task_type_police");
            case FIREFIGHTERS -> translator.get("task_type_firefighters");
            case CLEANING -> translator.get("task_type_cleaning");
            case FEED -> translator.get("task_type_feed");
            case PSYCHOLOGICAL -> translator.get("task_type_psychological");
            case BUILDING -> translator.get("task_type_building");
            case CLOTHING -> translator.get("task_type_clothing");
            case REFUGE -> translator.get("task_type_refuge");
            case OTHER -> translator.get("task_type_other");
            case SEARCH -> translator.get("task_type_search");
            case LOGISTICS -> translator.get("task_type_logistics");
            case COMMUNICATION -> translator.get("task_type_communication");
            case MOBILITY -> translator.get("task_type_mobility");
            case PEOPLEMANAGEMENT -> translator.get("task_type_people_management");
            case SAFETY -> translator.get("task_type_safety");
        };
    }

    public String formatUrgencyLevel(UrgencyLevel urgency) {
        if (urgency == null) return "";

        return switch (urgency) {
            case URGENT -> translator.get("urgent_priority");
            case MODERATE -> translator.get("moderate_priority");
            case LOW -> translator.get("low_priority");
        };
    }

    public String formatPriority(Priority priority) {
        if (priority == null) return "";

        return switch (priority) {
            case URGENT -> translator.get("urgent_priority");
            case MODERATE -> translator.get("moderate_priority");
            case LOW -> translator.get("low_priority");
        };
    }

    public String formatDateTime(LocalDateTime taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    public String formatDate(LocalDate taskDate){
        return taskDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public String formatResourceType(ResourceType type) {
        if (type == null) return "";
        return switch (type) {
            case FOOD -> translator.get("resource_type_food");
            case MEDICINE -> translator.get("resource_type_medicine");
            case CLOTHING -> translator.get("resource_type_clothing");
            case SHELTER -> translator.get("resource_type_shelter");
            case TOOLS -> translator.get("resource_type_tools");
            case FUEL -> translator.get("resource_type_fuel");
            case SANITATION -> translator.get("resource_type_sanitation");
            case COMMUNICATION -> translator.get("resource_type_communication");
            case TRANSPORTATION -> translator.get("resource_type_transportation");
            case BUILDING -> translator.get("resource_type_building");
            case MONETARY -> translator.get("resource_type_monetary");
            case STATIONERY -> translator.get("resource_type_stationery");
            case LOGISTICS -> translator.get("resource_type_logistics");
            case OTHER -> translator.get("resource_type_other");
        };
    }

    public String formatDonationStatus(DonationStatus status) {
        if (status == null) {
            return "";
        }

        return switch (status) {
            case COMPLETED -> translator.get("donation_status_completed");
            case IN_PROGRESS -> translator.get("donation_status_in_progress");
            case SCHEDULED -> translator.get("donation_status_scheduled");
            case CANCELLED -> translator.get("donation_status_cancelled");
        };
    }

    public String formatDonationType(DonationType type) {
        if (type == null) {
            return "";
        }

        return switch (type) {
            case FINANCIAL -> translator.get("donation_type_financial");
            case MATERIAL -> translator.get("donation_type_material");
            case SERVICE -> translator.get("donation_type_service");
        };
    }
}
