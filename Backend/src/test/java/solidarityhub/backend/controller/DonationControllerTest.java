package solidarityhub.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import solidarityhub.backend.dto.DonationDTO;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.Donation;
import solidarityhub.backend.model.Donor;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.enums.DonationStatus;
import solidarityhub.backend.model.enums.DonationType;
import solidarityhub.backend.model.enums.EmergencyLevel;
import solidarityhub.backend.service.DonationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DonationControllerTest {

    @Mock
    private DonationService donationService;

    @InjectMocks
    private DonationController donationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDonations() {
        // Arrange
        List<Donation> donations = new ArrayList<>();
        donations.add(createTestDonation(1));
        donations.add(createTestDonation(2));
        when(donationService.getAllDonations()).thenReturn(donations);

        // Act
        ResponseEntity<?> response = donationController.getDonations();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        assertTrue(responseBody.get(0) instanceof DonationDTO);
    }

    @Test
    void testGetDonation_ExistingId() {
        // Arrange
        int donationId = 1;
        Donation donation = createTestDonation(donationId);
        when(donationService.getDonationById(donationId)).thenReturn(donation);

        // Act
        ResponseEntity<?> response = donationController.getDonation(donationId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof DonationDTO);
        DonationDTO responseDonation = (DonationDTO) response.getBody();
        assertEquals(donationId, responseDonation.getId());
    }

    @Test
    void testGetDonation_NonExistingId() {
        // Arrange
        int nonExistingId = 999;
        when(donationService.getDonationById(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = donationController.getDonation(nonExistingId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetDonationsByCatastrophe() {
        // Arrange
        int catastropheId = 1;
        List<Donation> donations = new ArrayList<>();
        donations.add(createTestDonation(1));
        donations.add(createTestDonation(2));
        when(donationService.getDonationsByCatastrophe(catastropheId)).thenReturn(donations);

        // Act
        ResponseEntity<?> response = donationController.getDonationsByCatastrophe(catastropheId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(2, responseBody.size());
        assertTrue(responseBody.get(0) instanceof DonationDTO);
    }

    @Test
    void testCreateDonation_Success() {
        // Arrange
        DonationDTO donationDTO = createTestDonationDTO();
        Donation createdDonation = createTestDonation(1);
        when(donationService.createDonation(anyString(), anyInt(), any(Donation.class))).thenReturn(createdDonation);

        // Act
        ResponseEntity<?> response = donationController.createDonation(donationDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof DonationDTO);
        verify(donationService, times(1)).createDonation(anyString(), anyInt(), any(Donation.class));
    }

    @Test
    void testCreateDonation_Failure() {
        // Arrange
        DonationDTO donationDTO = createTestDonationDTO();
        when(donationService.createDonation(anyString(), anyInt(), any(Donation.class))).thenReturn(null);

        // Act
        ResponseEntity<?> response = donationController.createDonation(donationDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid donor or catastrophe", response.getBody());
        verify(donationService, times(1)).createDonation(anyString(), anyInt(), any(Donation.class));
    }

    @Test
    void testUpdateDonation_Success() {
        // Arrange
        int donationId = 1;
        DonationDTO donationDTO = createTestDonationDTO();
        Donation existingDonation = createTestDonation(donationId);
        Donation updatedDonation = createTestDonation(donationId);
        updatedDonation.setDescription("Updated Description");

        when(donationService.getDonationById(donationId)).thenReturn(existingDonation);
        when(donationService.saveDonation(any(Donation.class))).thenReturn(updatedDonation);

        // Act
        ResponseEntity<?> response = donationController.updateDonation(donationId, donationDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof DonationDTO);
        DonationDTO responseDTO = (DonationDTO) response.getBody();
        assertEquals("Updated Description", responseDTO.getDescription());
        verify(donationService, times(1)).getDonationById(donationId);
        verify(donationService, times(1)).saveDonation(any(Donation.class));
    }

    @Test
    void testUpdateDonation_NonExistingId() {
        // Arrange
        int nonExistingId = 999;
        DonationDTO donationDTO = createTestDonationDTO();
        when(donationService.getDonationById(nonExistingId)).thenReturn(null);

        // Act
        ResponseEntity<?> response = donationController.updateDonation(nonExistingId, donationDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(donationService, times(1)).getDonationById(nonExistingId);
        verify(donationService, never()).saveDonation(any(Donation.class));
    }

    @Test
    void testDeleteDonation() {
        // Arrange
        int donationId = 1;
        doNothing().when(donationService).deleteDonationById(donationId);

        // Act
        ResponseEntity<?> response = donationController.deleteDonation(donationId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(donationService, times(1)).deleteDonationById(donationId);
    }

    // Helper methods
    private Donation createTestDonation(int id) {
        Donor donor = new Donor("D-" + id, "Donor " + id);
        Catastrophe catastrophe = createTestCatastrophe(id);

        Donation donation = new Donation(
                DonationType.MATERIAL,
                "Test Donation " + id,
                LocalDate.now(),
                DonationStatus.COMPLETED,
                donor,
                catastrophe,
                10.0,
                "kg"
        );

        // Set ID using reflection since there's no setter
        try {
            java.lang.reflect.Field idField = Donation.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(donation, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return donation;
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

    private DonationDTO createTestDonationDTO() {
        DonationDTO dto = new DonationDTO();

        try {
            java.lang.reflect.Field typeField = DonationDTO.class.getDeclaredField("type");
            typeField.setAccessible(true);
            typeField.set(dto, DonationType.MATERIAL);

            java.lang.reflect.Field descriptionField = DonationDTO.class.getDeclaredField("description");
            descriptionField.setAccessible(true);
            descriptionField.set(dto, "Test Donation");

            java.lang.reflect.Field statusField = DonationDTO.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(dto, DonationStatus.COMPLETED);

            java.lang.reflect.Field donorDniField = DonationDTO.class.getDeclaredField("donorDni");
            donorDniField.setAccessible(true);
            donorDniField.set(dto, "D-1");

            java.lang.reflect.Field catastropheIdField = DonationDTO.class.getDeclaredField("catastropheId");
            catastropheIdField.setAccessible(true);
            catastropheIdField.set(dto, 1);

            java.lang.reflect.Field quantityField = DonationDTO.class.getDeclaredField("quantity");
            quantityField.setAccessible(true);
            quantityField.set(dto, 10.0);

            java.lang.reflect.Field unitField = DonationDTO.class.getDeclaredField("unit");
            unitField.setAccessible(true);
            unitField.set(dto, "kg");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }
}