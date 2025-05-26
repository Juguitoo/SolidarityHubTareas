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
import solidarityhub.backend.dto.NeedDTO;
import solidarityhub.backend.model.*;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.backend.model.enums.UrgencyLevel;
import solidarityhub.backend.repository.AffectedRepository;
import solidarityhub.backend.repository.CatastropheRepository;
import solidarityhub.backend.repository.NeedRepository;
import solidarityhub.backend.repository.TaskRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackages = "solidarityhub.backend")
@ContextConfiguration(classes = BackendApplication.class)
@Import(TestConfig.class)
public class NeedControllerTest {
    @Autowired
    private NeedController needController;
    @Autowired
    private NeedRepository needRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CatastropheRepository catastropheRepository;
    @Autowired
    private AffectedRepository affectedRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void testGetAllNeeds() {
        Affected affected = new Affected("12345678A", "Juan", "Pérez", "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe = new Catastrophe("Test Catastrophe", "Test Description", new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.save(catastrophe);
        entityManager.flush();
        Need need1 = new Need(affected, "Test Need", UrgencyLevel.LOW, TaskType.PSYCHOLOGICAL, null, catastrophe);
        Need need2 = new Need(affected, "Test Need 2", UrgencyLevel.MODERATE, TaskType.PSYCHOLOGICAL, null, catastrophe);
        Need need3 = new Need(affected, "Test Need 3", UrgencyLevel.URGENT, TaskType.PSYCHOLOGICAL, null, catastrophe);
        List<Need> needs = List.of(need1, need2, need3);
        List<Need> savedNeeds = needRepository.saveAll(needs);
        entityManager.flush();

        List<NeedDTO> needDTOs = savedNeeds.stream().map(NeedDTO::new).toList();

        ResponseEntity<?> response = needController.getAllNeeds(catastrophe.getId());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<NeedDTO> responseBody = (List<NeedDTO>) response.getBody();
        assertTrue(responseBody.containsAll(needDTOs));
    }

    @Test
    void testGetNeedsWithoutTask() {
        Affected affected = new Affected("12345678A", "Juan", "Pérez", "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe = new Catastrophe("Test Catastrophe", "Test Description", new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.save(catastrophe);

        Task task1 = new Task();
        taskRepository.save(task1);

        entityManager.flush();
        Need need1 = new Need(affected, "Test Need", UrgencyLevel.LOW, TaskType.PSYCHOLOGICAL, null, catastrophe);
        need1.setTask(task1);
        Need need2 = new Need(affected, "Test Need 2", UrgencyLevel.MODERATE, TaskType.PSYCHOLOGICAL, null, catastrophe);
        Need need3 = new Need(affected, "Test Need 3", UrgencyLevel.URGENT, TaskType.PSYCHOLOGICAL, null, catastrophe);
        List<Need> needsWithoutTask = List.of(need2, need3);
        Need withTask = needRepository.save(need1);
        List<Need> savedNeedsWithoutTask = needRepository.saveAll(needsWithoutTask);
        entityManager.flush();

        List<NeedDTO> needDTOs = savedNeedsWithoutTask.stream().map(NeedDTO::new).toList();
        NeedDTO withTaskDTO = new NeedDTO(withTask);

        ResponseEntity<?> response = needController.getNeedsWithoutTask(catastrophe.getId());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<NeedDTO> responseBody = (List<NeedDTO>) response.getBody();
        assertTrue(responseBody.containsAll(needDTOs));
        assertFalse(responseBody.contains(withTaskDTO));
    }

    @Test
    void testGetNeedsWithoutTaskCount() {
        Affected affected = new Affected("12345678A", "Juan", "Pérez", "juan.perez@example.com", 987654321, "Calle Falsa 123", "password123", false);
        affectedRepository.save(affected);

        Catastrophe catastrophe = new Catastrophe("Test Catastrophe", "Test Description", new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.save(catastrophe);

        Task task1 = new Task();
        taskRepository.save(task1);

        entityManager.flush();
        Need need1 = new Need(affected, "Test Need", UrgencyLevel.LOW, TaskType.PSYCHOLOGICAL, null, catastrophe);
        need1.setTask(task1);
        Need need2 = new Need(affected, "Test Need 2", UrgencyLevel.MODERATE, TaskType.PSYCHOLOGICAL, null, catastrophe);
        Need need3 = new Need(affected, "Test Need 3", UrgencyLevel.URGENT, TaskType.PSYCHOLOGICAL, null, catastrophe);
        List<Need> needsWithoutTask = List.of(need2, need3);
        Need withTask = needRepository.save(need1);
        List<Need> savedNeedsWithoutTask = needRepository.saveAll(needsWithoutTask);
        entityManager.flush();

        ResponseEntity<?> response = needController.getNeedWithoutTaskCount(catastrophe.getId());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Integer);
        Integer responseBody = (Integer) response.getBody();
        assertEquals(savedNeedsWithoutTask.size(), responseBody.intValue());
    }
}
