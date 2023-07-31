package com.example.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class HistoryRVAdapter extends RecyclerView.Adapter<SongRVAdapter.ViewHolder> {

    ArrayList<String> songList;

    private Context context;

    public HistoryRVAdapter(ArrayList<String> s, Context context){
        this.songList = s;
        this. context = context;
    }

    @NonNull
    @Override
    public SongRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_rv_item, parent, false);
        return new SongRVAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongRVAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
