package solidarityhub.backend.criteria.donations;

import solidarityhub.backend.model.Donation;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DateFilter implements DonationFilter {
    private Integer year;

    public DateFilter(Integer year) {
        this.year = year;
    }

    @Override
    public List<Donation> filter(List<Donation> donations) {
        return donations.stream()
                .filter(donation -> donation.getDate().getYear() == year)
                .collect(Collectors.toList());
    }
}
