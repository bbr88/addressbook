package com.vaadin.tutorial.dmdproject.authentication;

/**
 * Created by bbr on 26.10.15.
 */

import com.vaadin.server.VaadinSession;
import com.vaadin.server.WrappedSession;

/**
 * Retrieve and set the name of the current user
 * of the current session.
 */
public class User {

    /**
     * The attribute key used to store the username in the current session.
     */
    public static final String USERS_SESSION_ATTRIBUTE_KEY = User.class.getCanonicalName();

    //default constructor
    private User() {}

    /**
     * Returns the name of the current user
     * in the current session or an empty string
     * if there's no username.
     */
    public static String getUserName() {
        String username = (String) getHttpSession().getAttribute(USERS_SESSION_ATTRIBUTE_KEY);
        return username == null ? null : username;
    }

    private static WrappedSession getHttpSession() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            throw new IllegalStateException(
                    "There's no session for current thread.");
        }
        return session.getSession();
    }

    /**
     * Set the name of the current user
     * in the current session. If the name is {@code null}
     * then the user will be removed from the session.
     */
    public static void setUsername(String username) {
        if (username == null || username.length() == 0) {
            getHttpSession().removeAttribute(USERS_SESSION_ATTRIBUTE_KEY);
        } else {
            getHttpSession().setAttribute(USERS_SESSION_ATTRIBUTE_KEY, username);
        }
    }

}
