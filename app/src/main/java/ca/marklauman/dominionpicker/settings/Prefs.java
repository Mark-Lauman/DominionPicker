package ca.marklauman.dominionpicker.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.tools.Utils;

/** Used to set up the preferences and store the preference keys.
 *  @author Mark Lauman */
@SuppressWarnings({"WeakerAccess", "UnusedDeclaration"})
public abstract class Prefs {
    /////////// Active preference keys \\\\\\\\\\\
    /** Key used to save the supply size to the the preferences. */
    public static final String LIMIT_SUPPLY = "limit_supply";
    /** Key used to save the event limiter to the preferences. */
    public static final String LIMIT_EVENTS = "limit_event";
    /** Key used to save the version of the preferences */
    public static final String VERSION = "version";
    /** Key used to save the card sort order */
    public static final String SORT_CARD = "sort_card";
    /** Key used to save the set filter to the preferences */
    public static final String FILT_SET = "filt_set";
    /** Key used to save the cost filter to the preferences */
    public static final String FILT_COST = "filt_cost";
    /** Key used to save the curse filter to the preferences */
    public static final String FILT_CURSE = "filt_curse";
    /** Key used to save the current language filter */
    public static final String FILT_LANG = "filt_lang";
    /** Key used to save cards deselected from the card list */
    public static final String FILT_CARD = "filt_card";

    /////////// Obsolete preference keys \\\\\\\\\\\
    /** The old separator used in the deprecated MultiSelectImagePreference. */
    private static final String OLD_SEP = "\u0001\u0007\u001D\u0007\u0001";
    /** Deprecated key used to identify the version 0 preferences. */
    @Deprecated
    public static final String FILT_SET_BASE = "filt_set_base";
    /** Old attempt to store preference version from v1. Never worked properly. */
    @Deprecated
    public static final String PREF_VERSION = "pref_version";
    /** Selected cards - used in preferences before v6 */
    @Deprecated
    public static final String SELECTIONS = "selections";

    /////////// Current preference/application state \\\\\\\\\\\
    private static Context staticContext;
    public static String filt_lang;
    public static String sort_card;
    public static String sort_set;


    /////////// Other \\\\\\\\\\\
    /** Set of all listeners currently active */
    private static HashSet<Listener> listeners = new HashSet<>();


    /** Set default preference values and update old preference setups to newer versions.
     *  @param c A context within DominionPicker. */
    public static void setup(Context c) {
        // Get the current preference version
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        int version = getVersion(prefs);

        // Set the default value for any undefined preferences
        setDefaultValues(c);

        // Update the preferences to the current version.
        final int cur_ver = c.getResources()
                             .getInteger(R.integer.pref_version);
        switch(version) {
            case -1: break; // preferences have been set for the first time
            case 0: update0(prefs);
            case 1: update1(prefs);
            case 2: update2(prefs);
            case 3: // v3 -> v4 adds filt_lang. Setting default values is all that is needed.
            case 4: // v4 -> v5 adds sort_card. Setting default values is all that is needed.
            case 5: update5(prefs);
        }
        prefs.edit().putInt(VERSION, cur_ver).commit();

        // Load current configuration
        staticContext = c.getApplicationContext();
        parsePreference(c, FILT_LANG);
        parsePreference(c, SORT_CARD);
    }

    /** Register this listener to receive preference change notifications. */
    public static synchronized void addListener(Listener listener) {
        listeners.add(listener);
    }

    /** Unregister a listener. */
    public static synchronized void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /** Call this method when you change a preference.
     *  All listeners will be notified of the change
     *  @param key The key of the preference that was changed. */
    public static synchronized void notifyChange(Context context, String key) {
        parsePreference(context, key);
        for(Listener listener : listeners) {
            if(listener == null) listeners.remove(null);
            else listener.prefChanged(key);
        }
    }


