package com.vaadin.tutorial.dmdproject.dbms;


import com.vaadin.tutorial.dmdproject.backend.Paper;
import com.vaadin.ui.Notification;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

// this class is the most essential part of project
// all operation functions in it
public class SystemCat {

    // system catalog file name
    private static final String catalogFileName = "catalog.cat";

    // buffer on which all operations are processed
    public static final int PAGE_SIZE = 4048;
    private static byte[] catalogCurrentPage = new byte[PAGE_SIZE];

    // page number of page in memory
    private static int catalogCurrentPageNumber;

    // created random access file
    private static RandomAccessFile catalogFile;

    // table to load information
    static Table catalogCurrentTable = new Table();

    // empty page list, filled in init
    private static List<Integer> catalogEmptyPageList = new ArrayList<Integer>();

    // table names and page numbers, filled in initialization
    private static Hashtable catalogTableNames = new Hashtable();

    //**************************** getter and setters ****************************
    public static byte[] getCatalogCurrentPage() {
        return catalogCurrentPage;
    }

    public static void setCatalogCurrentPage(byte[] catalogCurrentPage) {
        SystemCat.catalogCurrentPage = catalogCurrentPage;
    }

    public static int getCatalogCurrentPageNumber() {
        return catalogCurrentPageNumber;
    }

    public static void setCatalogCurrentPageNumber(int catalogCurrentPageNumber) {
        SystemCat.catalogCurrentPageNumber = catalogCurrentPageNumber;
    }

    public static RandomAccessFile getCatalogFile() {
        return catalogFile;
    }

    public static void setCatalogFile(RandomAccessFile catalogFile) {
        SystemCat.catalogFile = catalogFile;
    }

    public static Table getCatalogCurrentTable() {
        return catalogCurrentTable;
    }

    public static void setCatalogCurrentTable(Table catalogCurrentTable) {
        SystemCat.catalogCurrentTable = catalogCurrentTable;
    }
    //********************************* getter and setters end *******************

    //******* system catalog file existence check, open, create and close ********
    public static boolean doesCatalogExist() {
        return new File(catalogFileName).exists();
    }

    public static void openCatalogFile() throws IOException {
        catalogFile = new RandomAccessFile(catalogFileName, "rws");
    }

    public static void createCatalogFile() throws IOException {
        catalogFile = new RandomAccessFile(catalogFileName, "rws");
    }

    public static void closeCatalogFile() throws IOException {
        catalogFile.close();
    }
    //******* system catalog file existence check, open, create and close end ****

    //******* buffer read from and write into catalog file ***********************
    public static void readCatalogPage(int pageNumber) throws IOException {
        catalogFile.seek(PAGE_SIZE * pageNumber);
        catalogFile.readFully(catalogCurrentPage);
    }

    public static void writeCatalogPage(int pageNumber) throws IOException {
        catalogFile.seek(PAGE_SIZE * pageNumber);
        catalogFile.write(catalogCurrentPage);
    }
    //******* buffer read from and write into catalog file end *******************

    // table existence test
    public static boolean doesTableExist(String tableName) {
        return catalogTableNames.containsKey(tableName);
    }

    // check whether table has already been loaded,
    // just return or load table
    public static void loadTable(String tableName) throws IOException {
        String name = catalogCurrentTable.getTableName();
        if (name == null || !name.equals(tableName)) {
            readCatalogPage((Integer) catalogTableNames.get(tableName));
            setTable(tableName);
        }
    }

    // create table with given name and fields

    public static void createTable(String tableName, String fields) throws IOException {

        // create data file with dat extension
        File file = new File(tableName + ".dat");
        if (!file.createNewFile()) {
            System.out.println("'" + tableName + ".dat' couldn't be created");
            return;
        }

        // clear buffer
        clearPage();
        // set marker to full
        catalogCurrentPage[0] = '1';

        // check whether there is space from deleted tables
        // according to this set table page number
        if (!catalogEmptyPageList.isEmpty()) {
            catalogCurrentPageNumber = catalogEmptyPageList.get(catalogEmptyPageList.size() - 1);
            catalogEmptyPageList.remove((Integer) catalogCurrentPageNumber);
        } else
            catalogCurrentPageNumber = (int) catalogFile.length() / PAGE_SIZE;

        // fill buffer with fields
        fillTableData(tableName, fields);
        // write buffer
        writeCatalogPage(catalogCurrentPageNumber);
        // add table name into table name list
        catalogTableNames.put(tableName, catalogCurrentPageNumber);

        System.out.println("New table is written into page '" +
                catalogCurrentPageNumber + "' of catalog.txt");
        System.out.println("Create table is successfully completed\n");
    }

