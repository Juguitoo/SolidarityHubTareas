package solidarityhub.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.DonationDTO;
import solidarityhub.backend.model.Donation;
import solidarityhub.backend.model.enums.DonationType;
import solidarityhub.backend.service.DonationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/solidarityhub/donations")
public class DonationController {
    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @GetMapping
    public ResponseEntity<?> getDonations(@RequestParam(required = false) String type,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String minQuantity,
                                          @RequestParam(required = false) String year,
                                          @RequestParam Integer catastropheId) {
        List<DonationDTO> donationDTOs = new ArrayList<>();
        if (catastropheId == null) {
            donationService.getAllDonations().forEach(d -> donationDTOs.add(new DonationDTO(d)));
            return ResponseEntity.ok(donationDTOs);
        }
        donationService.filter(type, status, minQuantity, year, catastropheId)
                .forEach(d -> donationDTOs.add(new DonationDTO(d)));
        return ResponseEntity.ok(donationDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDonation(@PathVariable Integer id) {
        Donation donation = donationService.getDonationById(id);
        if (donation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new DonationDTO(donation));
    }

    @GetMapping("/byCatastrophe")
    public ResponseEntity<?> getDonationsByCatastrophe(@RequestParam Integer catastropheId) {
        List<DonationDTO> donationDTOs = new ArrayList<>();
        donationService.getDonationsByCatastrophe(catastropheId).forEach(d -> donationDTOs.add(new DonationDTO(d)));
        return ResponseEntity.ok(donationDTOs);
    }

    @PostMapping
    public ResponseEntity<?> createDonation(@RequestBody DonationDTO donationDTO) {
        try {
            if (donationDTO.getCatastropheId() == null) {
                return ResponseEntity.badRequest().body("El ID de la catástrofe es obligatorio");
            }

            Donation donation = new Donation();
            donation.setType(donationDTO.getType());
            donation.setDescription(donationDTO.getDescription());
            donation.setStatus(donationDTO.getStatus());
            donation.setQuantity(donationDTO.getQuantity());
            donation.setUnit(donationDTO.getUnit());
            donation.setCantidad(donationDTO.getQuantity() + " " + donationDTO.getUnit());
            donation.setDate(LocalDate.now());

            Donation savedDonation = donationService.createDonation(
                    donationDTO.getDonorDni(),
                    donationDTO.getCatastropheId(),
                    donation
            );

            if (savedDonation == null) {
                return ResponseEntity.badRequest().body("Invalid donor or catastrophe");
            }

            // Process the donation to update resources
            donationService.saveAndProcessDonation(savedDonation);

            return ResponseEntity.status(HttpStatus.CREATED).body(new DonationDTO(savedDonation));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDonation(@PathVariable Integer id, @RequestBody DonationDTO donationDTO) {
        Donation donation = donationService.getDonationById(id);
        if (donation == null) {
            return ResponseEntity.notFound().build();
        }

        donation.setType(donationDTO.getType());
        donation.setDescription(donationDTO.getDescription());
        donation.setStatus(donationDTO.getStatus());
        donation.setQuantity(donationDTO.getQuantity());
        donation.setUnit(donationDTO.getUnit());
        donation.setCantidad(donationDTO.getQuantity() + " " + donationDTO.getUnit());

        Donation updatedDonation = donationService.saveDonation(donation);
        return ResponseEntity.ok(new DonationDTO(updatedDonation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDonation(@PathVariable Integer id) {
        donationService.deleteDonationById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/monetary-total")
    public ResponseEntity<?> getMonetaryTotal(@RequestParam Integer catastropheId) {
        double total = donationService.getDonationsByCatastrophe(catastropheId).stream()
                .filter(d -> d.getType() == DonationType.FINANCIAL)
                .mapToDouble(Donation::getQuantity)
                .sum();

        return ResponseEntity.ok(total);
    }
}
