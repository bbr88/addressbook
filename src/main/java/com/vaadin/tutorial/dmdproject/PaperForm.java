package com.vaadin.tutorial.dmdproject;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.tutorial.dmdproject.backend.Paper;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.button.MButton;

import java.util.List;

/* Create custom UI Components.
 *
 * Create your own Vaadin components by inheritance and composition.
 * This is a form component inherited from VerticalLayout. Use
 * Use BeanFieldGroup to bind data fields from DTO to UI fields.
 * Similarly named field by naming convention or customized
 * with @PropertyId annotation.
 */
public class PaperForm extends HorizontalLayout {

    private Button save = new MButton(FontAwesome.SAVE, this::save);
    private Button cancel = new Button("Cancel", this::cancel);
    private Button delete = new Button("Delete", this::delete);

    private BeanItemContainer<Paper> myBean = new BeanItemContainer<Paper>(Paper.class);
    private Grid relatedAuthors = new Grid("by author", myBean);
    private Grid relatedTitle = new Grid("by topic", myBean);

    private boolean isInsert = false;

    public boolean isInsert() {
        return isInsert;
    }
    public void setInsert(boolean state) {
        isInsert = state;
    }

    public Button getDelete() {
        return delete;
    }

    private TextField name = new TextField("Author");
    private TextField title = new TextField("Title");
    private TextField type = new TextField("Type");
    private TextField year = new TextField("Year");
    private DateField mdate = new DateField("mdate");
    private TextField url = new TextField("URL");

    Paper paper;
    Paper oldPaper;

    public Paper getPaper() {
        return paper;
    }

    // Easily bind forms to beans and manage validation and buffering
    BeanFieldGroup<Paper> formFieldBindings;

    public PaperForm() {
        configureComponents();
        buildLayout();
    }

    private void configureComponents() {
        /* Highlight primary actions.
         *
         * With Vaadin built-in styles you can highlight the primary save button
         * and give it a keyboard shortcut for a better UX.
         */
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        delete.setStyleName(ValoTheme.BUTTON_PRIMARY);
        delete.setClickShortcut(ShortcutAction.KeyCode.DELETE);

        setVisible(false);
    }

    private void buildLayout() {
        setSizeUndefined();
        setMargin(true);

        //TODO
        relatedAuthors.addSelectionListener(e -> {
            edit((Paper) relatedAuthors.getSelectedRow());
            getDelete().setVisible(true);
            setInsert(false);
        });

        relatedAuthors.setColumnOrder("name", "title", "type", "year");
        relatedAuthors.removeColumn("key");
        relatedAuthors.removeColumn("type");
        relatedAuthors.removeColumn("url");
        relatedAuthors.removeColumn("year");
        relatedAuthors.removeColumn("mdate");
        relatedAuthors.getColumn("name").setExpandRatio(1);
        relatedAuthors.getColumn("title").setExpandRatio(3);
        relatedAuthors.setSizeFull(); //TODO
        relatedAuthors.setHeight("250px");


        relatedTitle.setColumnOrder("name", "title", "type", "year");
        relatedTitle.removeColumn("key");
        relatedTitle.removeColumn("type");
        relatedTitle.removeColumn("url");
        relatedTitle.removeColumn("year");
        relatedTitle.removeColumn("mdate");
        relatedTitle.getColumn("name").setExpandRatio(1);
        relatedTitle.getColumn("title").setExpandRatio(3);
        relatedTitle.setSizeFull(); //TODO
        relatedTitle.setHeight("250px");


        VerticalLayout fields = new VerticalLayout();
        fields.setSpacing(true);

        VerticalLayout grid = new VerticalLayout(relatedAuthors, relatedTitle);
        grid.setCaption("Related articles:");
        grid.setSpacing(true);

        HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);
        actions.setSpacing(true);

        /*VerticalLayout nameAndGrid = new VerticalLayout(relatedAuthors);
        nameAndGrid.setSizeFull();*/


        title.setWidth("500px");
        type.setWidth("500px");
        name.setWidth("500px");
        year.setWidth("500px");
        mdate.setWidth("500px");
        url.setWidth("500px");
        fields.addComponents(name, title, type, year, mdate, url, actions);
        addComponents(fields, grid);
    }

    /* Use any JVM language.
     *
     * Vaadin supports all languages supported by Java Virtual Machine 1.6+.
     * This allows you to program user interface in Java 8, Scala, Groovy or any other
     * language you choose.
     * The new languages give you very powerful tools for organizing your code
     * as you choose. For example, you can implement the listener methods in your
     * compositions or in separate controller classes and receive
     * to various Vaadin component events, like button clicks. Or keep it simple
     * and compact with Lambda expressions.
     */
    public void save(Button.ClickEvent event) {
        try {
            // Commit the fields from UI to DAO
            formFieldBindings.commit();

            // Save DAO to backend with direct synchronous paperService API
            if (isInsert) {
                getUI().paperService.insert(paper);
            } else {
                getUI().paperService.save(paper, oldPaper);
            }

            getUI().refreshContacts();
        } catch (FieldGroup.CommitException e) {
            // Validation exceptions could be shown here
        }

        setVisible(false);
    }
    private void delete(Button.ClickEvent event) {
        try {
            formFieldBindings.commit();

            getUI().paperService.delete(paper);

        } catch (FieldGroup.CommitException e) {
            e.printStackTrace();
        }

        setVisible(false);
    }

    public void refreshAuthors(List<Paper> authors) {
        relatedAuthors.setContainerDataSource(new BeanItemContainer<Paper>(Paper.class, authors));
    }

    public void refreshTitles(List<Paper> titles) {
        relatedTitle.setContainerDataSource(new BeanItemContainer<Paper>(Paper.class, titles));
    }

    public void cancel(Button.ClickEvent event) {
        // Place to call business logic.
        Notification.show("Cancelled", Type.TRAY_NOTIFICATION);
        getUI().paperList.select(null);
        this.setVisible(false); //TODO
    }

    public void edit(Paper paper) {
        this.paper = paper;

        if (paper != null) {
            // Bind the properties of the paper POJO to fields in this form
            formFieldBindings = BeanFieldGroup.bindFieldsBuffered(paper, this);
            name.focus();
        }
        setVisible(paper != null);
    }

    @Override
    public PapersUI getUI() {
        return (PapersUI) super.getUI();
    }

}
