package cn.itcast.model;

import cn.itcast.util.SolrField;


public class Product {

    @SolrField(name = "id")
    private String id;

    @SolrField(name = "title",ishl = true)
    private String title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
