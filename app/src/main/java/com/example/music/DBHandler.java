package com.example.music;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "song_database";

    // below int is database version
    private static final int DB_VERSION = 4;  //Currently version 3

    // below variable is for table name.
    private static final String TABLE_NAME = "songinfo";

    private static final String TABLE_NAME2 = "fingerprint";

    private static final String TABLE_NAME3 = "hashfingerprint";

    // below variable is for id column.
    private static final String ID_COL = "id";

    // below variable is for course name column
    private static final String NAME_COL = "name";

    // below variable id for our course duration column.
    private static final String ARTIST_COL = "artist";

    // below variable for our course description column.
    private static final String ALBUM_COL = "album";

    private static final String ALBUM_URL = "url";

    // below variable is for our course tracks column.
    private static final String FINGERPRINT_COL = "fingerprint";

    private static final String query2 = "CREATE TABLE " + TABLE_NAME2 + "(id INTEGER PRIMARY KEY AUTOINCREMENT, songname TEXT, fingerprint BLOB)";
    private static final String query3 = "CREATE TABLE " + TABLE_NAME3 + "(id INTEGER PRIMARY KEY AUTOINCREMENT, songname TEXT, fingerprint TEXT)";

    // creating a constructor for our database handler.
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // on below line we are creating
        // an sqlite query and we are
        // setting our column names
        // along with their data types.
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME_COL + " TEXT,"
                + ARTIST_COL + " TEXT,"
                + ALBUM_COL + " TEXT,"
                + ALBUM_URL + " TEXT,"
                + FINGERPRINT_COL + " BLOB)";


        // at last we are calling a exec sql
        // method to execute above sql query
        //db.execSQL(query);
        db.execSQL(query3);
    }

    public void addNewSong(String songName, String artistName, String albumName, String url, byte[] fingerprintData) {

        // on below line we are creating a variable for
        // our sqlite database and calling writable method
        // as we are writing data in our database.
        SQLiteDatabase db = this.getWritableDatabase();

        // on below line we are creating a
        // variable for content values.
        ContentValues values = new ContentValues();

        // on below line we are passing all values
        // along with its key and value pair.
        values.put(NAME_COL, songName);
        values.put(ARTIST_COL, artistName);
        values.put(ALBUM_COL, albumName);
        values.put(ALBUM_URL, url);
        values.put(FINGERPRINT_COL, fingerprintData);

        // after adding all values we are passing
        // content values to our table.
        db.insert(TABLE_NAME, null, values);

        // at last we are closing our
        // database after adding database.
        db.close();
    }

    public void addFingerprint(String name, byte[] fing){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("songname",name);
        values.put("fingerprint",fing);
        db.insert(TABLE_NAME2, null,values);
        db.close();
    }

    public void clearTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM hashfingerprint");
    }

    public ArrayList<SongModel> readSongs() {
        // on below line we are creating a
        // database for reading our database.
        SQLiteDatabase db = this.getReadableDatabase();

        // on below line we are creating a cursor with query to read data from database.
        Cursor cursorSong = db.rawQuery("SELECT song_name, artist, album, blob_image, fingerprint FROM " + TABLE_NAME, null);

        // on below line we are creating a new array list.
        ArrayList<SongModel> songModalArrayList = new ArrayList<>();

        // moving our cursor to first position.
        if (cursorSong.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                songModalArrayList.add(new SongModel(cursorSong.getString(0), cursorSong.getString(1), cursorSong.getString(2),cursorSong.getBlob(3),cursorSong.getString(4)));
            } while (cursorSong.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        cursorSong.close();
        db.close();
        return songModalArrayList;
    }




    public ArrayList<SongFingerModel> readSongsOnlyFinger() {
        // on below line we are creating a
        // database for reading our database.
        SQLiteDatabase db = this.getReadableDatabase();

        // on below line we are creating a cursor with query to read data from database.
        Cursor cursorSong = db.rawQuery("SELECT id, fingerprint FROM " + TABLE_NAME , null);

        // on below line we are creating a new array list.
        ArrayList<SongFingerModel> songFingerModelArrayList = new ArrayList<>();

        // moving our cursor to first position.
        if (cursorSong.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                songFingerModelArrayList.add(new SongFingerModel(cursorSong.getInt(0),cursorSong.getString(1)));
            } while (cursorSong.moveToNext());
            // moving our cursor to next.
        }
        // at last closing our cursor
        // and returning our array list.
        cursorSong.close();
        db.close();
        return songFingerModelArrayList;
    }

    public SongModel readResult(Integer i){
        SQLiteDatabase db = getReadableDatabase();

        String selection = "id = ?";
        String[] selectionArgu = {Integer.toString(i)};
        String rawQuery = "SELECT song_name, artist, album, blob_image FROM songinfo WHERE " + selection;


        SongModel s = null;
        Cursor cursor = db.rawQuery(rawQuery, selectionArgu);
        //Cursor cursor = db.rawQuery("SELECT song_name, artist, album, blob_image FROM " + TABLE_NAME, argu);
        if(cursor != null && cursor.moveToFirst()){
             s = new SongModel(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getBlob(3));
        }

        cursor.close();
        db.close();
        return s;
    }



    public void addHashMap(String name, String jsonHash){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("songname",name);
        values.put("fingerprint",jsonHash);
        db.insert(TABLE_NAME3, null,values);
        db.close();
    }

    /*public ArrayList<SongFingerModel> readSongFinger(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME3, null);

        ArrayList<SongFingerModel> songFingerModelArrayList = new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                songFingerModelArrayList.add(new SongFingerModel(cursor.getString(1),cursor.getString(2)));
            }while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return songFingerModelArrayList;
    }*/

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < newVersion){
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
            //db.execSQL(query2);
        }
        // this method is called to check if the table exists already.
       // db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    @Override
    public synchronized void close() {
        super.close();

    }
}
