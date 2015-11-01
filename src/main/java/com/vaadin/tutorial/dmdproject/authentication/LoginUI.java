package com.vaadin.tutorial.dmdproject.authentication;

import com.vaadin.annotations.Theme;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.button.MButton;

import java.io.Serializable;

/**
 * Created by bbr on 26.10.15.
 */
@Theme("mockapp")
public class LoginUI extends CssLayout {

    private TextField username;
    private PasswordField password;
    private MButton loginButton;
    private MButton forgotPasswordButton;
    private LoginListener loginListener;
    private AccessControl accessControl;

    public LoginUI(AccessControl accessControl, LoginListener loginListener) {
        this.accessControl = accessControl;
        this.loginListener = loginListener;
        buildUI();
        username.focus();
    }

    private void buildUI() {
        addStyleName("login-screen");

        //login form, centered in the available part of the screen
        Component loginForm = buildLoginForm();

        //layout to center login form.
        VerticalLayout centeringLayout = new VerticalLayout();
        centeringLayout.setStyleName("centering-layout");
        centeringLayout.addComponent(loginForm);
        centeringLayout.setComponentAlignment(loginForm, Alignment.MIDDLE_CENTER);

        //info about logging in
        CssLayout loginInfo = buildLoginInfo();

        addComponent(centeringLayout);
        addComponent(loginInfo);
    }

    private Component buildLoginForm() {
        FormLayout loginForm = new FormLayout();

        loginForm.addStyleName("login-form");
//        loginForm.setSizeFull(); //TODO or undefined
        loginForm.setSizeUndefined();
        loginForm.setMargin(false);

        loginForm.addComponent(username = new TextField("Username", "")); //("Username", "admin") to fill the field with default uname
        username.setWidth(15, Unit.EM); //have no idea what this metric means

        loginForm.addComponent(password = new PasswordField("Password"));
        password.setWidth(15, Unit.EM);
        password.setDescription("There's no password at this moment. Just write anything"); //TODO registration!

        CssLayout buttons = new CssLayout();
        buttons.setStyleName("buttons");
        loginForm.addComponent(buttons);

        buttons.addComponent(loginButton = new MButton("Login"));
        loginButton.setDisableOnClick(true);

        loginButton.addClickListener(new MButton.ClickListener() {
            @Override
            public void buttonClick(MButton.ClickEvent event) {
                try {
                    login();
                } finally {
                    loginForm.setEnabled(true);
                }
            }
        });
        loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        loginButton.addStyleName(ValoTheme.BUTTON_FRIENDLY); //or _LINK

        buttons.addComponent(forgotPasswordButton = new MButton("Forgot your password?"));
        forgotPasswordButton.addClickListener(new MButton.ClickListener() {
            @Override
            public void buttonClick(MButton.ClickEvent event) {
                showNotification(new Notification("Hint: Just write anything.")); //TODO registration
            }
        });
        forgotPasswordButton.addStyleName(ValoTheme.BUTTON_LINK);
        return loginForm;
    }

    private CssLayout buildLoginInfo() {
        CssLayout loginInfo = new CssLayout();
        loginInfo.setStyleName("login-info");
        Label loginInfoText = new Label(
                "<h1>Login Information</h1>"
                + "Log in as &quot;admin&quot; to have edit capabilities." +
                        "Log in with any username to have read-only access. " +
                        "For every user any password is correct (at this time)",
                ContentMode.HTML);
        loginInfo.addComponent(loginInfoText);
        return loginInfo;
    }

    private void login() {
        if (accessControl.signIn(username.getValue(), password.getValue())) {
            loginListener.loginSuccessful();
        }
    }

    public interface LoginListener extends Serializable {
        void loginSuccessful();
    }

    private void showNotification(Notification notification) {
        // keep the notification visible a little while after moving the
        // mouse, or until clicked
        notification.setDelayMsec(2000);
        notification.show(Page.getCurrent());
    }

}
