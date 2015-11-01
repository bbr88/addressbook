package com.vaadin.tutorial.dmdproject.graph;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Created by bbr on 01.11.15.
 */
public class Graph extends HorizontalLayout implements View {

    public static final String VIEW_NAME = "Graph";

    public Graph() {
        CustomLayout aboutContent = new CustomLayout("graph");
        aboutContent.setStyleName("about-content");

        aboutContent.addComponent(new Label("graph"));

        setSizeFull();
        setStyleName("about-view");
        addComponent(aboutContent);
        setComponentAlignment(aboutContent, Alignment.MIDDLE_CENTER);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
    }
}
