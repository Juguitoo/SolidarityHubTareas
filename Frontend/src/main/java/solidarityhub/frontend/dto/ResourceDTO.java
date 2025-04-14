package solidarityhub.frontend.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ResourceDTO {
    private int id;
    private String name;
    private String type;
    private double quantity;
    private Integer catastropheId;

    public ResourceDTO(String name, String type, double quantity) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.catastropheId = null;
    }

    public ResourceDTO(int id, String name, String type, double quantity, Integer catastropheId) {
        this(name, type, quantity);
        this.catastropheId = catastropheId;
    }

    // Setter específico para el ID de catástrofe
    public void setCatastropheId(Integer catastropheId) {
        this.catastropheId = catastropheId;
    }
}
