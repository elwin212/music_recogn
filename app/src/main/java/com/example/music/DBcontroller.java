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
    private Button readDataBtn, createTable, generateFinger, importDatabase;
    private DBHandler dbHandler = new DBHandler(DBcontroller.this);

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

        readDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DBcontroller.this, ViewSongs.class);
                startActivity(i);

            }
        });

        createTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHandler.createSongTable();
            }
        });

        importDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Import database
                DBImport db = new DBImport(DBcontroller.this);
                if (db.checkDataBase()){
                    Toast.makeText(DBcontroller.this, "Database already existed", Toast.LENGTH_SHORT).show();
                }
                else{
                    try {
                        db.createDataBase();
                        Toast.makeText(DBcontroller.this, "Database has been imported!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        generateFinger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    generateHashMap();
                } catch (IOException e) {
                    Toast.makeText(DBcontroller.this, "No music in folder.", Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        });


    }

    private void initView(){
        readDataBtn = findViewById(R.id.idBtnReadData);
        generateFinger = findViewById(R.id.idBtnGenerateFingerPrint);
        importDatabase = findViewById(R.id.idBtnImportDatabase);
        createTable = findViewById(R.id.idBtnCreateTable);
        tvNumber = findViewById(R.id.tvNumber);
    }


    private void generateHashUsingParallel() throws IOException {
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
                        long duration = System.currentTimeMillis() - start;
                        total += duration;
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

    private void generateHashMap() throws IOException {
        DataHelper dataHelper = new DataHelper();
        final String[] list = getResources().getAssets().list("music");
        if(list.length == 0){
            Toast.makeText(DBcontroller.this, "No music in folder.", Toast.LENGTH_SHORT).show();
            return;
        }
        //Thread gen ;
        generateFingerPrint = new Thread(new Runnable() {
            @Override
            public void run() {
                long total = 0;

                byte[] data;
                FingerprintManager fingerprintManager = new FingerprintManager();
                try {
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
                }
            }
        },"generateFingerPrintThread");
        generateFingerPrint.start();

        return;
    }



    private int getFolderCount() throws IOException {
        final int cnt = getResources().getAssets().list("music").length;;
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