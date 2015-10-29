package com.vaadin.tutorial.dmdproject.authentication;

/**
 * Created by bbr on 26.10.15.
 */
public class TestAccessControl implements AccessControl {

    @Override
    public boolean signIn(String username, String password) {
        if (username == null || username.length() == 0) {
            return false;
        }

        User.setUsername(username); //TODO it probably would be better if setUsername method returns boolean.
        return true;
    }

    @Override
    public boolean isUserInRole(String role) {
        if ("admin".equals(role)) {
            //the only user is in the admin role
            return getPrincipalName().equals("admin"); //TODO some kind of registration.
        }
        //all users are in all non-admin roles
        return true;
    }

    @Override
    public boolean isUserSigned() {
        return User.getUserName() != null && !User.getUserName().isEmpty();

    }

    @Override
    public String getPrincipalName() {
        return User.getUserName();
    }
}
