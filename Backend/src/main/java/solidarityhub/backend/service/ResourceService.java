package solidarityhub.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import solidarityhub.backend.criteria.resources.*;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.Donation;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.enums.DonationType;
import solidarityhub.backend.model.enums.ResourceType;
import solidarityhub.backend.observer.impl.ResourceObservable;
import solidarityhub.backend.repository.ResourceRepository;

import java.util.List;

@Service
public class ResourceService {
    private final ResourceRepository resourceRepository;
    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }
    public List<Resource> getResources() {return resourceRepository.findAll();}
    public Resource save(Resource resource) {
        Resource savedResource = resourceRepository.save(resource);
        resourceObservable.resourceUpdated(savedResource);
        return savedResource;
    }
    public Resource getResourceById(Integer id) {return resourceRepository.findById(id).orElse(null);}
    public void deleteResource(Resource resource) {resourceRepository.delete(resource);}

    private ResourceObservable resourceObservable;


    public List<Resource> getResourcesByCatastrophe(int catastropheId) {return resourceRepository.getResourcesByCatastrophe(catastropheId);}

    public List<Resource> filter(String type, String minQuantity, String storageId, int catastropheId) {
        List<Resource> resources = resourceRepository.getResourcesByCatastrophe(catastropheId);
        ResourceFilter filter = null;

        if (type != null)
            filter = new TypeFilter(ResourceType.valueOf(type));
        if(minQuantity != null)
            if (filter != null) filter = new AndFilter(filter, new MinQuantityFilter(Double.parseDouble(minQuantity)));
            else filter = new MinQuantityFilter(Double.parseDouble(minQuantity));
        if(storageId != null)
            if (filter != null) filter = new AndFilter(filter, new StorageFilter(Integer.parseInt(storageId)));
            else filter = new StorageFilter(Integer.parseInt(storageId));

        if (filter != null){
            resources = filter.filter(resources);
        }

        return resources;
    }

    //Methods for the ResourceAssignmentService
    public Resource getOrCreateMonetaryResource(Catastrophe catastrophe) {
        List<Resource> resources = resourceRepository.getResourcesByCatastrophe(catastrophe.getId())
                .stream()
                .filter(r -> r.getType() == ResourceType.MONETARY)
                .toList();

        if (!resources.isEmpty()) {
            return resources.get(0);
        }

        // Create a new monetary resource
        Resource monetaryResource = new Resource(
                "Fondos disponibles",
                ResourceType.MONETARY,
                0.0,
                "€",
                null,
                catastrophe);

        return save(monetaryResource);
    }

    public Resource updateResourceFromDonation(Donation donation) {
        if (donation.getType() == DonationType.FINANCIAL) {
            // Find or create the monetary resource
            Resource monetaryResource = getOrCreateMonetaryResource(donation.getCatastrophe());
            monetaryResource.setQuantity(monetaryResource.getQuantity() + donation.getQuantity());
            monetaryResource.setCantidad(monetaryResource.getQuantity() + " " + monetaryResource.getUnit());
            return save(monetaryResource);
        } else if (donation.getType() == DonationType.MATERIAL) {
            // Look for an existing resource of the same type
            List<Resource> resources = resourceRepository.getResourcesByCatastrophe(donation.getCatastrophe().getId())
                    .stream()
                    .filter(r -> r.getName().equalsIgnoreCase(donation.getDescription()) &&
                            r.getUnit().equalsIgnoreCase(donation.getUnit()))
                    .toList();

            if (!resources.isEmpty()) {
                // Update existing resource
                Resource existingResource = resources.get(0);
                existingResource.setQuantity(existingResource.getQuantity() + donation.getQuantity());
                existingResource.setCantidad(existingResource.getQuantity() + " " + existingResource.getUnit());
                return save(existingResource);
            } else {
                // Create a new resource
                ResourceType resourceType = determineResourceType(donation.getDescription());
                Resource newResource = new Resource(
                        donation.getDescription(),
                        resourceType,
                        donation.getQuantity(),
                        donation.getUnit(),
                        null,
                        donation.getCatastrophe());

                return save(newResource);
            }
        }

        return null;
    }

    private ResourceType determineResourceType(String description) {
        description = description.toLowerCase();

        if (description.contains("aliment") || description.contains("comida") || description.contains("food")) {
            return ResourceType.FOOD;
        } else if (description.contains("medic") || description.contains("medicine")) {
            return ResourceType.MEDICINE;
        } else if (description.contains("ropa") || description.contains("cloth")) {
            return ResourceType.CLOTHING;
        } else if (description.contains("refugio") || description.contains("albergue") || description.contains("shelter")) {
            return ResourceType.SHELTER;
        } else if (description.contains("herramienta") || description.contains("tool")) {
            return ResourceType.TOOLS;
        } else if (description.contains("combustible") || description.contains("fuel")) {
            return ResourceType.FUEL;
        } else if (description.contains("sanitario") || description.contains("sanitation")) {
            return ResourceType.SANITATION;
        } else if (description.contains("comunicación") || description.contains("communication")) {
            return ResourceType.COMMUNICATION;
        } else if (description.contains("transporte") || description.contains("transportation")) {
            return ResourceType.TRANSPORTATION;
        } else if (description.contains("construcción") || description.contains("building")) {
            return ResourceType.BUILDING;
        } else if (description.contains("dinero") || description.contains("money") || description.contains("€") || description.contains("$")) {
            return ResourceType.MONETARY;
        } else if (description.contains("papelería") || description.contains("stationery")) {
            return ResourceType.STATIONERY;
        } else if (description.contains("logística") || description.contains("logistics")) {
            return ResourceType.LOGISTICS;
        } else {
            return ResourceType.OTHER;
        }
    }

    // Add this method to check resources
    public void checkResourceLevels() {
        List<Resource> resources = resourceRepository.findAll();
        resourceObservable.setResources(resources);
    }
}
