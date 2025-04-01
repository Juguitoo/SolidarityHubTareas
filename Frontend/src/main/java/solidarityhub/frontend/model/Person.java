package solidarityhub.frontend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public abstract class Person {
    private String dni;

    @Setter
    private String firstName;

    @Setter
    private String lastName;

    @Setter
    private String email;


    public Person(String dni, String firstName, String lastName, String email) {
        this.dni = dni;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}