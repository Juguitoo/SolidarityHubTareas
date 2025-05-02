package solidarityhub.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.ResourceDTO;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.service.CatastropheService;
import solidarityhub.backend.service.ResourceService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/solidarityhub/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final CatastropheService catastropheService;

    public ResourceController(ResourceService resourceService, CatastropheService catastropheService) {
        this.resourceService = resourceService;
        this.catastropheService = catastropheService;
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
        if (resourceDTO.getCatastropheId() == null) {
            return ResponseEntity.badRequest().body("El ID de la catástrofe es obligatorio");
        }
        Catastrophe catastrophe = catastropheService.getCatastrophe(resourceDTO.getCatastropheId());
        if (catastrophe == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new Resource(
                resourceDTO.getName(),
                resourceDTO.getType(),
                resourceDTO.getQuantity(),
                resourceDTO.getUnit(),
                catastrophe);
        resourceService.save(resource);

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
        resource.setUnit(resourceDTO.getUnit());
        resource.setCantidad(resourceDTO.getCantidad());
        resource.setStorage(resourceDTO.getStorage());

        resourceService.save(resource);

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
