package com.example.music;

/*
* This is a project designed and developed by Yi-Hsuan Wang
* E-mail: elwin@mmisys.net
* */


import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.musicg.wave.Wave;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity{
    private static final int REQUEST_MICROPHONE_PERMISSION = 1;

    private static final int SAMPLING_RATE_IN_HZ = 11025;

    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;  //Single Channel

    //private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO; //Dual Channel
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLING_RATE_IN_HZ,CHANNEL_CONFIG,AUDIO_FORMAT);

    private byte[] mBUFFER;

    private static final float THRESHOLD = 0.20f;

    private final AtomicBoolean recordingInProgress = new AtomicBoolean(false);

    private final AtomicBoolean getMatchingResult = new AtomicBoolean(false);

    private AudioRecord recorder = null;

    private Thread recordingThread = null;

    private Thread identifyThread = null;

    private TextView songNameTV, artistTV;

    private ImageButton recordBtn, clearBtn;
    private ImageView albumIV;

    private Button importButton;

    private long recordingStartTimeMs = 0;

    private static final int RECORDING_DURATION_MS = 12000; //Set listening duration = 12s

    private static final int MIN_RECORDING_DURATION_MS = 4000; //Set minimal recording duration = 4s

    final String AUDIO_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/output.pcm";
    final String WAV_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/output.wav";

    private ArrayList<SongModel> songModelArrayList;
    private  ArrayList<SongModel>historyArrayList = new ArrayList<>();

    private ArrayList<SongFingerModel> songFingerModelArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);

        Intent intentGetArray = getIntent();

        Bundle arrayList = intentGetArray.getBundleExtra("BUNDLE");
        if(arrayList != null){
            historyArrayList = (ArrayList<SongModel>) arrayList.getSerializable("ARRAYLIST");
        }

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottomNavigationView);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.home);

        // Perform item selected listener
        Bundle args = new Bundle();
        args.putSerializable("ARRAYLIST",(Serializable) historyArrayList);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch(item.getItemId())
            {
                case R.id.database:
                    byte[] temp;
                    Intent intentDatabase = new Intent(MainActivity.this,DBcontroller.class);
                   // temp = passFingerPrint();
                    intentDatabase.putExtra("BUNDLE", args);
                    startActivity(intentDatabase);
                    overridePendingTransition(0,0);
                    return true;
                case R.id.home:
                    return true;

                case R.id.history:
                    Intent intentHistory = new Intent(MainActivity.this, ViewHistory.class);

                    intentHistory.putExtra("BUNDLE",args);
                    startActivity(intentHistory);
                    overridePendingTransition(0,0);
                    return true;
            }
            return false;
        });

        initView();

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink_anime);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Import database
                DBImport db = new DBImport(MainActivity.this);
                try {
                    db.createDataBase();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long recordingDurationMs = System.currentTimeMillis() - recordingStartTimeMs;
                artistTV.setText("");
                // start recording method will
                // start the recording of audio.
                if(!recordingInProgress.get()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            albumIV.setImageResource(R.drawable.ic_headset);
                            albumIV.startAnimation(animation);
                            songNameTV.setText("Listening....");
                            recordBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.round_mic));
                            recordBtn.setImageResource(R.drawable.ic_back_hand);
                            startRecording();
                        }
                    });
                }
                else{
                    if(recordingDurationMs < MIN_RECORDING_DURATION_MS){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                songNameTV.setText("Not enough information...");
                                artistTV.setText("Please wait longer");
                                albumIV.clearAnimation();
                                recordBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.round_mic));
                                recordBtn.setImageResource(R.drawable.ic_mic_white);
                                forceStop();
                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                songNameTV.setText("Stopped....");
                                albumIV.clearAnimation();

                                    stopRecording();


                            }
                        });
                    }
                }

            }
        });
        clearBtn.setOnClickListener(v -> {
            clearBtn.setEnabled(false);
            songNameTV.setText("Press the button to begin");
            artistTV.setText("");
            albumIV.setImageResource(R.drawable.ic_headset);
        });
    }


    @SuppressLint("MissingPermission")
    private void startRecording() {
        if(CheckPermission()){
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

            if(recorder.getState() == AudioRecord.STATE_UNINITIALIZED){
                return;
            }

            recorder.startRecording();


            recordingInProgress.set(true);

            recordingThread = new Thread(new Runnable() {
                public void run() {
                    final File file = new File(Environment.getExternalStorageDirectory(), "output.pcm");
                    //final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
                    mBUFFER = new byte[BUFFER_SIZE];
                    recordingStartTimeMs = System.currentTimeMillis();
                    try (final FileOutputStream outStream = new FileOutputStream(file)) {

                        while (recordingInProgress.get()) {
                            int result = recorder.read(mBUFFER,0,BUFFER_SIZE);
                            if (result < 0) {
                                throw new RuntimeException("Reading of audio buffer failed: " +
                                        getBufferReadFailureReason(result));
                            }
                            //outStream.write(buffer.array(), 0, BUFFER_SIZE);
                            outStream.write(mBUFFER, 0, BUFFER_SIZE);
                            long recordingDurationMs = System.currentTimeMillis() - recordingStartTimeMs;
                            if(recordingDurationMs >= RECORDING_DURATION_MS){
                                outStream.close();
                                stopRecording();
                                break;
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Writing of recorded audio failed", e);
                    }
                }
            }, "AudioRecorder Thread");

            recordingThread.start();
        }
        else{
            RequestPermission();
        }
    }

    private void stopRecording() {

        if (null == recorder) {
            return;
        }
        DataProcess dataProcess = new DataProcess();
        dataProcess.AddWavHeader(AUDIO_FILE_PATH,WAV_FILE_PATH);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recordBtn.setEnabled(false);
                songNameTV.setText("Searching for song...");
                //recordBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.ic_play));
                //recordBtn.setEnabled(true);
                clearBtn.setEnabled(true);
            }
        });


        recordingInProgress.set(false);

        recorder.stop();

        recorder.release();

        recorder = null;

        recordingThread = null;

        optimizedCompare();

        identifyThread = null;

        mBUFFER = null;

    }

    private void forceStop(){

        if (null == recorder) {
            return;
        }
        recordingInProgress.set(false);

        recorder.stop();

        recorder.release();

        recorder = null;

        recordingThread = null;

        identifyThread = null;

        mBUFFER = null;

    }

    private void optimizedCompare(){
        recordBtn.setEnabled(false);
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<Integer, List<Integer>>>() {}.getType();
        DataHelper dataHelper = new DataHelper();
        DBHandler dbHandler = new DBHandler(MainActivity.this);

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                Wave w1 = new Wave(Environment.getExternalStorageDirectory().getAbsolutePath() + "/output.wav");
                DataHelper dataHelper1 = new DataHelper();
                long test1 = System.currentTimeMillis();
                byte[] input = dataHelper1.extractFingerPrint(w1);
                System.out.println("Extract: " + (System.currentTimeMillis() - test1));
                ArrayList<SongFingerModel> sfm = new ArrayList<>();
                sfm = dbHandler.readSongsOnlyFinger();
                SongModel songModel = null;
                System.out.println(sfm.size());
                long start = System.currentTimeMillis();
                for(SongFingerModel s : sfm){
                    HashMap<Integer,List<Integer>> fromDataBase = gson.fromJson(s.getFing(),type);
                    double sim = dataHelper.getSimilarFromHash(input, fromDataBase);
                    if(sim >= THRESHOLD){
                        getMatchingResult.set(true);
                        songModel = dbHandler.readResult(s.getId());
                        final String songName = songModel.getSongName();
                        final String artistName = songModel.getArtistName();
                        final String albumName = songModel.getAlbumName();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(songModel.getAlbumUrl(), 0, songModel.getAlbumUrl().length);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                songNameTV.setText(String.format("%s",songName));
                                albumIV.clearAnimation();
                                artistTV.setText(artistName + " / " + albumName);

                                albumIV.setImageBitmap(bitmap);
                                //Picasso.get().load(albumUrl).resize(275,275).into(albumIV);
                                recordBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.round_mic));
                                recordBtn.setImageResource(R.drawable.ic_mic_white);
                                recordBtn.setEnabled(true);
                            }
                        });
                        historyArrayList.add(songModel);
                        System.out.println("Song Found! " + songModel.getSongName());
                        System.out.println(System.currentTimeMillis() - start);

                        break;
                    }
                }
                if(!getMatchingResult.get()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            albumIV.clearAnimation();
                            songNameTV.setText(String.format("%s","No matched song..."));
                            artistTV.setText(String.format("%s","Try it again"));
                            recordBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.round_mic));
                            recordBtn.setImageResource(R.drawable.ic_mic_white);
                            recordBtn.setEnabled(true);
                        }
                    });
                }
            }
        },"OptimizedThread");
        t1.start();
    }

    private void compareAudioFromDataBase(){
        recordBtn.setEnabled(false);
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<Integer, List<Integer>>>() {}.getType();
        DataHelper dataHelper = new DataHelper();
        DBHandler dbHandler = new DBHandler(MainActivity.this);
        identifyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Wave w1 = new Wave(Environment.getExternalStorageDirectory().getAbsolutePath() + "/output.wav");
                //FingerprintManager fingerprintManager = new FingerprintManager();
                DataHelper dataHelper1 = new DataHelper();
                long test1 = System.currentTimeMillis();
                byte[] input = dataHelper1.extractFingerPrint(w1);

                System.out.println("Extract: " + (System.currentTimeMillis() - test1));
                songModelArrayList = new ArrayList<SongModel>();
                songModelArrayList = dbHandler.readSongs();

                //System.out.println(songModelArrayList.size());
                long start = System.currentTimeMillis();
                for(SongModel s : songModelArrayList){
                    long start1 = System.currentTimeMillis();
                    HashMap<Integer,List<Integer>> temp2 = gson.fromJson(s.getFingerPrint(),type);
                    double similarity = dataHelper.getSimilarFromHash(input,temp2);
                    //System.out.println(s.getSongName() + " / " + similarity + " / Process time: " + (System.currentTimeMillis() - start1));
                    final String songName = s.getSongName();
                    final String artistName = s.getArtistName();
                    final String albumName = s.getAlbumName();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(s.getAlbumUrl(), 0, s.getAlbumUrl().length);
                    //final byte[] albumUrl = s.getAlbumUrl();
                    if(similarity >= THRESHOLD){
                        getMatchingResult.set(true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                songNameTV.setText(String.format("%s",songName));
                                albumIV.clearAnimation();
                                artistTV.setText(artistName + " / " + albumName);

                                albumIV.setImageBitmap(bitmap);
                                //Picasso.get().load(albumUrl).resize(275,275).into(albumIV);
                                recordBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.round_mic));
                                recordBtn.setImageResource(R.drawable.ic_mic_white);
                                recordBtn.setEnabled(true);
                            }
                        });
                        historyArrayList.add(s);
                        System.out.println("Song Found! " + s.getSongName());
                        System.out.println(System.currentTimeMillis() - start);

                        break;
                    }
                }
                if(!getMatchingResult.get()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            albumIV.clearAnimation();
                            songNameTV.setText(String.format("%s","No matched song..."));
                            artistTV.setText(String.format("%s","Try it again"));
                            recordBtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.round_mic));
                            recordBtn.setImageResource(R.drawable.ic_mic_white);
                            recordBtn.setEnabled(true);
                        }
                    });
                }

            }
        },"Identify Thread");
        identifyThread.start();
    }


    private void initView(){
        recordBtn = findViewById(R.id.btnRecord);
        clearBtn = findViewById(R.id.btn_clear);
        songNameTV = findViewById(R.id.tvSongName);
        artistTV = findViewById(R.id.tvArtistName);
        albumIV = findViewById(R.id.imgAlbum);
        artistTV.setSelected(true);
        songNameTV.setText("Press the button to begin");
        importButton = findViewById(R.id.button2);

        artistTV.setText("");

        recordBtn.setEnabled(true);
        clearBtn.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        forceStop();
    }

    private boolean CheckPermission(){
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(),READ_EXTERNAL_STORAGE);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(),INTERNET);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO,WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE,INTERNET}, REQUEST_MICROPHONE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_MICROPHONE_PERMISSION:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionRead = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionInternet = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore && permissionRead && permissionInternet) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    private String getBufferReadFailureReason(int errorCode) {
        switch (errorCode) {
            case AudioRecord.ERROR_INVALID_OPERATION:
                return "ERROR_INVALID_OPERATION";
            case AudioRecord.ERROR_BAD_VALUE:
                return "ERROR_BAD_VALUE";
            case AudioRecord.ERROR_DEAD_OBJECT:
                return "ERROR_DEAD_OBJECT";
            case AudioRecord.ERROR:
                return "ERROR";
            default:
                return "Unknown (" + errorCode + ")";
        }
    }

}