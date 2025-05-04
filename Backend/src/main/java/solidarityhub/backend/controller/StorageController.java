package solidarityhub.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.StorageDTO;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.service.ResourceService;
import solidarityhub.backend.service.StorageService;
import solidarityhub.backend.dto.ResourceDTO;


import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/solidarityhub/storages")
public class StorageController {

    private final StorageService storageService;
    private final ResourceService resourceService;

    public StorageController(StorageService storageService, ResourceService resourceService) {
        this.storageService = storageService;
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<?> getStorages() {
        List<StorageDTO> storageDTOList = new ArrayList<>();
        storageService.getStorages().forEach(s -> {storageDTOList.add(new StorageDTO(s));});
        return ResponseEntity.ok(storageDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStorage(@PathVariable Integer id) {
        if (storageService.getStorageById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new StorageDTO(storageService.getStorageById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStorage(@PathVariable Integer id, @RequestBody StorageDTO storageDTO) {
        Storage storage = storageService.getStorageById(id);
        if (storage == null) {
            return ResponseEntity.notFound().build();
        }
        List<Resource> resources = new ArrayList<>();
        for (Integer resourceId : storageDTO.getResources()) {
            Resource resource = resourceService.getResourceById(resourceId);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }
            resources.add(resource);
        }

        storage.setName(storageDTO.getName());
        storage.setGpsCoordinates(storageDTO.getGpsCoordinates());
        storage.setFull(storageDTO.isFull());
        storage.setResources(resources);

        storageService.saveStorage(storage);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    // POST Y DELETE no está porque no se usan en el frontend
}
