package com.vaadin.tutorial.dmdproject.backend;

import com.vaadin.ui.Notification;
import org.apache.commons.beanutils.BeanUtils;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Separate Java service class.
 * Backend implementation for the address book application, with "detached entities"
 * simulating real world DAO.
 */
// Backend service class. This is just a typical Java backend implementation
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


    public List<Paper> search(String s, String searchType) {
        List<Paper> papers = new ArrayList();
        String row = "";

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement search = c.createStatement()) {
            searchType = searchType.toLowerCase();
            switch (searchType){
                case "author":
                    row = "(authors.name)";
                    break;
                case "title":
                    row = "(papers.title)";
                    break;
                case "type":
                    row = "(papers.type)";
                    break;
                case "year":
                    row = "(papers.year)";
                    break;
                case "book series":
                    row = "(books.series)";
                    break;
                case "proceedings series":
                    row = "(proceedings.series)";
                    break;
                case "inproceedings series":
                    row = "(inproceedings.series)";
                    break;
                case "journal":
                    row = "(articles.journal)";
                    break;
            }
            String sqlQuery = "";
            if(searchType.equals("author") || searchType.equals("title") || searchType.equals("type")){
                sqlQuery = "SELECT AUTHORS.name, PAPERS.key, PAPERS.title, PAPERS.type, PAPERS.year, PAPERS.url " +
                        "FROM AUTHORS " +
                        "JOIN WRITTEN " +
                        "ON AUTHORS.AuthorID = WRITTEN.AuthorID " +
                        "JOIN PAPERS " +
                        "ON WRITTEN.Key = PAPERS.Key " +
                        "WHERE LOWER " + row + " LIKE \'%" + s.toLowerCase() + "%\'";
            }
            else if(searchType.equals("year")){
                int year;
                try
                {
                    year = Integer.parseInt(s);
                    sqlQuery = "SELECT AUTHORS.name, PAPERS.key, PAPERS.title, PAPERS.type, PAPERS.year, PAPERS.url " +
                            "FROM AUTHORS " +
                            "JOIN WRITTEN " +
                            "ON AUTHORS.AuthorID = WRITTEN.AuthorID " +
                            "JOIN PAPERS " +
                            "ON WRITTEN.Key = PAPERS.Key " +
                            "WHERE " + row + " = " + year;
                }
                catch(NumberFormatException nfe)
                {
                    Notification.show("Use digits to find papers by year", Notification.Type.TRAY_NOTIFICATION);
                }
            }
            else if (searchType.equals("journal")){
                sqlQuery = "SELECT AUTHORS.name, PAPERS.key, PAPERS.title, PAPERS.type, PAPERS.year, PAPERS.url " +
                        "FROM AUTHORS " +
                        "JOIN WRITTEN " +
                        "ON AUTHORS.AuthorID = WRITTEN.AuthorID " +
                        "JOIN PAPERS " +
                        "ON WRITTEN.Key = PAPERS.Key " +
                        "JOIN ARTICLES " +
                        "ON ARTICLES.Key = PAPERS.Key " +
                        "WHERE LOWER " + row + " LIKE \'%" + s.toLowerCase() + "%\'";
            }
            else if(searchType.equals("book series")){
                sqlQuery = "SELECT AUTHORS.name, PAPERS.key, PAPERS.title, PAPERS.type, PAPERS.year, PAPERS.url " +
                        "FROM AUTHORS " +
                        "JOIN WRITTEN " +
                        "ON AUTHORS.AuthorID = WRITTEN.AuthorID " +
                        "JOIN PAPERS " +
                        "ON WRITTEN.Key = PAPERS.Key " +
                        "JOIN BOOKS " +
                        "ON BOOKS.Key = PAPERS.Key " +
                        "WHERE LOWER " + row + " LIKE \'%" + s.toLowerCase() + "%\'";
            }
            else if(searchType.equals("proceedings series")){
                sqlQuery = "SELECT AUTHORS.name, PAPERS.key, PAPERS.title, PAPERS.type, PAPERS.year, PAPERS.url " +
                        "FROM AUTHORS " +
                        "JOIN WRITTEN " +
                        "ON AUTHORS.AuthorID = WRITTEN.AuthorID " +
                        "JOIN PAPERS " +
                        "ON WRITTEN.Key = PAPERS.Key " +
                        "JOIN PROCEEDINGS " +
                        "ON PROCEEDINGS.Key = PAPERS.Key " +
                        "WHERE LOWER " + row + " LIKE \'%" + s.toLowerCase() + "%\'";
            }
            else if(searchType.equals("inproceedings series")){
                sqlQuery = "SELECT AUTHORS.name, PAPERS.key, PAPERS.title, PAPERS.type, PAPERS.year, PAPERS.url " +
                        "FROM AUTHORS " +
                        "JOIN WRITTEN " +
                        "ON AUTHORS.AuthorID = WRITTEN.AuthorID " +
                        "JOIN PAPERS " +
                        "ON WRITTEN.Key = PAPERS.Key " +
                        "JOIN INPROCEEDINGS " +
                        "ON INPROCEEDINGS.Key = PAPERS.Key " +
                        "WHERE LOWER " + row + " LIKE \'%" + s.toLowerCase() + "%\'";
            }

            ResultSet rs = search.executeQuery(sqlQuery);

            Paper paper = new Paper();
            while (rs.next()) {
                paper.setName(rs.getString(1));
                paper.setKey(rs.getString(2));
                paper.setTitle(rs.getString(3));
                paper.setType(rs.getString(4));
                paper.setYear(rs.getInt(5));
                paper.setMdate(new Date());
// paper.setMdate(new Date(rs.getString(5))); //TODO
                paper.setUrl(rs.getString(6));
                papers.add(paper);
                paper = new Paper();
            }
            rs.close();

            if (papers.isEmpty()) {
                Notification.show("No results found", Notification.Type.TRAY_NOTIFICATION);
            }

            Collections.sort(papers, (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
            return papers;
        } catch (SQLException ex) {
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
//                paper.setMdate(new Date(rs.getString(5))); //TODO
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

    public synchronized void delete(Paper paper) {
        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement delete = c.createStatement()) {
            String type = "";
            if (paper.getType().toLowerCase().equals("inproceedings") || paper.getType().toLowerCase().equals("proceedings")) {
                type = paper.getType();
            } else {
                type = paper.getType() + "s";
            }
            String sqlQuery = "DELETE FROM " + type +
                    " WHERE " + type + ".key = \'" + paper.getKey() + "\'";

            delete.executeUpdate(sqlQuery);

            sqlQuery = "DELETE FROM WRITTEN " +
                    "WHERE WRITTEN.Key = \'" + paper.getKey() + "\'";
            int r = delete.executeUpdate(sqlQuery);

            sqlQuery = "DELETE FROM PAPERS " +
                    "WHERE PAPERS.Key = \'" + paper.getKey() + "\'";
            int r2 = delete.executeUpdate(sqlQuery);
            if (r > 0 && r2 > 0) {
                Notification.show("Deleted successfully");
            }
            //TODO remove from papers
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }


    private int createNewAuthor(Paper paper) {
        int newAuthorID = 0;

        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement st = c.createStatement()) {

            ResultSet rs = st.executeQuery("SELECT AuthorID FROM AUTHORS ORDER BY AuthorID DESC LIMIT 1");

            if (rs.next()) {
                newAuthorID = rs.getInt(1) + 1;
                String sqlQuery = "INSERT INTO AUTHORS (name, AuthorID) VALUES" +
                        "(\'" + paper.getName() + "\', " + newAuthorID + ");";

                st.executeUpdate(sqlQuery);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return newAuthorID;
    }


    private int getAuthorId(Paper paper) {
        int result = Integer.MIN_VALUE;

        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement st = c.createStatement()) {

            ResultSet rs = st.executeQuery("SELECT authors.authorid FROM AUTHORS WHERE name = \'" + paper.getName() + "\'");
            if (rs.next()) {
                result = rs.getInt(1);
            }
            rs.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private boolean writtenExists(int authorId, String paperKey) {
        boolean result = false;

        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement st = c.createStatement()) {

            String sqlQuery = "SELECT * FROM written " +
                              "WHERE written.authorid = " + authorId +
                              " AND written.key = \'" + paperKey + "\'";

            ResultSet rs = st.executeQuery(sqlQuery);
            result = rs.next();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public void update(Paper newP, Paper oldP) throws SQLException {
        String sqlQuery = "";
        int r = 0;
        int r2 = 0;
        int id = 0;

        try (Connection c = DriverManager.getConnection(url, user, password);
             Statement update = c.createStatement()) {

            if ((id = getAuthorId(newP)) > 0) {

                if (!writtenExists(id, newP.getKey())) {
                    sqlQuery = "INSERT INTO written " +
                               "(authorid, key) VALUES " +
                               "(" + id + ", \'" + newP.getKey() + "\')";

                    update.executeUpdate(sqlQuery);
               }
            } else {
                int tempId = createNewAuthor(newP);

                sqlQuery = "UPDATE WRITTEN " +
                           "SET " +
                           "AuthorID = \'" + tempId + "\' " +
                           "WHERE AuthorID = " +
                           "(SELECT AuthorID " +
                           "FROM WRITTEN " +
                           "WHERE Key = \'" + newP.getKey() + "\' LIMIT 1)";

                update.executeUpdate(sqlQuery);
            }

            sqlQuery = "UPDATE papers " +
                       "SET (title, url) = (\'" + newP.getTitle() + "\', \'" + newP.getUrl() + "\') " +
                       "WHERE papers.Key = \'" + newP.getKey() + "\'";

            r2 = update.executeUpdate(sqlQuery);

            if (r2 > 0) {
                Notification.show("Updated successfully");
            }

            //TODO remove from papers
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void insert(Paper paper) {
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
            if (getAuthorId(paper) > 0) {
                ResultSet rs = insert.executeQuery("SELECT AuthorID FROM AUTHORS WHERE name = \'" + paper.getName() + "\'");
                if (rs.next()) {
                    id = rs.getInt(1);
                } else {
                    Notification.show("ti huy.");
                    return;
                }
            } else {
                id = createNewAuthor(paper);
            }
            sqlQuery = "INSERT INTO WRITTEN (AuthorID, Key)" +
                    "VALUES (\'" + id + "\'," +
                    "\'" + key + "\')";
            int r2 = insert.executeUpdate(sqlQuery);
            if (r > 0 && r2 > 0) {
                Notification.show("Inserted successfully");
            }
            //TODO remove from papers
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void save(Paper entry, Paper oldEntry) {

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
            update(entry, oldEntry);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        papers.put(entry.getKey(), entry);
    }

    public List<Paper> getRelatedAuthors(Paper paper) {
        List<Paper> authors = new ArrayList<>();
        if (paper != null) {
            try (Connection c = DriverManager.getConnection(url, user, password);
                 Statement search = c.createStatement()) {

                String sqlQuery = "SELECT AUTHORS.name, PAPERS.title " +
                                  "FROM AUTHORS " +
                                  "JOIN WRITTEN " +
                                  "ON AUTHORS.AuthorID = WRITTEN.AuthorID " +
                                  "JOIN PAPERS " +
                                  "ON WRITTEN.Key = PAPERS.Key " +
                                  "WHERE LOWER (authors.name) LIKE \'%" + paper.getName().toLowerCase() + "%\' LIMIT 5";

                ResultSet rs = search.executeQuery(sqlQuery);
                Paper p = new Paper();

                while (rs.next()) {
                    p.setName(rs.getString(1));
                    p.setTitle(rs.getString(2));
                    authors.add(p);
                    p = new Paper();
                }
                rs.close();
                Collections.sort(authors, (p1, p2) -> p1.getTitle().compareToIgnoreCase(p2.getTitle()));

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return authors;
    }

    public List<Paper> getRelatedTitles(Paper paper) {
        List<Paper> titles = new ArrayList<>();
        if (paper != null) {
            try (Connection c = DriverManager.getConnection(url, user, password);
                 Statement search = c.createStatement()) {

                String searchFor[] = paper.getTitle().replaceAll("[^a-zA-Z ]", "").toLowerCase().split(" ");
                String searchTitle = "";
                Random rand = new Random();

                while (searchTitle.length() <= 4) {
                    searchTitle = searchFor[rand.nextInt(searchFor.length)];
                }


                String sqlQuery = "SELECT DISTINCT AUTHORS.name, PAPERS.title " +
                                  "FROM AUTHORS " +
                                  "JOIN WRITTEN " +
                                  "ON AUTHORS.AuthorID = WRITTEN.AuthorID " +
                                  "JOIN PAPERS " +
                                  "ON WRITTEN.Key = PAPERS.Key " +
                                  "WHERE LOWER (papers.title) LIKE \'%" + searchTitle + "%\' LIMIT 5";

                ResultSet rs = search.executeQuery(sqlQuery);
                Paper p = new Paper();

                while (rs.next()) {
                    p.setName(rs.getString(1));
                    p.setTitle(rs.getString(2));
                    titles.add(p);
                    p = new Paper();
                }
                rs.close();
                Collections.sort(titles, (p1, p2) -> p1.getTitle().compareToIgnoreCase(p2.getTitle()));

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return titles;
    }

}
