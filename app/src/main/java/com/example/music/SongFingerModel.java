package com.example.music;

public class SongFingerModel {



    private String fing;
    private int id;

    SongFingerModel(int id, String data){
        this.id = id;
        this.fing = data;
    }

    public String getFing() {
        return fing;
    }

    public void setFing(String fing) {
        this.fing = fing;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}
