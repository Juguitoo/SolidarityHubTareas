package solidarityhub.frontend.views.task;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;

@Getter
public class CheckTaskComponent extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<CheckTaskComponent, Boolean>, Boolean> {
    private final TaskComponent taskComponent;
    private final Checkbox checkBox;

    public CheckTaskComponent(TaskComponent taskComponent) {
        this.taskComponent = taskComponent;
        this.checkBox = new Checkbox();

        add(checkBox, taskComponent);
    }

    @Override
    public void setValue(Boolean aBoolean) {
        checkBox.setValue(aBoolean);
    }

    @Override
    public Boolean getValue() {
        return checkBox.getValue();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<CheckTaskComponent, Boolean>> valueChangeListener) {
        return null;
    }

    @Override
    public void setReadOnly(boolean b) {

    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean b) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
