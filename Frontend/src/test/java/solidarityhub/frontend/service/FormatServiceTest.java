package solidarityhub.frontend.service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class FormatServiceTest {

    @BeforeEach
    public void setup() {
        VaadinSession mockSession = Mockito.mock(VaadinSession.class);
        UI mockUI = Mockito.mock(UI.class);
        VaadinRequest mockRequest = Mockito.mock(VaadinRequest.class);

        VaadinSession.setCurrent(mockSession);
        VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
        UI.setCurrent(mockUI);
    }

    @Test
    public void testSingletonInstance() {
        FormatService s1 = FormatService.getInstance();
        FormatService s2 = FormatService.getInstance();
        assertSame(s1, s2);
    }
}