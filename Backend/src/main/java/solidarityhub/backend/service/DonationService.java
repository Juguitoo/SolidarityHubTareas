package solidarityhub.backend.service;

import org.springframework.stereotype.Service;
import solidarityhub.backend.criteria.donations.*;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.Donation;
import solidarityhub.backend.model.Donor;
import solidarityhub.backend.model.enums.DonationStatus;
import solidarityhub.backend.model.enums.DonationType;
import solidarityhub.backend.repository.CatastropheRepository;
import solidarityhub.backend.repository.DonationRepository;
import solidarityhub.backend.repository.DonorRepository;
import solidarityhub.backend.repository.PersonRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DonationService {
    private final DonationRepository donationRepository;
    private final CatastropheRepository catastropheRepository;
    private final DonorRepository donorRepository;

    public DonationService(DonationRepository donationRepository, CatastropheRepository catastropheRepository, PersonRepository personRepository, DonorRepository donorRepository) {
        this.donationRepository = donationRepository;
        this.catastropheRepository = catastropheRepository;
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

    public List<Donation> filter(String type, String status, String minQuantity, String year, Integer catastropheId) {
        List<Donation> donations = donationRepository.findByCatastropheId(catastropheId);
        DonationFilter filter = null;

        if (type != null)
            filter = new TypeFilter(DonationType.valueOf(type));
        if (minQuantity != null)
            if (filter != null) filter = new AndFilter(filter, new MinQuantityFilter(Double.parseDouble(minQuantity)));
            else filter = new MinQuantityFilter(Double.parseDouble(minQuantity));
        if (year != null)
            if (filter != null) filter = new AndFilter(filter, new DateFilter(Integer.parseInt(year)));
            else filter = new DateFilter(Integer.parseInt(year));
        if (status != null)
            if (filter != null) filter = new AndFilter(filter, new StatusFilter(DonationStatus.valueOf(status)));
            else filter = new StatusFilter(DonationStatus.valueOf(status));

        if (filter != null) {
            donations = filter.filter(donations);
        }

        return donations;
    }
}
