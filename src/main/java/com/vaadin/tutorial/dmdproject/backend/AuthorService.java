package com.vaadin.tutorial.dmdproject.backend;

import com.vaadin.tutorial.dmdproject.AuthorForm;
import org.apache.commons.beanutils.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bbr on 31.10.15.
 */
public class AuthorService {

    private static AuthorService instance;

    private final static String url = "jdbc:postgresql://localhost/dmdprojectdb";
    private final static String user = "bbr";
    private final static String password = "1488";

    public static AuthorService createDemoService() {
        if (instance == null) {

            final AuthorService authorService = new AuthorService();

            instance = authorService;
        }
        return instance;
    }

    public synchronized void save(Author entry) {

        try {
            Class.forName("org.postgresql.Driver"); //TODO i'm not sure if it's necessary.

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {

            Author author = (Author) BeanUtils.cloneBean(entry);
//            Notification.show(entry.getKey() + "  " + entry.getTitle() + "  " + entry.getYear() + "  " + entry.getUrl());
//            update(entry.getKey(), entry.getTitle(), entry.getUrl());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
