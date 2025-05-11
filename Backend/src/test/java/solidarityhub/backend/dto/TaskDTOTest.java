package solidarityhub.backend.dto;

import org.junit.jupiter.api.Test;
import solidarityhub.backend.model.*;
import solidarityhub.backend.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskDTOTest {

    @Test
    void testTaskDTOConstructor_CompleteTask() {
        // Arrange
        Task task = createTestTask();
        Catastrophe catastrophe = createTestCatastrophe();
        Need need = createTestNeed(catastrophe);
        Volunteer volunteer = createTestVolunteer();

        List<Need> needs = new ArrayList<>();
        needs.add(need);
        task.setNeeds(needs);

        List<Volunteer> volunteers = new ArrayList<>();
        volunteers.add(volunteer);
        task.setVolunteers(volunteers);

        task.setCatastrophe(catastrophe);
        task.setMeetingDirection("Meeting Point");

        // Act
        TaskDTO dto = new TaskDTO(task);

        // Assert
        assertEquals(1, dto.getId());
        assertEquals("Test Task", dto.getName());
        assertEquals("Test Description", dto.getDescription());
        assertNotNull(dto.getStartTimeDate());
        assertNotNull(dto.getEstimatedEndTimeDate());
        assertEquals(TaskType.LOGISTICS, dto.getType());
        assertEquals(Priority.MODERATE, dto.getPriority());
        assertEquals(EmergencyLevel.MEDIUM, dto.getEmergencyLevel());
        assertEquals(Status.TO_DO, dto.getStatus());

        assertNotNull(dto.getNeeds());
        assertEquals(1, dto.getNeeds().size());

        assertNotNull(dto.getVolunteers());
        assertEquals(1, dto.getVolunteers().size());

        assertEquals(Integer.valueOf(1), dto.getCatastropheId());
        assertEquals("Meeting Point", dto.getMeetingDirection());
    }

    @Test
    void testTaskDTOConstructor_WithoutCatastrophe() {
        // Arrange
        Task task = createTestTask();
        task.setCatastrophe(null);

        // Act
        TaskDTO dto = new TaskDTO(task);

        // Assert
        assertEquals(1, dto.getId());
        assertNull(dto.getCatastropheId());
    }

    @Test
    void testTaskDTONoArgsConstructor() {
        // Act
        TaskDTO dto = new TaskDTO();

        // Assert
        assertNotNull(dto);
    }

    @Test
    void testTaskDTOJsonCreatorConstructor() {
        // Arrange
        int id = 1;
        String name = "JSON Task";
        String description = "JSON Description";
        LocalDateTime startTimeDate = LocalDateTime.now();
        LocalDateTime estimatedEndTimeDate = LocalDateTime.now().plusDays(5);
        TaskType type = TaskType.MEDICAL;
        Priority priority = Priority.URGENT;
        EmergencyLevel emergencyLevel = EmergencyLevel.HIGH;
        Status status = Status.IN_PROGRESS;
        List<NeedDTO> needs = new ArrayList<>();
        List<VolunteerDTO> volunteers = new ArrayList<>();
        Integer catastropheId = 2;
        String meetingDirection = "JSON Meeting Point";

        // Act
        TaskDTO dto = new TaskDTO(
                id, name, description, startTimeDate, estimatedEndTimeDate,
                type, priority, emergencyLevel, status, needs, volunteers,
                catastropheId, meetingDirection
        );

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(description, dto.getDescription());
        assertEquals(startTimeDate, dto.getStartTimeDate());
        assertEquals(estimatedEndTimeDate, dto.getEstimatedEndTimeDate());
        assertEquals(type, dto.getType());
        assertEquals(priority, dto.getPriority());
        assertEquals(emergencyLevel, dto.getEmergencyLevel());
        assertEquals(status, dto.getStatus());
        assertSame(needs, dto.getNeeds());
        assertSame(volunteers, dto.getVolunteers());
        assertEquals(catastropheId, dto.getCatastropheId());
        assertEquals(meetingDirection, dto.getMeetingDirection());
    }

    @Test
    void testTaskDTOJsonCreatorConstructor_NullLists() {
        // Arrange & Act
        TaskDTO dto = new TaskDTO(
                1, "Task", "Description", LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                TaskType.LOGISTICS, Priority.MODERATE, EmergencyLevel.MEDIUM, Status.TO_DO,
                null, null, 1, "Meeting Point"
        );

        // Assert
        assertNotNull(dto.getNeeds());
        assertTrue(dto.getNeeds().isEmpty());

        assertNotNull(dto.getVolunteers());
        assertTrue(dto.getVolunteers().isEmpty());
    }

    @Test
    void testTaskDTOSetters() {
        // Arrange
        TaskDTO dto = new TaskDTO();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(5);
        List<NeedDTO> needs = new ArrayList<>();
        List<VolunteerDTO> volunteers = new ArrayList<>();

        // Act
        dto.setId(1);
        dto.setName("Test Task");
        dto.setDescription("Test Description");
        dto.setStartTimeDate(startDate);
        dto.setEstimatedEndTimeDate(endDate);
        dto.setType(TaskType.LOGISTICS);
        dto.setPriority(Priority.MODERATE);
        dto.setEmergencyLevel(EmergencyLevel.MEDIUM);
        dto.setStatus(Status.TO_DO);
        dto.setNeeds(needs);
        dto.setVolunteers(volunteers);
        dto.setCatastropheId(1);
        dto.setMeetingDirection("Meeting Point");

        // Assert
        assertEquals(1, dto.getId());
        assertEquals("Test Task", dto.getName());
        assertEquals("Test Description", dto.getDescription());
        assertEquals(startDate, dto.getStartTimeDate());
        assertEquals(endDate, dto.getEstimatedEndTimeDate());
        assertEquals(TaskType.LOGISTICS, dto.getType());
        assertEquals(Priority.MODERATE, dto.getPriority());
        assertEquals(EmergencyLevel.MEDIUM, dto.getEmergencyLevel());
        assertEquals(Status.TO_DO, dto.getStatus());
        assertSame(needs, dto.getNeeds());
        assertSame(volunteers, dto.getVolunteers());
        assertEquals(Integer.valueOf(1), dto.getCatastropheId());
        assertEquals("Meeting Point", dto.getMeetingDirection());
    }

    // Helper methods
    private Task createTestTask() {
        Task task = new Task();

        try {
            java.lang.reflect.Field idField = Task.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(task, 1);

            // Set other required fields
            task.setTaskName("Test Task");
            task.setTaskDescription("Test Description");
            task.setStartTimeDate(LocalDateTime.now());
            task.setEstimatedEndTimeDate(LocalDateTime.now().plusDays(5));
            task.setPriority(Priority.MODERATE);
            task.setEmergencyLevel(EmergencyLevel.MEDIUM);
            task.setStatus(Status.TO_DO);
            task.setType(TaskType.LOGISTICS);
            task.setNeeds(new ArrayList<>());
            task.setVolunteers(new ArrayList<>());
        } catch (Exception e) {
            fail("Failed to set task fields: " + e.getMessage());
        }

        return task;
    }

    private Catastrophe createTestCatastrophe() {
        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Catastrophe catastrophe = new Catastrophe(
                "Test Catastrophe",
                "Test Description",
                coordinates,
                LocalDate.now(),
                EmergencyLevel.MEDIUM
        );

        try {
            java.lang.reflect.Field idField = Catastrophe.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(catastrophe, 1);
        } catch (Exception e) {
            fail("Failed to set catastrophe ID: " + e.getMessage());
        }

        return catastrophe;
    }

    private Need createTestNeed(Catastrophe catastrophe) {
        Affected affected = new Affected(
                "A-1",
                "John",
                "Doe",
                "john.doe@example.com",
                123456789,
                "123 Main St",
                "password",
                false
        );

        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);

        Need need = new Need(
                affected,
                "Test Need",
                UrgencyLevel.MODERATE,
                TaskType.LOGISTICS,
                coordinates,
                catastrophe
        );

        try {
            java.lang.reflect.Field idField = Need.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(need, 1);
        } catch (Exception e) {
            fail("Failed to set need ID: " + e.getMessage());
        }

        return need;
    }

    private Volunteer createTestVolunteer() {
        List<TaskType> taskTypes = new ArrayList<>();
        taskTypes.add(TaskType.LOGISTICS);

        List<ScheduleAvailability> scheduleAvailabilities = new ArrayList<>();
        ScheduleAvailability availability = new ScheduleAvailability(DayMoment.MORNING, WeekDay.MONDAY);
        scheduleAvailabilities.add(availability);

        Volunteer volunteer = new Volunteer(
                "V-1",
                "Jane",
                "Doe",
                "jane.doe@example.com",
                987654321,
                "456 Other St",
                "password",
                taskTypes,
                scheduleAvailabilities
        );

        for (ScheduleAvailability schedule : scheduleAvailabilities) {
            schedule.setVolunteer(volunteer);
        }

        return volunteer;
    }
}