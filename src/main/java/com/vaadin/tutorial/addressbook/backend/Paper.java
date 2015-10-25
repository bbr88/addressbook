package com.vaadin.tutorial.addressbook.backend;

import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * A simple DTO for the address book example.
 *
 * Serializable and cloneable Java Object that are typically persisted
 * in the database and can also be easily converted to different formats like JSON.
 */
// Backend DTO class. This is just a typical Java backend implementation
// class and nothing Vaadin specific.
public class Paper implements Serializable, Cloneable {

    private String key = "";
    private String name = "";
    private String title = "";
    private String type = "";
    private String year = "";
    private Date mdate;
    private String url = "";


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

    public String getURL() {
        return this.url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setYear(String year) {
        this.year = year;
    }
    public String getYear() {
        return this.year;
    }


    @Override
    public Paper clone() throws CloneNotSupportedException {
        try {
            return (Paper) BeanUtils.cloneBean(this);
        } catch (Exception ex) {
            throw new CloneNotSupportedException();
        }
    }

    @Override
    public String toString() {
        return "Paper{" + "key=" + key + ", name=" + name
                + ", title=" + title + ", type=" + type + ", year="
                + year + ", mDate=" + mdate + '}';
    }

}