    /** Parse a given preference into a usable sql filter
     *  @param key key of the preference to parse
     */
    private static void parsePreference(Context c, String key) {
        switch(key) {
            case FILT_LANG:
                // Load the default language and current filter
                String[] def_lang = c.getResources().getStringArray(R.array.def_trans);
                String[] pref_lang = PreferenceManager.getDefaultSharedPreferences(c)
                                                .getString(key, c.getString(R.string.filt_lang_def))
                                                .split(",");
                // Replace "default" values with the default language for the set
                for(int i=0; i< pref_lang.length; i++) {
                    if(pref_lang[i].equals("0"))
                        pref_lang[i] = def_lang[i];
                }
                // Group sets together by language code
                HashMap<String, String> lang_map = new HashMap<>(2);
                String language;
                for (int set = 0; set < pref_lang.length; set++) {
                    language = pref_lang[set];
                    if (!lang_map.containsKey(language))
                        lang_map.put(language, "");
                    lang_map.put(language, lang_map.get(language)+","+set);
                }
                // Make an sql filter using the language preference
                filt_lang = TableCard._LANG + "=NULL";
                for (String lang : lang_map.keySet())
                    filt_lang += " OR (" + TableCard._LANG + "='" + lang
                               + "' AND " + TableCard._SET_ID + " IN ("
                               + lang_map.get(lang).substring(1) + "))";
                filt_lang = "("+filt_lang+")";
                break;
            case SORT_CARD:
                String[] sort = PreferenceManager.getDefaultSharedPreferences(c)
                                                 .getString(SORT_CARD, c.getString(R.string.sort_card_def))
                                                 .split(",");
                String[] sort_card_col = c.getResources().getStringArray(R.array.sort_card_col);
                String[] sort_set_col = c.getResources().getStringArray(R.array.sort_set_col);
                sort_card = "";
                sort_set = "";

                for(String s : sort) {
                    int i = Integer.parseInt(s);
                    if(i == 1) break;
                    sort_card += sort_card_col[i]+", ";
                    if(!sort_set_col[i].equals(""))
                        sort_set += ", "+sort_set_col[i];
                }
                sort_card += sort_card_col[1];
                if(sort_set.length() < 1)
                    sort_set = sort_set_col[0];
                else sort_set = sort_set.substring(2);
        }
    }


    /** Set the default preference values (current version) */
    private static void setDefaultValues(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor edit = prefs.edit();
        Resources res = c.getResources();

        if(!prefs.contains(FILT_SET))
            edit.putString(FILT_SET, res.getString(R.string.filt_set_def));
        if(!prefs.contains(FILT_COST))
            edit.putString(FILT_COST, res.getString(R.string.filt_cost_def));
        if(!prefs.contains(FILT_CURSE))
            edit.putBoolean(FILT_CURSE, res.getBoolean(R.bool.filt_curse_def));
        if(!prefs.contains(FILT_LANG))
            edit.putString(FILT_LANG, res.getString(R.string.filt_lang_def));
        if(!prefs.contains(SORT_CARD))
            edit.putString(SORT_CARD, res.getString(R.string.sort_card_def));
        if(!prefs.contains(LIMIT_SUPPLY))
            edit.putInt(LIMIT_SUPPLY, res.getInteger(R.integer.limit_supply_def));
        if(!prefs.contains(LIMIT_EVENTS))
            edit.putInt(LIMIT_EVENTS, res.getInteger(R.integer.limit_event_def));
        if(!prefs.contains(FILT_CARD))
            edit.putString(FILT_CARD, "");
        edit.commit();
    }


    /** Determine which version the preferences are on.
     *  @return The preference version, or -1 for new preferences. */
    private static int getVersion(SharedPreferences prefs) {
        // filt_set_base only in v0
        if(prefs.contains("filt_set_base")) return 0;
        // filt_set in all versions except v0
        if(!prefs.contains(FILT_SET)) return -1;
        // The following distinguish v1
        if(prefs.contains("pref_version")) return 1;
        String filt = prefs.getString(FILT_SET, "");
        if(filt.contains(OLD_SEP)) return 1;
        filt = prefs.getString(FILT_COST, "");
        if(filt.contains(OLD_SEP)) return 1;
        // v2 does not have VERSION set.
        return prefs.getInt(VERSION, 2);
    }


