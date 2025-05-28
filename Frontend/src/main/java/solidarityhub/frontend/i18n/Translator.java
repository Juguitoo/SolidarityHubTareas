package solidarityhub.frontend.i18n;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.util.Locale;
import java.util.ResourceBundle;

@NoArgsConstructor
public class Translator {
    private ResourceBundle bundle;

    public Translator(Locale locale) {
        this.bundle = ResourceBundle.getBundle("i18n.messages", locale);
    }

    public void setBundle(Locale locale) {
        this.bundle = ResourceBundle.getBundle("i18n.messages", locale);
    }

    public String get(String key) {
        return bundle.getString(key);
    }

    public void initializeTranslator() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        } else {
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        this.setBundle(UI.getCurrent().getLocale());
    }
}
