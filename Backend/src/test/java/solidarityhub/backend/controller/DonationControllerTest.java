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
import solidarityhub.backend.dto.DonationDTO;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.Donation;
import solidarityhub.backend.model.Donor;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.enums.DonationStatus;
import solidarityhub.backend.model.enums.DonationType;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.repository.CatastropheRepository;
import solidarityhub.backend.repository.DonationRepository;
import solidarityhub.backend.repository.DonorRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackages = "solidarityhub.backend")
@ContextConfiguration(classes = BackendApplication.class)
@Import(TestConfig.class)
public class DonationControllerTest {

    @Autowired
    private DonationController donationController;
    @Autowired
    private DonationRepository donationRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DonorRepository donorRepository;
    @Autowired
    private CatastropheRepository catastropheRepository;

    @Test
    void testGetAllDonations() {
        Donor donor = new Donor("12345678A", "Donor 1");
        donorRepository.save(donor);
        Donation donation1 = new Donation(DonationType.MATERIAL, "Test Donation 1", LocalDate.now(), DonationStatus.COMPLETED, donor, null, 0.0, "kg");
        Donation donation2 = new Donation(DonationType.MATERIAL, "Test Donation 2", LocalDate.now(), DonationStatus.IN_PROGRESS, donor, null, 0.0, "kg");
        List<Donation> donations = List.of(donation1, donation2);
        List<Donation> savedDonations = donationRepository.saveAll(donations);
        entityManager.flush();

        List<DonationDTO> savedDonationDTOs = savedDonations.stream().map(DonationDTO::new).toList();

        ResponseEntity<?> response = donationController.getDonations(null, null, null, null, null);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<DonationDTO> donationDTOs = (List<DonationDTO>) response.getBody();
        assertNotNull(donationDTOs);
        for( DonationDTO donationDTO : donationDTOs) {
            assertInstanceOf(DonationDTO.class, donationDTO);
            assertTrue(savedDonationDTOs.contains(donationDTO));
        }
    }

