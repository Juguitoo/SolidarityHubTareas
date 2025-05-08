package solidarityhub.backend.service;

import org.springframework.stereotype.Service;
import solidarityhub.backend.model.Donation;
import solidarityhub.backend.model.Donor;
import solidarityhub.backend.model.Person;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.repository.DonationRepository;
import solidarityhub.backend.repository.DonorRepository;
import solidarityhub.backend.repository.PersonRepository;
import solidarityhub.backend.repository.CatastropheRepository;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
public class DonationService {
    private final DonationRepository donationRepository;
    private final CatastropheRepository catastropheRepository;
    private final PersonRepository personRepository;
    private final DonorRepository donorRepository;

    public DonationService(DonationRepository donationRepository, CatastropheRepository catastropheRepository, PersonRepository personRepository, DonorRepository donorRepository) {
        this.donationRepository = donationRepository;
        this.catastropheRepository = catastropheRepository;
        this.personRepository = personRepository;
        this.donorRepository = donorRepository;
    }

    public Donation saveDonation(Donation donation) {
        return donationRepository.save(donation);
    }

    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    public List<Donation> getDonationsByCatastrophe(Integer catastropheId) {
        return donationRepository.findByCatastropheId(catastropheId);
    }

    public List<Donation> getDonationsByDonor(String donorDni) {
        return donationRepository.findByDonorDni(donorDni);
    }

    public Donation getDonationById(Integer id) {
        return donationRepository.findById(id).orElse(null);
    }

    public void deleteDonationById(Integer id) {
        System.out.println(donationRepository.deleteDonationById(id));
    }

    public Donation createDonation(String donorDni, Integer catastropheId, Donation donation) {
        Optional<Donor> donor = donorRepository.findById(donorDni);
        Optional<Catastrophe> catastrophe = catastropheRepository.findById(catastropheId);

        if (donor.isPresent() && catastrophe.isPresent()) {
            donation.setDonor(donor.get());
            donation.setCatastrophe(catastrophe.get());
            donation.setDate(LocalDate.now());
            return saveDonation(donation);
        }

        return null;
    }
}
