package solidarityhub.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor
public class Admin {
    @Id
    private String dni;

    @Setter
    @Column(nullable = false)
    private String password;

    public Admin(String dni, String password) {
        this.dni = dni;
        this.password = password;
    }
}
