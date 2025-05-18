package solidarityhub.backend.model.builder;

import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.model.enums.Priority;
import solidarityhub.backend.model.enums.Status;
import solidarityhub.backend.model.strategy.DistanceStrategy;
import solidarityhub.backend.model.strategy.VolunteerAssigner;
import solidarityhub.backend.service.VolunteerService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ModerateTaskBuilder implements TaskBuilder {

    private final Task task;
    private Need need;
    private final VolunteerService volunteerService;

    public ModerateTaskBuilder(Need need, VolunteerService volunteerService) {
        this.task = new Task();
        this.need = need;
        this.volunteerService = volunteerService;
    }

    public ModerateTaskBuilder(VolunteerService volunteerService) {
        this.task = new Task();
        this.need = null;
        this.volunteerService = volunteerService;
    }

    @Override
    public void setNeed(Need need) {
        this.need = need;
    }

    @Override
    public void setTitle() {
        task.setTaskName("Tarea con prioridad moderada");
    }

    @Override
    public void setDescription() {
        task.setTaskDescription("Tarea con prioridad moderada para cubrir la siguiente necesidad " + need.getDescription());
    }

    @Override
    public void setStartDate() {
        task.setStartTimeDate(LocalDateTime.now().plusDays(2)
                .toLocalDate().atTime(9,0));
    }

    @Override
    public void setEndDate() {
        task.setEstimatedEndTimeDate(LocalDateTime.now().plusDays(5)
                .toLocalDate().atTime(18,0));
    }

    @Override
    public void setPriority() {
        task.setPriority(Priority.MODERATE);
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
        task.setVolunteers(new ArrayList<>());
        VolunteerAssigner volunteerAssigner = new VolunteerAssigner();
        volunteerAssigner.setStrategy(new DistanceStrategy());

        List<Volunteer> volunteers = volunteerService.getAllVolunteers();

        List<Volunteer> volunteersToAssign = volunteerAssigner.assignVolunteers(volunteers, new TaskDTO(this.task));
        if(volunteersToAssign.size() > 2) {
            task.setVolunteers(volunteersToAssign.subList(0, 2));
        }else{
            task.setVolunteers(volunteersToAssign);
        }
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