    /** Detects v0 preferences and updates them to v2. */
    private static void update0(SharedPreferences prefs) {
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
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(FILT_SET, newFilt);

        // remove old settings
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

        String filter = prefs.getString(FILT_SET, "");
        if(filter.contains(OLD_SEP)) {
            String newSets = "";
            if(filter.contains("Base")) newSets += ",0";
            if(filter.contains("Alchemy")) newSets += ",1";
            if(filter.contains("Black Market")) newSets += ",2";
            if(filter.contains("Cornucopia")) newSets += ",3";
            if(filter.contains("Dark Ages")) newSets += ",4";
            if(filter.contains("Envoy")) newSets += ",5";
            if(filter.contains("Governor")) newSets += ",6";
            if(filter.contains("Guilds")) newSets += ",7";
            if(filter.contains("Hinterlands")) newSets += ",8";
            if(filter.contains("Intrigue")) newSets += ",9";
            if(filter.contains("Prince")) newSets += ",10";
            if(filter.contains("Prosperity")) newSets += ",11";
            if(filter.contains("Seaside")) newSets += ",12";
            if(filter.contains("Stash")) newSets += ",13";
            if(filter.contains("Walled Village")) newSets += ",14";
            if(newSets.length() > 1) newSets = newSets.substring(1);
            edit.putString(FILT_SET, newSets);
        }

        // update costs to newest version
        filter = prefs.getString(FILT_COST, "");
        if(filter.contains(OLD_SEP)) {
            String newCost = "";
            if(filter.contains("Potion")) newCost += ",0";
            if(filter.contains("1")) newCost += ",1";
            if(filter.contains("2")) newCost += ",2";
            if(filter.contains("3")) newCost += ",3";
            if(filter.contains("4")) newCost += ",4";
            if(filter.contains("5")) newCost += ",5";
            if(filter.contains("6")) newCost += ",6";
            if(filter.contains("7")) newCost += ",7";
            if(filter.contains("8")) newCost += ",8";
            if(filter.contains("8*")) newCost += ",9";

            if(newCost.length() > 1) newCost = newCost.substring(1);
            edit.putString(FILT_COST, newCost);
        }
        edit.commit();
    }

    /** Updates preferences from v2 to v3. Does not detect version number. */
    private static void update2(SharedPreferences prefs) {
        String filter = prefs.getString(FILT_SET, "");
        if(filter.length() < 1)
             prefs.edit().putString(FILT_SET, "15").commit();
        else prefs.edit().putString(FILT_SET, filter + ",15").commit();
    }

    /** Updates preferences from v5 to v6. Does not detect version number */
    private static void update5(SharedPreferences prefs) {
        SharedPreferences.Editor edit = prefs.edit();

        // Add another language to FILT_LANG for the "Summon" card set
        String pref = prefs.getString(FILT_LANG, "");
        edit.putString(FILT_LANG, pref+",0");

        // Change the set filter from a NOT IN filter to an IN filter.
        String[] filt_set = prefs.getString(FILT_SET, "").split(",");
        HashSet<Integer> not_filt_set = new HashSet<>(filt_set.length);
        for(String s : filt_set)
            not_filt_set.add(Integer.parseInt(s));
        ArrayList<Integer> new_filt_set = new ArrayList<>(17 - filt_set.length);
        for(int i=0; i<17; i++)
            if(!not_filt_set.contains(i)) new_filt_set.add(i);
        edit.putString(FILT_SET, Utils.join(",", new_filt_set));

        // Remove all old selections
        edit.remove(SELECTIONS);

        edit.commit();
    }


    /** Listeners added to {@link Prefs} will be notified when it learns of preference changes. */
    public interface Listener {
        /** This method is called when a preference is changed.
         *  @param key The key identifying the changed preference */
        void prefChanged(String key);
    }

    /** Get the context of this application (not the display thread)
     *  Warning: This context behaves differently that the display context.
     *  Handle with extreme care. */
    public static Context getStaticContext() {
        return staticContext;
    }
}