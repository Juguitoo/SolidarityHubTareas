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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DonationService {
    private final DonationRepository donationRepository;
    private final CatastropheRepository catastropheRepository;
    private final DonorRepository donorRepository;

    private final ResourceService resourceService;

    public DonationService(DonationRepository donationRepository, CatastropheRepository catastropheRepository, DonorRepository donorRepository, ResourceService resourceService) {
        this.donationRepository = donationRepository;
        this.catastropheRepository = catastropheRepository;
        this.donorRepository = donorRepository;
        this.resourceService = resourceService;
    }

    public Donation saveDonation(Donation donation) {
        return donationRepository.save(donation);
    }

    public Donation saveAndProcessDonation(Donation donation) {
        Donation savedDonation = donationRepository.save(donation);

        resourceService.updateResourceFromDonation(savedDonation);

        return savedDonation;
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

        List<DonationFilter> filters = new ArrayList<>();

        if (type != null) {
            filters.add(new TypeFilter(DonationType.valueOf(type)));
        }
        if (minQuantity != null) {
            filters.add(new MinQuantityFilter(Double.parseDouble(minQuantity)));
        }
        if (year != null) {
            filters.add(new DateFilter(Integer.parseInt(year)));
        }
        if (status != null) {
            filters.add(new StatusFilter(DonationStatus.valueOf(status)));
        }

        DonationFilter composedFilter = composeFilters(filters);
        return composedFilter != null ? composedFilter.filter(donations) : donations;
    }

    private DonationFilter composeFilters(List<DonationFilter> filters) {
        if (filters.isEmpty()) return null;
        DonationFilter result = filters.get(0);
        for (int i = 1; i < filters.size(); i++) {
            result = new AndFilter(result, filters.get(i));
        }
        return result;
    }

}
