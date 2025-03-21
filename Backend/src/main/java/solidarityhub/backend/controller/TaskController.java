package solidarityhub.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.NeedType;
import solidarityhub.backend.model.enums.Priority;
import solidarityhub.backend.model.enums.Status;
import solidarityhub.backend.service.NeedService;
import solidarityhub.backend.service.TaskService;
import solidarityhub.backend.service.VolunteerService;

import java.time.LocalDate;
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
        return ResponseEntity.ok(taskService.getTasks());
    }

    @PostMapping
    public ResponseEntity<?> addTask(@RequestBody Map<String, String> payload) {
        Integer needId = Integer.valueOf(payload.get("needId"));
        Need need = needService.findNeed(needId);
        String taskName = payload.get("taskName");
        String taskDescription = payload.get("taskDescription");
        LocalDateTime startTimeDate = LocalDateTime.parse(payload.get("startTimeDate"));
        LocalDateTime estimatedEndTimeDate = LocalDateTime.parse(payload.get("estimatedEndTimeDate"));
        Priority priority = Priority.valueOf(payload.get("priority"));
        Status status = Status.valueOf(payload.get("status"));
        String volunteerId = payload.get("volunteerId");
        Volunteer volunteer = volunteerService.getVolunteer(volunteerId);

        Task task = new Task(need, taskName, taskDescription, startTimeDate, estimatedEndTimeDate, priority, status, volunteer);

        taskService.saveTask(task);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer taskId, @RequestBody Map<String, String> payload) {
        String taskName = payload.get("taskName");
        String taskDescription = payload.get("taskDescription");
        LocalDateTime startTimeDate = LocalDateTime.parse(payload.get("startTimeDate"));
        LocalDateTime estimatedEndTimeDate = LocalDateTime.parse(payload.get("estimatedEndTimeDate"));
        Priority priority = Priority.valueOf(payload.get("priority"));
        Status status = Status.valueOf(payload.get("status"));

        return ResponseEntity.ok(taskId); //a cambiar
    }
}
