package solidarityhub.backend.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@MappedSuperclass
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "dni")
public abstract class Person {
    @Id
    private String dni;

    @Setter
    @Column(nullable = false)
    private String firstName;

    @Setter
    @Column(nullable = false)
    private String lastName;

    @Setter
    @Column(unique=true, nullable = false)
    private String email;

    @Setter
    @Column(nullable = false)
    private int phone;

    @Setter
    @Column(nullable = false)
    private String homeAddress;

    @Setter
    @Column(nullable = false)
    private String password;

    public Person(String dni, String firstName, String lastName, String email,
                     int phone, String address, String password) {
        this.dni = dni;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.homeAddress = address;
        this.password = password;
    }
}