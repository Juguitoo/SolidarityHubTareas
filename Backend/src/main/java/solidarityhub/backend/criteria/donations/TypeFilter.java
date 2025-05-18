package solidarityhub.backend.criteria.donations;

import solidarityhub.backend.model.Donation;
import solidarityhub.backend.model.enums.DonationType;

import java.util.List;
import java.util.stream.Collectors;

public class TypeFilter implements DonationFilter {
    private DonationType type;

    public TypeFilter(DonationType type) {
        this.type = type;
    }

    @Override
    public List<Donation> filter(List<Donation> donations) {
        return donations.stream()
                .filter(donation -> donation.getType().equals(type))
                .collect(Collectors.toList());
    }
}
