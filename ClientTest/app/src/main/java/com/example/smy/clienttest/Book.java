package com.example.smy.clienttest;

import java.io.Serializable;

/**
 * Created by SMY on 2016/6/10.
 */
public class Book implements Serializable {

    private String bookName;
    private String author;
    private double price;
    private int pages;

    public String getBookName()
    {
        return bookName;
    }

    public void setBookName(String name)
    {
        this.bookName = name;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public int getPages()
    {
        return pages;
    }

    public void setPages(int pages)
    {
        this.pages = pages;
    }


}
