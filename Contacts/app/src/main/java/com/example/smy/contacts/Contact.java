package com.example.smy.contacts;

/**
 * Created by SMY on 2016/6/10.
 */
public class Contact {
    private String name;
    private String sortKey;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSortKey()
    {
        return sortKey;
    }

    public void setSortKey(String key)
    {
        this.sortKey = key;
    }
}
