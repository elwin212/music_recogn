package com.example.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.musicg.fingerprint.FingerprintManager;
import com.musicg.wave.Wave;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBcontroller extends AppCompatActivity {

    private EditText songNameEdt, artistEdt, albumEdt, UrlEdt;
    private Button addSongInfoBtn,readDataBtn, insertDataBtn;
    private DBHandler dbHandler;

    private Thread generateFingerPrint;
    private TextView tvNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbcontroller);
        // Initialize and assign variable
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottomNavigationView);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.database);
        ArrayList<SongModel> historyList = new ArrayList<>();
        Intent intentGetArray = getIntent();

        Bundle arrayList = intentGetArray.getBundleExtra("BUNDLE");
        if(arrayList!=null){
            historyList = (ArrayList<SongModel>) arrayList.getSerializable("ARRAYLIST");
        }


        Bundle pass = new Bundle();
        pass.putSerializable("ARRAYLIST", (Serializable)historyList);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch(item.getItemId())
            {
                case R.id.home:
                    Intent intentHome = new Intent(DBcontroller.this, MainActivity.class);
                    intentHome.putExtra("BUNDLE", pass);
                    startActivity(intentHome);
                    overridePendingTransition(0,0);
                    return true;
                case R.id.database:
                    return true;
                case R.id.history:
                    Intent intentHistory = new Intent(DBcontroller.this, ViewHistory.class);
                    intentHistory.putExtra("BUNDLE",pass);
                    startActivity(intentHistory);
                    overridePendingTransition(0,0);
            }
            return false;
        });
        initView();
        final byte[] fingerPrintsData = getIntent().getByteArrayExtra("FingerPrint");

        dbHandler = new DBHandler(DBcontroller.this);

        addSongInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String songName = songNameEdt.getText().toString();
                String artistName = artistEdt.getText().toString();
                String albumName = albumEdt.getText().toString();
                String albumUrl = UrlEdt.getText().toString();


                // validating if the text fields are empty or not.
                if (songName.isEmpty() && artistName.isEmpty() && albumName.isEmpty() && fingerPrintsData==null) {
                    Toast.makeText(DBcontroller.this, "Please enter all the data..", Toast.LENGTH_SHORT).show();
                    return;
                }

                //dbHandler.addNewSong(songName,artistName,albumName, albumUrl ,fingerPrintsData);
                dbHandler.addFingerprint(songName,fingerPrintsData);
                Toast.makeText(DBcontroller.this, "Song has been added.", Toast.LENGTH_SHORT).show();
                songNameEdt.setText("");
                artistEdt.setText("");
                albumEdt.setText("");
                UrlEdt.setText("");
            }
        });

        readDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DBcontroller.this, ViewSongs.class);
                startActivity(i);

            }
        });

        insertDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //dbHandler.clearTable();
                    //generateFingerprint();
                    //generateHashMap();
                /*try {
                    generateHashUsingParallel();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }*/
            }
        });
    }

    private void initView(){
        songNameEdt = findViewById(R.id.idEdtSongName);
        artistEdt = findViewById(R.id.idEdtArtist);
        albumEdt = findViewById(R.id.idEdtAlbum);
        UrlEdt = findViewById(R.id.idEdtAlbumUrl);
        addSongInfoBtn = findViewById(R.id.idBtnAddData);
        readDataBtn = findViewById(R.id.idBtnReadData);
        insertDataBtn = findViewById(R.id.idBtnInsert);
        tvNumber = findViewById(R.id.tvNumber);
    }

    private void generateFingerprint()  {
        dbHandler = new DBHandler(DBcontroller.this);
        int cnt = 0;
        try {
            cnt = getFolderCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] list;


        byte[] data;
        try {
            list = getResources().getAssets().list("music");
            for(int i = 0 ; i < cnt ; ++i){
                //InputStream inputStream =am.open("music/" + list[i]);
                InputStream inputStream =getAssets().open("music/"+list[i]);
                Wave wave = new Wave(inputStream);
                FingerprintManager fingerprintManager = new FingerprintManager();
                data = fingerprintManager.extractFingerprint(wave);
                String noExt;
                noExt = list[i].substring(0,list[i].lastIndexOf('.'));
                dbHandler.addFingerprint(noExt,data);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            Toast.makeText(DBcontroller.this, "Song has been added.", Toast.LENGTH_SHORT).show();
        }


        //dbHandler.addFingerprint(songName,fing);
        return;
    }

    private void generateHashUsingParallel() throws IOException {
        dbHandler = new DBHandler(DBcontroller.this);
        DataHelper dataHelper = new DataHelper();
        Gson gson = new Gson();

        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(threads);
        String[] list = getResources().getAssets().list("music");
        service.execute(new Runnable() {
            @Override
            public void run() {
                byte[] data;
                long total = 0;
                int i = 1;
                FingerprintManager fingerprintManager = new FingerprintManager();
                for(String s: list){
                    try{
                        long start = System.currentTimeMillis();
                        InputStream inputStream =getAssets().open("music/"+s);
                        Wave wave = new Wave(inputStream);
                        data = fingerprintManager.extractFingerprint(wave);
                        HashMap<Integer, List<Integer>> temp = dataHelper.fingerPrintToHashMap(data);
                        String json = gson.toJson(temp);
                        long duraion = System.currentTimeMillis() - start;
                        total += duraion;
                        inputStream.close();
                        System.out.println("Process time:  " + (System.currentTimeMillis() - start));
                        System.out.println("Song " + i  +" / " + list.length);
                        ++i;
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                }
                System.out.println("Avg time: " + total / list.length);
            }
        });


    }

    private void generateHashMap(){
        dbHandler = new DBHandler(DBcontroller.this);
        DataHelper dataHelper = new DataHelper();

        //Thread gen ;
        generateFingerPrint = new Thread(new Runnable() {
            @Override
            public void run() {
                long total = 0;
                String[] list;
                byte[] data;
                FingerprintManager fingerprintManager = new FingerprintManager();
                try {
                    list = getResources().getAssets().list("music");
                    for(int i = 0 ; i < list.length ; ++i){
                        long start = System.currentTimeMillis();
                        InputStream inputStream =getAssets().open("music/"+list[i]);
                        Wave wave = new Wave(inputStream);
                        data = fingerprintManager.extractFingerprint(wave);
                        HashMap<Integer, List<Integer>> temp = dataHelper.fingerPrintToHashMap(data);

                        Gson gson = new Gson();
                        String json = gson.toJson(temp);
                        String noExt;
                        noExt = list[i].substring(0,list[i].lastIndexOf('.'));
                        dbHandler.addHashMap(noExt,json);
                        long duration = System.currentTimeMillis() - start;
                        total += duration;
                        inputStream.close();
                        System.out.println("Process time:  " + duration);
                        System.out.println((i+1) + " / " + list.length);
                    }
                    System.out.println("Avg time: " + total / list.length + "  Total time: " +  (total/1000));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }finally {
                    //Toast.makeText(DBcontroller.this, "Song has been added.", Toast.LENGTH_SHORT).show();
                }
            }
        },"generateFingerPrintThread");
        generateFingerPrint.start();

        return;
    }



    private int getFolderCount() throws IOException {
        int cnt ;
        cnt = getResources().getAssets().list("music").length;

        return cnt;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dbHandler = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHandler = null;
    }
}