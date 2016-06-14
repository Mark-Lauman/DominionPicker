package ca.marklauman.dominionpicker.database;

import android.content.Context;
import android.database.Cursor;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import ca.marklauman.dominionpicker.R;

/** Handles connections to the core database.
 *  (The database containing publisher-provided information)
 *  @author Mark Lauman */
class CoreDb extends SQLiteAssetHelper {
    /** The file that the database is stored in. */
    static final String FILE_NAME = "core.db";

    public CoreDb(Context c) {
        super(c, FILE_NAME, null, c.getResources().getInteger(R.integer.db_ver_core));
        setForcedUpgrade();
    }

    /** Perform an sql query on this database */
    Cursor query(String table, String[] projection,
                 String selection, String[] selectionArgs,
                 String sortOrder, boolean distinct) {
        return getReadableDatabase()
                .query(distinct, table, projection, selection, selectionArgs, null, null, sortOrder, null);
    }

}
