package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.repository.CatastropheRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CatastropheServiceTest {

    @Mock
    private CatastropheRepository catastropheRepository;

    @InjectMocks
    private CatastropheService catastropheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        // Arrange
        Catastrophe catastrophe = createTestCatastrophe(1);
        when(catastropheRepository.save(any(Catastrophe.class))).thenReturn(catastrophe);

        // Act
        Catastrophe savedCatastrophe = catastropheService.save(catastrophe);

        // Assert
        assertNotNull(savedCatastrophe);
        assertEquals(1, savedCatastrophe.getId());
        assertEquals("Test Catastrophe 1", savedCatastrophe.getName());
        verify(catastropheRepository, times(1)).save(catastrophe);
    }

    @Test
    void testGetAllCatastrophes() {
        // Arrange
        List<Catastrophe> catastrophes = new ArrayList<>();
        catastrophes.add(createTestCatastrophe(1));
        catastrophes.add(createTestCatastrophe(2));
        when(catastropheRepository.findAll()).thenReturn(catastrophes);

        // Act
        List<Catastrophe> result = catastropheService.getAllCatastrophes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Catastrophe 1", result.get(0).getName());
        assertEquals("Test Catastrophe 2", result.get(1).getName());
        verify(catastropheRepository, times(1)).findAll();
    }

    @Test
    void testGetCatastrophe_ExistingId() {
        // Arrange
        Catastrophe catastrophe = createTestCatastrophe(1);
        when(catastropheRepository.findById(1)).thenReturn(Optional.of(catastrophe));

        // Act
        Catastrophe result = catastropheService.getCatastrophe(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Catastrophe 1", result.getName());
        verify(catastropheRepository, times(1)).findById(1);
    }

    @Test
    void testGetCatastrophe_NonExistingId() {
        // Arrange
        when(catastropheRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        Catastrophe result = catastropheService.getCatastrophe(999);

        // Assert
        assertNull(result);
        verify(catastropheRepository, times(1)).findById(999);
    }

    @Test
    void testDeleteCatastrophe() {
        // Arrange
        doNothing().when(catastropheRepository).deleteById(any(Integer.class));

        // Act
        catastropheService.deleteCatastrophe(1);

        // Assert
        verify(catastropheRepository, times(1)).deleteById(1);
    }

    // Helper method to create test catastrophes
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