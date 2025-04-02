package solidarityhub.frontend.views.volunteer;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import solidarityhub.frontend.dto.VolunteerDTO;
import solidarityhub.frontend.service.VolunteerService;
import java.util.stream.Collectors;

public class VolunteerListView extends VerticalLayout {

    public VolunteerListView() {
        VolunteerService volunteerService = new VolunteerService();

        Checkbox volunteerCheckbox = new Checkbox("Elegir voluntarios automaticamente");



        MultiSelectListBox<String> volunteersListBox = new MultiSelectListBox<>();
        volunteersListBox.setItems(volunteerService.getVolunteers().stream().map(VolunteerDTO::getFirstName).collect(Collectors.toList()));

        volunteerCheckbox.addClickListener(checkboxClickEvent -> volunteersListBox.setEnabled(!volunteerCheckbox.getValue()));

        add(volunteerCheckbox, volunteersListBox);
    }


}
