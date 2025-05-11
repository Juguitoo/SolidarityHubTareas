package solidarityhub.backend.dto;

import org.junit.jupiter.api.Test;
import solidarityhub.backend.model.*;
import solidarityhub.backend.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NeedDTOTest {

    @Test
    void testNeedDTOConstructor() {
        // Arrange
        Need need = createTestNeed();

        // Set up a task for this need
        Task task = createTestTask();
        need.setTask(task);

        // Act
        NeedDTO dto = new NeedDTO(need);

        // Assert
        assertEquals(1, dto.getId());
        assertEquals("Test Need", dto.getDescription());
        assertEquals(UrgencyLevel.MODERATE, dto.getUrgency());
        assertEquals(TaskType.LOGISTICS, dto.getTaskType());
        assertNotNull(dto.getLocation());
        assertEquals(40.416775, dto.getLocation().getLatitude());
        assertEquals(-3.703790, dto.getLocation().getLongitude());
        assertEquals(1, dto.getTaskId());
        assertEquals(1, dto.getCatastropheId());
    }

    @Test
    void testNeedDTOConstructor_WithoutTask() {
        // Arrange
        Need need = createTestNeed();
        need.setTask(null);

        // Act
        NeedDTO dto = new NeedDTO(need);

        // Assert
        assertEquals(1, dto.getId());
        assertEquals("Test Need", dto.getDescription());
        assertEquals(UrgencyLevel.MODERATE, dto.getUrgency());
        assertEquals(TaskType.LOGISTICS, dto.getTaskType());
        assertNotNull(dto.getLocation());
        assertEquals(-1, dto.getTaskId()); // -1 indica que no hay task
        assertEquals(1, dto.getCatastropheId());
    }

    @Test
    void testNeedDTONoArgsConstructor() {
        // Act
        NeedDTO dto = new NeedDTO();

        // Assert
        assertNotNull(dto);
        // No podemos verificar valores específicos ya que todos los campos son null
    }

    // Helper methods
    private Need createTestNeed() {
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

        Catastrophe catastrophe = new Catastrophe(
                "Test Catastrophe",
                "Test Description",
                coordinates,
                LocalDate.now(),
                EmergencyLevel.MEDIUM
        );

        // Set catastrophe ID
        try {
            java.lang.reflect.Field idField = Catastrophe.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(catastrophe, 1);
        } catch (Exception e) {
            fail("Failed to set catastrophe ID: " + e.getMessage());
        }

        Need need = new Need(
                affected,
                "Test Need",
                UrgencyLevel.MODERATE,
                TaskType.LOGISTICS,
                coordinates,
                catastrophe
        );

        // Set need ID
        try {
            java.lang.reflect.Field idField = Need.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(need, 1);
        } catch (Exception e) {
            fail("Failed to set need ID: " + e.getMessage());
        }

        return need;
    }

    private Task createTestTask() {
        List<Need> needs = new ArrayList<>();
        List<Volunteer> volunteers = new ArrayList<>();

        Task task = new Task();

        // Set ID directly since we're not using the constructor with needs
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
            task.setVolunteers(volunteers);
            task.setNeeds(needs);
            task.setType(TaskType.LOGISTICS);
        } catch (Exception e) {
            fail("Failed to set task fields: " + e.getMessage());
        }

        return task;
    }
}