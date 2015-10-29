package com.vaadin.tutorial.dmdproject;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.tutorial.dmdproject.backend.Paper;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.button.MButton;

/* Create custom UI Components.
 *
 * Create your own Vaadin components by inheritance and composition.
 * This is a form component inherited from VerticalLayout. Use
 * Use BeanFieldGroup to bind data fields from DTO to UI fields.
 * Similarly named field by naming convention or customized
 * with @PropertyId annotation.
 */
public class PaperForm extends FormLayout {

    Button save = new MButton(FontAwesome.SAVE, this::save);
    Button cancel = new Button("Cancel", this::cancel);
    TextField name = new TextField("Author");
    TextField title = new TextField("Title");
    TextField type = new TextField("Type");
    TextField year = new TextField("Year");
    DateField mdate = new DateField("mdate");
    TextField url = new TextField("URL");

    Paper paper;

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
        setVisible(false);
    }

    private void buildLayout() {
        setSizeUndefined();
        setMargin(true);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setSpacing(true);

        addComponents(actions, name, title, type, year, mdate, url);
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
            getUI().service.save(paper);

            String msg = String.format("Saved '%s'.",
                    paper.getName());
            if (paper != null && paper.getName().length() > 0) {
                Notification.show(msg, Type.TRAY_NOTIFICATION);
            }

            getUI().refreshContacts();
        } catch (FieldGroup.CommitException e) {
            // Validation exceptions could be shown here
        }
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
