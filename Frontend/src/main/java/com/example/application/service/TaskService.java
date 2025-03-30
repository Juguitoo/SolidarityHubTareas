package com.example.application.service;

import com.example.application.dto.NeedDTO;
import com.example.application.dto.TaskDTO;
import com.example.application.dto.VolunteerDTO;
import com.example.application.model.Need;
import com.example.application.model.Task;
import com.example.application.model.Volunteer;
import com.example.application.model.enums.NeedType;
import com.example.application.model.enums.Priority;
import com.example.application.model.enums.Status;
import com.example.application.model.enums.UrgencyLevel;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskService {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public TaskService() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:8082/tasks";
    }

    //========================== TAREAS =============================
    public void addTask(TaskDTO taskDTO) {
        restTemplate.postForEntity(baseUrl, taskDTO, TaskDTO.class);
    }

    public void updateTask(int id, TaskDTO taskDTO) {
        restTemplate.put(baseUrl + "/" + id, taskDTO);
    }

    public void deleteTask(int id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    public List<TaskDTO> getTasks() {
        try {
            ResponseEntity<TaskDTO[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, TaskDTO[].class);
            TaskDTO[] tasks = response.getBody();
            if (tasks != null) {
                return List.of(tasks);
            }
            return new ArrayList<>();
        } catch (RestClientException e) {
            return getExampleTasks();
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

    public TaskDTO getTask(int id) {
        ResponseEntity<TaskDTO> response = restTemplate.exchange(baseUrl + "/" + id, HttpMethod.GET, null, TaskDTO.class);
        return response.getBody();
    }

    //Metodo para obtener tareas de ejemplo (Por si la Base de Datos no está disponible)
    private List<TaskDTO> getExampleTasks() {
        List<Need> needs = convertToNeedList(getExampleNeeds());
        List<Volunteer> volunteers = convertToVolunteerList(getExampleVolunteers());

        Task exampleTask = new Task(
                needs,
                "Tarea de ejemplo",
                "Tarea de ejemplo",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(3),
                Priority.LOW,
                Status.TO_DO,
                volunteers
        );

        // Convertir a DTO para devolver
        TaskDTO taskDTO = new TaskDTO(exampleTask);
        List<TaskDTO> exampleTasks = new ArrayList<>();
        exampleTasks.add(taskDTO);

        return exampleTasks;
    }

    //========================== VOLUNTARIOS =============================
    public List<VolunteerDTO> getVolunteers() {
        try {
            ResponseEntity<VolunteerDTO[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, VolunteerDTO[].class);
            VolunteerDTO[] volunteers = response.getBody();
            if (volunteers != null) {
                return List.of(volunteers);
            }
            return new ArrayList<>();
        } catch (RestClientException e) {
            return getExampleVolunteers();
        }
    }

    public void addVolunteer(VolunteerDTO volunteerDTO) {
        restTemplate.postForEntity(baseUrl, volunteerDTO, VolunteerDTO.class);
    }

    public void updateVolunteer(String id, VolunteerDTO volunteerDTO) {
        restTemplate.put(baseUrl + "/" + id, volunteerDTO);
    }

    public void deleteVolunteer(String id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    public VolunteerDTO getVolunteer(String id) {
        ResponseEntity<VolunteerDTO> response = restTemplate.exchange(baseUrl + "/" + id, HttpMethod.GET, null, VolunteerDTO.class);
        return response.getBody();
    }

    private List<VolunteerDTO> getExampleVolunteers() {
        List<VolunteerDTO> volunteerDTOs = new ArrayList<>();
        volunteerDTOs.add(new VolunteerDTO(new Volunteer("33", "Fernando", "Alonso", "alonso@astonmartin.com")));
        volunteerDTOs.add(new VolunteerDTO(new Volunteer("24", "Carlos", "Alvarez", "carlos@levante.com")));
        return volunteerDTOs;
    }

    //Metodo para convertir VolunteerDTO a Volunteer
    private List<Volunteer> convertToVolunteerList(List<VolunteerDTO> volunteerDTOs) {
        List<Volunteer> volunteers = new ArrayList<>();
        for (VolunteerDTO dto : volunteerDTOs) {
            volunteers.add(new Volunteer(
                    dto.getDni(),
                    dto.getFirstName(),
                    dto.getLastName(),
                    dto.getEmail()
            ));
        }
        return volunteers;
    }

    //========================== NECESIDADES =============================
    public List<NeedDTO> getNeeds() {
        try {
            ResponseEntity<NeedDTO[]> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, NeedDTO[].class);
            NeedDTO[] needs = response.getBody();
            if (needs != null) {
                return List.of(needs);
            }
            return new ArrayList<>();
        } catch (RestClientException e) {
            return getExampleNeeds();
        }
    }

    public void addNeed(NeedDTO needDTO) {
        restTemplate.postForEntity(baseUrl, needDTO, NeedDTO.class);
    }

    public void updateNeed(int id, NeedDTO needDTO) {
        restTemplate.put(baseUrl + "/" + id, needDTO);
    }

    public void deleteNeed(int id) {
        restTemplate.delete(baseUrl + "/" + id);
    }

    public NeedDTO getNeed(int id) {
        ResponseEntity<NeedDTO> response = restTemplate.exchange(baseUrl + "/" + id, HttpMethod.GET, null, NeedDTO.class);
        return response.getBody();
    }

    private List<NeedDTO> getExampleNeeds() {
        List<NeedDTO> needDTOs = new ArrayList<>();
        needDTOs.add(new NeedDTO(new Need("Material de construcción", UrgencyLevel.MODERATE, NeedType.BUILDING, null, null)));
        needDTOs.add(new NeedDTO(new Need("Alimentos no perecederos", UrgencyLevel.URGENT, NeedType.FEED, null, null)));
        return needDTOs;
    }

    //Metodo para convertir NeedDTO a Need
    private List<Need> convertToNeedList(List<NeedDTO> needDTOs) {
        List<Need> needs = new ArrayList<>();
        for (NeedDTO dto : needDTOs) {
            needs.add(new Need(
                    dto.getDescription(),
                    dto.getUrgency(),
                    dto.getNeedType(),
                    null,
                    null
            ));
        }
        return needs;
    }

}
