package solidarityhub.backend.model.builder;

import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.model.enums.Priority;
import solidarityhub.backend.model.enums.Status;
import solidarityhub.backend.model.strategy.AvailabilityStrategy;
import solidarityhub.backend.model.strategy.DistanceStrategy;
import solidarityhub.backend.model.strategy.VolunteerAssigner;
import solidarityhub.backend.service.VolunteerService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LowPriorityTask implements TaskBuilder {

    private final Task task;
    private final Need need;
    private final VolunteerService volunteerService;

    public LowPriorityTask(Need need, VolunteerService volunteerService) {
        this.task = new Task();
        this.need = need;
        this.volunteerService = volunteerService;
    }

    @Override
    public void setTitle() {
        task.setTaskName("Tarea con prioridad baja");
    }

    @Override
    public void setDescription() {
        task.setTaskDescription("Tarea con prioridad baja para cubrir la siguiente necesidad " + need.getDescription());
    }

    @Override
    public void setStartDate() {
        task.setStartTimeDate(LocalDateTime.now().plusDays(3).toLocalDate().atTime(9,0));
    }

    @Override
    public void setEndDate() {
        task.setEstimatedEndTimeDate(LocalDateTime.now().plusDays(5).toLocalDate().atTime(18,0));
    }

    @Override
    public void setPriority() {
        task.setPriority(Priority.LOW);
    }

    @Override
    public void setEmergencyLevel() {
        task.setEmergencyLevel(EmergencyLevel.MEDIUM);
    }

    @Override
    public void setStatus() {
        task.setStatus(Status.TO_DO);
    }

    @Override
    public void setVolunteers() {
        VolunteerAssigner volunteerAssigner = new VolunteerAssigner();
        volunteerAssigner.setStrategy(new AvailabilityStrategy());

        List<Volunteer> volunteers = volunteerService.getAllVolunteers();

        List<Volunteer> volunteersToAssign = volunteerAssigner.assignVolunteers(volunteers, new TaskDTO(this.task)).subList(0,1);
        task.setVolunteers(volunteersToAssign);
    }

    @Override
    public void setNeed() {
        task.setNeeds(new ArrayList<>(List.of(need)));
        task.setType(need.getTaskType());
    }

    @Override
    public void setCatastrophe() {
        task.setCatastrophe(need.getCatastrophe());
    }

    @Override
    public Task getResult() {
        return task;
    }
}
