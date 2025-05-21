package solidarityhub.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.ResourceDTO;
import solidarityhub.backend.model.Catastrophe;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.model.enums.ResourceType;
import solidarityhub.backend.service.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/solidarityhub/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final CatastropheService catastropheService;
    private final StorageService storageService;
    private final ResourceAssignmentService resourceAssignmentService;
    @Autowired
    private ResourceMonitorService resourceMonitorService;


    public ResourceController(ResourceService resourceService, CatastropheService catastropheService, StorageService storageService, ResourceAssignmentService resourceAssignmentService) {
        this.resourceService = resourceService;
        this.catastropheService = catastropheService;
        this.storageService = storageService;
        this.resourceAssignmentService = resourceAssignmentService;
    }

    @GetMapping
    public ResponseEntity<?> getResources(@RequestParam(required = false) String type,
                                          @RequestParam(required = false) String minQuantity,
                                          @RequestParam(required = false) String storageId,
                                          @RequestParam Integer catastropheId) {
        List<ResourceDTO> resourceDTOList = new ArrayList<>();
        if (catastropheId == null) {
            resourceService.getResources().forEach(r -> {resourceDTOList.add(new ResourceDTO(r));});
            return ResponseEntity.ok(resourceDTOList);
        }
        resourceService.filter(type, minQuantity, storageId, catastropheId)
                .forEach(r -> {resourceDTOList.add(new ResourceDTO(r));});
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
        Storage storage = storageService.getStorageById(resourceDTO.getStorageId());
        Resource resource = new Resource(
                resourceDTO.getName(),
                resourceDTO.getType(),
                resourceDTO.getQuantity(),
                resourceDTO.getUnit(),
                storage,
                catastrophe);
        resourceService.save(resource);

        Resource savedResource = resourceService.save(resource);
        resourceMonitorService.resourceUpdated(savedResource);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateResource(@PathVariable Integer id, @RequestBody ResourceDTO resourceDTO) {
        Resource resource = resourceService.getResourceById(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        Storage storage = storageService.getStorageById(resourceDTO.getStorageId());

        resource.setName(resourceDTO.getName());
        resource.setType(resourceDTO.getType());
        resource.setQuantity(resourceDTO.getQuantity());
        resource.setUnit(resourceDTO.getUnit());
        resource.setCantidad(resourceDTO.getCantidad());
        resource.setStorage(storage);

        resourceService.save(resource);

        Resource updatedResource = resourceService.save(resource);

        // Notify observers about the updated resource
        resourceMonitorService.resourceUpdated(updatedResource);


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

    @GetMapping("/summary")
    public ResponseEntity<?> getResourceSummary(@RequestParam Integer catastropheId) {
        List<Resource> resources = resourceService.getResourcesByCatastrophe(catastropheId);

        // Group by type
        Map<ResourceType, List<Resource>> resourcesByType = resources.stream()
                .collect(Collectors.groupingBy(Resource::getType));

        List<Map<String, Object>> summaries = new ArrayList<>();

        resourcesByType.forEach((type, typeResources) -> {
            int count = typeResources.size();
            double totalQuantity = typeResources.stream().mapToDouble(Resource::getQuantity).sum();

            // Calculate assigned quantity
            double assignedQuantity = 0.0;
            for (Resource resource : typeResources) {
                Double assigned = resourceAssignmentService.getTotalAssignedQuantity(resource.getId());
                if (assigned != null) {
                    assignedQuantity += assigned;
                }
            }

            Map<String, Object> summary = new HashMap<>();
            summary.put("type", type);
            summary.put("count", count);
            summary.put("totalQuantity", totalQuantity);
            summary.put("assignedQuantity", assignedQuantity);
            summary.put("availableQuantity", totalQuantity - assignedQuantity);

            summaries.add(summary);
        });

        return ResponseEntity.ok(summaries);
    }


}
