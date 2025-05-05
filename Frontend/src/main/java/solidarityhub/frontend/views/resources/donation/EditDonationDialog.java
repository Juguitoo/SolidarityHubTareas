package solidarityhub.frontend.views.resources.donation;

import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.DonationDTO;

public class EditDonationDialog extends AddDonationDialog {

    public EditDonationDialog(CatastropheDTO selectedCatastrophe, DonationDTO donation) {
        super(selectedCatastrophe, donation);
    }

    @Override
    protected void populateFormFields() {
        super.populateFormFields();
        // Si necesitas hacer algo adicional específico para la edición
    }
}