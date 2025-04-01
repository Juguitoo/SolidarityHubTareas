package solidarityhub.frontend.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.dto.VolunteerDTO;
import solidarityhub.frontend.model.enums.EmergencyLevel;
import solidarityhub.frontend.model.enums.NeedType;
import solidarityhub.frontend.model.enums.Priority;
import solidarityhub.frontend.model.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskService {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final VolunteerService volunteerService;
    private final NeedService needService;

    public TaskService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/tasks";
        this.volunteerService = new VolunteerService();
        this.needService = new NeedService();
    }

    //CRUD METHODS
    public void addTask(TaskDTO taskDTO) {
        restTemplate.postForEntity(baseUrl, taskDTO, TaskDTO.class);
    }

    public void updateTask(int id, TaskDTO taskDTO) {
        restTemplate.put(baseUrl + "/" + id, taskDTO);
    }

    public void deleteTask(int id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    //GET METHODS
    public List<TaskDTO> getTasks() {
        try {
            ResponseEntity<TaskDTO[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, TaskDTO[].class);
            TaskDTO[] tasks = response.getBody();
            if (tasks != null) {
                return List.of(tasks);
            }
            return new ArrayList<>();
        } catch (RestClientException e) {
            return getExampleTasks(5);
        }
    }

    public List<TaskDTO> getToDoTasks(int limit) {
        return getTasksByStatus(Status.TO_DO, limit);
    }

    public List<TaskDTO> getDoingTasks(int limit) {
        return getTasksByStatus(Status.IN_PROGRESS, limit);
    }

    public List<TaskDTO> getDoneTasks(int limit) {
        return getTasksByStatus(Status.FINISHED, limit);
    }

    private List<TaskDTO> getTasksByStatus(Status status) {
        return getTasks().stream()
                .filter(task -> status.equals(task.getStatus()))
                .toList();
    }

    private List<TaskDTO> getTasksByStatus(Status status, int limit) {
        if(limit <= 0) {
            return getTasksByStatus(status);
        }
        return getTasks().stream()
                .filter(task -> status.equals(task.getStatus()))
                .sorted(Comparator.comparing(TaskDTO::getStartTimeDate).reversed())
                .limit(limit)
                .toList();
    }

    public TaskDTO getTaskById(int id) {
        ResponseEntity<TaskDTO> response = restTemplate.exchange(baseUrl + "/" + id, HttpMethod.GET, null, TaskDTO.class);
        return response.getBody();
    }

    //GET EXAMPLE TASKS
    private List<TaskDTO> getExampleTasks(int limit) {
        List<NeedDTO> needs = needService.getExampleNeeds();
        List<VolunteerDTO> volunteers = volunteerService.getExampleVolunteers();

        List<TaskDTO> exampleTasks = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            // Convertir a DTO para devolver
            TaskDTO taskDTO = new TaskDTO(
                    "Tarea de ejemplo " + i,
                    "Descripcion de ejemplo " + i,
                    LocalDateTime.now().plusHours(i),
                    LocalDateTime.now().plusDays(3),
                    NeedType.OTHER,
                    Priority.LOW,
                    EmergencyLevel.HIGH,
                    Status.IN_PROGRESS,
                    needs,
                    volunteers
            );
            exampleTasks.add(taskDTO);
        }
        for (int i = 0; i < limit; i++) {
            // Convertir a DTO para devolver
            TaskDTO taskDTO = new TaskDTO(
                    "Tarea de ejemplo " + i,
                    "Descripcion de ejemplo " + i,
                    LocalDateTime.now().plusHours(i),
                    LocalDateTime.now().plusDays(3),
                    NeedType.OTHER,
                    Priority.LOW,
                    EmergencyLevel.HIGH,
                    Status.TO_DO,
                    needs,
                    volunteers
            );
            exampleTasks.add(taskDTO);
        }
        for (int i = 0; i < limit; i++) {
            // Convertir a DTO para devolver
            TaskDTO taskDTO = new TaskDTO(
                    "Tarea de ejemplo " + i,
                    "Descripcion de ejemplo " + i,
                    LocalDateTime.now().plusHours(i),
                    LocalDateTime.now().plusDays(3),
                    NeedType.OTHER,
                    Priority.LOW,
                    EmergencyLevel.HIGH,
                    Status.FINISHED,
                    needs,
                    volunteers
            );
            exampleTasks.add(taskDTO);
        }

        return exampleTasks;
    }


}
