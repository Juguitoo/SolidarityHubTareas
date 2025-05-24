package solidarityhub.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import solidarityhub.backend.BackendApplication;
import solidarityhub.backend.config.TestConfig;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.dto.VolunteerDTO;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.backend.repository.VolunteerRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackages = "solidarityhub.backend")
@ContextConfiguration(classes = BackendApplication.class)
@Import(TestConfig.class)
public class VolunteerControllerTest {
    @Autowired
    private VolunteerController volunteerController;
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private VolunteerRepository volunteerRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testGetAllVolunteers() throws JsonProcessingException {
        Volunteer volunteer1 = new Volunteer("12345678A", "1", "1", "1", 1, "1", "1", List.of(), List.of());
        Volunteer volunteer2 = new Volunteer("23456789B", "2", "2", "2", 2, "2", "2", List.of(), List.of());
        Volunteer volunteer3 = new Volunteer("34567890C", "3", "3", "3", 3, "3", "3", List.of(), List.of());
        List<Volunteer> volunteers = List.of(volunteer1, volunteer2, volunteer3);
        List<Volunteer> volunteersSaved = volunteerRepository.saveAll(volunteers);
        entityManager.flush();
        List<VolunteerDTO> volunteerDTOs = new ArrayList<>();
        volunteersSaved.forEach(volunteer -> volunteerDTOs.add(new VolunteerDTO(volunteer)));

        TaskDTO taskDTO = new TaskDTO(0, "Test Task", "Description", LocalDateTime.now(), LocalDateTime.now(), null, null, null, null, null, null, null, null);
        String taskString = URLEncoder.encode(objectMapper.writeValueAsString(taskDTO), StandardCharsets.UTF_8);

        ResponseEntity<?> response = volunteerController.getVolunteers("none", taskString);
        assertNotNull(response);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<VolunteerDTO> responseBody = (List<VolunteerDTO>) response.getBody();
        assertEquals(volunteerDTOs.size(), responseBody.size());
        for (int i = 0; i < volunteerDTOs.size(); i++) {
            assertEquals(volunteerDTOs.get(i), responseBody.get(i));
        }
    }

    @Test
    void testGetVolunteersBySkill() throws JsonProcessingException {
        Volunteer volunteer1 = new Volunteer("12345678A", "1", "1", "1", 1, "1", "1", List.of(TaskType.POLICE), List.of());
        Volunteer volunteer2 = new Volunteer("23456789B", "2", "2", "2", 2, "2", "2", List.of(TaskType.POLICE), List.of());
        Volunteer volunteer3 = new Volunteer("34567890C", "3", "3", "3", 3, "3", "3", List.of(TaskType.OTHER), List.of());
        List<Volunteer> correctVolunteers = List.of(volunteer1, volunteer2);
        List<Volunteer> volunteersSaved = volunteerRepository.saveAll(correctVolunteers);
        volunteerRepository.save(volunteer3);
        entityManager.flush();

        List<VolunteerDTO> volunteerDTOs = new ArrayList<>();
        volunteersSaved.forEach(volunteer -> volunteerDTOs.add(new VolunteerDTO(volunteer)));

        TaskDTO taskDTO = new TaskDTO(0, "Test Task", "Description", LocalDateTime.now(), LocalDateTime.now(), TaskType.POLICE, null, null, null, null, null, null, null);
        String taskString = URLEncoder.encode(objectMapper.writeValueAsString(taskDTO), StandardCharsets.UTF_8);

        ResponseEntity<?> response = volunteerController.getVolunteers("habilidades", taskString);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<VolunteerDTO> responseBody = (List<VolunteerDTO>) response.getBody();
        assertEquals(volunteerDTOs.size(), responseBody.size());
        for (int i = 0; i < volunteerDTOs.size(); i++) {
            assertEquals(volunteerDTOs.get(i), responseBody.get(i));
        }
        assertFalse(responseBody.contains(new VolunteerDTO(volunteer3)));
    }

