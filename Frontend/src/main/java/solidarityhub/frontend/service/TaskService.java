package solidarityhub.frontend.service;

import org.pingu.domain.enums.Status;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import solidarityhub.frontend.dto.TaskDTO;

import java.util.*;

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

    // AGREGAR MÉTODO PARA LIMPIAR CACHÉ
    public void clearCache() {
        tasksByCatastropheCache.clear();
        suggestedTasksCache.clear();
    }

    //CRUD METHODS
    public void addTask(TaskDTO taskDTO) {
        restTemplate.postForEntity(baseUrl, taskDTO, TaskDTO.class);
        clearCache(); // Limpiar caché después de agregar
    }

    public TaskDTO addAndGetNewTask(TaskDTO taskDTO) {
        try {
            ResponseEntity<TaskDTO> response = restTemplate.postForEntity(baseUrl, taskDTO, TaskDTO.class);

            if (response.getBody() == null) {
                System.err.println("Error: El servidor devolvió una respuesta vacía");
                throw new RuntimeException("La respuesta del servidor está vacía");
            }

            System.out.println("Tarea guardada con ID: " + response.getBody().getId());
            clearCache(); // Limpiar caché después de agregar
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("Error al guardar la tarea: " + e.getMessage());
            throw e;
        }
    }

    public void updateTask(int id, TaskDTO taskDTO) {
        restTemplate.put(baseUrl + "/" + id, taskDTO);
        clearCache(); // Limpiar caché después de actualizar
    }

    public void deleteTask(int id) {
        restTemplate.delete(baseUrl + "/" + id);
        clearCache(); // Limpiar caché después de eliminar
    }

    public void updateTaskStatusOnly(int id, Status newStatus) {
        try {
            System.out.println("=== INICIO updateTaskStatusOnly ===");
            System.out.println("ID de tarea: " + id);
            System.out.println("Nuevo estado: " + newStatus);

            Map<String, String> statusUpdate = new HashMap<>();
            statusUpdate.put("status", newStatus.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(statusUpdate, headers);

            String url = baseUrl + "/" + id + "/status";
            System.out.println("URL de la petición: " + url);
            System.out.println("Payload: " + statusUpdate);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class
            );

            System.out.println("Código de respuesta: " + response.getStatusCode());
            System.out.println("Respuesta del servidor: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✓ Actualización exitosa en el backend");
                clearCache(); // Limpiar caché después de actualizar estado
            } else {
                System.err.println("✗ Error en la respuesta del servidor: " + response.getStatusCode());
            }

            System.out.println("=== FIN updateTaskStatusOnly ===");

        } catch (Exception e) {
            System.err.println("✗ Error actualizando estado: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    public TaskDTO getTaskByIdWithDebug(int id) {
        try {
            System.out.println("=== VERIFICANDO TAREA ===");
            System.out.println("Obteniendo tarea con ID: " + id);

            ResponseEntity<TaskDTO> response = restTemplate.exchange(
                    baseUrl + "/" + id,
                    HttpMethod.GET,
                    null,
                    TaskDTO.class
            );

            TaskDTO task = response.getBody();
            if (task != null) {
                System.out.println("✓ Tarea encontrada");
                System.out.println("  - ID: " + task.getId());
                System.out.println("  - Nombre: " + task.getName());
                System.out.println("  - Estado: " + task.getStatus());
            } else {
                System.err.println("✗ Tarea no encontrada");
            }

            System.out.println("=== FIN VERIFICACIÓN ===");
            return task;
        } catch (Exception e) {
            System.err.println("Error obteniendo tarea: " + e.getMessage());
            return null;
        }
    }
    //GET METHODS - MODIFICAR PARA USAR CACHÉ CORRECTAMENTE
    public List<TaskDTO> getTasksByCatastrophe(int catastropheId) {
        // SIEMPRE OBTENER DATOS FRESCOS PARA DRAG AND DROP
        try {
            String url = baseUrl + "/catastrophe/" + catastropheId;
            ResponseEntity<TaskDTO[]> response = restTemplate.exchange(url, HttpMethod.GET, null, TaskDTO[].class);
            TaskDTO[] tasks = response.getBody();
            if (tasks != null) {
                tasksByCatastropheCache.clear();
                tasksByCatastropheCache.addAll(Arrays.asList(tasks));
                return Arrays.asList(tasks);
            }
            return new ArrayList<>();
        } catch (RestClientException e) {
            System.err.println("Error obteniendo tareas: " + e.getMessage());
            return tasksByCatastropheCache; // Usar caché como fallback
        }
    }

    // Resto de métodos permanecen igual...
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

    public List<TaskDTO> getToDoTasksByCatastrophe(int catastropheId, int limit) {
        return getTasksByCatastrophe(catastropheId).stream()
                .filter(t->t.getStatus().equals(Status.TO_DO))
                .limit(limit).toList();
    }

    public List<TaskDTO> getDoingTasksByCatastrophe(int catastropheId, int limit) {
        return getTasksByCatastrophe(catastropheId).stream()
                .filter(t->t.getStatus().equals(Status.IN_PROGRESS))
                .limit(limit).toList();
    }

    public List<TaskDTO> getDoneTasksByCatastrophe(int catastropheId, int limit) {
        return getTasksByCatastrophe(catastropheId).stream()
                .filter(t->t.getStatus().equals(Status.FINISHED))
                .limit(limit).toList();
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