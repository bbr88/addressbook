package com.vaadin.tutorial.addressbook.backend;

import org.apache.commons.beanutils.BeanUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public static ContactService createDemoService() {
        if (instance == null) {

            final ContactService contactService = new ContactService();

            Random r = new Random(0);
            Calendar cal = Calendar.getInstance();
            for (int i = 0; i < 100; i++) {
                Paper paper = new Paper();
                paper.setName(fnames[r.nextInt(fnames.length)]);
                paper.setKey(keys[r.nextInt(keys.length)]);
                cal.set(1930 + r.nextInt(70),
                        r.nextInt(11), r.nextInt(28));
                paper.setMdate(cal.getTime());
                contactService.save(paper);
            }
            instance = contactService;
        }

        return instance;
    }

    private HashMap<String, Paper> contacts = new HashMap<>();
    private long nextId = 0;

    public synchronized List<Paper> findAll(String stringFilter) {
        ArrayList arrayList = new ArrayList();
        for (Paper paper : contacts.values()) {
            try {
                boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())
                        || paper.toString().toLowerCase()
                        .contains(stringFilter.toLowerCase());
                if (passesFilter) {
                    arrayList.add(paper.clone());
                }
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(ContactService.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
        Collections.sort(arrayList, new Comparator<Paper>() {

            @Override
            public int compare(Paper o1, Paper o2) {
                //return (int) (o2.getId() - o1.getId());
                return 0; //TODO
            }
        });
        return arrayList;
    }

    public synchronized long count() {
        return contacts.size();
    }

    public synchronized void delete(Paper value) {
        //contacts.remove(value.getId());
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
        contacts.put(entry.getKey(), entry);
    }

}
