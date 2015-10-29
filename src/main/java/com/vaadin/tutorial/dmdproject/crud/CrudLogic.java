package com.vaadin.tutorial.dmdproject.crud;

import com.vaadin.tutorial.dmdproject.PapersUI;

/**
 * Created by bbr on 28.10.15.
 */
public class CrudLogic { //TODO

    private CrudView crudView;

    public CrudLogic(CrudView crudView) {
        this.crudView = crudView;
    }

    public void init() {
        if (!PapersUI.get().getAccessControl().isUserInRole("admin")) {
            crudView.setPaperListEnabled(false);
            crudView.setNewPaperEnabled(false);
        }
//        crudView.refreshContacts();
//        crudView.setIt();
    }
}