    @Test
    void testGetVolunteersByAvailability() throws JsonProcessingException {
        Volunteer volunteer1 = new Volunteer("12345678A", "1", "1", "1", 1, "1", "1", List.of(TaskType.POLICE), List.of());
        Volunteer volunteer2 = new Volunteer("23456789B", "2", "2", "2", 2, "2", "2", List.of(TaskType.POLICE), List.of());
        Volunteer volunteer3 = new Volunteer("34567890C", "3", "3", "3", 3, "3", "3", List.of(TaskType.OTHER), List.of());
        List<Volunteer> volunteers = List.of(volunteer1, volunteer2, volunteer3);
        List<Volunteer> volunteersSaved = volunteerRepository.saveAll(volunteers);
        entityManager.flush();

        LocalDateTime startDate = LocalDateTime.of(2025, 5, 5, 10, 0); // Monday at 10:00 AM
        LocalDateTime endDate = LocalDateTime.of(2025, 5, 5, 12, 0); // Monday at 12:00 PM

        List<Volunteer> orderedVolunteers = volunteersSaved.stream().sorted(Comparator.comparingInt((Volunteer v) ->
                        v.isAvailable(startDate, endDate)).reversed()).toList();

        List<VolunteerDTO> volunteerDTOs = new ArrayList<>();
        orderedVolunteers.forEach(volunteer -> volunteerDTOs.add(new VolunteerDTO(volunteer)));

        TaskDTO taskDTO = new TaskDTO(0, "Test Task", "Description", startDate, endDate, null, null, null, null, null, null, null, null);
        String taskString = URLEncoder.encode(objectMapper.writeValueAsString(taskDTO), StandardCharsets.UTF_8);

        ResponseEntity<?> response = volunteerController.getVolunteers("disponibilidad", taskString);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<VolunteerDTO> responseBody = (List<VolunteerDTO>) response.getBody();
        assertEquals(volunteerDTOs.size(), responseBody.size());
        for (int i = 0; i < volunteerDTOs.size(); i++) {
            assertEquals(volunteerDTOs.get(i), responseBody.get(i));
        }
    }

    @Test
    void testGetExitingVolunteer() {
        Volunteer volunteer1 = new Volunteer("12345678A", "1", "1", "1", 1, "1", "1", List.of(), List.of());
        Volunteer savedVolunteer = volunteerRepository.save(volunteer1);
        entityManager.flush();

        VolunteerDTO volunteerDTO = new VolunteerDTO(savedVolunteer);

        ResponseEntity<?> response = volunteerController.getVolunteer(savedVolunteer.getDni());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        VolunteerDTO responseBody = (VolunteerDTO) response.getBody();
        assertEquals(volunteerDTO, responseBody);
    }

    @Test
    void testGetNonExistingVolunteer() {
        String nonExistingVolunteerId = "99999999Z";
        ResponseEntity<?> response = volunteerController.getVolunteer(nonExistingVolunteerId);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCheckAvailability() {
        Volunteer volunteer1 = new Volunteer("12345678A", "1", "1", "1", 1, "1", "1", List.of(), List.of());
        Volunteer savedVolunteer = volunteerRepository.save(volunteer1);
        entityManager.flush();

        LocalDateTime startDate = LocalDateTime.of(2025, 5, 5, 10, 0); // Monday at 10:00 AM
        LocalDateTime endDate = LocalDateTime.of(2025, 5, 5, 12, 0); // Monday at 12:00 PM

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("volunteerId", "12345678A");
        requestParams.put("startDate", startDate.toString());
        requestParams.put("endDate", endDate.toString());

        ResponseEntity<Integer> response = volunteerController.checkAvailability(requestParams);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Integer availabilityScore = response.getBody();
        assertNotNull(availabilityScore);
        assertEquals(volunteer1.isAvailable(startDate, endDate), availabilityScore);
    }

    @Test
    void testCheckAvailability_MissingParameters() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("volunteerId", "12345678A");

        ResponseEntity<Integer> response = volunteerController.checkAvailability(requestParams);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCheckAvailability_VolunteerNotFound() {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("volunteerId", "NONEXISTENT");
        requestParams.put("startDate", LocalDateTime.now().toString());
        requestParams.put("endDate", LocalDateTime.now().plusHours(2).toString());

        ResponseEntity<Integer> response = volunteerController.checkAvailability(requestParams);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