    public static void deleteTable(String tableName) throws IOException {

        // delete data file
        File file = new File(tableName + ".dat");
        if (!file.delete()) {
            System.out.println(tableName + ".dat couldn't be deleted");
            return;
        }

        // get page number of table
        int pageNumber = (Integer) catalogTableNames.get(tableName);

        // set marker to deleted
        catalogFile.seek(pageNumber * PAGE_SIZE);
        catalogFile.write('0');

        // delete table name from table name list
        catalogTableNames.remove(tableName);

        // add its page to empty page list
        catalogEmptyPageList.add(pageNumber);

        System.out.println("Table is deleted from page '" + pageNumber + "' of catalog.txt");
        System.out.println("Delete Table is successfully completed\n");
    }

    // check whether there is table
    // if exists, print their names
    public static void listTables() {
        if (catalogTableNames.isEmpty())
            System.out.println("There is no table");
        else {
            Enumeration e = catalogTableNames.keys();
            while (e.hasMoreElements())
                System.out.println((String) e.nextElement());
        }
        System.out.println();
    }

    // read catalog file sequentially, note which table is in which page
    public static void fillCatalogTableNames() throws IOException {

        for (int i = 0; i < catalogFile.length() / PAGE_SIZE; i++) {
            readCatalogPage(i);
            catalogCurrentPageNumber = i;

            // check if it is valid
            if (catalogCurrentPage[0] == '1') {
                int j = 1;
                while (catalogCurrentPage[j] != ':') {
                    j++;
                }
                String tableName = new String(catalogCurrentPage, 1, j - 1);
                catalogTableNames.put(tableName, catalogCurrentPageNumber);
            } else {
                catalogEmptyPageList.add(new Integer(i));
            }
        }
    }

    // this function just copies given strings into buffer with predefined markers
    public static void fillTableData(String tableName, String fields) {
        int offset = tableName.length();
        System.arraycopy(tableName.getBytes(), 0, catalogCurrentPage, 1, offset);
        catalogCurrentPage[++offset] = ':';
        System.arraycopy(fields.getBytes(), 0, catalogCurrentPage, ++offset, fields.length());
        offset += fields.length();
        catalogCurrentPage[offset] = ';';
    }

    // using buffer data fill previously created table object
    public static void setTable(String name) {
        catalogCurrentTable.setTableName(name);
        setFieldsOfTable(2 + name.length());
    }

    // called by above function to fill fields
    public static void setFieldsOfTable(int offset) {

        Hashtable hash = catalogCurrentTable.getTableFields();
        hash.clear();

        int start;
        while (catalogCurrentPage[offset] != ';') {
            start = offset;
            while (catalogCurrentPage[offset] != ',') offset++;
            String fieldName = new String(catalogCurrentPage, start, offset - start);
            int fieldLength = Utility.byteArrayToShort(catalogCurrentPage, ++offset);
            hash.put(fieldName, fieldLength);
            offset += 2;
        }
    }

    // check whether given primary key exists
    public static boolean doesPkExist(int reqPk) throws IOException {

        RandomAccessFile r = new RandomAccessFile(catalogCurrentTable.getTableName() + ".dat", "rws");
        int len = catalogCurrentTable.getFieldsTotalLength();

        for (int i = 0; i < r.length() / PAGE_SIZE; i++) {
            r.seek(i * PAGE_SIZE);
            r.readFully(catalogCurrentPage);
            int j = 0;
            while (j < PAGE_SIZE) {
                // get record from buffer
                if (catalogCurrentPage[j] == '1') {
                    if (reqPk == Utility.byteArrayToInt(catalogCurrentPage, j + 1))
                        return true;
                }
                j += len;
            }
        }
        r.close();
        return false;
    }

