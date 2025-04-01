package solidarityhub.backend.service;

import solidarityhub.backend.model.Donation;
import solidarityhub.backend.repository.DonationRepository;
import org.springframework.stereotype.Service;

@Service
public class DonationService {
    private final DonationRepository donationRepository;
    public DonationService(DonationRepository donationRepository) {this.donationRepository = donationRepository;}
    public Donation saveDonation(Donation donation) {return donationRepository.save(donation);}
}
