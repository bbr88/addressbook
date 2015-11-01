package com.vaadin.tutorial.dmdproject.about;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.Version;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import java.awt.*;


/**
 * Created by bbr on 28.10.15.
 */
public class Authors extends VerticalLayout implements View {

    public static final String VIEW_NAME = "Authors";

    public Authors() {
        CustomLayout aboutContent = new CustomLayout("authorsview");
        aboutContent.setStyleName("about-content");

        aboutContent.addComponent(new Label("info"));

        setSizeFull();
        setStyleName("about-view");
        addComponent(aboutContent);
        setComponentAlignment(aboutContent, Alignment.MIDDLE_CENTER);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
    }

}