    //add record to preloaded table
    public static void addRecord() throws IOException {

        // get new primary key
        int pk = getPk(false);
        // get record to be inserted
        String record = getRecordFromUser(pk);
        // table name to insert
        String tableName = catalogCurrentTable.getTableName();
        // record length
        int len = record.length();
        // open related data file
        RandomAccessFile r = new RandomAccessFile(tableName + ".dat", "rws");

        int pageNumber = 0;
        boolean isAdded = false;
        // read records
        for (int i = 0; i < r.length() / PAGE_SIZE; i++) {
            r.seek(i * PAGE_SIZE);
            // read page
            r.readFully(catalogCurrentPage);

            int j = 0;
            while (j < PAGE_SIZE) {
                // get record from buffer
                if (catalogCurrentPage[j] == '0') {
                    if (j + len < PAGE_SIZE) {
                        System.arraycopy(record.getBytes(), 0, catalogCurrentPage, j, len);
                        r.seek(i * PAGE_SIZE);
                        r.write(catalogCurrentPage);
                        pageNumber = i;
                        isAdded = true;
                        break;
                    }
                }
                j += len;
            }
        }

        if (!isAdded) {
            clearPage();
            System.arraycopy(record.getBytes(), 0, catalogCurrentPage, 0, len);
            r.seek(r.length());
            r.write(catalogCurrentPage);
            pageNumber = (int) r.length() / PAGE_SIZE - 1;
        }

        //close file
        r.close();

        //print success message
        System.out.println("Record is added to " + pageNumber +
                " page of " + tableName + ".dat");
        System.out.println("Add Record is successfully completed\n");
    }

    public static void deleteRecord() throws IOException {
        // get table name
        String tableName = catalogCurrentTable.getTableName();
        // get existing primary key
        int reqPk = getPk(true);
        // get record length
        int len = catalogCurrentTable.getFieldsTotalLength();
        // open data file
        RandomAccessFile r = new RandomAccessFile(tableName + ".dat", "rws");

        int pageNumber = 0;
        // read records
        for (int i = 0; i < r.length() / PAGE_SIZE; i++) {
            r.seek(i * PAGE_SIZE);
            // read page
            r.readFully(catalogCurrentPage);

            int j = 0;
            while (j < PAGE_SIZE) {
                // get record from buffer
                if (catalogCurrentPage[j] == '1') {
                    if (reqPk == Utility.byteArrayToInt(catalogCurrentPage, j + 1)) {
                        catalogCurrentPage[j] = '0';
                        r.seek(i * PAGE_SIZE);
                        r.write(catalogCurrentPage);
                        pageNumber = i;
                        break;
                    }
                }
                j += len;
            }
        }

        // close file
        r.close();

        // print operation results
        System.out.println("Record is deleted from page '" + pageNumber +
                "' of " + tableName + ".dat");
        System.out.println("Delete Record is successfully completed\n");

    }

    public static void listRecords() throws IOException {
        // open file
        RandomAccessFile r = new RandomAccessFile(catalogCurrentTable.getTableName() +
                ".dat", "rws");

        Hashtable hash = catalogCurrentTable.getTableFields();
        Enumeration e = hash.keys();
        // print names of fields with formatting
        System.out.print("PK        \t");
        while (e.hasMoreElements()) {
            String fieldName = (String) e.nextElement();
            int fieldNameLen = fieldName.length();
            System.out.print(fieldName);
            if (fieldNameLen < (Integer) hash.get(fieldName)) {
                for (int k = 0; k < (Integer) hash.get(fieldName) - fieldNameLen; k++)
                    System.out.print(" ");
            }
            System.out.print("\t");
        }
        System.out.println();

        int len = catalogCurrentTable.getFieldsTotalLength();
        // read records
        for (int i = 0; i < r.length() / PAGE_SIZE; i++) {
            r.seek(i * PAGE_SIZE);
            // read page
            r.readFully(catalogCurrentPage);

            int j = 0;
            while (j < PAGE_SIZE) {
                // get record from buffer
                if (catalogCurrentPage[j] == '1') {
                    e = hash.keys();
                    int pk = Utility.byteArrayToInt(catalogCurrentPage, j + 1);
                    System.out.print(pk);
                    for (int k = 0; k < 10 - Integer.toString(pk).length(); k++) System.out.print(" ");
                    System.out.print("\t");
                    String record = new String(catalogCurrentPage, j + 5, len - 5);

                    int length;
                    int k = 0;
                    while (e.hasMoreElements()) {
                        String s = (String) e.nextElement();
                        length = (Integer) hash.get(s);
                        System.out.print(record.substring(k, k + length));
                        for (int x = 0; x < s.length() - length; x++) System.out.print(" ");   //formatting
                        System.out.print("\t");
                        k += length;
                    }
                    System.out.println();
                }
                j += len;
            }
        }

        // close file
        r.close();
        System.out.println();
    }

