package solidarityhub.backend.controller;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import solidarityhub.backend.BackendApplication;
import solidarityhub.backend.config.TestConfig;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.*;
import solidarityhub.backend.model.enums.*;
import solidarityhub.backend.repository.CatastropheRepository;
import solidarityhub.backend.repository.NeedRepository;
import solidarityhub.backend.repository.TaskRepository;
import solidarityhub.backend.repository.AffectedRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackages = "solidarityhub.backend")
@ContextConfiguration(classes = BackendApplication.class)
@Import(TestConfig.class)
public class TaskControllerTest {

    @Autowired
    private TaskController taskController;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private NeedRepository needRepository;
    @Autowired
    private CatastropheRepository catastropheRepository;
    @Autowired
    private AffectedRepository affectedRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void testGetAllTasks() {
        Affected affected = new Affected("12345678A", "Juan", "Pérez", "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe1 = new Catastrophe("Test Catastrophe", "Test Description", new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        Catastrophe catastrophe2 = new Catastrophe("Test Catastrophe 2", "Test Description 2", new GPSCoordinates(1.0, 1.0), LocalDate.now().plusDays(1), EmergencyLevel.MEDIUM);
        catastropheRepository.saveAll(List.of(catastrophe1, catastrophe2));
        entityManager.flush();
        Need need1 = new Need(affected, "Test Need", UrgencyLevel.LOW, TaskType.PSYCHOLOGICAL, null, catastrophe1);
        Need need2 = new Need(affected, "Test Need 2", UrgencyLevel.MODERATE, TaskType.PSYCHOLOGICAL, null, catastrophe2);
        needRepository.saveAll(List.of(need1, need2));

        Task task1 = new Task(List.of(need1), "Test Task", "Test Description", LocalDateTime.now(), LocalDateTime.now().plusDays(1), Priority.LOW, EmergencyLevel.HIGH, Status.TO_DO, List.of(), "Micasa", catastrophe1);
        Task task2 = new Task(List.of(need2), "Test Task 2", "Test Description 2", LocalDateTime.now(), LocalDateTime.now().plusDays(2), Priority.LOW, EmergencyLevel.MEDIUM, Status.IN_PROGRESS, List.of(), "MiTrabajo", catastrophe2);
        List<Task> tasks = List.of(task1, task2);
        List<Task> savedTasks = taskRepository.saveAll(tasks);
        entityManager.flush();

        List<TaskDTO> savedTaskDTOs = savedTasks.stream().map(TaskDTO::new).toList();

        ResponseEntity<?> response = taskController.getTasks("", "", "", "", null);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<TaskDTO> taskDTOs = (List<TaskDTO>) response.getBody();
        assertTrue(taskDTOs.containsAll(savedTaskDTOs));
    }

    @Test
    void testGetTasksByCatastrophe() {
        Affected affected = new Affected("12345678A", "Juan", "Pérez", "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe1 = new Catastrophe("Test Catastrophe", "Test Description", new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        Catastrophe catastrophe2 = new Catastrophe("Test Catastrophe 2", "Test Description 2", new GPSCoordinates(1.0, 1.0), LocalDate.now().plusDays(1), EmergencyLevel.MEDIUM);
        catastropheRepository.saveAll(List.of(catastrophe1, catastrophe2));
        entityManager.flush();
        Need need1 = new Need(affected, "Test Need", UrgencyLevel.LOW, TaskType.PSYCHOLOGICAL, null, catastrophe1);
        Need need2 = new Need(affected, "Test Need 2", UrgencyLevel.MODERATE, TaskType.PSYCHOLOGICAL, null, catastrophe2);
        Need need3 = new Need(affected, "Test Need 3", UrgencyLevel.URGENT, TaskType.PSYCHOLOGICAL, null, catastrophe1);
        needRepository.saveAll(List.of(need1, need2));

        Task task1 = new Task(List.of(need1), "Test Task", "Test Description", LocalDateTime.now(), LocalDateTime.now().plusDays(1), Priority.LOW, EmergencyLevel.HIGH, Status.TO_DO, List.of(), "Micasa", catastrophe1);
        Task task2 = new Task(List.of(need2), "Test Task 2", "Test Description 2", LocalDateTime.now(), LocalDateTime.now().plusDays(2), Priority.LOW, EmergencyLevel.MEDIUM, Status.IN_PROGRESS, List.of(), "MiTrabajo", catastrophe2);
        Task task3 = new Task(List.of(need3), "Test Task 3", "Test Description 3", LocalDateTime.now(), LocalDateTime.now().plusDays(3), Priority.URGENT, EmergencyLevel.HIGH, Status.FINISHED, List.of(), "MiCasa2", catastrophe1);
        List<Task> tasksCatastrophe = List.of(task1, task3);
        List<Task> savedTasks = taskRepository.saveAll(tasksCatastrophe);
        Task anotherSavedTask = taskRepository.save(task2);
        entityManager.flush();

        List<TaskDTO> savedTaskDTOs = savedTasks.stream().map(TaskDTO::new).toList();

        ResponseEntity<?> response = taskController.getTasks("", "", "", "", catastrophe1.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<TaskDTO> taskDTOs = (List<TaskDTO>) response.getBody();
        assertTrue(taskDTOs.containsAll(savedTaskDTOs));
        assertFalse(taskDTOs.contains(anotherSavedTask));
    }

    @Test
    void testGetTask_ExistingId() {
        Affected affected = new Affected("12345678A", "Juan", "Pérez", "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe1 = new Catastrophe("Test Catastrophe", "Test Description", new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.saveAll(List.of(catastrophe1));
        entityManager.flush();
        Need need1 = new Need(affected, "Test Need", UrgencyLevel.LOW, TaskType.PSYCHOLOGICAL, null, catastrophe1);
        needRepository.saveAll(List.of(need1));

        Task task1 = new Task(List.of(need1), "Test Task", "Test Description", LocalDateTime.now(), LocalDateTime.now().plusDays(1), Priority.LOW, EmergencyLevel.HIGH, Status.TO_DO, List.of(), "Micasa", catastrophe1);
        Task savedTask = taskRepository.save(task1);
        entityManager.flush();

        TaskDTO taskDTO = new TaskDTO(savedTask);

        ResponseEntity<?> response = taskController.getTask(savedTask.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        TaskDTO responseBody = (TaskDTO) response.getBody();
        assertEquals(taskDTO, responseBody);
    }

    @Test
    void testGetTask_NonExistingId() {
        ResponseEntity<?> response = taskController.getTask(9999);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testAddTask_Success() {
        Affected affected = new Affected("12345678A", "Juan", "Pérez", "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe1 = new Catastrophe("Test Catastrophe", "Test Description", new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.saveAll(List.of(catastrophe1));
        entityManager.flush();

        Need need1 = new Need(affected, "Test Need", UrgencyLevel.LOW, TaskType.PSYCHOLOGICAL, null, catastrophe1);
        needRepository.saveAll(List.of(need1));

        Task task1 = new Task(List.of(need1), "Test Task", "Test Description", LocalDateTime.now(), LocalDateTime.now().plusDays(1), Priority.LOW, EmergencyLevel.HIGH, Status.TO_DO, List.of(), "Micasa", catastrophe1);
        TaskDTO taskDTO = new TaskDTO(task1);
        ResponseEntity<?> response = taskController.addTask(taskDTO);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        TaskDTO responseBody = (TaskDTO) response.getBody();
        assertNotNull(responseBody);
        assertEquals(taskDTO.getName(), responseBody.getName());
        assertEquals(taskDTO.getDescription(), responseBody.getDescription());
        assertEquals(taskDTO.getStartTimeDate(), responseBody.getStartTimeDate());
        assertEquals(taskDTO.getEstimatedEndTimeDate(), responseBody.getEstimatedEndTimeDate());
        assertEquals(taskDTO.getPriority(), responseBody.getPriority());
        assertEquals(taskDTO.getEmergencyLevel(), responseBody.getEmergencyLevel());
        assertEquals(taskDTO.getCatastropheId(), responseBody.getCatastropheId());
    }

    @Test
    void testAddTask_NullCatastrophe() {
        Affected affected = new Affected("12345678A", "Juan", "Pérez", "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe1 = new Catastrophe("Test Catastrophe", "Test Description", new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.saveAll(List.of(catastrophe1));
        entityManager.flush();

        Need need1 = new Need(affected, "Test Need", UrgencyLevel.LOW, TaskType.PSYCHOLOGICAL, null, catastrophe1);
        needRepository.saveAll(List.of(need1));

        Task task1 = new Task(List.of(need1), "Test Task", "Test Description", LocalDateTime.now(), LocalDateTime.now().plusDays(1), Priority.LOW, EmergencyLevel.HIGH, Status.TO_DO, List.of(), "Micasa", null);
        TaskDTO taskDTO = new TaskDTO(task1);

        ResponseEntity<?> response = taskController.addTask(taskDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("La catástrofe especificada no existe", response.getBody());
    }

    @Test
    void testAddTask_InvalidCatastrophe(){
        Affected affected = new Affected("12345678A", "Juan", "Pérez", "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe1 = new Catastrophe("Test Catastrophe", "Test Description", new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.saveAll(List.of(catastrophe1));
        entityManager.flush();

        Need need1 = new Need(affected, "Test Need", UrgencyLevel.LOW, TaskType.PSYCHOLOGICAL, null, catastrophe1);
        needRepository.saveAll(List.of(need1));

        Task task1 = new Task(List.of(need1), "Test Task", "Test Description", LocalDateTime.now(), LocalDateTime.now().plusDays(1), Priority.LOW, EmergencyLevel.HIGH, Status.TO_DO, List.of(), "Micasa", catastrophe1);
        TaskDTO taskDTO = new TaskDTO(task1);
        taskDTO.setCatastropheId(99);

        ResponseEntity<?> response = taskController.addTask(taskDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("La catástrofe especificada no existe", response.getBody());
    }

    @Test
    void testAddTask_NoNeeds(){
        Affected affected = new Affected("12345678A", "Juan", "Pérez", "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe1 = new Catastrophe("Test Catastrophe", "Test Description", new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.saveAll(List.of(catastrophe1));
        entityManager.flush();

        Task task1 = new Task(List.of(), "Test Task", "Test Description", LocalDateTime.now(), LocalDateTime.now().plusDays(1), Priority.LOW, EmergencyLevel.HIGH, Status.TO_DO, List.of(), "Micasa", catastrophe1);
        TaskDTO taskDTO = new TaskDTO(task1);
        ResponseEntity<?> response = taskController.addTask(taskDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Se debe seleccionar al menos una necesidad", response.getBody());
    }

    @Test
    void testUpdateTask_Success() {
        Affected affected = new Affected("12345678A", "Juan", "Pérez", "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe1 = new Catastrophe("Test Catastrophe", "Test Description", new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.saveAll(List.of(catastrophe1));
        entityManager.flush();

        Need need1 = new Need(affected, "Test Need", UrgencyLevel.LOW, TaskType.PSYCHOLOGICAL, null, catastrophe1);
        needRepository.saveAll(List.of(need1));

        Task task1 = new Task(List.of(need1), "Test Task", "Test Description", LocalDateTime.now(), LocalDateTime.now().plusDays(1), Priority.LOW, EmergencyLevel.HIGH, Status.TO_DO, List.of(), "Micasa", catastrophe1);
        Task savedTask = taskRepository.save(task1);
        entityManager.flush();

        TaskDTO taskDTO = new TaskDTO(savedTask);
        taskDTO.setName("Updated Task Name");
        taskDTO.setDescription("Updated Task Description");

        ResponseEntity<?> response = taskController.updateTask(savedTask.getId(), taskDTO);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        TaskDTO responseBody = (TaskDTO) response.getBody();
        assertNotNull(responseBody);
        assertEquals("Updated Task Name", responseBody.getName());
        assertEquals("Updated Task Description", responseBody.getDescription());
    }

    @Test
    void testUpdateTask_NonExistingId() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setName("Updated Task Name");
        taskDTO.setDescription("Updated Task Description");

        ResponseEntity<?> response = taskController.updateTask(9999, taskDTO);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testDeleteTask_Success() {
        Affected affected = new Affected("12345678A", "Juan", "Pérez", "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe1 = new Catastrophe("Test Catastrophe", "Test Description", new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.saveAll(List.of(catastrophe1));
        entityManager.flush();

        Need need1 = new Need(affected, "Test Need", UrgencyLevel.LOW, TaskType.PSYCHOLOGICAL, null, catastrophe1);
        needRepository.saveAll(List.of(need1));

        Task task1 = new Task(List.of(need1), "Test Task", "Test Description", LocalDateTime.now(), LocalDateTime.now().plusDays(1), Priority.LOW, EmergencyLevel.HIGH, Status.TO_DO, List.of(), "Micasa", catastrophe1);
        Task savedTask = taskRepository.save(task1);
        entityManager.flush();

        ResponseEntity<?> response = taskController.deleteTask(savedTask.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(taskRepository.existsById(savedTask.getId()));
    }

    @Test
    void testDeleteTask_NonExistingId() {
        ResponseEntity<?> response = taskController.deleteTask(9999);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetSuggestedTasks() {
        Affected affected = new Affected("12345678A", "Juan", "Pérez",
                "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe1 = new Catastrophe("Test Catastrophe", "Test Description",
                new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.saveAll(List.of(catastrophe1));
        entityManager.flush();

        Need need1 = new Need(affected, "Test Need", UrgencyLevel.LOW, TaskType.PSYCHOLOGICAL, null, catastrophe1);
        Need need2 = new Need(affected, "Test Need 2", UrgencyLevel.MODERATE, TaskType.PSYCHOLOGICAL, null, catastrophe1);
        Need need3 = new Need(affected, "Test Need 3", UrgencyLevel.URGENT, TaskType.PSYCHOLOGICAL, null, catastrophe1);
        needRepository.saveAll(List.of(need1, need2, need3));

        ResponseEntity<?> response = taskController.getSuggestedTasks(catastrophe1.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<TaskDTO> taskDTOs = (List<TaskDTO>) response.getBody();
        assertNotNull(taskDTOs);
        assertFalse(taskDTOs.isEmpty());
        assertEquals(3, taskDTOs.size());
    }

    @Test
    void testGetSuggestedTasks_NonExistingCatastrophe() {
        ResponseEntity<?> response = taskController.getSuggestedTasks(9999);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Se debe seleccionar al menos una necesidad", response.getBody());
    }
}
