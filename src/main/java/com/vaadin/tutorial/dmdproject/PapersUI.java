package com.vaadin.tutorial.dmdproject;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.tutorial.dmdproject.authentication.AccessControl;
import com.vaadin.tutorial.dmdproject.authentication.LoginUI;
import com.vaadin.tutorial.dmdproject.authentication.TestAccessControl;
import com.vaadin.tutorial.dmdproject.backend.AuthorService;
import com.vaadin.tutorial.dmdproject.backend.Paper;
import com.vaadin.tutorial.dmdproject.backend.PaperService;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.grid.MGrid;

import javax.servlet.annotation.WebServlet;

//User Interface written in Java.
@Title("Paper search")
@Theme("mockapp")
public class PapersUI extends UI {

    TextField filter = new TextField();
    Grid paperList = new MGrid();
    Button newContact = new Button("New paper");

    // PaperForm is an example of a custom component class
    PaperForm paperForm = new PaperForm();

    // PaperService and authorService are an in-memory mock DAO that mimics
    // a real-world datasource. Typically implemented for
    // example as EJB or Spring Data based paperService.
    PaperService paperService = PaperService.createDemoService();
    AuthorService authorService = AuthorService.createDemoService();

    /* The "Main method".
     *
     * This is the entry point method executed to initialize and configure
     * the visible user interface. Executed on every browser reload because
     * a new instance is created for each web page loaded.
     */
    private AccessControl accessControl = new TestAccessControl();

    public static PapersUI get() {
        return (PapersUI) UI.getCurrent();
    }

    public AccessControl getAccessControl() {
        return accessControl;
    }

    @Override
    protected void init(VaadinRequest request) {
        Responsive.makeResponsive(this);
        setLocale(request.getLocale());
        getPage().setTitle("DBLP for u and me");
        if (!accessControl.isUserSigned()) {
            setContent(new LoginUI(accessControl, new LoginUI.LoginListener() {
                @Override
                public void loginSuccessful() {
                    showMainView();
                }
            }));
        } else {
            showMainView();
        }
    }

    protected void showMainView() {
        addStyleName(ValoTheme.UI_WITH_MENU);
        setContent(new MainScreen(PapersUI.this));
        getNavigator().navigateTo(getNavigator().getState());
    }


    void refreshContacts() {
        refreshContacts(filter.getValue());
    }

    private void refreshContacts(String stringFilter) {
        paperList.setContainerDataSource(new BeanItemContainer<>(
                Paper.class, paperService.findAll(stringFilter)));
        paperForm.setVisible(false);
    }




    /*  Deployed as a Servlet or Portlet.
     *
     *  You can specify additional servlet parameters like the URI and UI
     *  class name and turn on production mode when you have finished developing the application.
     */
    @WebServlet(urlPatterns = "/*")
    @VaadinServletConfiguration(ui = PapersUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }

    public static void main(String[] args) {

    }
}
