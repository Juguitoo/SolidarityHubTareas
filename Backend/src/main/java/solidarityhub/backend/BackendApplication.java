package solidarityhub.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import solidarityhub.backend.model.*;
import solidarityhub.backend.model.enums.*;
import solidarityhub.backend.service.*;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@EnableAsync
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Autowired
	public BackendApplication(TaskService taskService, NeedService needService, VolunteerService volunteerService,
							  CatastropheService catastropheService, AffectedService affectedService, NotificationService notificationService) {

		/*Affected affected = affectedService.getAffectedById("12345678A");
		Catastrophe catastrophe = catastropheService.getCatastrophe(3);
		Need need = new Need(affected, "Necesidad de bomberos", UrgencyLevel.MODERATE, TaskType.FIREFIGHTERS, new GPSCoordinates(47.8965000, -10.7025600), catastrophe);
		Need need1 = new Need(affected, "Necesidad de policias", UrgencyLevel.MODERATE, TaskType.POLICE, new GPSCoordinates(47.8965000, -10.7025600), catastrophe);
		Need need2 = new Need(affected, "Necesidad de gestión de personas", UrgencyLevel.MODERATE, TaskType.PEOPLEMANAGEMENT, new GPSCoordinates(47.8965000, -10.7025600), catastrophe);
		Need need3 = new Need(affected, "Necesidad de psicológica", UrgencyLevel.MODERATE, TaskType.PSYCHOLOGICAL, new GPSCoordinates(47.8965000, -10.7025600), catastrophe);
		needService.saveNeed(need);
		needService.saveNeed(need1);
		needService.saveNeed(need2);
		needService.saveNeed(need3);*/

	}
}
