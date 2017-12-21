package edu.buffalo.cse.cse486586.simpledht;

/**
 * Created by prasad-pc on 2/17/17.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SimpleDhtHelper extends SQLiteOpenHelper
{

    private static final String DATABASE_NAME = "PA3";
    private static final int DATABASE_VERSION = 1;

    SimpleDhtHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SimpleDhtDB.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SimpleDhtDB.onUpgrade(db, oldVersion, newVersion);
    }

}
