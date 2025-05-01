package solidarityhub.backend.service;

import solidarityhub.backend.model.Person;
import solidarityhub.backend.repository.PersonRepository;
import org.springframework.stereotype.Service;

@Service
public class PersonService {
    private final PersonRepository personRepository;
    public PersonService(PersonRepository personRepository) {this.personRepository = personRepository;}
    public Person save(Person person) {return personRepository.save(person);}
}
