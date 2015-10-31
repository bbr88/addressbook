package com.vaadin.tutorial.dmdproject.backend;

import com.vaadin.tutorial.dmdproject.AuthorForm;
import com.vaadin.ui.Notification;
import org.apache.commons.beanutils.BeanUtils;

import java.sql.*;
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

    public boolean authorExists(Author author) {
        boolean result = false;
        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement st = c.createStatement()) {

            ResultSet rs = st.executeQuery("SELECT authors.authorid FROM AUTHORS WHERE name = \'" + author.getName() + "\'");

            result = rs.next();
            rs.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public void createNewAuthor(Author author) {
        int newAuthorID = 0;

        if (authorExists(author)) {
            Notification.show("Author with name " + author.getName() + " already exists.", Notification.Type.ERROR_MESSAGE);
            return;
        }
        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement st = c.createStatement()) {

            ResultSet rs = st.executeQuery("SELECT AuthorID FROM AUTHORS ORDER BY AuthorID DESC LIMIT 1");

            if (rs.next()) {
                newAuthorID = rs.getInt(1) + 1;
                String sqlQuery = "INSERT INTO AUTHORS (name, AuthorID, lab, university) VALUES" +
                        "(\'" + author.getName() + "\', " + newAuthorID + ", \'" + author.getLab() + "\', \'" + author.getUniversity() + "\');";

                st.executeUpdate(sqlQuery);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void save(Author entry) {

        try {
            Class.forName("org.postgresql.Driver"); //TODO i'm not sure if it's necessary.

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {

            Author author = (Author) BeanUtils.cloneBean(entry);
            createNewAuthor(author);
//            Notification.show(entry.getKey() + "  " + entry.getTitle() + "  " + entry.getYear() + "  " + entry.getUrl());
//            update(entry.getKey(), entry.getTitle(), entry.getUrl());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
