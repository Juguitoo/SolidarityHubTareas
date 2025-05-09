package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class PDFDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Getter
    @Lob
    private byte[] contenido;

    public PDFDocument (String nombre, byte[] contenido) {
        this.nombre = nombre;
        this.contenido = contenido;
    }
}
