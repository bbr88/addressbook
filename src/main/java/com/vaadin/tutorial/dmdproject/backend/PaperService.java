package com.vaadin.tutorial.dmdproject.backend;

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

    private static String url = "jdbc:postgresql://localhost/dmdprojectdb";
    private static String user = "bbr";
    private static String password = "1488";


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

    public synchronized void save(Paper entry) {
        if (entry.getKey() == null) {
            return;
        }
        try {
            entry = (Paper) BeanUtils.cloneBean(entry);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        papers.put(entry.getKey(), entry);
    }


}
