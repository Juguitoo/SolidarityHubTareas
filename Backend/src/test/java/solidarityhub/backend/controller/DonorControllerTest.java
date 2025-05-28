package solidarityhub.backend.controller;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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
import solidarityhub.backend.dto.DonorDTO;
import solidarityhub.backend.model.Donor;
import solidarityhub.backend.repository.DonorRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackages = "solidarityhub.backend")
@ContextConfiguration(classes = BackendApplication.class)
@Import(TestConfig.class)
public class DonorControllerTest {

    @Autowired
    private DonorController donorController;
    @Autowired
    private DonorRepository donorRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void deleteDataBase() {
        donorRepository.deleteAll();
        entityManager.flush();
    }

    @Test
    void testGetAllDonors() {
        Donor donor1 = new Donor("12345678A", "D-1");
        Donor donor2 = new Donor("87654321B", "D-2");
        List<Donor> donors = List.of(donor1, donor2);
        List<Donor> donorsSaved = donorRepository.saveAll(donors);
        entityManager.flush();

        List<DonorDTO> donorDTOs = donorsSaved.stream()
                .map(DonorDTO::new)
                .toList();

        ResponseEntity<?> response = donorController.getAllDonors();
        assertNotNull(response);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<DonorDTO> responseBody = (List<DonorDTO>) response.getBody();
        for (DonorDTO donorDTO : responseBody) {
            assertInstanceOf(DonorDTO.class, donorDTO);
            assertTrue(donorDTOs.contains(donorDTO));
        }
    }

    @Test
    void testGetDonorByDni() {
        Donor donor1 = new Donor("12345678A", "D-1");
        Donor donor2 = new Donor("87654321B", "D-2");
        List<Donor> donors = List.of(donor1, donor2);
        List<Donor> donorsSaved = donorRepository.saveAll(donors);
        entityManager.flush();

        DonorDTO donorDTO = new DonorDTO(donorsSaved.get(0));
        ResponseEntity<?> response = donorController.getDonorByDni(donor1.getDni());
        assertNotNull(response);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        DonorDTO responseBody = (DonorDTO) response.getBody();
        assertEquals(donorDTO, responseBody);
    }

    @Test
    void testGetDonorByDni_NonExistingDni() {
        // Arrange
        String nonExistingDni = "99999999Z";

        // Act
        ResponseEntity<?> response = donorController.getDonorByDni(nonExistingDni);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateDonor_Success() {
        Donor donor = new Donor("12345678A", "New Donor");
        DonorDTO donorDTO = new DonorDTO(donor);

        ResponseEntity<?> response = donorController.createDonor(donorDTO);
        assertNotNull(response);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        DonorDTO responseBody = (DonorDTO) response.getBody();
        assertEquals(new DonorDTO(donorRepository.findById(donorDTO.getDni()).get()), responseBody);
    }

    @Test
    void testCreateDonor_ExistingDni() {
        // Arrange
        Donor donor = new Donor("12345678A", "Existing Donor");
        donorRepository.save(donor);
        entityManager.flush();
        DonorDTO donorDTO = new DonorDTO(donor);

        // Act
        ResponseEntity<?> response = donorController.createDonor(donorDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testUpdateDonor_Success() {
        // Arrange
        Donor donor = new Donor("12345678A", "Old Donor");
        donorRepository.save(donor);
        entityManager.flush();
        DonorDTO donorDTO = new DonorDTO(donor);
        donorDTO.setName("Updated Donor");

        // Act
        ResponseEntity<?> response = donorController.updateDonor(donorDTO.getDni(),donorDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        DonorDTO responseBody = (DonorDTO) response.getBody();
        assertEquals("Updated Donor", responseBody.getName());
    }

    @Test
    void testUpdateDonor_NonExistingDni() {
        // Arrange
        String nonExistingDni = "99999999Z";
        DonorDTO donorDTO = new DonorDTO();
        donorDTO.setDni(nonExistingDni);
        donorDTO.setName("Non Existing Donor");

        // Act
        ResponseEntity<?> response = donorController.updateDonor(nonExistingDni, donorDTO);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteDonor_Success() {
        // Arrange
        Donor donor = new Donor("12345678A", "Donor to Delete");
        donorRepository.save(donor);
        entityManager.flush();

        // Act
        ResponseEntity<?> response = donorController.deleteDonor(donor.getDni());

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(donorRepository.existsById(donor.getDni()));
    }
}
