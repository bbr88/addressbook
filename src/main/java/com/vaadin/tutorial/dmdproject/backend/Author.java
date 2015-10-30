package com.vaadin.tutorial.dmdproject.backend;

import org.apache.commons.beanutils.BeanUtils;

import java.util.Date;

/**
 * Created by bbr on 30.10.15.
 */
public class Author {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLab() {
        return lab;
    }

    public void setLab(String lab) {
        this.lab = lab;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Author clone() throws CloneNotSupportedException {
        try {
            return (Author) BeanUtils.cloneBean(this);
        } catch (Exception ex) {
            throw new CloneNotSupportedException();
        }
    }

    @Override
    public String toString() {
        return "Author{" + "id=" + id + ", name=" + name
                + ", university=" + university + ", lab=" + lab + '}';
    }

    private String name = "";
    private String lab = "";
    private String university = "";
    private Integer id = 0;


}
