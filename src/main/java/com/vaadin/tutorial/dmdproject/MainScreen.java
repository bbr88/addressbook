package com.vaadin.tutorial.dmdproject;


import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.tutorial.dmdproject.crud.CrudView;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;

import com.vaadin.tutorial.dmdproject.about.About;

/**
 * Created by bbr on 28.10.15.
 */
public class MainScreen extends HorizontalLayout {

    private Menu menu;

    public MainScreen(PapersUI ui) {

        CssLayout mainContainer = new CssLayout();
        mainContainer.addStyleName("valo-content");
        mainContainer.setSizeFull();

        final Navigator navigator = new Navigator(ui, mainContainer);
        navigator.setErrorView(ErrorView.class);
        menu = new Menu(navigator);
        menu.addView(new CrudView(), CrudView.VIEW_NAME, CrudView.VIEW_NAME, FontAwesome.EDIT);
        menu.addView(new About(), About.VIEW_NAME, About.VIEW_NAME, FontAwesome.INFO_CIRCLE);

        navigator.addViewChangeListener(viewChangeListener);

        addComponent(menu);
        addComponent(mainContainer);
        setExpandRatio(mainContainer, 1);
        setSizeFull();
    }

    // notify the view menu about view changes so that it can display which view
    // is currently active
    ViewChangeListener viewChangeListener = new ViewChangeListener() {

        @Override
        public boolean beforeViewChange(ViewChangeEvent event) {
            return true;
        }

        @Override
        public void afterViewChange(ViewChangeEvent event) {
            menu.setActiveView(event.getViewName());
        }

    };
}