    //  get primary key from user
    //  according to parameter exist or nox-exist primary key
    public static int getPk(boolean b) throws IOException {
        int pk = -1;
        if (!b) {
            do {
                try {
                    System.out.println("Please enter key");
                    pk = Integer.parseInt(UIHelper.get().readLine());
                } catch (NumberFormatException nfe) {
                }

            } while (doesPkExist(pk) || (pk == -1));
        } else {

            do {
                try {
                    System.out.println("Please enter key");
                    pk = Integer.parseInt(UIHelper.get().readLine());
                } catch (NumberFormatException nfe) {
                }
            } while (!doesPkExist(pk) || (pk == -1));
        }
        return pk;
    }

    // get record data from user
    public static String getRecordFromUser(int pk) throws IOException {

        StringBuilder sb = new StringBuilder();

        sb.append("1");
        sb.append(new String(Utility.intToByteArray(pk)));

        Hashtable fields = catalogCurrentTable.getTableFields();
        Enumeration e = fields.keys();

        String fieldName;
        String input;

        while (e.hasMoreElements()) {
            fieldName = (String) e.nextElement();
            int fieldLen = (Integer) fields.get(fieldName);
            System.out.println("Please enter data of field '" + fieldName +
                    "'" + " max:'" + fieldLen + "'");
            input = UIHelper.get().readLine();
            while (input.length() > fieldLen) {
                System.out.println("Please enter data of field '" + fieldName +
                        "'" + " max:'" + fieldLen + "'");
                input = UIHelper.get().readLine();
            }
            sb.append(input);
            for (int i = 0; i < fieldLen - input.length(); i++)
                sb.append(" ");
        }
        return sb.toString();
    }

    // clear buffer
    public static void clearPage() {
        clearPage(0);
    }

    // clear buffer starting from given index
    public static void clearPage(int offset) {
        for (int i = offset; i < PAGE_SIZE; i++) catalogCurrentPage[i] = '0';
    }

    // check whether table name hash is empty
    public static boolean isTableNamesEmpty() {
        return catalogTableNames.isEmpty();
    }


    public static List<List<String>> searchForValue(String searchValue) throws IOException {
        // open file
        UIHelper.init();
        HashSet<Integer> pkList = new HashSet<>();

        RandomAccessFile r = new RandomAccessFile(catalogCurrentTable.getTableName() +
                ".dat", "rws");

        Hashtable hash = catalogCurrentTable.getTableFields();
        Enumeration e = hash.keys();
        int len = catalogCurrentTable.getFieldsTotalLength();
        // read records
        for (int i = 0; i < r.length() / PAGE_SIZE; i++) {
            r.seek(i * PAGE_SIZE);
            // read page
            r.readFully(catalogCurrentPage);

            int j = 0;
            while (j < PAGE_SIZE) {
                // get record from buffer
                if (catalogCurrentPage[j] == '1') {
                    e = hash.keys();
                    int pk = Utility.byteArrayToInt(catalogCurrentPage, j + 1);
                    String record = new String(catalogCurrentPage, j + 5, len - 5);

                    int length;
                    int k = 0;
                    while (e.hasMoreElements()) {
                        String s = (String) e.nextElement();
                        length = (Integer) hash.get(s);
                        if (record.substring(k, k + length).contains(searchValue)) {
                            pkList.add(pk);
                        }
                        k += length;
                    }
                }
                j += len;
            }
        }

        List<List<String>> results = new ArrayList<>();

        pkList.stream().forEach(integer -> {
                    try {
                        results.add(searchAndListRecords(integer));
                        System.out.println("\n");
                        System.out.println(results.get(0).get(0));
                        System.out.println(results.get(0).get(1));
                        System.out.println(results.get(0).get(2));
                    } catch (IOException ex) {
                    }
                }
        );

        // close file
        r.close();
        return results;
    }

