package solidarityhub.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@MappedSuperclass
@NoArgsConstructor
public abstract class Person {
    @Id
    private String dNI;

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

    public Person(String dNI, String firstName, String lastName, String email,
                     int phone, String address, String password) {
        this.dNI = dNI;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.homeAddress = address;
        this.password = password;
    }
}