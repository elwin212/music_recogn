package com.example.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class ViewSongs extends AppCompatActivity {

    private ArrayList<SongModel> songModelArrayList;
    private DBHandler dbHandler;
    private SongRVAdapter songRVAdapter;
    private RecyclerView songsRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_songs);

        songModelArrayList = new ArrayList<>();
        dbHandler = new DBHandler(ViewSongs.this);
        songModelArrayList = dbHandler.readSongs();
        songRVAdapter = new SongRVAdapter(songModelArrayList,ViewSongs.this);
        songsRV = findViewById(R.id.idRVSongs);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ViewSongs.this, RecyclerView.VERTICAL, false);
        songsRV.setLayoutManager(linearLayoutManager);

        // setting our adapter to recycler view.
        songsRV.setAdapter(songRVAdapter);
    }

}