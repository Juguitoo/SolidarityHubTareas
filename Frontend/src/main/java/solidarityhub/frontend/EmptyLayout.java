package solidarityhub.frontend;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.RouterLayout;


public class EmptyLayout extends Div implements RouterLayout {

    public EmptyLayout() {
        // Layout completamente vacío
        setSizeFull();
        getStyle().set("padding", "0");
        getStyle().set("margin", "0");
    }

    public void showRouterLayoutContent(Component content) {
        getElement().removeAllChildren();
        getElement().appendChild(content.getElement());
    }
}
