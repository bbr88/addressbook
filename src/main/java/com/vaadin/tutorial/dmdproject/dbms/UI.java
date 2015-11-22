package com.vaadin.tutorial.dmdproject.dbms;

import java.io.IOException;

public class UI {

    public static void main(String[] args) throws IOException {

        // checks system catalog and loads table names

        UIHelper.init();
        SystemCat.loadTable("test");
    }
}
