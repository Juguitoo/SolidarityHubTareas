package solidarityhub.backend.service;

import solidarityhub.backend.model.Resource;
import solidarityhub.backend.repository.ResourceRepository;
import org.springframework.stereotype.Service;

@Service
public class ResourceService {
    private final ResourceRepository resourceRepository;
    public ResourceService(ResourceRepository resourceRepository) {this.resourceRepository = resourceRepository;}
    public Resource saveResource(Resource resource) {return resourceRepository.save(resource);}
}
