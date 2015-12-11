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
    /** The version of this database, set on first construction */
    private static int version;

    public CoreDb(Context c) {
        super(c, FILE_NAME, null, checkVersion(c));
        setForcedUpgrade(version);

    }

    /** Check what version this database is on and update {@link #version}
     *  @param c Local context.
     * @return The version number */
    private static int checkVersion(Context c) {
        version = c.getResources().getInteger(R.integer.db_ver_core);
        return version;
    }

    /** Perform an sql query on this database */
    Cursor query(String table, String[] projection,
                 String selection, String[] selectionArgs,
                 String sortOrder) {
        return getReadableDatabase()
                .query(table, projection, selection, selectionArgs, null, null, sortOrder);
    }
}
