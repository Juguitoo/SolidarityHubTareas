package solidarityhub.backend.criteria.donations;

import solidarityhub.backend.model.Donation;

import java.util.List;

public class AndFilter implements DonationFilter {
    private DonationFilter filter1;
    private DonationFilter filter2;

    public AndFilter(DonationFilter filter1, DonationFilter filter2) {
        this.filter1 = filter1;
        this.filter2 = filter2;
    }

    @Override
    public List<Donation> filter(List<Donation> donations) {
        List<Donation> filteredList1 = filter1.filter(donations);
        return filter2.filter(filteredList1);
    }
}
