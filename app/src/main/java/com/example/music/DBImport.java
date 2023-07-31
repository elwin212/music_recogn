package com.example.music;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBImport extends SQLiteOpenHelper {
    private static final String TAG = "DataBaseHelper"; // Tag just for the LogCat window
    private static final String DB_NAME ="song_database"; // Database name
    private static final int DB_VERSION = 1; // Database version
    private final File DB_FILE;
    private SQLiteDatabase mDataBase;

    private static final String DB_PATH = "/data/user/0/com.example.music/databases/";
    private final Context mContext;

    public DBImport(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        DB_FILE = context.getDatabasePath(DB_NAME);
        System.out.println(context.getDatabasePath(DB_NAME));
        this.mContext = context;
    }

    public void createDataBase() throws IOException {
        // If the database does not exist, copy it from the assets.
        boolean mDataBaseExist = checkDataBase();
        if(!mDataBaseExist) {
            System.out.println("Creating database.");
            this.getReadableDatabase();
            this.close();
            copyDataBase();
            // Copy the database from assets
            copyDataBase();
            Log.e(TAG, "Database created");
        }
        else {

            System.out.println("Database already exist");
        }
    }

    // Check that the database file exists in databases folder
    public boolean checkDataBase() {
        File databaseFile = new File(DB_PATH + DB_NAME);
        return databaseFile.exists();
    }

    // Copy the database from assets
    private void copyDataBase() {
        try {
            InputStream myInput = mContext.getAssets().open(DB_NAME);
            // Path to the just created empty db
            String outFileName = DB_PATH + DB_NAME;
            //Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);
            //transfer bytes from the input file to the output file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer))>0){
                myOutput.write(buffer, 0, length);
            }

            //Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        }
        catch (IOException e){
            Log.e(TAG, "Error copying database", e);
        }

    }

    // Open the database, so we can query it
    /*public boolean openDataBase() throws SQLException {
        // Log.v("DB_PATH", DB_FILE.getAbsolutePath());

        mDataBase = SQLiteDatabase.openDatabase(String.valueOf(DB_FILE), null, SQLiteDatabase.CREATE_IF_NECESSARY);
        // mDataBase = SQLiteDatabase.openDatabase(DB_FILE, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        return mDataBase != null;
    }*/

    public SQLiteDatabase openDatabase() throws SQLException {
        File dbFile = mContext.getDatabasePath(DB_NAME);

        if (!dbFile.exists()) {
            try {
                InputStream inputStream = mContext.getAssets().open(DB_NAME);
                OutputStream outputStream = new FileOutputStream(dbFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("Error creating source database", e);
            }
        }

        return SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if(mDataBase != null) {
            mDataBase.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
