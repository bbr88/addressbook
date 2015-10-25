package com.vaadin.tutorial.addressbook.backend;

import org.apache.commons.beanutils.BeanUtils;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.IntStream;

/** Separate Java service class.
 * Backend implementation for the address book application, with "detached entities"
 * simulating real world DAO. Typically these something that the Java EE
 * or Spring backend services provide.
 */
// Backend service class. This is just a typical Java backend implementation
// class and nothing Vaadin specific.
public class ContactService {

    // Create dummy data by randomly combining first and last names
    static String[] fnames = { "Peter", "Alice", "John", "Mike", "Olivia",
            "Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik", "Rene", "Lisa",
            "Linda", "Timothy", "Daniel", "Brian", "George", "Scott",
            "Jennifer" };

    static String[] keys = { "Peter", "Alice", "John", "Mike", "Olivia",
            "Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik", "Rene", "Lisa",
            "Linda", "Timothy", "Daniel", "Brian", "George", "Scott",
            "Jennifer" };

    private static ContactService instance;

    private static String url = "jdbc:postgresql://localhost/dmdprojectdb";
    private static String user = "bbr";
    private static String password = "1488";


    public static ContactService createDemoService() {
        if (instance == null) {

            final ContactService contactService = new ContactService();
            List<Paper> papers = new ArrayList<>(selectPapers());

            IntStream.range(0, 10)
                     .forEach(i -> contactService.save(papers.get(i)));

            /*Random r = new Random(0);
            Calendar cal = Calendar.getInstance();
            for (int i = 0; i < 100; i++) {
                Paper paper = new Paper();
                paper.setName(fnames[r.nextInt(fnames.length)]);
                paper.setKey(keys[r.nextInt(keys.length)]);
                cal.set(1930 + r.nextInt(70),
                        r.nextInt(11), r.nextInt(28));
                paper.setMdate(cal.getTime());
                contactService.save(paper);
            }*/
            instance = contactService;
        }
        return instance;
    }

    private static List<Paper> selectPapers() {
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

            while (rs.next()) {
                paper.setKey(rs.getString(1));
                paper.setTitle(rs.getString(2));
                paper.setType(rs.getString(3));
                paper.setYear(rs.getInt(4));
                paper.setMdate(new Date());
//                paper.setMdate(new Date(rs.getString(5))); //TODO and it fucking doesn't work!
                paper.setURL(rs.getString(6));
                papers.add(paper);
                paper = new Paper();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return papers;
    }

//    private long nextId = 0;
    private HashMap<String, Paper> papers = new HashMap<>();

    @SuppressWarnings("unchecked")
    public synchronized List<Paper> findAll(String stringFilter) {
        ArrayList arrayList = new ArrayList();
        papers.entrySet().stream()
                           .forEach(e -> {
                               boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())
                                       || e.getValue().toString().toLowerCase().contains(stringFilter.toLowerCase());
                               if (passesFilter) {
                                   arrayList.add(e.getValue());
                               }
                           });

        Collections.sort(arrayList, new Comparator<Paper>() {

            @Override
            public int compare(Paper o1, Paper o2) {
                //return (int) (o2.getId() - o1.getId());
                return o1.getTitle().compareTo(o2.getTitle());
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
