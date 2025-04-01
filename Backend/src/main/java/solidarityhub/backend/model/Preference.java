package solidarityhub.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Preference {
    @Id
    private String name;

    public Preference(String name) {
        this.name = name;
    }
}
