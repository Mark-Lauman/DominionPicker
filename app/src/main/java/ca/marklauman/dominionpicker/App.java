package ca.marklauman.dominionpicker;

import android.content.Context;
import ca.marklauman.dominionpicker.database.CardDb;
import java.util.HashMap;

/** Used to store generic info that is useful all over the app. */
public abstract class App {

    // Columns that a database table must include to use the translation filter.
    /** Language column used in the translated databases. */
    public static final String COL_LANG = "language";
    /** Set id column used in the translated databases. */
    public static final String COL_SET_ID = "set_id";

    /** The context of the process that the application is in.
     *  (This is different from the context of each thread,
     *  and should only be used only if no other context is available) */
    public static Context staticContext;
    /** Unique identifier for the current translation. */
    public static String transId;
    /** Card filter used to provide the current translation. */
    public static String transFilter;
    /** Order used to sort cards before display. */
    public static final String sortOrder = CardDb._SET_NAME+", "+CardDb._NAME;

    /** Updates the data in this class from the given context object.
     *  Should be called in the onCreate() and onStart() method
     *  of each entry point to the app where the data is needed. */
    public static synchronized void updateInfo(Context c) {
        staticContext = c.getApplicationContext();

        // Check if the translation has changed
        String transId = c.getResources().getConfiguration().locale.getLanguage();
        // TODO: Check if the language settings have changed
        if (transId.equals(App.transId)) return;

        // We need to update the translation
        String[] trans = App.staticContext.getResources().getStringArray(R.array.def_trans);
        // TODO: Override default translation with settings.
        // Group the sets together by language
        HashMap<String, String> map = new HashMap<>();
        String language;
        for (int set = 0; set < trans.length; set++) {
            language = trans[set];
            if (!map.containsKey(language))
                map.put(language, "");
            map.put(language, map.get(language)+","+set);
        }
        // Turn the translation into an SQL "WHERE" clause
        // Cards with null language are user defined, and load regardless of language.
        String filter = COL_LANG + "=NULL";
        for (String key : map.keySet()) {
            filter += " OR (" + COL_LANG + "='" + key
                    + "' AND "+COL_SET_ID+" IN ("+map.get(key).substring(1) + "))";
        }
        // Surround the filter with brackets to prevent logic leaks.
        App.transFilter = "(" + filter + ")";
        App.transId = transId;
    }
}