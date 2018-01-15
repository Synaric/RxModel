package com.synaric.app.rxmodel.demo;

/**
 * <br/><br/>Created by Synaric on 2018/1/15.
 */

public class GameBean {

    private String id;
    private String name;
    private int size;

    public GameBean(String id, String name, int size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
