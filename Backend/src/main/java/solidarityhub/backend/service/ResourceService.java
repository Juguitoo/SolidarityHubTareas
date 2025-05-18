package solidarityhub.backend.service;

import org.springframework.stereotype.Service;
import solidarityhub.backend.criteria.*;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.enums.ResourceType;
import solidarityhub.backend.repository.ResourceRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResourceService {
    private final ResourceRepository resourceRepository;
    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }
    public List<Resource> getResources() {return resourceRepository.findAll();}
    public Resource save(Resource resource) {return resourceRepository.save(resource);}
    public Resource getResourceById(Integer id) {return resourceRepository.findById(id).orElse(null);}
    public void deleteResource(Resource resource) {resourceRepository.delete(resource);}

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
}
