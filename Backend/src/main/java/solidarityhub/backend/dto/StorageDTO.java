package solidarityhub.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import solidarityhub.backend.model.Storage;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StorageDTO)) return false;

        StorageDTO that = (StorageDTO) o;

        if (id != that.id) return false;
        if (isFull != that.isFull) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }
}
