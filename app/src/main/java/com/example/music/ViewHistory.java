package com.example.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.Serializable;
import java.util.ArrayList;

public class ViewHistory extends AppCompatActivity {

    private RecyclerView songsRV;
    private ArrayList<SongModel> songModelArrayList;
    private DBHandler dbHandler;

    private ImageButton imageButton;
    private SongRVAdapter songRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history);

        ArrayList<SongModel> historyList = new ArrayList<>();
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        if(args != null){
            historyList = (ArrayList<SongModel>) args.getSerializable("ARRAYLIST");
        }


        BottomNavigationView bottomNavigationView=findViewById(R.id.bottomNavigationView);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.history);

        Bundle pass = new Bundle();
        pass.putSerializable("ARRAYLIST",(Serializable)historyList);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch(item.getItemId())
            {
                case R.id.home:
                    Intent intentHome = new Intent(ViewHistory.this, MainActivity.class);

                    intentHome.putExtra("BUNDLE",pass);
                    startActivity(intentHome);
                    overridePendingTransition(0,0);
                    return true;
                case R.id.database:
                    Intent intentDataBase = new Intent(ViewHistory.this,DBcontroller.class);
                    intentDataBase.putExtra("BUNDLE",pass);
                    startActivity(intentDataBase);
                    overridePendingTransition(0,0);
                    return true;
                case R.id.history:
                    return true;
            }
            return false;
        });
        ArrayList<SongModel> finalHistoryList = historyList;
        imageButton = findViewById(R.id.clear_history);
        dbHandler = new DBHandler(ViewHistory.this);
        songsRV = findViewById(R.id.idHistorySongs);
        songRVAdapter = new SongRVAdapter(finalHistoryList,ViewHistory.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ViewHistory.this, RecyclerView.VERTICAL, false);
        linearLayoutManager.setReverseLayout(false);
        songsRV.setLayoutManager(linearLayoutManager);
        songsRV.setAdapter(songRVAdapter);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(songRVAdapter.getItemCount() != 0){
                    songRVAdapter.clear();
                    Toast.makeText(getApplicationContext(), "History cleared! ", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "No history to clear! ", Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}