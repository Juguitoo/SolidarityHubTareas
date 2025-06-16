package solidarityhub.frontend.views.task.manageTasks;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.VaadinSession;
import solidarityhub.frontend.dto.*;
import solidarityhub.frontend.i18n.Translator;
import solidarityhub.frontend.service.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.pingu.domain.enums.EmergencyLevel;
import org.pingu.domain.enums.Priority;
import org.pingu.domain.enums.Status;
import org.pingu.domain.enums.TaskType;
import solidarityhub.frontend.views.HeaderComponent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import solidarityhub.frontend.views.task.AssignResourceDialog;
import solidarityhub.frontend.views.task.TaskComponent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Añadir tarea")
@Route("tasks/addtask")
public class AddTaskView extends ManageTaskBaseView implements BeforeEnterObserver {

    public AddTaskView() {
        super();
    }

    private void saveNewTask(){
        if (validateForm()) {
            try {
                TaskDTO newTaskDTO = getTaskDTO();
                TaskDTO SavedTask = taskService.addAndGetNewTask(newTaskDTO);

                saveAssignedResources(SavedTask);

                getConfirmationDialog().open();

            } catch (Exception ex) {
                Notification.show(translator.get("error_saving_task") + ex.getMessage(),
                        5000, Notification.Position.MIDDLE);
            }
        } else {
            Notification.show(translator.get("check_fields"),
                    3000, Notification.Position.MIDDLE);
        }
    }

    //===============================Get Components=========================================
    @Override
    protected Component getHeader(){
        return new HeaderComponent(translator.get("add_task_title"), "window.history.back()");
    }

    @Override
    protected Component getActionButtons() {
        HorizontalLayout buttons = new HorizontalLayout();

        Button saveTaskButton = new Button(translator.get("add_button"));
        saveTaskButton.addClickListener(e -> saveNewTask());

        Button cancel = new Button(translator.get("cancel_button"));
        cancel.addClickListener(e -> exitWithoutSavingDialog(translator.get("confirm_cancel_add_text")));

        buttons.add(cancel, saveTaskButton);
        setAlignSelf(Alignment.END, buttons);

        return buttons;
    }

}