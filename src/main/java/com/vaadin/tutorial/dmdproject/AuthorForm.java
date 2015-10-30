package com.vaadin.tutorial.dmdproject;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.tutorial.dmdproject.backend.Author;
import com.vaadin.tutorial.dmdproject.backend.Paper;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.button.MButton;

/**
 * Created by bbr on 30.10.15.
 */
public class AuthorForm extends FormLayout {

    private Button save = new MButton(FontAwesome.SAVE, this::save);
    private Button cancel = new Button("Cancel", this::cancel);

    private TextField name = new TextField("Author's name");
    private TextField title = new TextField("Laboratory");
    private TextField type = new TextField("University");

    private Author author;

    // Easily bind forms to beans and manage validation and buffering
    private BeanFieldGroup<Author> formFieldBindings;

    public AuthorForm() {
        configureComponents();
        buildLayout();
    }

    /* Highlight primary actions.
     *
     * With Vaadin built-in styles you can highlight the primary save button
     * and give it a keyboard shortcut for a better UX.
     */
    private void configureComponents() {
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        setVisible(false);
    }

    private void buildLayout() {
        setSizeUndefined();
        setMargin(true);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setSpacing(true);

        title.setWidth("500px");
        type.setWidth("500px");
        name.setWidth("500px");

        addComponents(name, title, type, actions);
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

            // Save DAO to backend with direct synchronous service API
//            getUI().service.save(author); TODO

            String msg = String.format("Saved '%s'.",
                    author.getName());
            if (author != null && author.getName().length() > 0) {
                Notification.show(msg, Notification.Type.TRAY_NOTIFICATION);
            }

//            getUI().refreshContacts();
        } catch (FieldGroup.CommitException e) {
            // Validation exceptions could be shown here
        }

        setVisible(false);
    }

    private void delete(Button.ClickEvent event) {

    }
    public void deleteAuthor() {
//        Notification.show(this.paper.getKey());
    }

    public void cancel(Button.ClickEvent event) {
        // Place to call business logic.
        Notification.show("Cancelled", Notification.Type.TRAY_NOTIFICATION);
        getUI().paperList.select(null);
        this.setVisible(false); //TODO
    }

    public void edit(Author author) {
        this.author = author;
        if (author != null) {
            // Bind the properties of the paper POJO to fields in this form
            formFieldBindings = BeanFieldGroup.bindFieldsBuffered(author, this);
            name.focus();
        }
        setVisible(author != null);
    }

    @Override
    public PapersUI getUI() {
        return (PapersUI) super.getUI();
    }

}
