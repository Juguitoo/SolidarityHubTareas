package solidarityhub.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.ResourceDTO;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.service.ResourceService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/solidarityhub/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<?> getResources() {
        List<ResourceDTO> resourceDTOList = new ArrayList<>();
        resourceService.getResources().forEach(r -> {resourceDTOList.add(new ResourceDTO(r));});
        return ResponseEntity.ok(resourceDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getResource(@PathVariable Integer id) {
        if (resourceService.getResourceById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ResourceDTO(resourceService.getResourceById(id)));
    }

    @PostMapping
    public ResponseEntity<?> createResource(@RequestBody ResourceDTO resourceDTO) {
        Resource resource = new Resource(resourceDTO.getName(), resourceDTO.getType(), resourceDTO.getQuantity());
        resourceService.saveResource(resource);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateResource(@PathVariable Integer id, @RequestBody ResourceDTO resourceDTO) {
        Resource resource = resourceService.getResourceById(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        resource.setName(resourceDTO.getName());
        resource.setType(resourceDTO.getType());
        resource.setQuantity(resourceDTO.getQuantity());
        resource.setStorage(resourceDTO.getStorage());

        resourceService.saveResource(resource);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable Integer id) {
        Resource resource = resourceService.getResourceById(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        resourceService.deleteResource(resource);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
