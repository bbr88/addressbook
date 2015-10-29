package com.vaadin.tutorial.dmdproject;

import com.vaadin.navigator.View;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.Reindeer;

/**
 * Created by bbr on 28.10.15.
 */
public class ErrorView extends VerticalLayout implements View {
    private Label explanation;

    public ErrorView() {
        setMargin(true);
        setSpacing(true);

        Label header = new Label("The view could not be found");
        header.addStyleName(Reindeer.LABEL_H1);
        addComponent(header);
        addComponent(explanation = new Label());
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        explanation.setValue(String.format(
                "You tried to navigate to a view ('%s') that does not exist.",
                event.getViewName()));
    }

}
