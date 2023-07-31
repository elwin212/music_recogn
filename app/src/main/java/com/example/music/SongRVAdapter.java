package com.example.music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongRVAdapter extends RecyclerView.Adapter<SongRVAdapter.ViewHolder> {

    private ArrayList<SongModel> songModelArrayList;
    private Context context;

    public SongRVAdapter(ArrayList<SongModel> songModelArrayList, Context context) {
        this.songModelArrayList = songModelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public SongRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongRVAdapter.ViewHolder holder, int position) {

        SongModel songModel = songModelArrayList.get(getItemCount() - position -1);
        holder.songNameTV.setText(songModel.getSongName());
        holder.artistTV.setText(songModel.getArtistName());
        holder.albumTV.setText(songModel.getAlbumName());
       //holder.fingerPrintTV.setText(string);
    }

    @Override
    public int getItemCount() {
        return songModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // creating variables for our text views.
        private TextView songNameTV, artistTV, albumTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our text views
            songNameTV = itemView.findViewById(R.id.idTVSongName);
            artistTV = itemView.findViewById(R.id.idTVArtist);
            albumTV = itemView.findViewById(R.id.idTVAlbum);

        }
    }

    public void clear() {
        int size = songModelArrayList.size();
        songModelArrayList.clear();
        notifyItemRangeRemoved(0, size);
    }
}
