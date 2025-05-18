package solidarityhub.frontend.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.BackendDTOObservableService;
import solidarityhub.frontend.dto.TaskDTO;
import org.pingu.domain.enums.Status;

import java.util.*;
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
        taskCache.clear();
    }

    public void deleteTask(int id) {
        restTemplate.delete(baseUrl + "/" + id);
        taskCache.clear();
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
        if(taskCache == null || taskCache.isEmpty()) {
            try {
                String url = baseUrl + "/catastrophe/" + catastropheId;
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
        return getTasksByCatastrophe(catastropheId).stream()
                .filter(task -> status.equals(task.getStatus()))
                .sorted(Comparator.comparing(TaskDTO::getStartTimeDate).reversed())
                .limit(limit)
                .toList();
    }

    private List<TaskDTO> getTasksByStatusAndCatastrophe(Status status, int catastropheId) {
        return getTasksByCatastrophe(catastropheId).stream()
                .filter(task -> status.equals(task.getStatus()))
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
            ResponseEntity<TaskDTO[]> response = restTemplate.exchange(baseUrl + "/suggestedTasks?catastropheId=" + catastropheId,
                    HttpMethod.GET, null, TaskDTO[].class);
            TaskDTO[] suggestedTasks = response.getBody();
            if (suggestedTasks != null) {
                return List.of(suggestedTasks);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void updateTaskStatus(int id, Status newStatus) {
        try {
            // Obtener la tarea actual
            TaskDTO currentTask = getTaskById(id);
            if (currentTask == null) {
                return;
            }

            // Clonar la tarea y actualizar solo el estado
            TaskDTO updatedTask = new TaskDTO(
                    currentTask.getName(),
                    currentTask.getDescription(),
                    currentTask.getStartTimeDate(),
                    currentTask.getEstimatedEndTimeDate(),
                    currentTask.getType(),
                    currentTask.getPriority(),
                    currentTask.getEmergencyLevel(),
                    newStatus, // Nuevo estado
                    currentTask.getNeeds(),
                    currentTask.getVolunteers(),
                    currentTask.getCatastropheId(),
                    currentTask.getMeetingDirection()
            );

            // Actualizar en el backend
            restTemplate.put(baseUrl + "/" + id, updatedTask);

            // Actualizar la caché localmente
            if (!taskCache.isEmpty()) {
                for (int i = 0; i < taskCache.size(); i++) {
                    TaskDTO cachedTask = taskCache.get(i);
                    if (cachedTask.getId() == id) {
                        cachedTask.setStatus(newStatus);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // Manejar la excepción
            System.err.println("Error al actualizar el estado de la tarea: " + e.getMessage());
            throw e;
        }
    }
}