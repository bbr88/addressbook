package com.vaadin.tutorial.dmdproject.authentication;

/**
 * Created by bbr on 26.10.15.
 */

public interface AccessControl {
    public boolean signIn(String username, String password);
    public boolean isUserSigned();
    public boolean isUserInRole(String role);
    public String getPrincipalName();
}
