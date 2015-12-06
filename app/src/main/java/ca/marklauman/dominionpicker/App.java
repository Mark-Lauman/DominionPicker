package ca.marklauman.dominionpicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.tools.Utils;

import java.util.ArrayList;
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
    /** Unique identifier for the active translation */
    public static String transId;
    /** Card filter used to provide the current translation. */
    public static String transFilter;
    /** Unique identifier for the sort order */
    public static String sortId;
    /** Order used to sort cards before display. */
    public static String sortOrder;

    /** <p></p>Updates the data in this class from the given context object.</p>
     *  This method should be called in the following circumstances:
     *  <ul><li>In the onCreate() methods of Activities and Fragments
     *  that use the App variables.</li>
     *  <li>In the onStart() method of activities amd fragments if the
     *  language preference could change before the activity is destroyed.
     *  (Such as if the user switches to the settings screen then
     *  switches back)</li></ul> */
    public static synchronized void updateInfo(Context c) {
        // Update the context
        staticContext = c.getApplicationContext();

        // Update the active preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        Resources res = c.getResources();
        updateTrans(res, prefs.getString(Prefs.FILT_LANG, ""));
        updateSort(res, prefs.getString(Prefs.SORT_CARD, ""));
    }


    /** Update the current translation if necessary */
    private static void updateTrans(@NonNull Resources res, @NonNull String prefRaw) {
        // Check if an update is needed
        String transId = res.getConfiguration().locale.getLanguage()+"|"+prefRaw;
        if(transId.equals(App.transId)) return;

        // Load the preference and perform a sanity check
        String[] pref = prefRaw.split(",");
        String[] def = res.getStringArray(R.array.def_trans);
        if(pref.length != def.length)
            pref = def;

        // Sets using the "default" translation are assigned a language here
        String[] trans = new String[def.length];
        for(int i=0; i< trans.length; i++) {
            trans[i] = def[i];
            if(! pref[i].startsWith("0"))
                trans[i] = pref[i];
        }

        // Sets are grouped together by language (for SQL "IN" queries)
        HashMap<String, String> map = new HashMap<>();
        String language;
        for (int set = 0; set < trans.length; set++) {
            language = trans[set];
            if (!map.containsKey(language))
                map.put(language, "");
            map.put(language, map.get(language)+","+set);
        }

        // Turn the grouped sets into an SQL "WHERE" clause.
        String filter = COL_LANG + "=NULL";
        for (String key : map.keySet())
            filter += " OR (" + COL_LANG + "='" + key
                    + "' AND "+COL_SET_ID+" IN ("+map.get(key).substring(1) + "))";

        // Apply the new translation
        App.transId = transId;
        App.transFilter = "("+filter+")";
    }


    private static void updateSort(@NonNull Resources res, @NonNull String prefRaw) {
        // Quick check to see if an update is needed.
        if(prefRaw.equals(App.sortId)) return;

        final String[] cols = res.getStringArray(R.array.sort_card_col);
        String[] pref = prefRaw.split(",");
        String[] def = res.getString(R.string.sort_card_def).split(",");
        if(pref.length != def.length)
            pref = def;

        // Map the preference to column names
        ArrayList<String> ret = new ArrayList<>(2);
        for(String s : pref) {
            try {
                ret.add(cols[Integer.parseInt(s)]);
            } catch(Exception ignored){}
            if("1".equals(s)) break;
        }

        sortId = prefRaw;
        sortOrder = Utils.join(", ", ret);
    }
}