    public static void searchAndListRecords() throws IOException {

        String value;
        // get search key
        while (true) {
            try {
                System.out.println("Please enter search phrase");
                value = UIHelper.get().readLine();
                List<List<String>> results = new ArrayList<>();
                results = searchForValue(value);
                break;
            } catch (IOException e) {
            }
        }
    }

    public static List<String> searchAndListRecords(int reqPk) throws IOException {

        // get search key
        String tableName = catalogCurrentTable.getTableName();
        Hashtable hash = catalogCurrentTable.getTableFields();
        Enumeration e = hash.keys();

        int len = catalogCurrentTable.getFieldsTotalLength();
        RandomAccessFile r = new RandomAccessFile(tableName + ".dat", "rws");

        // read records
        for (int i = 0; i < r.length() / PAGE_SIZE; i++) {
            // read page
            r.seek(i * PAGE_SIZE);
            r.readFully(catalogCurrentPage);

            int j = 0;
            while (j < PAGE_SIZE) {
                // get record from buffer
                if (catalogCurrentPage[j] == '1') {
                    e = hash.keys();
                    int pk = Utility.byteArrayToInt(catalogCurrentPage, j + 1);
                    if (reqPk == pk) {
                        String result = pk + Lenny.FACE.getValue();
                        for (int k = 0; k < 10 - Integer.toString(pk).length(); k++)
                            System.out.print(" ");
                        System.out.print("\t");

                        String record = new String(catalogCurrentPage, j + 5, len - 5);
                        int length = 0, k = 0;
                        while (e.hasMoreElements()) {
                            String s = (String) e.nextElement();
                            length = (Integer) hash.get(s);
                            result += record.substring(k, k + length) + Lenny.FACE.getValue();
                            for (int x = 0; x < s.length() - length; x++) System.out.print(" ");
                            System.out.print("\t");
                            k += length;
                        }
                        StringTokenizer tokenizer = new StringTokenizer(result, Lenny.FACE.getValue());
                        List<String> results = new ArrayList<>();

                        while (tokenizer.hasMoreTokens()) {
                            results.add(tokenizer.nextToken());
                        }

                        return results;
                    }
                }
                j += len;
            }
        }

        r.close();
        System.out.println();
        return new ArrayList<>();
    }

