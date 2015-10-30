package com.vaadin.tutorial.dmdproject.backend;

import com.vaadin.data.util.filter.Not;
import com.vaadin.ui.Notification;
import org.apache.commons.beanutils.BeanUtils;

import java.sql.*;
import java.util.*;
import java.util.Date;

/** Separate Java service class.
 * Backend implementation for the address book application, with "detached entities"
 * simulating real world DAO. Typically these something that the Java EE
 * or Spring backend services provide.
 */
// Backend service class. This is just a typical Java backend implementation
// class and nothing Vaadin specific.
public class PaperService {

    private static PaperService instance;

    private final static String url = "jdbc:postgresql://localhost/dmdprojectdb";
    private final static String user = "bbr";
    private final static String password = "1488";


    public static PaperService createDemoService() {
        if (instance == null) {

            final PaperService paperService = new PaperService();
            List<Paper> papers = new ArrayList<>(selectPapers());

            /*IntStream.range(0, 30)
                    .forEach(i -> paperService.save(papers.get(i)));*/

            instance = paperService;
        }
        return instance;
    }


    public List<Paper> search(String s){
        List<Paper> papers = new ArrayList<>();


        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement search = c.createStatement()) {

            String sqlQuery = "SELECT AUTHORS.name, PAPERS.key, PAPERS.title, PAPERS.type, PAPERS.year, PAPERS.url " +
                    "FROM AUTHORS " +
                    "JOIN WRITTEN " +
                    "ON AUTHORS.AuthorID = WRITTEN.AuthorID " +
                    "JOIN PAPERS " +
                    "ON WRITTEN.Key = PAPERS.Key " +
                    "WHERE LOWER (authors.name) LIKE \'%" + s.toLowerCase() + "%\'" +
                    "UNION " +
                    "SELECT AUTHORS.name, PAPERS.key, PAPERS.title, PAPERS.type, PAPERS.year, PAPERS.url " +
                    "FROM AUTHORS " +
                    "JOIN WRITTEN " +
                    "ON AUTHORS.AuthorID = WRITTEN.AuthorID " +
                    "JOIN PAPERS " +
                    "ON WRITTEN.Key = PAPERS.Key " +
                    "WHERE LOWER (papers.title) LIKE \'%" + s.toLowerCase() + "%\'";

            ResultSet rs = search.executeQuery(sqlQuery);

            if(!rs.next()){
                Notification.show("No results found", Notification.Type.TRAY_NOTIFICATION);
            }
            Paper paper = new Paper();
            while(rs.next()){
                paper.setName(rs.getString(1));
                paper.setKey(rs.getString(2));
                paper.setTitle(rs.getString(3));
                paper.setType(rs.getString(4));
                paper.setYear(rs.getInt(5));
                paper.setMdate(new Date());
// paper.setMdate(new Date(rs.getString(5))); //TODO and it fucking doesn't work!
                paper.setUrl(rs.getString(6));
                papers.add(paper);
                paper = new Paper();
            }
            rs.close();

            Collections.sort(papers, (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
            return papers;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }




    public static List<Paper> selectPapers() {
        List<Paper> papers = new ArrayList<>();
        try {
            Class.forName("org.postgresql.Driver"); //TODO i'm not sure if it's necessary.

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement s = c.createStatement()) {

            String sqlQuery = "SELECT * FROM PAPERS";
            ResultSet rs = s.executeQuery(sqlQuery);
            Paper paper = new Paper();

            int counter = 0;
            while (counter <= 100 && rs.next()) {
                paper.setKey(rs.getString(1));
                paper.setTitle(rs.getString(2));
                paper.setType(rs.getString(3));
                paper.setYear(rs.getInt(4));
                paper.setMdate(new Date());
//                paper.setMdate(new Date(rs.getString(5))); //TODO and it fucking doesn't work!
                paper.setUrl(rs.getString(6));
                papers.add(paper);
                paper = new Paper();
                counter++;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        Collections.sort(papers, (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
        return papers;
    }

    private HashMap<String, Paper> papers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public synchronized List<Paper> findAll(String stringFilter) {
        ArrayList arrayList = new ArrayList();
        papers.entrySet().stream()
                .forEach(e -> {
                               boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())
                                       || e.getValue().getTitle().toLowerCase().contains(stringFilter.toLowerCase());
                               if (passesFilter) {
                                   arrayList.add(e.getValue());
                               }
                           });

        Collections.sort(arrayList, new Comparator<Paper>() {

            @Override
            public int compare(Paper o1, Paper o2) {
                return o1.getTitle().compareToIgnoreCase(o2.getTitle());
            }
        });
        return arrayList;
    }



    public synchronized long count() {
        return papers.size();
    }

    public synchronized void delete(Paper value) {
        //papers.remove(value.getId());
        //TODO
    }



    public int createNewAuthor(Paper paper){
        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT AuthorID FROM AUTHORS ORDER BY AuthorID DESC LIMIT 1");
            int newAuthorID = 0;
            if (rs.next()) {
                newAuthorID = rs.getInt(1) + 1;

                String sqlQuery = "INSERT INTO AUTHORS (name, AuthorID) VALUES" +
                        "(\'" + paper.getName() + "\', " + newAuthorID + ");";
                st.executeUpdate(sqlQuery);
            }
            return newAuthorID;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }


    public boolean authorExists(Paper paper){
        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT name FROM AUTHORS WHERE name = \'" + paper.getName() + "\'");
            boolean r = rs.next();
            rs.close();
            return r;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void update(Paper newP) throws SQLException {
        int r = 0;
        int r2 = 0;
        int id = 0;
        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement update = c.createStatement()) {
            if(authorExists(newP)){
                ResultSet rs = update.executeQuery("SELECT AuthorID FROM AUTHORS WHERE name = \'" + newP.getName() + "\'");
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }
            else{
                int tempId = createNewAuthor(newP);
                if (tempId != 0) {
                    id = tempId;
                } else {
                    Notification.show("pidrsuka.");
                    return;
                }
            }
            String sqlQuery = "UPDATE WRITTEN " +
                    "SET " +
                    "AuthorID = \'" + id + "\' " +
                    "WHERE WRITTEN.Key = \'" + newP.getKey() + "\') ";
            r = update.executeUpdate(sqlQuery);
            sqlQuery = "UPDATE papers " +
                    "SET (title, url) = (\'" + newP.getTitle() + "\', \'" + newP.getUrl() + "\') " +
                    "WHERE papers.Key = \'" + newP.getKey() + "\'";
            r2 = update.executeUpdate(sqlQuery);
            if(r > 0 && r2 > 0){
                Notification.show("Updated successfully");
            }
            update.executeQuery(sqlQuery);
            //TODO remove from papers
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void insert(Paper paper){
        int id;
        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement insert = c.createStatement()) {
            long key = paper.getTitle().hashCode();
            String sqlQuery = "INSERT INTO PAPERS (key, title, type, year, url, mdate)" +
                    "VALUES (\'" + key + "\'," +
                    "\'" + paper.getTitle() + "\'," +
                    "\'" + paper.getType() + "\'," +
                    "" + paper.getYear() + ", " +
                    "\'" + paper.getUrl() + "\', " +
                    "\'" + new java.sql.Date(1232141L) + "\')";
            int r = insert.executeUpdate(sqlQuery);
            if(authorExists(paper)){
                ResultSet rs = insert.executeQuery("SELECT AuthorID FROM AUTHORS WHERE name = \'" + paper.getName() + "\'");
                if (rs.next()) {
                    id = rs.getInt(1);
                }
                else {
                    Notification.show("ti huy.");
                    return;
                }
            }
            else{
                id = createNewAuthor(paper);
            }
            sqlQuery = "INSERT INTO WRITTEN (AuthorID, Key)" +
                    "VALUES (\'" + id + "\'," +
                    "\'" + key + "\')";
            int r2 = insert.executeUpdate(sqlQuery);
            if(r > 0 && r2 > 0){
                Notification.show("Inserted successfully");
            }
            //TODO remove from papers
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public void update(String key, String title, String paperUrl){

        try {
            Class.forName("org.postgresql.Driver"); //TODO i'm not sure if it's necessary.

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement update = c.createStatement()) {

            String sqlQuery = "UPDATE papers " +
                    "SET (title, url) = (\'" + title + "\', \'" + paperUrl + "\') " +
                    "WHERE papers.Key = \'" + key + "\'";
            int r = update.executeUpdate(sqlQuery);
            if(r != 0) {
                Notification.show("Updated successfully");
            }

//            update.executeQuery(sqlQuery);
            //TODO remove from papers
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void save(Paper entry) {

        try {
            Class.forName("org.postgresql.Driver"); //TODO i'm not sure if it's necessary.

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (entry.getKey() == null) {
            return;
        }
        try {
            entry = (Paper) BeanUtils.cloneBean(entry);
//            Notification.show(entry.getKey() + "  " + entry.getTitle() + "  " + entry.getYear() + "  " + entry.getUrl());
//            update(entry.getKey(), entry.getTitle(), entry.getUrl());
            insert(entry);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        papers.put(entry.getKey(), entry);
    }




}
