package com.vaadin.tutorial.dmdproject.lazy_container;

/**
 * Created by bbr on 02.11.15.
 */
import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.data.util.sqlcontainer.query.generator.StatementHelper;
import com.vaadin.data.util.sqlcontainer.query.generator.filter.QueryBuilder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Query for using the database's device-status table as a data source
 * for a Vaadin container (table).
 */
public class PaperQuery implements Query
{
    private static final Logger log = Logger.getLogger(PaperQuery.class.getName());
    private final static String url = "jdbc:postgresql://localhost/dmdprojectdb";
    private final static String user = "bbr";
    private final static String password = "1488";

    /** The table column names. Use these instead of typo-prone magic strings. */
    public static enum Column
    {
        key, name, title, type, year, mdate, url;
        /*hostname, loc_id, update_when, net_ip, lan_ip, lan_mac, hardware,
        opsys, image, sw_ver, cpu_load, proc_count, mem_usage, disk_usage;*/

        public boolean is(Object other)
        {
            if (other instanceof String)
                return this.toString().equals(other);
            else
                return (this == other);
        }
    };

    public static class Factory implements QueryFactory
    {
        private int     locId;

        /**
         * Constructor
         * @param locId - location ID
         */
        public Factory(int locId)
        {
            this.locId = locId;
        }

        @Override
        public Query constructQuery(QueryDefinition def)
        {
            return new PaperQuery(def, locId);
        }

    }//class Factory


    ///////////////////////  INSTANCE  /////////////////////

    private String            countQuery;
    private String            fetchQuery;
    /** Borrow from SQLContainer to build filter queries */
    private StatementHelper   stmtHelper = new StatementHelper();

    /**
     * Constructor
     * @param locId - location ID
     * @param locId - ID of user viewing the data
     */
    private PaperQuery(QueryDefinition def, int locId)
    {
        // Build filters block
        List<Filter> filters = def.getFilters();
        String filterStr = null;
        if (filters != null && !filters.isEmpty())
            filterStr = QueryBuilder.getJoinedFilterString(filters, "AND", stmtHelper);

        // Count query
        StringBuilder query = new StringBuilder( "SELECT COUNT(*) FROM papers");
//        query.append(" WHERE loc_id=").append(locId);

        if (filterStr != null)
            query.append(" AND ").append(filterStr);
        this.countQuery = query.toString();

        // Fetch query
        query = new StringBuilder(
                "SELECT *" +
                        " FROM papers");
//        query.append(" WHERE loc_id=").append(locId);

        if (filterStr != null)
            query.append(" AND ").append(filterStr);

        // Build Order by
        Object[]  sortIds = def.getSortPropertyIds();
        if (sortIds != null && sortIds.length > 0)
        {
            query.append(" ORDER BY ");
            boolean[] sortAsc = def.getSortPropertyAscendingStates();
            assert sortIds.length == sortAsc.length;

            for (int si = 0; si < sortIds.length; ++si)
            {
                if (si > 0)
                    query.append(',');

                query.append(sortIds[si]);
                if (sortAsc[si])
                    query.append(" ASC");
                else
                    query.append(" DESC");
            }
        }
        else
            query.append(" ORDER BY name");

        this.fetchQuery = query.toString();

        log.info("DeviceStatusQuery count: {" + this.countQuery + "}");
        log.info("DeviceStatusQuery count: {" + this.fetchQuery + "}");

    }//constructor

    @Override
    public int size()
    {
        int result = 0;
        try (Connection c = DriverManager.getConnection(url, user, password)) {

            PreparedStatement stmt = c.prepareStatement(this.countQuery);
            stmtHelper.setParameterValuesToStatement(stmt);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                result = rs.getInt(1);

            stmt.close();
        }
        catch (SQLException ex)
        {
            log.severe("DB access failure" + ex.getSQLState());
        }

        log.info("DeviceStatusQuery size={" + result + "}");
        return result;
    }

    @Override
    public List<Item> loadItems(int startIndex, int count)
    {
        List<Item> items = new ArrayList<Item>();
        try (Connection c = DriverManager.getConnection(url, user, password)) {

            String q = this.fetchQuery + " LIMIT " + count + " OFFSET " + startIndex;
            PreparedStatement stmt = c.prepareStatement(q);
            stmtHelper.setParameterValuesToStatement(stmt);

            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                PropertysetItem item = new PropertysetItem();
//                item.addItemProperty(Column.update_when, new ObjectProperty<Timestamp>(rs.getTimestamp(3), Timestamp.class));

                // Include the data type parameter on ObjectProperty any time the value could be null
                item.addItemProperty(Column.key, new ObjectProperty<String>(rs.getString(1), String.class));
                item.addItemProperty(Column.name, new ObjectProperty<String>(rs.getString(2), String.class));

                items.add(item);
            }
            rs.close();
            stmt.close();
        }
        catch (SQLException ex)
        {
            log.severe("DB access failure " + ex.getSQLState());
        }

        log.info("DeviceStatusQuery load {" + count + "}" + "items from {" + startIndex + "}={" + items.size() + "} found");
        return items;
    }//loadItems()

    /**
     * Only gets here if loadItems() fails, so return an empty state. Throwing from here
     * causes an infinite loop.
     */
    @Override
    public Item constructItem()
    {
        PropertysetItem item = new PropertysetItem();
        /*item.addItemProperty(Column.hostname, new ObjectProperty<String>(""));
        item.addItemProperty(Column.loc_id, new ObjectProperty<Integer>(-1));
        item.addItemProperty(Column.update_when, new ObjectProperty<Timestamp>(new Timestamp(System.currentTimeMillis())));
        item.addItemProperty(Column.net_ip, new ObjectProperty<String>(""));
        item.addItemProperty(Column.lan_ip, new ObjectProperty<String>(""));
        item.addItemProperty(Column.lan_mac, new ObjectProperty<String>(""));
        item.addItemProperty(Column.hardware, new ObjectProperty<String>(""));
        item.addItemProperty(Column.opsys, new ObjectProperty<String>(""));
        item.addItemProperty(Column.image, new ObjectProperty<String>(""));
        item.addItemProperty(Column.sw_ver, new ObjectProperty<String>(""));
        item.addItemProperty(Column.cpu_load, new ObjectProperty<String>(""));
        item.addItemProperty(Column.proc_count, new ObjectProperty<Integer>(0));
        item.addItemProperty(Column.mem_usage, new ObjectProperty<Integer>(0));
        item.addItemProperty(Column.disk_usage, new ObjectProperty<Integer>(0));*/

        log.warning("Shouldn't be calling DeviceStatusQuery.constructItem()");
        return item;
    }

    @Override
    public boolean deleteAllItems()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveItems(List<Item> arg0, List<Item> arg1, List<Item> arg2)
    {
        throw new UnsupportedOperationException();
    }
}

