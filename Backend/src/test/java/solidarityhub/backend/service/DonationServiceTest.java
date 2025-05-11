package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import solidarityhub.backend.repository.PersonRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class DonationServiceTest {

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private CatastropheRepository catastropheRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private DonorRepository donorRepository;

    @InjectMocks
    private DonationService donationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveDonation() {
        // Arrange
        Donation donation = createTestDonation(1);
        when(donationRepository.save(any(Donation.class))).thenReturn(donation);

        // Act
        Donation savedDonation = donationService.saveDonation(donation);

        // Assert
        assertNotNull(savedDonation);
        assertEquals(1, savedDonation.getId());
        verify(donationRepository, times(1)).save(donation);
    }

    @Test
    void testGetAllDonations() {
        // Arrange
        List<Donation> donations = new ArrayList<>();
        donations.add(createTestDonation(1));
        donations.add(createTestDonation(2));
        when(donationRepository.findAll()).thenReturn(donations);

        // Act
        List<Donation> result = donationService.getAllDonations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(donationRepository, times(1)).findAll();
    }

    @Test
    void testGetDonationsByCatastrophe() {
        // Arrange
        int catastropheId = 1;
        List<Donation> donations = new ArrayList<>();
        donations.add(createTestDonation(1));
        donations.add(createTestDonation(2));
        when(donationRepository.findByCatastropheId(catastropheId)).thenReturn(donations);

        // Act
        List<Donation> result = donationService.getDonationsByCatastrophe(catastropheId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(donationRepository, times(1)).findByCatastropheId(catastropheId);
    }

    @Test
    void testGetDonationsByDonor() {
        // Arrange
        String donorDni = "D-1";
        List<Donation> donations = new ArrayList<>();
        donations.add(createTestDonation(1));
        donations.add(createTestDonation(2));
        when(donationRepository.findByDonorDni(donorDni)).thenReturn(donations);

        // Act
        List<Donation> result = donationService.getDonationsByDonor(donorDni);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(donationRepository, times(1)).findByDonorDni(donorDni);
    }

    @Test
    void testGetDonationById_ExistingId() {
        // Arrange
        int donationId = 1;
        Donation donation = createTestDonation(donationId);
        when(donationRepository.findById(donationId)).thenReturn(Optional.of(donation));

        // Act
        Donation result = donationService.getDonationById(donationId);

        // Assert
        assertNotNull(result);
        assertEquals(donationId, result.getId());
        verify(donationRepository, times(1)).findById(donationId);
    }

    @Test
    void testGetDonationById_NonExistingId() {
        // Arrange
        int nonExistingId = 999;
        when(donationRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Donation result = donationService.getDonationById(nonExistingId);

        // Assert
        assertNull(result);
        verify(donationRepository, times(1)).findById(nonExistingId);
    }

    @Test
    void testDeleteDonationById() {
        // Arrange
        int donationId = 1;
        when(donationRepository.deleteDonationById(donationId)).thenReturn(1);

        // Act
        donationService.deleteDonationById(donationId);

        // Assert
        verify(donationRepository, times(1)).deleteDonationById(donationId);
    }

    @Test
    void testCreateDonation_Success() {
        // Arrange
        String donorDni = "D-1";
        int catastropheId = 1;
        Donor donor = new Donor(donorDni, "Donor 1");
        Catastrophe catastrophe = createTestCatastrophe(catastropheId);
        Donation donation = new Donation();
        donation.setType(DonationType.MATERIAL);
        donation.setDescription("Test Donation");

        when(donorRepository.findById(donorDni)).thenReturn(Optional.of(donor));
        when(catastropheRepository.findById(catastropheId)).thenReturn(Optional.of(catastrophe));
        when(donationRepository.save(any(Donation.class))).thenAnswer(invocation -> {
            Donation savedDonation = invocation.getArgument(0);
            try {
                java.lang.reflect.Field idField = Donation.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(savedDonation, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return savedDonation;
        });

        // Act
        Donation result = donationService.createDonation(donorDni, catastropheId, donation);

        // Assert
        assertNotNull(result);
        assertEquals(donor, result.getDonor());
        assertEquals(catastrophe, result.getCatastrophe());
        assertNotNull(result.getDate());
        verify(donorRepository, times(1)).findById(donorDni);
        verify(catastropheRepository, times(1)).findById(catastropheId);
        verify(donationRepository, times(1)).save(any(Donation.class));
    }

    @Test
    void testCreateDonation_NonExistingDonor() {
        // Arrange
        String donorDni = "NONEXISTENT";
        int catastropheId = 1;
        Catastrophe catastrophe = createTestCatastrophe(catastropheId);
        Donation donation = new Donation();

        when(donorRepository.findById(donorDni)).thenReturn(Optional.empty());
        when(catastropheRepository.findById(catastropheId)).thenReturn(Optional.of(catastrophe));

        // Act
        Donation result = donationService.createDonation(donorDni, catastropheId, donation);

        // Assert
        assertNull(result);
        verify(donorRepository, times(1)).findById(donorDni);
        verify(catastropheRepository, times(1)).findById(catastropheId);
        verify(donationRepository, never()).save(any(Donation.class));
    }

    @Test
    void testCreateDonation_NonExistingCatastrophe() {
        // Arrange
        String donorDni = "D-1";
        int catastropheId = 999;
        Donor donor = new Donor(donorDni, "Donor 1");
        Donation donation = new Donation();

        when(donorRepository.findById(donorDni)).thenReturn(Optional.of(donor));
        when(catastropheRepository.findById(catastropheId)).thenReturn(Optional.empty());

        // Act
        Donation result = donationService.createDonation(donorDni, catastropheId, donation);

        // Assert
        assertNull(result);
        verify(donorRepository, times(1)).findById(donorDni);
        verify(catastropheRepository, times(1)).findById(catastropheId);
        verify(donationRepository, never()).save(any(Donation.class));
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
}