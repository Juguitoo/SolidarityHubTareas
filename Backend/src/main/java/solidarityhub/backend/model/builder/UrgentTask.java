package solidarityhub.backend.model.builder;

import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.model.enums.Priority;
import solidarityhub.backend.model.enums.Status;
import solidarityhub.backend.model.strategy.DistanceStrategy;
import solidarityhub.backend.model.strategy.SkillStrategy;
import solidarityhub.backend.model.strategy.VolunteerAssigner;
import solidarityhub.backend.service.VolunteerService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UrgentTask implements TaskBuilder {

    private final Task task;
    private final Need need;
    private final VolunteerService volunteerService;

    public UrgentTask(Need need, VolunteerService volunteerService) {
        this.task = new Task();
        this.need = need;
        this.volunteerService = volunteerService;
    }

    @Override
    public void setTitle() {
        task.setTaskName("Tarea con prioridad urgente");
    }

    @Override
    public void setDescription() {
        task.setTaskDescription("Tarea urgente para cubrir la siguiente necesidad " + need.getDescription());
    }

    @Override
    public void setStartDate() {
        task.setStartTimeDate(LocalDateTime.now().plusDays(1).toLocalDate().atTime(9,0));
    }

    @Override
    public void setEndDate() {
        task.setEstimatedEndTimeDate(LocalDateTime.now().plusDays(5).toLocalDate().atTime(18,0));
    }

    @Override
    public void setPriority() {
        task.setPriority(Priority.URGENT);
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
        List<Volunteer> volunteersToAssign = volunteerService.getAllVolunteers();

        VolunteerAssigner volunteerAssigner = new VolunteerAssigner();
        volunteerAssigner.setStrategy(new SkillStrategy());

        List<Volunteer> volunteersBySkill = volunteerAssigner.assignVolunteers(volunteersToAssign, new TaskDTO(this.task));

        volunteerAssigner.setStrategy(new DistanceStrategy());
        List<Volunteer> volunteersByDistance = volunteerAssigner.assignVolunteers(volunteersToAssign, new TaskDTO(this.task));

        if(!volunteersBySkill.isEmpty()){
            volunteersToAssign.retainAll(volunteersBySkill);
        }

        volunteersToAssign.retainAll(volunteersByDistance);

        if (volunteersToAssign.size() > 3) {
            task.setVolunteers(volunteersToAssign.subList(0,2));
        }else{
            task.setVolunteers(volunteersToAssign);
        }
    }

    @Override
    public void setNeed() {
        task.setNeeds(new ArrayList<>(List.of(need)));
        task.setType(need.getTaskType());
    }

    public void setCatastrophe() {
        task.setCatastrophe(need.getCatastrophe());
    }

    @Override
    public Task getResult() {
        return task;
    }
}
