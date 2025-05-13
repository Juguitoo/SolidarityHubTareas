package solidarityhub.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import solidarityhub.backend.dto.NeedDTO;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.dto.VolunteerDTO;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.ScheduleAvailability;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.DayMoment;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.backend.model.enums.WeekDay;
import solidarityhub.backend.service.VolunteerService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VolunteerControllerTest {

    @Mock
    private VolunteerService volunteerService;

    @InjectMocks
    private VolunteerController volunteerController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testGetVolunteers() throws JsonProcessingException {
        // Arrange
        String strategy = "disponibilidad";
        TaskDTO taskDTO = createTestTaskDTO();
        String taskString = URLEncoder.encode(objectMapper.writeValueAsString(taskDTO), StandardCharsets.UTF_8);

        List<Volunteer> volunteers = new ArrayList<>();
        Volunteer volunteer1 = createTestVolunteer("V-1", "Alice");
        Volunteer volunteer2 = createTestVolunteer("V-2", "Bob");
        volunteers.add(volunteer1);
        volunteers.add(volunteer2);

        // El problema estaba aquí: debemos asegurarnos de que el mock retorne los voluntarios
        // independientemente de los parámetros exactos que se pasen al método
        when(volunteerService.getVolunteersByStrategy(anyString(), any(TaskDTO.class))).thenReturn(volunteers);

        // Act
        ResponseEntity<?> response = volunteerController.getVolunteers(strategy, taskString);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size(), "La respuesta debería contener 2 voluntarios");
        assertTrue(responseBody.get(0) instanceof VolunteerDTO);
        verify(volunteerService, times(1)).getVolunteersByStrategy(anyString(), any(TaskDTO.class));
    }

    @Test
    void testGetVolunteer_ExistingId() {
        // Arrange
        String volunteerId = "V-1";
        Volunteer volunteer = createTestVolunteer(volunteerId, "Alice");
        when(volunteerService.getVolunteer(volunteerId)).thenReturn(volunteer);

        // Act
        ResponseEntity<?> response = volunteerController.getVolunteer(volunteerId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof VolunteerDTO);
        VolunteerDTO responseVolunteer = (VolunteerDTO) response.getBody();
        assertEquals(volunteerId, responseVolunteer.getDni());
        assertEquals("Alice", responseVolunteer.getFirstName());
        verify(volunteerService, times(1)).getVolunteer(volunteerId);
    }

    @Test
    void testCheckAvailability_Available() {
        // Arrange
        String volunteerId = "V-1";
        LocalDateTime startDate = LocalDateTime.of(2025, 5, 5, 10, 0); // Monday at 10:00 AM
        LocalDateTime endDate = LocalDateTime.of(2025, 5, 5, 12, 0); // Monday at 12:00 PM

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("volunteerId", volunteerId);
        requestParams.put("startDate", startDate.toString());
        requestParams.put("endDate", endDate.toString());

        Volunteer volunteer = createTestVolunteer(volunteerId, "Alice");
        when(volunteerService.getVolunteer(volunteerId)).thenReturn(volunteer);

        // Act
        ResponseEntity<Integer> response = volunteerController.checkAvailability(requestParams);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(volunteerService, times(1)).getVolunteer(volunteerId);
    }

    @Test
    void testCheckAvailability_MissingParameters() {
        // Arrange
        Map<String, Object> requestParams = new HashMap<>();
        // Missing volunteerId
        requestParams.put("startDate", LocalDateTime.now().toString());
        requestParams.put("endDate", LocalDateTime.now().plusHours(2).toString());

        // Act
        ResponseEntity<Integer> response = volunteerController.checkAvailability(requestParams);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(volunteerService, never()).getVolunteer(anyString());
    }

    @Test
    void testCheckAvailability_VolunteerNotFound() {
        // Arrange
        String nonExistingId = "NONEXISTENT";
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusHours(2);

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("volunteerId", nonExistingId);
        requestParams.put("startDate", startDate.toString());
        requestParams.put("endDate", endDate.toString());

        when(volunteerService.getVolunteer(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<Integer> response = volunteerController.checkAvailability(requestParams);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(volunteerService, times(1)).getVolunteer(nonExistingId);
    }

    // Helper methods
    private Volunteer createTestVolunteer(String dni, String firstName) {
        List<TaskType> taskTypes = new ArrayList<>();
        taskTypes.add(TaskType.MEDICAL);
        taskTypes.add(TaskType.LOGISTICS);

        List<ScheduleAvailability> scheduleAvailabilities = new ArrayList<>();
        ScheduleAvailability mondayMorning = new ScheduleAvailability(DayMoment.MORNING, WeekDay.MONDAY);
        ScheduleAvailability tuesdayAfternoon = new ScheduleAvailability(DayMoment.AFTERNOON, WeekDay.TUESDAY);
        scheduleAvailabilities.add(mondayMorning);
        scheduleAvailabilities.add(tuesdayAfternoon);

        Volunteer volunteer = new Volunteer(
                dni,
                firstName,
                "LastName",
                firstName.toLowerCase() + "@example.com",
                123456789,
                "123 Test St",
                "password",
                taskTypes,
                scheduleAvailabilities
        );

        volunteer.setLocation(new GPSCoordinates(40.416775, -3.703790));

        // Set the volunteer reference in each schedule
        for (ScheduleAvailability schedule : scheduleAvailabilities) {
            schedule.setVolunteer(volunteer);
        }

        return volunteer;
    }

    private TaskDTO createTestTaskDTO() {
        TaskDTO taskDTO = new TaskDTO();

        try {
            java.lang.reflect.Field idField = TaskDTO.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(taskDTO, 1);

            java.lang.reflect.Field nameField = TaskDTO.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(taskDTO, "Test Task");

            java.lang.reflect.Field descriptionField = TaskDTO.class.getDeclaredField("description");
            descriptionField.setAccessible(true);
            descriptionField.set(taskDTO, "Test Description");

            java.lang.reflect.Field startTimeDateField = TaskDTO.class.getDeclaredField("startTimeDate");
            startTimeDateField.setAccessible(true);
            startTimeDateField.set(taskDTO, LocalDateTime.of(2025, 5, 5, 10, 0)); // Monday at 10:00 AM

            java.lang.reflect.Field estimatedEndTimeDateField = TaskDTO.class.getDeclaredField("estimatedEndTimeDate");
            estimatedEndTimeDateField.setAccessible(true);
            estimatedEndTimeDateField.set(taskDTO, LocalDateTime.of(2025, 5, 5, 12, 0)); // Monday at 12:00 PM

            java.lang.reflect.Field typeField = TaskDTO.class.getDeclaredField("type");
            typeField.setAccessible(true);
            typeField.set(taskDTO, TaskType.MEDICAL);

            // Initialize needs list to avoid NullPointerException in Volunteer.getDistance
            java.lang.reflect.Field needsField = TaskDTO.class.getDeclaredField("needs");
            needsField.setAccessible(true);
            List<NeedDTO> needs = new ArrayList<>();
            NeedDTO needDTO = new NeedDTO();
            needDTO.setLocation(new GPSCoordinates(40.4, -3.7));
            needs.add(needDTO);
            needsField.set(taskDTO, needs);

            // Initialize volunteers list
            java.lang.reflect.Field volunteersField = TaskDTO.class.getDeclaredField("volunteers");
            volunteersField.setAccessible(true);
            volunteersField.set(taskDTO, new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return taskDTO;
    }
}