package solidarityhub.frontend.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.NeedDTO;
import solidarityhub.frontend.dto.TaskDTO;
import solidarityhub.frontend.dto.VolunteerDTO;
import solidarityhub.frontend.model.enums.EmergencyLevel;
import solidarityhub.frontend.model.enums.Priority;
import solidarityhub.frontend.model.enums.Status;
import solidarityhub.frontend.model.enums.TaskType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final VolunteerService volunteerService;
    private final NeedService needService;
    public List<TaskDTO> taskCache;
    public List<TaskDTO> suggestedTasksCache;

    public TaskService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/solidarityhub/tasks";
        this.volunteerService = new VolunteerService();
        this.needService = new NeedService();
        this.taskCache = new ArrayList<>();
        this.suggestedTasksCache = new ArrayList<>();
    }

    //CRUD METHODS
    public void addTask(TaskDTO taskDTO) {
        restTemplate.postForEntity(baseUrl, taskDTO, TaskDTO.class);
        taskCache.clear();
    }

    public void updateTask(int id, TaskDTO taskDTO) {
        restTemplate.put(baseUrl + "/" + id, taskDTO);
    }

    public void deleteTask(int id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    public void clearCache() {
        taskCache.clear();
    }

    //GET METHODS
    public List<TaskDTO> getTasks() {
        if(taskCache == null || taskCache.isEmpty()) {
            try {
                ResponseEntity<TaskDTO[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, TaskDTO[].class);
                TaskDTO[] tasks = response.getBody();
                if (tasks != null) {
                    taskCache = new ArrayList<>(List.of(tasks));
                } else {
                    taskCache = new ArrayList<>();
                }
            } catch (RestClientException e) {
                return new ArrayList<>();
            }
        }
        return taskCache;
    }

    public List<TaskDTO> getTasksByCatastrophe(int catastropheId) {
        return getTasks().stream()
                .filter(task -> task.getCatastropheId() == catastropheId).toList();
    }

    public List<TaskDTO> getToDoTasksByCatastrophe(int catastropheId, int limit) {
        return getTasksByStatusAndCatastrophe(Status.TO_DO, catastropheId, limit);
    }

    public List<TaskDTO> getDoingTasksByCatastrophe(int catastropheId, int limit) {
        return getTasksByStatusAndCatastrophe(Status.IN_PROGRESS, catastropheId, limit);
    }

    public List<TaskDTO> getDoneTasksByCatastrophe(int catastropheId, int limit) {
        return getTasksByStatusAndCatastrophe(Status.FINISHED, catastropheId, limit);
    }

    private List<TaskDTO> getTasksByStatusAndCatastrophe(Status status, int catastropheId, int limit) {
        if(limit <= 0) {
            return getTasksByStatusAndCatastrophe(status, catastropheId);
        }
        return getTasks().stream()
                .filter(task -> status.equals(task.getStatus()))
                .filter(task -> task.getCatastropheId() == catastropheId)
                .sorted(Comparator.comparing(TaskDTO::getStartTimeDate).reversed())
                .limit(limit)
                .toList();
    }

    private List<TaskDTO> getTasksByStatusAndCatastrophe(Status status, int catastropheId) {
        return getTasks().stream()
                .filter(task -> status.equals(task.getStatus()))
                .filter(task -> task.getCatastropheId() == catastropheId)
                .sorted(Comparator.comparing(TaskDTO::getStartTimeDate).reversed())
                .toList();
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

    public List<TaskDTO> getSuggestedTasks(Integer catastropheId) {
        try {
            ResponseEntity<TaskDTO[]> response = restTemplate.exchange(baseUrl + "/suggestedTasks?catastropheId=" + catastropheId, HttpMethod.GET, null, TaskDTO[].class);
            TaskDTO[] suggestedTasks = response.getBody();
            if (suggestedTasks != null) {
                return List.of(suggestedTasks);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}