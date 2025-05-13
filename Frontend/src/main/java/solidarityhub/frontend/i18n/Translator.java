package solidarityhub.frontend.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class Translator {
    private final ResourceBundle bundle;

    public Translator(Locale locale) {
        this.bundle = ResourceBundle.getBundle("i18n.messages", locale);
    }

    public String get(String key) {
        return bundle.getString(key);
    }
}
