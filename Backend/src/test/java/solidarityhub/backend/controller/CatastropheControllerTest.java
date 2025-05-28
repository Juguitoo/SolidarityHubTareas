package solidarityhub.backend.controller;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import solidarityhub.backend.BackendApplication;
import solidarityhub.backend.config.TestConfig;
import solidarityhub.backend.dto.CatastropheDTO;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.repository.CatastropheRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackages = "solidarityhub.backend")
@ContextConfiguration(classes = BackendApplication.class)
@Import(TestConfig.class)
public class CatastropheControllerTest {

    @Autowired
    private CatastropheController catastropheController;
    @Autowired
    private CatastropheRepository catastropheRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    public void testGetCatastrophes() {
        Catastrophe catastrophe1 = new Catastrophe("catastrophe1", "description1", new GPSCoordinates(12.34, 56.78), LocalDate.now(), EmergencyLevel.MEDIUM);
        Catastrophe catastrophe2 = new Catastrophe("catastrophe2", "description2", new GPSCoordinates(23.45, 67.89), LocalDate.now(), EmergencyLevel.HIGH);
        List<Catastrophe> catastropheList = List.of(catastrophe1, catastrophe2);
        List<Catastrophe> savedCatastrophes = catastropheRepository.saveAll(catastropheList);
        entityManager.flush();

        List<CatastropheDTO> savedDTOs = savedCatastrophes.stream().map(CatastropheDTO::new).toList();

        ResponseEntity<?> response = catastropheController.getCatastrophes();
        assertNotNull(response);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<CatastropheDTO> responseBody = (List<CatastropheDTO>) response.getBody();
        for( CatastropheDTO catastropheDTO : responseBody) {
            assertInstanceOf(CatastropheDTO.class, catastropheDTO);
            assertTrue(savedDTOs.contains(catastropheDTO));
        }
    }

    @Test
    public void testGetCatastrophe_ExistingId() {
        Catastrophe catastrophe = new Catastrophe("catastrophe1", "description1", new GPSCoordinates(12.34, 56.78), LocalDate.now(), EmergencyLevel.MEDIUM);
        Catastrophe savedCatastrophe = catastropheRepository.save(catastrophe);
        entityManager.flush();

        ResponseEntity<?> response = catastropheController.getCatastrophe(savedCatastrophe.getId());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Catastrophe);
        Catastrophe responseBody = (Catastrophe) response.getBody();
        assertEquals(savedCatastrophe, responseBody);
    }

    @Test
    void testGetCatastrophe_NonExistingId() {
        int nonExistingId = 999;
        ResponseEntity<?> response = catastropheController.getCatastrophe(nonExistingId);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testAddCatastrophe() {
        Catastrophe catastrophe = new Catastrophe("New Catastrophe", "description1", new GPSCoordinates(12.34, 56.78), LocalDate.now(), EmergencyLevel.MEDIUM);
        CatastropheDTO catastropheDTO = new CatastropheDTO(catastrophe);

        ResponseEntity<?> response = catastropheController.addCatastrophe(catastropheDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Catastrophe);

        Catastrophe savedCatastrophe = (Catastrophe) response.getBody();
        assertEquals(catastrophe.getName(), savedCatastrophe.getName());
        assertEquals(catastrophe.getDescription(), savedCatastrophe.getDescription());
        assertEquals(catastrophe.getLocation().getLongitude(), savedCatastrophe.getLocation().getLongitude());
        assertEquals(catastrophe.getLocation().getLatitude(), savedCatastrophe.getLocation().getLatitude());
        assertEquals(catastrophe.getStartDate(), savedCatastrophe.getStartDate());
        assertEquals(catastrophe.getEmergencyLevel(), savedCatastrophe.getEmergencyLevel());
    }

    @Test
    void testUpdateCatastrophe_Success() {
        Catastrophe catastrophe = new Catastrophe("catastrophe1", "description1", new GPSCoordinates(12.34, 56.78), LocalDate.now(), EmergencyLevel.MEDIUM);
        Catastrophe savedCatastrophe = catastropheRepository.save(catastrophe);
        entityManager.flush();

        CatastropheDTO updatedCatastropheDTO = new CatastropheDTO(savedCatastrophe);
        updatedCatastropheDTO.setName("Updated Catastrophe");
        updatedCatastropheDTO.setDescription("Updated Description");

        ResponseEntity<?> response = catastropheController.updateCatastrophe(savedCatastrophe.getId(), updatedCatastropheDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Catastrophe);

        Catastrophe updatedCatastrophe = (Catastrophe) response.getBody();
        assertEquals(savedCatastrophe.getId(), updatedCatastrophe.getId());
        assertEquals("Updated Catastrophe", updatedCatastrophe.getName());
        assertEquals("Updated Description", updatedCatastrophe.getDescription());
    }

    @Test
    void testUpdateCatastrophe_NonExistingId() {
        int nonExistingId = 999;
        CatastropheDTO catastropheDTO = new CatastropheDTO();
        catastropheDTO.setName("Updated Catastrophe");
        catastropheDTO.setDescription("Updated Description");

        ResponseEntity<?> response = catastropheController.updateCatastrophe(nonExistingId, catastropheDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteCatastrophe_ExistingId() {
        Catastrophe catastrophe = new Catastrophe("catastrophe1", "description1", new GPSCoordinates(12.34, 56.78), LocalDate.now(), EmergencyLevel.MEDIUM);
        Catastrophe savedCatastrophe = catastropheRepository.save(catastrophe);
        entityManager.flush();

        ResponseEntity<?> response = catastropheController.deleteCatastrophe(savedCatastrophe.getId());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify that the catastrophe was deleted
        assertFalse(catastropheRepository.existsById(savedCatastrophe.getId()));
    }

    @Test
    void testDeleteCatastrophe_NonExistingId() {
        int nonExistingId = 999;
        ResponseEntity<?> response = catastropheController.deleteCatastrophe(nonExistingId);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
