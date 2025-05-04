package solidarityhub.backend.dto;

import jakarta.websocket.server.ServerEndpoint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.GPSCoordinates;
import solidarityhub.backend.model.Resource;
import solidarityhub.backend.model.Storage;

import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class StorageDTO {
    private int id;

    private String name;

    private GPSCoordinates gpsCoordinates;

    private boolean isFull;

    private List<Integer> resources;

    public StorageDTO(Storage storage) {
        this.id = storage.getId();
        this.name = storage.getName();
        this.gpsCoordinates = storage.getGpsCoordinates();
        this.isFull = storage.isFull();
        this.resources = storage.getResources().stream().map(Resource::getId).toList();
    }
}
