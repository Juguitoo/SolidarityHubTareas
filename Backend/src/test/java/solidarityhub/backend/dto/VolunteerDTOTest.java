package solidarityhub.backend.dto;

import org.junit.jupiter.api.Test;
import solidarityhub.backend.model.ScheduleAvailability;
import solidarityhub.backend.model.Task;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.DayMoment;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.backend.model.enums.WeekDay;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VolunteerDTOTest {

    @Test
    void testVolunteerDTOConstructor() {
        // Arrange
        Volunteer volunteer = createTestVolunteer();
        Task task = createTestTask();

        List<Task> tasks = new ArrayList<>();
        tasks.add(task);
        volunteer.setTasks(tasks);

        // Act
        VolunteerDTO dto = new VolunteerDTO(volunteer);

        // Assert
        assertEquals("V-1", dto.getDni());
        assertEquals("Jane", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("jane.doe@example.com", dto.getEmail());

        assertNotNull(dto.getTasks());
        assertEquals(1, dto.getTasks().size());
        assertEquals(1, dto.getTasks().get(0));

        assertNotNull(dto.getTaskTypes());
        assertEquals(2, dto.getTaskTypes().size());
        assertTrue(dto.getTaskTypes().contains(TaskType.MEDICAL));
        assertTrue(dto.getTaskTypes().contains(TaskType.LOGISTICS));

        assertEquals(0, dto.getAvailabilityStatus());
    }

    @Test
    void testVolunteerDTOConstructor_NoTasks() {
        // Arrange
        Volunteer volunteer = createTestVolunteer();
        volunteer.setTasks(new ArrayList<>());

        // Act
        VolunteerDTO dto = new VolunteerDTO(volunteer);

        // Assert
        assertEquals("V-1", dto.getDni());
        assertNotNull(dto.getTasks());
        assertTrue(dto.getTasks().isEmpty());
    }

    @Test
    void testVolunteerDTONoArgsConstructor() {
        // Act
        VolunteerDTO dto = new VolunteerDTO();

        // Assert
        assertNotNull(dto);
    }

    @Test
    void testVolunteerDTOSetters() {
        // Arrange
        VolunteerDTO dto = new VolunteerDTO();
        List<Integer> tasks = new ArrayList<>();
        tasks.add(1);
        List<TaskType> taskTypes = new ArrayList<>();
        taskTypes.add(TaskType.MEDICAL);

        // Act
        dto.setDni("V-2");
        dto.setFirstName("John");
        dto.setLastName("Smith");
        dto.setEmail("john.smith@example.com");
        dto.setTasks(tasks);
        dto.setTaskTypes(taskTypes);
        dto.setAvailabilityStatus(2);

        // Assert
        assertEquals("V-2", dto.getDni());
        assertEquals("John", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
        assertEquals("john.smith@example.com", dto.getEmail());
        assertSame(tasks, dto.getTasks());
        assertSame(taskTypes, dto.getTaskTypes());
        assertEquals(2, dto.getAvailabilityStatus());
    }

    // Helper methods
    private Volunteer createTestVolunteer() {
        List<TaskType> taskTypes = new ArrayList<>();
        taskTypes.add(TaskType.MEDICAL);
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

    private Task createTestTask() {
        Task task = new Task();

        try {
            java.lang.reflect.Field idField = Task.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(task, 1);
        } catch (Exception e) {
            fail("Failed to set task ID: " + e.getMessage());
        }

        return task;
    }
}