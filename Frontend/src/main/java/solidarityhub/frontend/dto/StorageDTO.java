package solidarityhub.frontend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class StorageDTO {
    private int id;
    private String name;
    private boolean isFull;

    public StorageDTO(String name, boolean isFull) {
        this.name = name;
        this.isFull = isFull;
    }
}
