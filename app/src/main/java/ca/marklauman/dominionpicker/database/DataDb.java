package ca.marklauman.dominionpicker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import ca.marklauman.dominionpicker.R;

/** This database stores user created data and card id numbers.
 *  Cards are referenced by id so they can update with language changes.
 *  @author Mark Lauman */
public class DataDb extends SQLiteOpenHelper {

    /** The internal name of the history table, which stores
     *  all shuffles by the user.                         */
    public static final String TABLE_HISTORY = "history";


    /** Column storing the timestamp of the shuffle.
     *  Serves as the index of the table.<br/>
     *  History Table, Java Long */
    public static final String _H_TIME = BaseColumns._ID;
    /** Column storing the name of the shuffle. The name is optional.
     *  If non-null, the row is a favorite.<br/>
     *  History Table, Java String */
    public static final String _H_NAME = "name";
    /** Column storing the cards found in this shuffle.
     *  This is stored as a string of card ids separated by commas.<br/>
     *  History Table, Java String */
    public static final String _H_CARDS = "cards";
    /** Column storing if this shuffle is a high cost game.
     *  (with colonies and shelters).<br/>
     *  History Table, Java Boolean */
    public static final String _H_HIGH_COST = "high_cost";
    /** Column storing if this shuffle uses shelters.<br/>
     *  History Table, Java String */
    public static final String _H_SHELTERS = "shelters";
    /** Column storing the id of the bane card.
     *  The card is set to -1 if there is no bane card.<br/>
     *  History Table, Java Long */
    public static final String _H_BANE = "bane";


    public DataDb(Context c) {
        super(c, "data.db", null, c.getResources().getInteger(R.integer.db_version));
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + " ("
                    + _H_TIME + " INTEGER PRIMARY KEY, "
                    + _H_NAME + " TEXT DEFAULT NULL, "
                    + _H_CARDS + " TEXT, "
                    + _H_HIGH_COST + " INTEGER, "
                    + _H_SHELTERS + " INTEGER, "
                    + _H_BANE + " INTEGER DEFAULT -1);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This is version 1 of the data db. There are no upgrades yet.
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Since future structures are unknown, just flash the known tables.
        db.execSQL("DROP TABLE " + TABLE_HISTORY);
        onCreate(db);
    }
}