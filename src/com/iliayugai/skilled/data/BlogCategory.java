package com.iliayugai.skilled.data;

public class BlogCategory {

    public String strId;
    public String strName;
    public BlogCategory parent;
    public int nRowNum;
    public int nSelected;
    public boolean bHasSubItem;

    public BlogCategory() {
        this.strId = "";
        this.strName = "";
        this.parent = null;
        this.nRowNum = this.nSelected = 0;
        this.bHasSubItem = false;
    }

    public BlogCategory(String id, String name) {
        this.strId = id;
        this.strName = name;
        this.parent = null;
        this.nRowNum = this.nSelected = 0;
        this.bHasSubItem = false;
    }

}
