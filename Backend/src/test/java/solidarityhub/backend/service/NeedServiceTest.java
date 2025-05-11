package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import solidarityhub.backend.model.Affected;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.backend.model.enums.UrgencyLevel;
import solidarityhub.backend.repository.NeedRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class NeedServiceTest {

    @Mock
    private NeedRepository needRepository;

    @InjectMocks
    private NeedService needService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        // Arrange
        Need need = createTestNeed(1, 1);
        when(needRepository.save(any(Need.class))).thenReturn(need);

        // Act
        Need savedNeed = needService.save(need);

        // Assert
        assertNotNull(savedNeed);
        assertEquals(1, savedNeed.getId());
        assertEquals("Test Need 1", savedNeed.getDescription());
        assertEquals(UrgencyLevel.MODERATE, savedNeed.getUrgency());
        verify(needRepository, times(1)).save(need);
    }

    @Test
    void testFindNeed_ExistingId() {
        // Arrange
        Integer id = 1;
        Need need = createTestNeed(id, 1);
        when(needRepository.findById(id)).thenReturn(Optional.of(need));

        // Act
        Need result = needService.findNeed(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Need 1", result.getDescription());
        verify(needRepository, times(1)).findById(id);
    }

    @Test
    void testFindNeed_NonExistingId_ShouldThrowException() {
        // Arrange
        Integer nonExistingId = 999;
        when(needRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> {
            needService.findNeed(nonExistingId);
        });
        verify(needRepository, times(1)).findById(nonExistingId);
    }

    @Test
    void testGetAllNeeds() {
        // Arrange
        Integer catastropheId = 1;
        List<Need> needs = new ArrayList<>();
        needs.add(createTestNeed(1, catastropheId));
        needs.add(createTestNeed(2, catastropheId));
        when(needRepository.getAllNeeds(catastropheId)).thenReturn(needs);

        // Act
        List<Need> result = needService.getAllNeeds(catastropheId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        verify(needRepository, times(1)).getAllNeeds(catastropheId);
    }

    @Test
    void testGetNeedsWithoutTask() {
        // Arrange
        Integer catastropheId = 1;
        List<Need> needsWithoutTask = new ArrayList<>();
        needsWithoutTask.add(createTestNeed(1, catastropheId));
        needsWithoutTask.add(createTestNeed(2, catastropheId));
        when(needRepository.getNeedsWithoutTask(catastropheId)).thenReturn(needsWithoutTask);

        // Act
        List<Need> result = needService.getNeedsWithoutTask(catastropheId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        verify(needRepository, times(1)).getNeedsWithoutTask(catastropheId);
    }

    @Test
    void testGetNeedWithoutTaskCount() {
        // Arrange
        Integer catastropheId = 1;
        int count = 5;
        when(needRepository.getNeedWithoutTaskCount(catastropheId)).thenReturn(count);

        // Act
        int result = needService.getNeedWithoutTaskCount(catastropheId);

        // Assert
        assertEquals(count, result);
        verify(needRepository, times(1)).getNeedWithoutTaskCount(catastropheId);
    }

    // Helper methods
    private Need createTestNeed(int id, int catastropheId) {
        Affected affected = createTestAffected("A-" + id);
        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Catastrophe catastrophe = createTestCatastrophe(catastropheId);

        Need need = new Need(
                affected,
                "Test Need " + id,
                UrgencyLevel.MODERATE,
                TaskType.LOGISTICS,
                coordinates,
                catastrophe
        );

        // Set ID using reflection since there's no setter
        try {
            java.lang.reflect.Field idField = Need.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(need, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set task to null to simulate a need without task
        need.setTask(null);
        need.setStartTimeDate(LocalDateTime.now());

        return need;
    }

    private Affected createTestAffected(String dni) {
        return new Affected(
                dni,
                "John",
                "Doe",
                "john.doe@example.com",
                123456789,
                "123 Main St",
                "password",
                false
        );
    }

    private Catastrophe createTestCatastrophe(int id) {
        GPSCoordinates coordinates = new GPSCoordinates(40.416775, -3.703790);
        Catastrophe catastrophe = new Catastrophe(
                "Test Catastrophe " + id,
                "Test Description " + id,
                coordinates,
                LocalDate.now(),
                EmergencyLevel.MEDIUM
        );

        // Set ID using reflection since there's no setter
        try {
            java.lang.reflect.Field idField = Catastrophe.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(catastrophe, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return catastrophe;
    }
}