package solidarityhub.frontend.views.home;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import org.pingu.domain.enums.EmergencyLevel;
import solidarityhub.frontend.dto.CatastropheDTO;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.CatastropheService;
import solidarityhub.frontend.views.HeaderComponent;
import com.vaadin.flow.component.html.Image;
import java.util.Locale;

@PageTitle("Inicio")
@Route("home")
public class HomeView extends VerticalLayout implements BeforeEnterObserver {

    private final CatastropheService catastropheService;
    private CatastropheDTO selectedCatastrophe;
    protected static Translator translator;

    public HomeView() {
        Locale sessionLocale = VaadinSession.getCurrent().getAttribute(Locale.class);
        if (sessionLocale != null) {
            UI.getCurrent().setLocale(sessionLocale);
        }else{
            VaadinSession.getCurrent().setAttribute(Locale.class, new Locale("es"));
            UI.getCurrent().setLocale(new Locale("es"));
        }
        translator = new Translator(UI.getCurrent().getLocale());

        this.catastropheService = new CatastropheService();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedCatastrophe = (CatastropheDTO) VaadinSession.getCurrent().getAttribute("selectedCatastrophe");
        if (!catastropheService.isCatastropheSelected(event, selectedCatastrophe)) {
            return;
        }

        buildView();
    }

    private void buildView(){
        removeAll();

        setWidthFull();
        addClassName("home-view");

        HeaderComponent header = new HeaderComponent(selectedCatastrophe.getName());

        HorizontalLayout contentLayout = new HorizontalLayout();
        contentLayout.setWidthFull();
        contentLayout.setAlignItems(Alignment.START);
        contentLayout.add(
                getAdminCard(),
                getCatastropheCard()
        );

        add(
            header,
            contentLayout
        );
    }

    //===============================Get Components=========================================
    //Admin card
    private Component getAdminCard() {
        VerticalLayout adminInfo = new VerticalLayout();
        adminInfo.addClassName("admin-info-card");
        adminInfo.setWidth("50%");

        adminInfo.add(
            getAdminHeader(),
            getAdminPreferences()
        );

        return adminInfo;
    }

    private Component getAdminHeader() {
        HorizontalLayout adminInfoHeader = new HorizontalLayout();
        adminInfoHeader.setAlignItems(Alignment.CENTER);

        Image adminIcon = new Image("images/profile.png", "Admin Icon");
        adminIcon.addClassName("admin-icon");

        VerticalLayout adminInfoText = new VerticalLayout();
        adminInfoText.setPadding(false);
        adminInfoText.setSpacing(false);
        H3 adminName = new H3("Perro Sanchez");
        Span adminRole = new Span("Presidente del Gobierno");
        adminInfoText.add(adminName, adminRole);

        adminInfoHeader.add(adminIcon, adminInfoText);
        return adminInfoHeader;
    }

    private Component getAdminPreferences() {
        VerticalLayout adminPreferences = new VerticalLayout();
        //translator.get("home-preferences")
        Span preferencesTitle = new Span("Preferencias");

        HorizontalLayout preferencesButtons = new HorizontalLayout();
        preferencesButtons.setWidthFull();
        preferencesButtons.add(getThemeBtn(), getLanguageSelector());

        adminPreferences.add(preferencesTitle, preferencesButtons);

        return adminPreferences;
    }

    private Component getThemeBtn(){
        Span themeIcon = new Span();
        themeIcon.setText("🌙");

        Button toggleTheme = new Button(themeIcon);
        toggleTheme.addClassName("theme-toggle-btn");
        toggleTheme.getElement().setAttribute("aria-label", "Cambiar tema");

        toggleTheme.addClickListener(event -> UI.getCurrent().getPage().executeJs("""
                const html = document.documentElement;
                const current = html.getAttribute('data-theme');
                const next = current === 'dark' ? 'light' : 'dark';
                html.setAttribute('data-theme', next);
                localStorage.setItem('theme', next);
                const icon = next === 'dark' ? '🌙' : '🌞';
                document.getElementById('theme-icon').textContent = icon;"""
        ));
        themeIcon.setId("theme-icon");
        return toggleTheme;
    }

    private Component getLanguageSelector(){
        ComboBox<String> languageSelector = new ComboBox<>();
        languageSelector.setAllowCustomValue(false);
        languageSelector.addClassName("language-selector");
        languageSelector.setItems("Español", "Valencià", "English");
        languageSelector.setValue(getCurrentLanguage());

        languageSelector.addValueChangeListener(event -> {
            String selected = event.getValue();
            Locale newLocale = switch (selected) {
                case "English" -> new Locale("en");
                case "Valencià" -> new Locale("va");
                default -> new Locale("es");
            };

            VaadinSession.getCurrent().setAttribute(Locale.class, newLocale);
            UI.getCurrent().setLocale(newLocale);
            UI.getCurrent().getPage().reload();
        });
        return languageSelector;
    }

    private String getCurrentLanguage() {
        Locale current = UI.getCurrent().getLocale();
        return switch (current.getLanguage()) {
            case "en" -> "English";
            case "va" -> "Valencià";
            default -> "Español";
        };
    }

    //Catastrophe card
    private Component getCatastropheCard() {
        VerticalLayout catastropheInfo = new VerticalLayout();
        catastropheInfo.addClassName("catastrophe-info-card");
        catastropheInfo.setWidth("50%");

        //translator.get("catastrophe_title")
        H3 catastropheTitle = new H3("Sobre esta catástrofe:");
        Div catastropheDescription = new Div(selectedCatastrophe.getDescription());

        catastropheInfo.add(
            catastropheTitle,
            catastropheDescription,
            getCatastropheInfo()
        );

        return catastropheInfo;
    }

    private Component getCatastropheInfo() {
        HorizontalLayout catastropheInfo = new HorizontalLayout();

        Span catastropheEmergencyLevel = new Span(translator.get("emergency_level") + formatEmergencyLevel(selectedCatastrophe.getEmergencyLevel()));
        Span catastropheDate = new Span(translator.get("start_date_catastrophe") + selectedCatastrophe.getStartDate().toString());

        catastropheInfo.add(catastropheEmergencyLevel, catastropheDate);
        return catastropheInfo;
    }

    private Component getCatastropheStatistics() {
        VerticalLayout catastropheStatistics = new VerticalLayout();
        catastropheStatistics.addClassName("catastrophe-statistics-card");

        //translator.get("catastrophe_statistics")
        Span statisticsTitle = new Span("Estadísticas de la catástrofe:");



        catastropheStatistics.add(statisticsTitle);

        return catastropheStatistics;
    }

    //===============================Format methods=========================================
    private static String formatEmergencyLevel(EmergencyLevel level) {
        if (level == null) return translator.get("unknown_emergency_level");

        return switch (level) {
            case LOW -> translator.get("low_emergency_level");
            case MEDIUM -> translator.get("medium_emergency_level");
            case HIGH -> translator.get("high_emergency_level");
            case VERYHIGH -> translator.get("very_high_emergency_level");
        };
    }
}
