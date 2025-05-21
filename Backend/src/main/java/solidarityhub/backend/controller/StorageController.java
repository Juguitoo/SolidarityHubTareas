package solidarityhub.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import solidarityhub.backend.dto.StorageDTO;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.Storage;
import solidarityhub.backend.service.ResourceService;
import solidarityhub.backend.service.StorageMonitorService;
import solidarityhub.backend.service.StorageService;
import solidarityhub.backend.dto.ResourceDTO;


import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/solidarityhub/storages")
public class StorageController {

    private final StorageService storageService;
    private final ResourceService resourceService;
    @Autowired
    private StorageMonitorService storageMonitorService;

    public StorageController(StorageService storageService, ResourceService resourceService) {
        this.storageService = storageService;
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<?> getStorages() {
        List<StorageDTO> storageDTOList = storageService.getStorages();
        return ResponseEntity.ok(storageDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStorage(@PathVariable Integer id) {
        Storage storage = storageService.getStorageById(id);
        if (storage == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new StorageDTO(storage));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStorage(@PathVariable Integer id, @RequestBody StorageDTO storageDTO) {
        Storage storage = storageService.getStorageById(id);
        if (storage == null) {
            return ResponseEntity.notFound().build();
        }

        storage.setName(storageDTO.getName());
        storage.setFull(storageDTO.isFull());

        storageService.saveStorage(storage);

        Storage updatedStorage = storageService.saveStorage(storage);

        // Notify observers about the updated storage
        storageMonitorService.checkStorage(updatedStorage);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    // POST Y DELETE no está porque no se usan en el frontend
}
