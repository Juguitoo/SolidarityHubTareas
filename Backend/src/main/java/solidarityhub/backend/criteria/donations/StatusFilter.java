package solidarityhub.backend.criteria.donations;

import solidarityhub.backend.model.Donation;
import solidarityhub.backend.model.enums.DonationStatus;

import java.util.List;
import java.util.stream.Collectors;

public class StatusFilter implements DonationFilter {
    private DonationStatus status;

    public StatusFilter(DonationStatus status) {
        this.status = status;
    }

    @Override
    public List<Donation> filter(List<Donation> donations) {
        return donations.stream()
                .filter(donation -> donation.getStatus().equals(status))
                .collect(Collectors.toList());
    }
}
