package solidarityhub.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import solidarityhub.backend.model.Person;
import solidarityhub.backend.model.ScheduleAvailability;
import solidarityhub.backend.model.Volunteer;
import solidarityhub.backend.model.enums.DayMoment;
import solidarityhub.backend.model.enums.TaskType;
import solidarityhub.backend.model.enums.WeekDay;
import solidarityhub.backend.repository.PersonRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        // Arrange
        Person person = createTestPerson();
        when(personRepository.save(any(Person.class))).thenReturn(person);

        // Act
        Person savedPerson = personService.save(person);

        // Assert
        assertNotNull(savedPerson);
        assertEquals("12345678A", savedPerson.getDni());
        assertEquals("John", savedPerson.getFirstName());
        assertEquals("Doe", savedPerson.getLastName());
        verify(personRepository, times(1)).save(person);
    }

    // Helper method
    private Person createTestPerson() {
        // Since Person is abstract, we use a Volunteer as a concrete implementation
        List<TaskType> taskTypes = new ArrayList<>();
        taskTypes.add(TaskType.MEDICAL);

        List<ScheduleAvailability> scheduleAvailabilities = new ArrayList<>();
        scheduleAvailabilities.add(new ScheduleAvailability(DayMoment.MORNING, WeekDay.MONDAY));

        return new Volunteer(
                "12345678A",
                "John",
                "Doe",
                "john.doe@example.com",
                123456789,
                "123 Main St",
                "password",
                taskTypes,
                scheduleAvailabilities
        );
    }
}