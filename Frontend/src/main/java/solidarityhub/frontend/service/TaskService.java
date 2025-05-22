package solidarityhub.frontend.service;

import org.pingu.domain.enums.Status;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.TaskDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TaskService {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    public List<TaskDTO> suggestedTasksCache;
    private final List<TaskDTO> tasksByCatastropheCache = new ArrayList<>();

    public TaskService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/solidarityhub/tasks";
        this.suggestedTasksCache = new ArrayList<>();
    }

    //CRUD METHODS
    public void addTask(TaskDTO taskDTO) {
        restTemplate.postForEntity(baseUrl, taskDTO, TaskDTO.class);
    }

    public TaskDTO addAndGetNewTask(TaskDTO taskDTO) {
        try {
            ResponseEntity<TaskDTO> response = restTemplate.postForEntity(baseUrl, taskDTO, TaskDTO.class);

            if (response.getBody() == null) {
                System.err.println("Error: El servidor devolvió una respuesta vacía");
                throw new RuntimeException("La respuesta del servidor está vacía");
            }

            System.out.println("Tarea guardada con ID: " + response.getBody().getId());
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("Error al guardar la tarea: " + e.getMessage());
            throw e;
        }
    }

    public void updateTask(int id, TaskDTO taskDTO) {
        restTemplate.put(baseUrl + "/" + id, taskDTO);
    }

    public void deleteTask(int id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    //GET METHODS
    public List<TaskDTO> getTasks(String status, String priority, String type, String emergencyLevel, Integer catastropheId) {
            try {
                String url = baseUrl;
                if(status != null && !status.isEmpty()) {
                    url += "?status=" + status;
                }
                if(priority != null && !priority.isEmpty()) {
                    url += (url.contains("?") ? "&" : "?") + "priority=" + priority;
                }
                if(type != null && !type.isEmpty()) {
                    url += (url.contains("?") ? "&" : "?") + "type=" + type;
                }
                if(emergencyLevel != null && !emergencyLevel.isEmpty()) {
                    url += (url.contains("?") ? "&" : "?") + "emergencyLevel=" + emergencyLevel;
                }
                if(catastropheId != null) {
                    url += (url.contains("?") ? "&" : "?") + "catastropheId=" + catastropheId;
                }

                ResponseEntity<TaskDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, null, TaskDTO[].class);
                TaskDTO[] tasks = response.getBody();
                return tasks != null ? Arrays.asList(tasks) : new ArrayList<>();
            } catch (RestClientException e) {
                return new ArrayList<>();
            }
    }

    public List<TaskDTO> getTasksByCatastrophe(int catastropheId) {
        if(tasksByCatastropheCache.isEmpty()) {
            try {
                String url = baseUrl + "/catastrophe/" + catastropheId;
                ResponseEntity<TaskDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, null, TaskDTO[].class);
                TaskDTO[] tasks = response.getBody();
                if (tasks != null) {
                    tasksByCatastropheCache.clear();
                    tasksByCatastropheCache.addAll(Arrays.asList(tasks));
                }
                return tasks != null ? Arrays.asList(tasks) : new ArrayList<>();
            } catch (RestClientException e) {
                return new ArrayList<>();
            }
        }else return tasksByCatastropheCache;
    }

    public List<TaskDTO> getToDoTasksByCatastrophe(int catastropheId, int limit) {
        return getTasksByCatastrophe(catastropheId).stream().filter(t->t.getStatus().equals(Status.TO_DO)).limit(limit).toList();
    }

    public List<TaskDTO> getDoingTasksByCatastrophe(int catastropheId, int limit) {
        return getTasksByCatastrophe(catastropheId).stream().filter(t->t.getStatus().equals(Status.IN_PROGRESS)).limit(limit).toList();
    }

    public List<TaskDTO> getDoneTasksByCatastrophe(int catastropheId, int limit) {
        return getTasksByCatastrophe(catastropheId).stream().filter(t->t.getStatus().equals(Status.FINISHED)).limit(limit).toList();
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
        } catch (Exception e) {
            // Manejar la excepción
            System.err.println("Error al actualizar el estado de la tarea: " + e.getMessage());
            throw e;
        }
    }

    public int getToDoTasksCount(int catastropheId) {
        ResponseEntity<Integer> response = restTemplate.exchange(baseUrl + "/todo?catastropheId=" + catastropheId,
                HttpMethod.GET, null, Integer.class);
        if(response.getBody() == null) {
            return 0;
        }
        return response.getBody();
    }

    public int getInProgressTasksCount(int catastropheId) {
        ResponseEntity<Integer> response = restTemplate.exchange(baseUrl + "/inProgress?catastropheId=" + catastropheId,
                HttpMethod.GET, null, Integer.class);
        if(response.getBody() == null) {
            return 0;
        }
        return response.getBody();
    }

    public int getFinishedTasksCount(int catastropheId) {
        ResponseEntity<Integer> response = restTemplate.exchange(baseUrl + "/finished?catastropheId=" + catastropheId,
                HttpMethod.GET, null, Integer.class);
        if(response.getBody() == null) {
            return 0;
        }
        return response.getBody();
    }
}