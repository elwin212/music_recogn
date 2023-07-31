package com.example.music;

import java.io.Serializable;

public class SongModel implements Serializable {
    private String songName;
    private String artistName;
    private String albumName;
    private byte[] albumUrl;
    private String fingerPrint;
    private int id;

    public SongModel(String songName, String artistName, String albumName, byte[] albumUrl, String fingerPrint) {
        this.songName = songName;
        this.artistName = artistName;
        this.albumName = albumName;
        this.albumUrl = albumUrl;
        this.fingerPrint = fingerPrint;
    }

    public SongModel(String songName, String artistName, String albumName, byte[] blobImage) {
        this.songName = songName;
        this.artistName = artistName;
        this.albumName = albumName;
        this.albumUrl = blobImage;
    }

    public SongModel(){

    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public byte[] getAlbumUrl(){return albumUrl;}

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getFingerPrint(){
        return this.fingerPrint;
    }
}
