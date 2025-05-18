package solidarityhub.backend.criteria.donations;

import solidarityhub.backend.model.Donation;

import java.util.List;
import java.util.stream.Collectors;

public class MinQuantityFilter implements DonationFilter {
    private double minQuantity;

    public MinQuantityFilter(double minQuantity) {
        this.minQuantity = minQuantity;
    }

    @Override
    public List<Donation> filter(List<Donation> donations) {
        return donations.stream()
                .filter(donation -> donation.getQuantity() >= minQuantity)
                .collect(Collectors.toList());
    }
}