    @Test
    void testGetDonationWithFilters() {
        Donor donor = new Donor("12345678A", "Donor 1");
        donorRepository.save(donor);
        Catastrophe catastrophe = new Catastrophe("Test Catastrophe", "Description",
                new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.save(catastrophe);

        Donation donation1 = new Donation(DonationType.SERVICE, "Test Donation 1", LocalDate.now(),
                DonationStatus.SCHEDULED, donor, catastrophe, 5.0, "kg");
        Donation donation2 = new Donation(DonationType.MATERIAL, "Test Donation 2", LocalDate.now(),
                DonationStatus.IN_PROGRESS, donor, null, 1.0, "kg");
        Donation donation3 = new Donation(DonationType.MATERIAL, "Test Donation 3", LocalDate.now(),
                DonationStatus.COMPLETED, donor, catastrophe, 10.0, "kg");
        Donation donation4 = new Donation(DonationType.MATERIAL, "Test Donation 4", LocalDate.now(),
                DonationStatus.COMPLETED, donor, catastrophe, 2.0, "kg");

        List<Donation> savedDonations = donationRepository.saveAll(List.of(donation1, donation2, donation3, donation4));
        entityManager.flush();

        List<Donation> donationsWithCatastrophe = List.of(savedDonations.get(0), savedDonations.get(2), savedDonations.get(3));
        Donation donationWithoutCatastrophe = savedDonations.get(1);

        List<Donation> donationsWithTypeM = List.of(savedDonations.get(3), savedDonations.get(2));
        Donation donationWithTypeS = savedDonations.get(0);

        List<Donation> donationsWithStatusCompleted = List.of(savedDonations.get(3), savedDonations.get(2));
        Donation donationWithStatusInProgress = savedDonations.get(0);

        List<DonationDTO> donationsWithCatastropheDTOs = donationsWithCatastrophe.stream().map(DonationDTO::new).toList();
        DonationDTO donationDTOWithoutCatastrophe = new DonationDTO(donationWithoutCatastrophe);

        List<DonationDTO> donationsWithTypeMDTOs = donationsWithTypeM.stream().map(DonationDTO::new).toList();
        DonationDTO donationWithTypeSDTO = new DonationDTO(donationWithTypeS);

        List<DonationDTO> donationsWithStatusCompletedDTOs = donationsWithStatusCompleted.stream().map(DonationDTO::new).toList();
        DonationDTO donationWithStatusInProgressDTO = new DonationDTO(donationWithStatusInProgress);

        ResponseEntity<?> responseCatastrophe = donationController.getDonations(null, null,
                null, null, catastrophe.getId());
        assertNotNull(responseCatastrophe);
        assertEquals(HttpStatus.OK, responseCatastrophe.getStatusCode());
        List<DonationDTO> donationCatastropheDTOs = (List<DonationDTO>) responseCatastrophe.getBody();
        assertNotNull(donationCatastropheDTOs);
        assertTrue(donationCatastropheDTOs.containsAll(donationsWithCatastropheDTOs));
        assertFalse(donationCatastropheDTOs.contains(donationDTOWithoutCatastrophe));

        ResponseEntity<?> responseType = donationController.getDonations(DonationType.MATERIAL.toString(), null,
                null, null, catastrophe.getId());
        assertNotNull(responseType);
        assertEquals(HttpStatus.OK, responseType.getStatusCode());
        List<DonationDTO> donationTypeDTOs = (List<DonationDTO>) responseType.getBody();
        assertNotNull(donationTypeDTOs);
        assertTrue(donationTypeDTOs.containsAll(donationsWithTypeMDTOs));
        assertFalse(donationTypeDTOs.contains(donationWithTypeSDTO));

        ResponseEntity<?> responseStatus = donationController.getDonations(null, DonationStatus.COMPLETED.toString(),
                null, null, catastrophe.getId());
        assertNotNull(responseStatus);
        assertEquals(HttpStatus.OK, responseStatus.getStatusCode());
        List<DonationDTO> donationStatusDTOs = (List<DonationDTO>) responseStatus.getBody();
        assertNotNull(donationStatusDTOs);
        assertTrue(donationStatusDTOs.containsAll(donationsWithStatusCompletedDTOs));
        assertFalse(donationStatusDTOs.contains(donationWithStatusInProgressDTO));
    }

    @Test
    void testGetDonation_ExistingId() {
        Donor donor = new Donor("12345678A", "Donor 1");
        donorRepository.save(donor);
        Donation donation = new Donation(DonationType.MATERIAL, "Test Donation", LocalDate.now(), DonationStatus.COMPLETED, donor, null, 0.0, "kg");
        Donation savedDonation = donationRepository.save(donation);
        entityManager.flush();

        ResponseEntity<?> response = donationController.getDonation(savedDonation.getId());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        DonationDTO responseBody = (DonationDTO) response.getBody();
        assertNotNull(responseBody);
        assertEquals(new DonationDTO(savedDonation), responseBody);
    }

    @Test
    void testGetDonation_NonExistingId() {
        ResponseEntity<?> response = donationController.getDonation(999);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testCreateDonation_Success() {
        Donor donor = new Donor("12345678A", "Donor 1");
        donorRepository.save(donor);
        Catastrophe catastrophe = new Catastrophe("Test Catastrophe", "Description",
                new GPSCoordinates(0.0, 0.0), LocalDate.now(), EmergencyLevel.HIGH);
        catastropheRepository.save(catastrophe);

        Donation donation1 = new Donation(DonationType.MATERIAL, "Test Donation 1", LocalDate.now(),
                DonationStatus.COMPLETED, donor, catastrophe, 5.0, "kg");
        DonationDTO donationDTO = new DonationDTO(donation1);

        ResponseEntity<?> response = donationController.createDonation(donationDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        DonationDTO createdDonation = (DonationDTO) response.getBody();
        assertNotNull(createdDonation);
        assertEquals(donationDTO.getDescription(), createdDonation.getDescription());
        assertEquals(donationDTO.getType(), createdDonation.getType());
        assertEquals(donationDTO.getStatus(), createdDonation.getStatus());
        assertEquals(donationDTO.getDonorDni(), createdDonation.getDonorDni());
    }

    @Test
    void testCreateDonation_InvalidCatastrophe() {
        DonationDTO donationDTO = new DonationDTO();
        donationDTO.setType(DonationType.MATERIAL);
        donationDTO.setDescription("Test Donation");
        donationDTO.setStatus(DonationStatus.COMPLETED);
        donationDTO.setDonorDni("12345678A");
        donationDTO.setQuantity(5.0);
        donationDTO.setUnit("kg");

        ResponseEntity<?> response = donationController.createDonation(donationDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El ID de la catástrofe es obligatorio", response.getBody());
    }

    @Test
    void testUpdateDonation_Success() {
        Donor donor = new Donor("12345678A", "Donor 1");
        donorRepository.save(donor);
        Donation donation = new Donation(DonationType.MATERIAL, "Test Donation", LocalDate.now(),
                DonationStatus.COMPLETED, donor, null, 0.0, "kg");
        Donation savedDonation = donationRepository.save(donation);
        entityManager.flush();

        DonationDTO donationDTO = new DonationDTO(savedDonation);
        donationDTO.setDescription("Updated Description");
        donationDTO.setStatus(DonationStatus.IN_PROGRESS);

        ResponseEntity<?> response = donationController.updateDonation(savedDonation.getId(), donationDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        DonationDTO updatedDonation = (DonationDTO) response.getBody();
        assertNotNull(updatedDonation);
        assertEquals("Updated Description", updatedDonation.getDescription());
        assertEquals(DonationStatus.IN_PROGRESS, updatedDonation.getStatus());
    }

    @Test
    void testUpdateDonation_NonExistingId() {
        DonationDTO donationDTO = new DonationDTO();
        donationDTO.setType(DonationType.MATERIAL);
        donationDTO.setDescription("Test Donation");
        donationDTO.setStatus(DonationStatus.COMPLETED);
        donationDTO.setDonorDni("12345678A");
        donationDTO.setQuantity(5.0);
        donationDTO.setUnit("kg");

        ResponseEntity<?> response = donationController.updateDonation(999, donationDTO);
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteDonation() {
        Donor donor = new Donor("12345678A", "Donor 1");
        donorRepository.save(donor);
        Donation donation = new Donation(DonationType.MATERIAL, "Test Donation", LocalDate.now(), DonationStatus.COMPLETED, donor, null, 0.0, "kg");
        Donation savedDonation = donationRepository.save(donation);
        entityManager.flush();

        ResponseEntity<?> response = donationController.deleteDonation(savedDonation.getId());
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertFalse(donationRepository.existsById(savedDonation.getId()));
    }
}
