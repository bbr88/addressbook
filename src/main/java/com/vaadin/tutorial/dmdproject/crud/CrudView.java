package com.vaadin.tutorial.dmdproject.crud;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.server.FontAwesome;
import com.vaadin.tutorial.dmdproject.AuthorForm;
import com.vaadin.tutorial.dmdproject.PaperForm;
import com.vaadin.tutorial.dmdproject.backend.Author;
import com.vaadin.tutorial.dmdproject.backend.Paper;
import com.vaadin.tutorial.dmdproject.backend.PaperService;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.button.MButton;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bbr on 28.10.15.
 */
public class CrudView extends CssLayout implements View {

    public static final String VIEW_NAME = "DBLP"; //digital
    private Grid paperList;
    private PaperForm paperForm;
    private AuthorForm authorForm;
    private ComboBox searchBox;

    private CrudLogic crudLogic = new CrudLogic(this);
    private MButton newPaper;
    private MButton newAuthor;
    private MButton searchPaper;
    private TextField filter;

    private PaperService service = new PaperService();

    public CrudView() {
        setSizeFull();
        addStyleName("crud-view");
        HorizontalLayout topLayout = createTopBar();

        BeanItemContainer<Paper> myBean = new BeanItemContainer<Paper>(Paper.class);
        paperList = new Grid(myBean);
        paperList.addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        paperList.addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        paperList.setColumnOrder("name", "title", "type", "year");
        paperList.removeColumn("key");
        paperList.removeColumn("type");
        paperList.removeColumn("url");
        paperList.removeColumn("year");
        paperList.removeColumn("mdate");
        paperList.getColumn("name").setExpandRatio(1);
        paperList.getColumn("title").setExpandRatio(3);
        paperList.setSizeFull(); //TODO
        paperList.setSelectionMode(Grid.SelectionMode.SINGLE);
        paperList.addSelectionListener(e
                -> {
            if (e != null) {
                paperForm.edit((Paper) paperList.getSelectedRow());
                paperForm.getDelete().setVisible(true);
                paperForm.setInsert(false);

                paperForm.refreshAuthors(service.getRelatedAuthors((Paper) paperList.getSelectedRow()));
                paperForm.refreshTitles(service.getRelatedTitles((Paper) paperList.getSelectedRow()));
            }
        });
        refreshPapers();
        setIt();

        VerticalLayout barAndListLayout = new VerticalLayout();
        barAndListLayout.addComponent(topLayout);
        barAndListLayout.addComponent(paperList);
        barAndListLayout.setMargin(true);
        barAndListLayout.setSpacing(true);
        barAndListLayout.setSizeFull();
        barAndListLayout.setExpandRatio(paperList, 1);
        barAndListLayout.setStyleName("crud-main-layout");

        barAndListLayout.addComponent(paperForm);
        addComponent(paperForm);
        addComponent(authorForm);
        addComponent(barAndListLayout);

        crudLogic.init();
    }

    private HorizontalLayout createTopBar() {
        filter = new TextField();
        filter.setStyleName("filter-textfield");
        filter.setInputPrompt("Filter papers...");
        //filter.addTextChangeListener(e -> refreshPapers(filter.getValue()));

        searchPaper = new MButton("Search");
        searchPaper.addStyleName(ValoTheme.BUTTON_PRIMARY);
        searchPaper.setIcon(FontAwesome.ARCHIVE);
        searchPaper.addClickListener(e -> searchPapers(filter.getValue(), searchBox.getValue().toString()));

        searchBox = new ComboBox();
        searchBox.setTextInputAllowed(false);
        searchBox.setNullSelectionAllowed(false);

        searchBox.addItem("author");
        searchBox.addItem("title");
        searchBox.addItem("type");
        searchBox.addItem("year");
        searchBox.addItem("book series");
        searchBox.addItem("proceedings series");
        searchBox.addItem("inproceedings series");
        searchBox.addItem("journal");
        searchBox.setValue("title");

        newPaper = new MButton("New paper");
        newPaper.addStyleName(ValoTheme.BUTTON_PRIMARY);
        newPaper.setIcon(FontAwesome.PLUS_CIRCLE);
        newPaper.addClickListener(e -> {
            paperForm.edit(new Paper());
            paperForm.getDelete().setVisible(false);
            paperForm.setInsert(true);
        });

        newAuthor = new MButton("Add author");
        newAuthor.addStyleName(ValoTheme.BUTTON_PRIMARY);
        newAuthor.setIcon(FontAwesome.PLUS_CIRCLE);
        newAuthor.addClickListener(e -> {
            authorForm.edit(new Author());
//            authorForm.getDelete().setVisible(false);
        });

        paperForm = new PaperForm();
        paperForm.setSizeFull();

        authorForm = new AuthorForm();
        authorForm.setSizeFull();

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");

        mainLayout.addComponent(filter);
        mainLayout.addComponent(searchPaper);
        mainLayout.addComponent(searchBox);
        mainLayout.addComponent(newPaper);
        mainLayout.addComponent(newAuthor);
        mainLayout.addComponent(paperForm);
        mainLayout.addComponent(authorForm);

        mainLayout.setComponentAlignment(paperForm, Alignment.TOP_CENTER); //TODO
        mainLayout.setComponentAlignment(authorForm, Alignment.TOP_CENTER); //TODO
        mainLayout.setComponentAlignment(searchPaper, Alignment.MIDDLE_LEFT);
        mainLayout.setComponentAlignment(newPaper, Alignment.TOP_RIGHT);
        mainLayout.setComponentAlignment(newAuthor, Alignment.TOP_RIGHT);
        mainLayout.setComponentAlignment(filter, Alignment.MIDDLE_LEFT);

        mainLayout.setExpandRatio(newPaper, 1);
        mainLayout.setStyleName("top-bar");
        return mainLayout;
    }

    @Override
    public void enter(ViewChangeEvent event) {
        Notification.show("Hello, Joo", Notification.Type.HUMANIZED_MESSAGE);
    }

    protected void setPaperListEnabled(boolean state) {
        paperList.setEnabled(state); //TODO probably it's not necessary
    }
    protected void setNewPaperEnabled(boolean state) {
        newPaper.setEnabled(state);
    }

    protected void setIt() {
        paperList.setContainerDataSource(new BeanItemContainer<>(
                Paper.class, service.findAll("")));
    }

    protected void refreshPapers() {
        refreshPapers(filter.getValue());

    }

    private void searchPapers(String s, String type) {
        if (s == null || s.length() == 0) {
            Notification.show("Empty search field", Notification.Type.HUMANIZED_MESSAGE);
            return;
        }

        List<Paper> papers = new ArrayList(service.search(s, type));

        if (!papers.isEmpty()) {
            paperList.setContainerDataSource(new BeanItemContainer<>(Paper.class, papers));
        }
    }

    private void refreshPapers(String stringFilter) {
        paperList.setContainerDataSource(new BeanItemContainer<>(
                Paper.class, service.findAll(stringFilter)));

        if (paperForm != null) {
            paperForm.setVisible(false);
        }
    }

}
