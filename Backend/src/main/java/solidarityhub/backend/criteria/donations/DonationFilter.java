package solidarityhub.backend.criteria.donations;

import solidarityhub.backend.model.Donation;

import java.util.List;

public interface DonationFilter {
    List<Donation> filter(List<Donation> donations);
}
