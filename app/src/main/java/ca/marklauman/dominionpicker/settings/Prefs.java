package ca.marklauman.dominionpicker.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import ca.marklauman.dominionpicker.R;

/** Used to set up the preferences and store the preference keys. */
@SuppressWarnings({"WeakerAccess", "UnusedDeclaration"})
public abstract class Prefs {
    /** The old separator used in the deprecated MultiSelectImagePreference. */
    private static final String OLD_SEP = "\u0001\u0007\u001D\u0007\u0001";


    /** Key used to save picked cards to the preferences. */
    public static final String SELECTIONS = "selections";
    /** Key used to save the version of the preferences */
    public static final String VERSION = "version";


    /** Deprecated key used to identify the version 0 preferences. */
    @Deprecated
    public static final String FILT_SET_BASE = "filt_set_base";
    /** Old attempt to store preference version from v1. Never worked properly. */
    @Deprecated
    public static final String PREF_VERSION = "pref_version";

    /** Set default preference values and update old preference setups to newer versions.
     *  @param c A context within DominionPicker.                      */
    public static void setup(Context c) {
        PreferenceManager.setDefaultValues(c, R.xml.pref_filters, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        Log.d("Version", ""+prefs.getInt(VERSION, 0));

        update0(prefs);
        update1(prefs);
        int version = c.getResources()
                       .getInteger(R.integer.pref_version);
        prefs.edit().putInt(VERSION, version)
             .commit();
    }


    /** Detects v0 preferences and updates them to v2. */
    private static void update0(SharedPreferences prefs) {
        if(! prefs.contains("filt_set_base")) return;

        // interpret old settings
        String newFilt = "";
        if (!prefs.getBoolean("filt_set_base", true)) newFilt += ",0";
        if (!prefs.getBoolean("filt_set_alchemy", true)) newFilt += ",1";
        if (!prefs.getBoolean("filt_set_black_market", true)) newFilt += ",2";
        if (!prefs.getBoolean("filt_set_cornucopia", true)) newFilt += ",3";
        if (!prefs.getBoolean("filt_set_dark_ages", true)) newFilt += ",4";
        if (!prefs.getBoolean("filt_set_envoy", true)) newFilt += ",5";
        if (!prefs.getBoolean("filt_set_governor", true)) newFilt += ",6";
        if (!prefs.getBoolean("filt_set_guilds", true)) newFilt += ",7";
        if (!prefs.getBoolean("filt_set_hinterlands", true)) newFilt += ",8";
        if (!prefs.getBoolean("filt_set_intrigue", true)) newFilt += ",9";
        // Prince not in version 0
        if (!prefs.getBoolean("filt_set_prosperity", true)) newFilt += ",11";
        if (!prefs.getBoolean("filt_set_seaside", true)) newFilt += ",12";
        if (!prefs.getBoolean("filt_set_stash", true)) newFilt += ",13";
        if (!prefs.getBoolean("filt_set_walled_village", true)) newFilt += ",14";
        // trim off the comma at the beginning
        if (newFilt.length() > 1) newFilt = newFilt.substring(1);

        // write them in the new format
        prefs.edit().putString("filt_set", newFilt);

        // remove old settings
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove("filt_set_base");
        edit.remove("filt_set_alchemy");
        edit.remove("filt_set_black_market");
        edit.remove("filt_set_cornucopia");
        edit.remove("filt_set_dark_ages");
        edit.remove("filt_set_envoy");
        edit.remove("filt_set_governor");
        edit.remove("filt_set_guilds");
        edit.remove("filt_set_hinterlands");
        edit.remove("filt_set_intrigue");
        edit.remove("filt_set_prosperity");
        edit.remove("filt_set_seaside");
        edit.remove("filt_set_stash");
        edit.remove("filt_set_walled_village");
        edit.commit();
    }


    /** Detects v1 preferences and updates them to v2. */
    private static void update1(SharedPreferences prefs) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove("pref_version");

        String filt = prefs.getString("filt_set", "");
        if(filt.contains(OLD_SEP)) {
            String newSets = "";
            if(filt.contains("Base")) newSets += ",0";
            if(filt.contains("Alchemy")) newSets += ",1";
            if(filt.contains("Black Market")) newSets += ",2";
            if(filt.contains("Cornucopia")) newSets += ",3";
            if(filt.contains("Dark Ages")) newSets += ",4";
            if(filt.contains("Envoy")) newSets += ",5";
            if(filt.contains("Governor")) newSets += ",6";
            if(filt.contains("Guilds")) newSets += ",7";
            if(filt.contains("Hinterlands")) newSets += ",8";
            if(filt.contains("Intrigue")) newSets += ",9";
            if(filt.contains("Prince")) newSets += ",10";
            if(filt.contains("Prosperity")) newSets += ",11";
            if(filt.contains("Seaside")) newSets += ",12";
            if(filt.contains("Stash")) newSets += ",13";
            if(filt.contains("Walled Village")) newSets += ",14";
            if(newSets.length() > 1) newSets = newSets.substring(1);
            edit.putString("filt_set", newSets);
        }

        // update costs to newest version
        filt = prefs.getString("filt_cost", "");
        if(filt.contains(OLD_SEP)) {
            String newCost = "";
            if(filt.contains("Potion")) newCost += ",0";
            if(filt.contains("1")) newCost += ",1";
            if(filt.contains("2")) newCost += ",2";
            if(filt.contains("3")) newCost += ",3";
            if(filt.contains("4")) newCost += ",4";
            if(filt.contains("5")) newCost += ",5";
            if(filt.contains("6")) newCost += ",6";
            if(filt.contains("7")) newCost += ",7";
            if(filt.contains("8")) newCost += ",8";
            if(filt.contains("8*")) newCost += ",9";

            if(newCost.length() > 1) newCost = newCost.substring(1);
            edit.putString("filt_cost", newCost);
        }
        edit.commit();
    }
}