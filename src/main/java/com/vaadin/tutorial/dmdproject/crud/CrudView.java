package com.vaadin.tutorial.dmdproject.crud;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.server.FontAwesome;
import com.vaadin.tutorial.dmdproject.PaperForm;
import com.vaadin.tutorial.dmdproject.backend.Paper;
import com.vaadin.tutorial.dmdproject.backend.PaperService;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.grid.MGrid;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by bbr on 28.10.15.
 */
public class CrudView extends CssLayout implements View {

    public static final String VIEW_NAME = "DBLP"; //digital
    private Grid paperList;
    private PaperForm paperForm;

    private CrudLogic crudLogic = new CrudLogic(this);
    private MButton newPaper;
    private TextField filter;

    private PaperService service = new PaperService();

    public CrudView() {
        setSizeFull();
        addStyleName("crud-view");
        HorizontalLayout topLayout = createTopBar();

        BeanItemContainer<Paper> myBean = new BeanItemContainer<Paper>(Paper.class);
        paperList = new Grid(myBean);
        paperList.setColumnOrder("name", "title", "type", "year");
        paperList.removeColumn("key");
        paperList.setSizeFull(); //TODO
        paperList.setSelectionMode(Grid.SelectionMode.SINGLE);
        paperList.addSelectionListener(e
                -> paperForm.edit((Paper) paperList.getSelectedRow()));
        refreshContacts();
        setIt();

        paperList.setContainerDataSource(new BeanItemContainer<>(Paper.class, service.selectPapers()));

        /*List<Paper> papers = new ArrayList<>();
        Paper testPaper = new Paper();
        testPaper.setKey("qwe");
        testPaper.setMdate(new Date());
        testPaper.setURL("qwe.com");
        testPaper.setName("IMYA KAROCH");
        testPaper.setTitle("the title");
        testPaper.setType("book");
        testPaper.setYear(2075);
        papers.add(testPaper);

        paperList.setContainerDataSource(new BeanItemContainer<>(
                Paper.class, papers));*/



        paperForm = new PaperForm();

        VerticalLayout barAndListLayout = new VerticalLayout();
        barAndListLayout.addComponent(topLayout);
        barAndListLayout.addComponent(paperList);
        barAndListLayout.setMargin(true);
        barAndListLayout.setSpacing(true);
        barAndListLayout.setSizeFull();
        barAndListLayout.setExpandRatio(paperList, 1);
        barAndListLayout.setStyleName("crud-main-layout");

        addComponent(barAndListLayout);
        addComponent(paperForm);

        crudLogic.init();
//        refreshContacts();
//        setIt();
    }

    private HorizontalLayout createTopBar() {
        filter = new TextField();
        filter.setStyleName("filter-textfield");
        filter.setInputPrompt("Filter papers...");
        filter.addTextChangeListener(e -> refreshContacts(filter.getValue()));

        newPaper = new MButton("New paper");
        newPaper.addStyleName(ValoTheme.BUTTON_PRIMARY);
        newPaper.setIcon(FontAwesome.PLUS_CIRCLE);
        newPaper.addClickListener(e -> paperForm.edit(new Paper()));

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setWidth("100%");
        mainLayout.addComponent(filter);
        mainLayout.addComponent(newPaper);
        mainLayout.setComponentAlignment(newPaper, Alignment.TOP_RIGHT); //TODO
        mainLayout.setComponentAlignment(filter, Alignment.MIDDLE_LEFT);
        mainLayout.setExpandRatio(filter, 1);
        mainLayout.setExpandRatio(newPaper, 1);
        mainLayout.setStyleName("top-bar");

        return mainLayout;
    }

    @Override
    public void enter(ViewChangeEvent event) {
        //crudLogic.enter(event.getParameters());
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

    protected void refreshContacts() {
        refreshContacts(filter.getValue());

    }
    private void refreshContacts(String stringFilter) {
        paperList.setContainerDataSource(new BeanItemContainer<>(
                Paper.class, service.findAll(stringFilter)));
        if (paperForm != null) {
            paperForm.setVisible(false);
        }
    }

}
