package solidarityhub.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import solidarityhub.backend.dto.NeedDTO;
import solidarityhub.backend.model.Affected;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.Need;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.backend.model.enums.UrgencyLevel;
import solidarityhub.backend.service.NeedService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NeedControllerTest {

    @Mock
    private NeedService needService;

    @InjectMocks
    private NeedController needController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllNeeds() {
        // Arrange
        Integer catastropheId = 1;
        List<Need> needs = new ArrayList<>();
        needs.add(createTestNeed(1, catastropheId));
        needs.add(createTestNeed(2, catastropheId));
        when(needService.getAllNeeds(catastropheId)).thenReturn(needs);

        // Act
        ResponseEntity<?> response = needController.getAllNeeds(catastropheId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        assertTrue(responseBody.get(0) instanceof NeedDTO);
        verify(needService, times(1)).getAllNeeds(catastropheId);
    }

    @Test
    void testGetNeedsWithoutTask() {
        // Arrange
        Integer catastropheId = 1;
        List<Need> needsWithoutTask = new ArrayList<>();
        needsWithoutTask.add(createTestNeed(1, catastropheId));
        needsWithoutTask.add(createTestNeed(2, catastropheId));
        when(needService.getNeedsWithoutTask(catastropheId)).thenReturn(needsWithoutTask);

        // Act
        ResponseEntity<?> response = needController.getNeedsWithoutTask(catastropheId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        assertTrue(responseBody.get(0) instanceof NeedDTO);
        verify(needService, times(1)).getNeedsWithoutTask(catastropheId);
    }

    @Test
    void testGetNeedWithoutTaskCount() {
        // Arrange
        Integer catastropheId = 1;
        int count = 5;
        when(needService.getNeedWithoutTaskCount(catastropheId)).thenReturn(count);

        // Act
        ResponseEntity<?> response = needController.getNeedWithoutTaskCount(catastropheId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(count, response.getBody());
        verify(needService, times(1)).getNeedWithoutTaskCount(catastropheId);
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
        Affected affected = new Affected(
                dni,
                "John",
                "Doe",
                "john.doe@example.com",
                123456789,
                "123 Main St",
                "password",
                false
        );

        return affected;
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