    public List<Paper> executeSearchQuery(String s, String searchType) {
        List<Paper> papers = new ArrayList();
        List<List<String>> resultsPapers = new ArrayList<>();
        List<List<String>> resultsWritten = new ArrayList<>();
        List<List<String>> resultsAuthors = new ArrayList<>();
        List<String> resultsAuthorsNames = new ArrayList<>();
        final List<List<String>> resultsPapers_ = new ArrayList<>();


        String[] tables = s.split("FROM")[1].split("WHERE")[0].split(", ");
        String searchPhrase = s.split("LIKE ")[1];
        String row = s.split("WHERE ")[1].split(" LIKE")[0];
        if (searchType.equals("author") || searchType.equals("title") || searchType.equals("type")) {
            try {
                UIHelper.init();
                if (row.equals("(papers.title)") || row.equals("(papers.type)")) {
                    SystemCat.loadTable(tables[0].toLowerCase()); //papers
                    resultsPapers = SystemCat.searchForValue(searchPhrase.toLowerCase());
                    //Notification.show("TEST: " + resultsPapers.size() + "s.toLower:= " + searchPhrase.toLowerCase());
                    SystemCat.loadTable(tables[1].toLowerCase()); //written
                    resultsPapers.stream()
                            .forEach(strings -> {
                                try {
                                    /**find paper key in written and add the first record from written to list
                                     * adds to the list only first record for every found article
                                     */
                                    resultsWritten.add(SystemCat.searchForValue(strings.get(1)).get(0));
                                } catch (IOException ex) {
                                }
                            });
                    resultsWritten.stream()
                            .forEach(strings -> {
                                try {
                                    /**find authorID in authors by authorId in written
                                     * add author name to the list
                                     */
                                    SystemCat.loadTable(tables[2].toLowerCase()); //authors
                                    resultsAuthorsNames.add(SystemCat.searchForValue(strings.get(1)).get(0).get(2));
                                } catch (IOException ex) {
                                }
                            });
                    for (int i = 0; i < resultsPapers.size(); i++) {
                        final Paper paper_ = new Paper();
                        paper_.setName(resultsAuthorsNames.get(i));
                        paper_.setKey(resultsPapers.get(i).get(1));
                        paper_.setTitle(resultsPapers.get(i).get(2));
                        paper_.setType(resultsPapers.get(i).get(3));
                        paper_.setYear(Integer.parseInt(resultsPapers.get(i).get(4)));
                        paper_.setMdate(new Date());
                        paper_.setUrl(resultsPapers.get(i).get(5));
                        papers.add(paper_);
                    }
                } else if (row.equals("(authors.name)")) {
                    SystemCat.loadTable(tables[2]);
                    resultsAuthors = SystemCat.searchForValue(searchPhrase.toLowerCase());
                    SystemCat.loadTable(tables[1].toLowerCase()); //written
                    resultsAuthors.stream().forEach(strings -> {
                        try {
                            /**find author ID in written and all the corresponding papers
                             * adds to the list all the papers of the author
                             */
                            resultsWritten.addAll(SystemCat.searchForValue(strings.get(2)));

                        } catch (IOException ex) {
                        }
                    });
                    resultsWritten.stream()
                            .forEach(strings -> {
                                try {
                                    /**find paperKey in papers for each pair authorID+key in written
                                     * add all the found papers Keys into the list
                                     */
                                    SystemCat.loadTable(tables[0].toLowerCase()); //papers
                                    resultsPapers_.add(SystemCat.searchForValue(strings.get(1)).get(0));
                                    resultsAuthorsNames.add(SystemCat.searchForValue(strings.get(1)).get(0).get(2));
                                } catch (IOException ex) {
                                }
                            });
                    for (int i = 0; i < resultsWritten.size(); i++) {
                        final Paper paper_ = new Paper();
                        paper_.setName(resultsAuthorsNames.get(i));
                        paper_.setKey(resultsPapers_.get(i).get(1));
                        paper_.setTitle(resultsPapers_.get(i).get(2));
                        paper_.setType(resultsPapers_.get(i).get(3));
                        paper_.setYear(Integer.parseInt(resultsPapers_.get(i).get(4)));
                        paper_.setMdate(new Date());
                        paper_.setUrl(resultsPapers_.get(i).get(5));
                        papers.add(paper_);
                    }
                }
            } catch (IOException ex) {
            }
        }

        if (s.contains("SORT")) {
            if (s.contains("ASC"))
                Collections.sort(papers, (o1, o2) -> o2.getTitle().compareToIgnoreCase(o1.getTitle()));
            if (s.contains("DESC"))
                Collections.sort(papers, (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
        }

        return papers;
    }

    public List<Paper> search(String s, String searchType) {
        List<Paper> papers = new ArrayList();
        String row = "";

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        searchType = searchType.toLowerCase();
        switch (searchType) {
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

        try {
            SystemCat.searchForValue(s.toLowerCase());

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        String sqlQuery = "";
        sqlQuery = "SELECT Authors.name, Papers.title " +
                "FROM Papers, Written, Authors " +
                "WHERE " + row +
                " LIKE " + s.toLowerCase();
        papers = executeSearchQuery(sqlQuery, searchType);
        if (papers.isEmpty()) {
            Notification.show("No results found", Notification.Type.TRAY_NOTIFICATION);
        }

//            Collections.sort(papers, (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
        return papers;

    }
}


