package solidarityhub.frontend.views.resources.resource;

import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.dto.ResourceDTO;
import solidarityhub.frontend.dto.StorageDTO;

import java.util.List;

public class EditResourceDialog extends AddResourceDialog {

        public EditResourceDialog(CatastropheDTO selectedCatastrophe, ResourceDTO resource) {
            super(selectedCatastrophe, resource);
        }

        @Override
        protected void populateFormFields() {
            // Llenar campos con datos del recurso a editar
            if (resource != null) {
                nameField.setValue(resource.getName());
                typeField.setValue(resource.getType());
                quantityField.setValue(resource.getQuantity());
                unitField.setValue(resource.getUnit());

                if (resource.getStorageId() != null) {
                    List<StorageDTO> storages = storageService.getStorages();
                    storageField.setValue(storages.stream()
                            .filter(s -> s.getId() == resource.getStorageId())
                            .findFirst()
                            .orElse(null));
                }
            }
        }
}