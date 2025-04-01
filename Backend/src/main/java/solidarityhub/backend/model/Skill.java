package solidarityhub.backend.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Skill {
    @Id
    private String name;

    public Skill(String name) {
        this.name = name;
    }
}

