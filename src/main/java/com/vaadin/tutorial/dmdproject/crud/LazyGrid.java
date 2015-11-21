package com.vaadin.tutorial.dmdproject.crud;

import com.google.gwt.core.shared.GWT;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.grid.GridClientRpc;
import com.vaadin.shared.ui.grid.ScrollDestination;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bbr on 04.11.15.
 */
public class LazyGrid extends Grid implements LazyGridScrollListener {

    private static final long serialVersionUID = 231241551512L;
    List<LazyGridScrollListener> listeners = new ArrayList<>();

    private void fireScrollEvent() {
        listeners.stream()
                 .forEach(LazyGridScrollListener::scrollTheGrid);
    }

    public LazyGrid(BeanItemContainer container) {
        super(container);
    }

    @Override
    public void scrollTheGrid() {
        Notification.show("scroll meh moar, bitch!", Notification.Type.TRAY_NOTIFICATION);
    }


}
