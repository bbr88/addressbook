package com.vaadin.tutorial.dmdproject;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.tutorial.dmdproject.authentication.AccessControl;
import com.vaadin.tutorial.dmdproject.authentication.LoginUI;
import com.vaadin.tutorial.dmdproject.authentication.TestAccessControl;
import com.vaadin.tutorial.dmdproject.backend.Paper;
import com.vaadin.tutorial.dmdproject.backend.PaperService;
import com.vaadin.ui.*;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.grid.MGrid;

import javax.servlet.annotation.WebServlet;

/* User Interface written in Java.
 *
 * Define the user interface shown on the Vaadin generated web page by extending the UI class.
 * By default, a new UI instance is automatically created when the page is loaded. To reuse
 * the same instance, add @PreserveOnRefresh.
 */
@Title("Paper search")
//@Theme("valo")
//@Theme("dmdproject")
@Theme("mockapp")
//@Theme("reindeer")
//@Theme("runo")
public class PapersUI extends UI {







    /* Hundreds of widgets.
     * Vaadin's user interface components are just Java objects that encapsulate
     * and handle cross-browser support and client-server communication. The
     * default Vaadin components are in the com.vaadin.ui package and there
     * are over 500 more in vaadin.com/directory.
     */
    TextField filter = new TextField();
    Grid paperList = new MGrid();
    Button newContact = new Button("New paper");

    // PaperForm is an example of a custom component class
    PaperForm paperForm = new PaperForm();

    // PaperService is a in-memory mock DAO that mimics
    // a real-world datasource. Typically implemented for
    // example as EJB or Spring Data based service.
    PaperService service = PaperService.createDemoService();


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

    /*protected void init(VaadinRequest request) {
        setLocale(request.getLocale());
        getPage().setTitle("DBLP for u and me");
        if (!accessControl.isUserSigned()) {
            setContent(new LoginUI(accessControl, new LoginUI.LoginListener() {
                @Override
                public void loginSuccessful() {
                    configureComponents();
                    buildLayout();
                }
            }));
        } else {
            configureComponents();
            buildLayout();
        }
    }*/

    /*protected void showMainView() {
        addStyleName(ValoTheme.UI_WITH_MENU);
        //setContent(new MainScreen(MockAppUI.this));
        configureComponents();
        buildLayout();
        getNavigator().navigateTo(getNavigator().getState());
    }*/

    private void configureComponents() {
         /* Synchronous event handling.
         *
         * Receive user interaction events on the server-side. This allows you
         * to synchronously handle those events. Vaadin automatically sends
         * only the needed changes to the web page without loading a new page.
         */
        newContact.addClickListener(e -> paperForm.edit(new Paper()));

        filter.setInputPrompt("Filter papers...");
        filter.addTextChangeListener(e -> refreshContacts(e.getText()));

        BeanItemContainer<Paper> myBean = new BeanItemContainer<Paper>(Paper.class);
        paperList = new Grid(myBean);
        //paperList.setContainerDataSource(new BeanItemContainer<>(Paper.class));
        paperList.setColumnOrder("name", "title", "type", "year");
        paperList.removeColumn("key");
        paperList.setSelectionMode(Grid.SelectionMode.SINGLE);
        paperList.addSelectionListener(e
                -> paperForm.edit((Paper) paperList.getSelectedRow()));
        refreshContacts();
    }

    /* Robust layouts.
     *
     * Layouts are components that contain other components.
     * HorizontalLayout contains TextField and Button. It is wrapped
     * with a Grid into VerticalLayout for the left side of the screen.
     * Allow user to resize the components with a SplitPanel.
     *
     * In addition to programmatically building layout in Java,
     * you may also choose to setup layout declaratively
     * with Vaadin Designer, CSS and HTML.
     */
    private void buildLayout() {

        HorizontalLayout actions = new HorizontalLayout(filter, newContact);
        actions.setWidth("100%");
        filter.setWidth("100%");
        actions.setExpandRatio(filter, 1);

        VerticalLayout left = new VerticalLayout(actions, paperList);
        left.setSizeFull();
        paperList.setSizeFull();
        left.setExpandRatio(paperList, 1);

        HorizontalLayout mainLayout = new HorizontalLayout(left, paperForm);
        mainLayout.setSizeFull();
        mainLayout.setExpandRatio(left, 1);


        // Split and allow resizing
        setContent(mainLayout);

    }

    /* Choose the design patterns you like.
     *
     * It is good practice to have separate data access methods that
     * handle the back-end access and/or the user interface updates.
     * You can further split your code into classes to easier maintenance.
     * With Vaadin you can follow MVC, MVP or any other design pattern
     * you choose.
     */
    void refreshContacts() {
        refreshContacts(filter.getValue());
    }

    private void refreshContacts(String stringFilter) {
        paperList.setContainerDataSource(new BeanItemContainer<>(
                Paper.class, service.findAll(stringFilter)));
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


}
