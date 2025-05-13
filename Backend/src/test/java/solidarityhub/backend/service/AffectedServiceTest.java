package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import solidarityhub.backend.model.Affected;
import solidarityhub.backend.repository.AffectedRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AffectedServiceTest {

    @Mock
    private AffectedRepository affectedRepository;

    @InjectMocks
    private AffectedService affectedService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        // Arrange
        Affected affected = createTestAffected("12345678A");
        when(affectedRepository.save(any(Affected.class))).thenReturn(affected);

        // Act
        Affected savedAffected = affectedService.save(affected);

        // Assert
        assertNotNull(savedAffected);
        assertEquals("12345678A", savedAffected.getDni());
        assertEquals("John", savedAffected.getFirstName());
        assertEquals("Doe", savedAffected.getLastName());
        verify(affectedRepository, times(1)).save(affected);
    }

    @Test
    void testGetAffectedById_ExistingId() {
        // Arrange
        String id = "12345678A";
        Affected affected = createTestAffected(id);
        when(affectedRepository.findById(id)).thenReturn(Optional.of(affected));

        // Act
        Affected result = affectedService.getAffectedById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getDni());
        assertEquals("John", result.getFirstName());
        verify(affectedRepository, times(1)).findById(id);
    }

    @Test
    void testGetAffectedById_NonExistingId_ShouldThrowException() {
        // Arrange
        String nonExistingId = "99999999Z";
        when(affectedRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> {
            affectedService.getAffectedById(nonExistingId);
        });
        verify(affectedRepository, times(1)).findById(nonExistingId);
    }

    // Helper method
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
}