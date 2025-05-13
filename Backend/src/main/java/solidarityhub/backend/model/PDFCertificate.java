package solidarityhub.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class PDFCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String titulo;

    @ManyToOne
    @JoinColumn(name = "volunteer_dni")
    private Volunteer volunteer;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @Lob
    private byte[] contenido;

    public PDFCertificate (String titulo, Volunteer volunteer, Task task, byte[] contenido) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.volunteer = volunteer;
        this.task = task;
    }

    public PDFCertificate(String titulo, byte[] contenido) {
        this.titulo = titulo;
        this.contenido = contenido;
    }
}
