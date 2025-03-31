package solidarityhub.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.NeedDTO;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.Priority;
import solidarityhub.backend.model.enums.Status;
import solidarityhub.backend.service.NeedService;
import solidarityhub.backend.service.TaskService;
import solidarityhub.backend.service.VolunteerService;
import solidarityhub.backend.dto.VolunteerDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    private final VolunteerService volunteerService;
    private final NeedService needService;

    public TaskController(TaskService taskService, VolunteerService volunteerService, NeedService needService) {
        this.taskService = taskService;
        this.volunteerService = volunteerService;
        this.needService = needService;
    }

    @GetMapping
    public ResponseEntity<?> getTasks() {
        List<TaskDTO> taskDTOList = new ArrayList<>();
        taskService.getAllTasks().forEach(t -> {taskDTOList.add(new TaskDTO(t));});
        return ResponseEntity.ok(taskDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable Integer id) {
        if(taskService.getTaskById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new TaskDTO(taskService.getTaskById(id)));
    }

    @PostMapping
    public ResponseEntity<?> addTask(@RequestBody TaskDTO taskDTO) {
        List<Need> needs = new ArrayList<>();
        List<Volunteer> volunteers = new ArrayList<>();

        List<Integer> needIds = taskDTO.getNeeds().stream().map(NeedDTO::getId).toList();
        for (Integer id : needIds) {
            Need need = needService.findNeed(id);
            if (need != null) {
                needs.add(need);
            }
        }

        List<String> volunteerDnis = taskDTO.getVolunteers().stream().map(VolunteerDTO::getDni).toList();
        for (String dni : volunteerDnis) {
            Volunteer volunteer = volunteerService.getVolunteer(dni);
            if (volunteer != null) {
                volunteers.add(volunteer);
            }
        }

        Task task = new Task(needs, taskDTO.getName(), taskDTO.getDescription(), taskDTO.getStartTimeDate(), taskDTO.getEstimatedEndTimeDate(), taskDTO.getPriority(), taskDTO.getStatus(), volunteers);
        taskService.saveTask(task);

        for (Need need : needs) {
            need.setTask(task);
            needService.saveNeed(need);
        }

        for (Volunteer volunteer : volunteers) {
            volunteer.getTasks().add(task);
            volunteer.notifyEmail("Nueva tarea", "Se le ha asignado una nueva tarea: " + task.getTaskName());
            volunteerService.saveVolunteer(volunteer);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        Task task = taskService.getTaskById(id);
        if(task == null) {
            return ResponseEntity.notFound().build();
        }

        List<Need> needs = new ArrayList<>();
        List<Volunteer> volunteers = new ArrayList<>();

        List<Integer> needIds = (List<Integer>) payload.get("needs");
        for (Integer needId : needIds) {
            Need need = needService.findNeed(needId);
            if (need != null && !task.getNeeds().contains(need)) {
                needs.add(need);
            }
        }

        List<String> volunteerIDs = (List<String>) payload.get("volunteers");
        for (String volunteerID : volunteerIDs) {
            Volunteer volunteer = volunteerService.getVolunteer(volunteerID);
            if (volunteer != null && !task.getVolunteers().contains(volunteer)) {
                volunteers.add(volunteer);
            }
        }

        String taskName = (String) payload.get("taskName");
        String taskDescription = (String) payload.get("taskDescription");
        LocalDateTime startTimeDate = LocalDateTime.parse((String) payload.get("startTimeDate"));
        LocalDateTime estimatedEndTimeDate = LocalDateTime.parse((String) payload.get("estimatedEndTimeDate"));
        Priority priority = Priority.valueOf((String) payload.get("priority"));
        Status status = Status.valueOf((String) payload.get("status"));

        task.setTaskName(taskName);
        task.setTaskDescription(taskDescription);
        task.setStartTimeDate(startTimeDate);
        task.setEstimatedEndTimeDate(estimatedEndTimeDate);
        task.setPriority(priority);
        task.setStatus(status);
        task.setVolunteers(volunteers);
        task.setNeeds(needs);

        taskService.saveTask(task);

        for (Need need : needs) {
            if(need.getTask() == null){
                need.setTask(task);
                needService.saveNeed(need);
            }
        }

        for (Volunteer volunteer : volunteers) {
            if(!volunteer.getTasks().contains(task)){
                volunteer.getTasks().add(task);
                volunteer.notifyEmail("Tarea actualizada", "Se ha actualizado una tarea que se le había asignado. Nombre de la tarea: " + task.getTaskName());
                volunteerService.saveVolunteer(volunteer);
            }
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer id) {
        Task task = taskService.getTaskById(id);
        if(task == null) {
            return ResponseEntity.notFound().build();
        }
        taskService.deleteTask(task);
        return ResponseEntity.ok().build();
    }
}
