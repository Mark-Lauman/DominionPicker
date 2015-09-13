package ca.marklauman.dominionpicker;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import ca.marklauman.dominionpicker.database.CardDb;
import ca.marklauman.dominionpicker.settings.Prefs;

import java.security.InvalidParameterException;
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

    /** <p></p>Updates the data in this class from the given context object.</p>
     *  This method should be called in the following circumstances:
     *  <ul><li>In the onCreate() methods of Activities and Fragments
     *  that use the App variables.</li>
     *  <li>In the onStart() method of activities amd fragments if the
     *  language preference could change before the activity is destroyed.
     *  (Such as if the user switches to the settings screen then
     *  switches back)</li></ul> */
    public static synchronized void updateInfo(Context c) {
        staticContext = c.getApplicationContext();

        // Check if the translation has changed
        String filt = PreferenceManager.getDefaultSharedPreferences(c)
                                       .getString(Prefs.FILT_LANG, "");
        String transId = c.getResources().getConfiguration().locale.getLanguage()
                         +"|"+filt;
        if (transId.equals(App.transId)) return;

        // We need to update the translation
        String[] trans = getTrans(filt.split(","),
                                  App.staticContext.getResources()
                                                   .getStringArray(R.array.def_trans));
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

    /** Get the translation setup given the preference and default values.
     *  @param pref The preference split into individual language codes.
     * @param def The default languages to fall back to.
     * @return The translation that should be used. */
    private static @NonNull String[] getTrans(@NonNull String[] pref, @NonNull String[] def) {
        if(pref.length != def.length)
            throw new InvalidParameterException("Language preference and default not the same");
        String[] res = new String[def.length];
        for(int i=0; i< res.length; i++) {
            res[i] = def[i];
            if(! pref[i].startsWith("0"))
                res[i] = pref[i];
        }
        return res;
    }
}