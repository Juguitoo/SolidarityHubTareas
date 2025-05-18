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

    private boolean isFull;


    public StorageDTO(Storage storage) {
        this.id = storage.getId();
        this.name = storage.getName();
        this.isFull = storage.isFull();
    }

    public StorageDTO(int id, String name, boolean isFull) {
        this.id = id;
        this.name = name;
        this.isFull = isFull;
    }
}
