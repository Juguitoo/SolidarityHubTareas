package solidarityhub.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import solidarityhub.backend.dto.DonorDTO;
import solidarityhub.backend.model.Donor;
import solidarityhub.backend.repository.DonorRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DonorService {

    private final DonorRepository donorRepository;

    @Autowired
    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    public List<DonorDTO> getAllDonors() {
        return donorRepository.findAll().stream()
                .map(DonorDTO::new)
                .collect(Collectors.toList());
    }

    public DonorDTO getDonorByDni(String dni) {
        Optional<Donor> donor = donorRepository.findById(dni);
        return donor.map(DonorDTO::new).orElse(null);
    }

    public DonorDTO saveDonor(DonorDTO donorDTO) {
        Donor donor = new Donor(
                donorDTO.getDni(),
                donorDTO.getName()
        );
        return new DonorDTO(donorRepository.save(donor));
    }

    public boolean existsById(String dni) {
        return donorRepository.existsById(dni);
    }

    public void deleteDonor(String dni) {
        donorRepository.deleteById(dni);
    }
}