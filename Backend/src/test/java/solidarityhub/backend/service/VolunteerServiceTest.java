package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import solidarityhub.backend.dto.TaskDTO;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.ScheduleAvailability;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.DayMoment;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.backend.model.enums.WeekDay;
import solidarityhub.backend.model.strategy.DistanceStrategy;
import solidarityhub.backend.model.strategy.NoFilterStrategy;
import solidarityhub.backend.model.strategy.SkillStrategy;
import solidarityhub.backend.model.strategy.VolunteerAssigner;
import solidarityhub.backend.repository.VolunteerRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class VolunteerServiceTest {

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private CoordinatesService coordinatesService;

    @Spy
    private VolunteerAssigner volunteerAssigner = new VolunteerAssigner(new NoFilterStrategy());

    @InjectMocks
    private VolunteerService volunteerService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Replace the volunteerAssigner in the service with our spied instance using reflection
        java.lang.reflect.Field volunteerAssignerField = VolunteerService.class.getDeclaredField("volunteerAssigner");
        volunteerAssignerField.setAccessible(true);
        volunteerAssignerField.set(volunteerService, volunteerAssigner);
    }

    @Test
    void testSave() {
        // Arrange
        Volunteer volunteer = createTestVolunteer("12345678A", "John");
        when(volunteerRepository.save(any(Volunteer.class))).thenReturn(volunteer);

        // Act
        Volunteer savedVolunteer = volunteerService.save(volunteer);

        // Assert
        assertNotNull(savedVolunteer);
        assertEquals("12345678A", savedVolunteer.getDni());
        assertEquals("John", savedVolunteer.getFirstName());
        verify(volunteerRepository, times(1)).save(volunteer);
    }

    @Test
    void testGetVolunteer_ExistingId() {
        // Arrange
        String volunteerId = "12345678A";
        Volunteer volunteer = createTestVolunteer(volunteerId, "John");
        when(volunteerRepository.findById(volunteerId)).thenReturn(Optional.of(volunteer));

        // Act
        Volunteer result = volunteerService.getVolunteer(volunteerId);

        // Assert
        assertNotNull(result);
        assertEquals(volunteerId, result.getDni());
        assertEquals("John", result.getFirstName());
        verify(volunteerRepository, times(1)).findById(volunteerId);
    }

    @Test
    void testGetVolunteer_NonExistingId() {
        // Arrange
        String nonExistingId = "NONEXISTENT";
        when(volunteerRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Volunteer result = volunteerService.getVolunteer(nonExistingId);

        // Assert
        assertNull(result);
        verify(volunteerRepository, times(1)).findById(nonExistingId);
    }

    @Test
    void testGetAllVolunteers() {
        // Arrange
        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(createTestVolunteer("12345678A", "John"));
        volunteers.add(createTestVolunteer("87654321B", "Jane"));
        when(volunteerRepository.findAll()).thenReturn(volunteers);

        // For the coordinates service mock
        Map<String, Double> coordinates = new HashMap<>();
        coordinates.put("lat", 40.4);
        coordinates.put("lon", -3.7);
        when(coordinatesService.getCoordinates(anyString())).thenReturn(coordinates);

        // Act
        List<Volunteer> result = volunteerService.getAllVolunteers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("12345678A", result.get(0).getDni());
        assertEquals("87654321B", result.get(1).getDni());
        verify(volunteerRepository, times(1)).findAll();
    }

    @Test
    void testGetAllVolunteers_WithExistingCoordinates() {
        // Arrange
        Volunteer volunteer = createTestVolunteer("12345678A", "John");
        volunteer.setLocation(new GPSCoordinates(40.4, -3.7));

        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(volunteer);
        when(volunteerRepository.findAll()).thenReturn(volunteers);

        // Act
        List<Volunteer> result = volunteerService.getAllVolunteers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("12345678A", result.get(0).getDni());
        assertNotNull(result.get(0).getLocation());
        assertEquals(40.4, result.get(0).getLocation().getLatitude());
        assertEquals(-3.7, result.get(0).getLocation().getLongitude());
        verify(volunteerRepository, times(1)).findAll();
        // Should not call coordinatesService since the volunteer already has coordinates
        verify(coordinatesService, never()).getCoordinates(anyString());
    }

    @Test
    void testGetVolunteersByStrategy_DisponibilidadStrategy() {
        // Arrange
        String strategy = "disponibilidad";
        TaskDTO taskDTO = createTestTaskDTO();
        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(createTestVolunteer("12345678A", "John"));

        when(volunteerRepository.findAll()).thenReturn(volunteers);

        // For the coordinates service mock
        Map<String, Double> coordinates = new HashMap<>();
        coordinates.put("lat", 40.4);
        coordinates.put("lon", -3.7);
        when(coordinatesService.getCoordinates(anyString())).thenReturn(coordinates);

        // Act
        List<Volunteer> result = volunteerService.getVolunteersByStrategy(strategy, taskDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(volunteerAssigner, times(1)).setStrategy(any());
        verify(volunteerAssigner, times(1)).assignVolunteers(any(), any());
    }

    @Test
    void testGetVolunteersByStrategy_HabilidadesStrategy() {
        // Arrange
        String strategy = "habilidades";
        TaskDTO taskDTO = createTestTaskDTO();
        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(createTestVolunteer("12345678A", "John"));

        when(volunteerRepository.findAll()).thenReturn(volunteers);

        // For the coordinates service mock
        Map<String, Double> coordinates = new HashMap<>();
        coordinates.put("lat", 40.4);
        coordinates.put("lon", -3.7);
        when(coordinatesService.getCoordinates(anyString())).thenReturn(coordinates);

        // Act
        List<Volunteer> result = volunteerService.getVolunteersByStrategy(strategy, taskDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(volunteerAssigner, times(1)).setStrategy(any());
        verify(volunteerAssigner, times(1)).assignVolunteers(any(), any());
    }

    @Test
    void testGetVolunteersByStrategy_DistanciaStrategy() {
        // Arrange
        String strategy = "distancia";
        TaskDTO taskDTO = createTestTaskDTO();
        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(createTestVolunteer("12345678A", "John"));

        when(volunteerRepository.findAll()).thenReturn(volunteers);

        // For the coordinates service mock
        Map<String, Double> coordinates = new HashMap<>();
        coordinates.put("lat", 40.4);
        coordinates.put("lon", -3.7);
        when(coordinatesService.getCoordinates(anyString())).thenReturn(coordinates);

        // Act
        List<Volunteer> result = volunteerService.getVolunteersByStrategy(strategy, taskDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(volunteerAssigner, times(1)).setStrategy(any());
        verify(volunteerAssigner, times(1)).assignVolunteers(any(), any());
    }

    @Test
    void testGetVolunteersByStrategy_DefaultStrategy() {
        // Arrange
        String strategy = "unknown";
        TaskDTO taskDTO = createTestTaskDTO();
        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(createTestVolunteer("12345678A", "John"));

        when(volunteerRepository.findAll()).thenReturn(volunteers);

        // For the coordinates service mock
        Map<String, Double> coordinates = new HashMap<>();
        coordinates.put("lat", 40.4);
        coordinates.put("lon", -3.7);
        when(coordinatesService.getCoordinates(anyString())).thenReturn(coordinates);

        // Act
        List<Volunteer> result = volunteerService.getVolunteersByStrategy(strategy, taskDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(volunteerAssigner, times(1)).setStrategy(any());
        verify(volunteerAssigner, times(1)).assignVolunteers(any(), any());
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
            needsField.set(taskDTO, new ArrayList<>());

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