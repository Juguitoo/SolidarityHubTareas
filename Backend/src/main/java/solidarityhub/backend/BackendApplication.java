package solidarityhub.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import solidarityhub.backend.model.*;
import solidarityhub.backend.model.enums.*;
import solidarityhub.backend.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Autowired
	public BackendApplication(TaskService taskService, NeedService needService, VolunteerService volunteerService,
							  CatastropheService catastropheService, AffectedService affectedService) {
		Need need = needService.findNeed(26);
		Volunteer volunteer = volunteerService.getVolunteer("12345678A");

		Task task1 = new Task(List.of(need), "test", "descrptionTest", LocalDateTime.now(), LocalDateTime.now(), Priority.LOW, Status.IN_PROGRESS, List.of(volunteer));
		taskService.saveTask(task1);
		need.setTask(task1);
		needService.saveNeed(need);
		volunteer.getTasks().add(task1);
		volunteerService.saveVolunteer(volunteer);
	}

	private static List<Preference> getPreferenceList() {
		Preference preference1 = new Preference("Safety");
		Preference preference2 = new Preference("Cooking");
		Preference preference3 = new Preference("People_managing");
		return List.of(preference1, preference2, preference3);
	}

	private static List<ScheduleAvailability> getScheduleAvailabilities() {
		ScheduleAvailability scheduleAvailability1 = new ScheduleAvailability(DayMoment.MORNING, WeekDay.WEDNESDAY);
		ScheduleAvailability scheduleAvailability2 = new ScheduleAvailability(DayMoment.AFTERNOON, WeekDay.SUNDAY);
		return List.of(scheduleAvailability1, scheduleAvailability2);
	}

	private static List<Skill> getSkillList() {
		Skill skill1 = new Skill("Teamwork");
		Skill skill2 = new Skill("Leadership");
		Skill skill3 = new Skill("Improvisation");
		return List.of(skill1, skill2, skill3);
	}
}
