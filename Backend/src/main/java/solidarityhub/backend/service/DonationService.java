package solidarityhub.backend.service;

import org.springframework.stereotype.Service;
import solidarityhub.backend.model.Donation;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.repository.DonationRepository;
import solidarityhub.backend.repository.VolunteerRepository;
import solidarityhub.backend.repository.CatastropheRepository;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
public class DonationService {
    private final DonationRepository donationRepository;
    private final VolunteerRepository volunteerRepository;
    private final CatastropheRepository catastropheRepository;

    public DonationService(DonationRepository donationRepository,
                           VolunteerRepository volunteerRepository,
                           CatastropheRepository catastropheRepository) {
        this.donationRepository = donationRepository;
        this.volunteerRepository = volunteerRepository;
        this.catastropheRepository = catastropheRepository;
    }

    public Donation saveDonation(Donation donation) {
        // If no code is set, generate one
        if (donation.getCode() == null || donation.getCode().isEmpty()) {
            donation.setCode(generateDonationCode());
        }
        return donationRepository.save(donation);
    }

    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    public List<Donation> getDonationsByCatastrophe(Integer catastropheId) {
        return donationRepository.findByCatastropheId(catastropheId);
    }

    public List<Donation> getDonationsByVolunteer(String volunteerDni) {
        return donationRepository.findByVolunteerDni(volunteerDni);
    }

    public Donation getDonationById(Integer id) {
        return donationRepository.findById(id).orElse(null);
    }

    public void deleteDonation(Integer id) {
        donationRepository.deleteById(id);
    }

    // Helper method to generate donation code in format DON-YYYY-NNN
    private String generateDonationCode() {
        String currentYear = String.valueOf(Year.now().getValue());
        Integer maxNumber = donationRepository.findMaxDonationNumberForYear(currentYear);
        int nextNumber = (maxNumber != null) ? maxNumber + 1 : 1;

        // Format the sequence number with leading zeros
        return String.format("DON-%s-%03d", currentYear, nextNumber);
    }

    public Donation createDonation(String volunteerDni, Integer catastropheId, Donation donation) {
        Optional<Volunteer> volunteer = volunteerRepository.findById(volunteerDni);
        Optional<Catastrophe> catastrophe = catastropheRepository.findById(catastropheId);

        if (volunteer.isPresent() && catastrophe.isPresent()) {
            donation.setVolunteer(volunteer.get());
            donation.setCatastrophe(catastrophe.get());
            donation.setDate(LocalDate.now());
            return saveDonation(donation);
        }

        return null;
    }
}
