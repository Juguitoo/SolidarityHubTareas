package solidarityhub.backend.service;

import org.springframework.stereotype.Service;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.repository.ResourceRepository;

import java.util.List;

@Service
public class ResourceService {
    private final ResourceRepository resourceRepository;
    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }
    public List<Resource> getResources() {return resourceRepository.findAll();}
    public Resource saveResource(Resource resource) {return resourceRepository.save(resource);}
    public Resource getResourceById(Integer id) {return resourceRepository.findById(id).orElse(null);}
    public void deleteResource(Resource resource) {resourceRepository.delete(resource);}

}
