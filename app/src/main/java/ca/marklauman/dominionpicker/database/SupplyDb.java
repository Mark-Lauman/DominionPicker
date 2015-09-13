package ca.marklauman.dominionpicker.database;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import ca.marklauman.dominionpicker.App;
import ca.marklauman.dominionpicker.R;

/** Describes the database used to store sample supplies
 *  provided by the publisher.
 *  @author Mark Lauman */
public class SupplyDb extends SQLiteAssetHelper {
    /** The file that the database is stored in. */
    static final String FILE_NAME = "supply.db";

    /** Each supply has a unique id. */
    public static final String _ID = BaseColumns._ID;
    /** The name of the supply. */
    public static final String _NAME = "name";
    /** What language the supply name is in. */
    public static final String _LANG = App.COL_LANG;
    /** The set that this supply came from. */
    public static final String _SET_ID = App.COL_SET_ID;
    /** The name of the set this supply comes from. */
    public static final String _SET_NAME = "set_name";
    /** Cards in this supply. */
    public static final String _CARDS = "cards";
    /** Column storing if this shuffle is a high cost game.
     *  (with colonies and shelters)  */
    public static final String _HIGH_COST = "high_cost";
    /** Column storing if this shuffle uses shelters.<br/>
     *  History Table, Java String */
    public static final String _SHELTERS = "shelters";
    /** Column storing the id of the bane card.
     *  The card is set to -1 if there is no bane card. */
    public static final String _BANE = "bane";

    public SupplyDb(Context c) {
        super(c, FILE_NAME, null, c.getResources().getInteger(R.integer.db_version));
    }

    Cursor query(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getReadableDatabase()
                   .query("supply", projection,
                           selection, selectionArgs,
                           null, null, sortOrder);
    }
}