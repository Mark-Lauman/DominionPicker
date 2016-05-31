package ca.marklauman.dominionpicker.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.HashSet;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.userinterface.recyclerview.AdapterCards;
import ca.marklauman.tools.Utils;

/** This class manages the SharedPreferences of this activity.
 *  It contains the key for every preference, tools for reading preferences
 *  and routines for keeping compound preferences up to date.
 *  @author Mark Lauman */
public abstract class Pref implements OnSharedPreferenceChangeListener {

    /** Context of this app's application */
    private static Context appContext;

    /////////// Active preference keys \\\\\\\\\\\
    /** Current preference version */
    public static final String VERSION = "version";
    /** Index of the tab that the MainActivity should start on. */
    public static final String ACTIVE_TAB = "active_tab";
    /** Maximum number of kingdom cards in a supply. */
    public static final String LIMIT_SUPPLY = "limit_supply";
    /** Maximum number of event cards in a supply. */
    public static final String LIMIT_EVENTS = "limit_event";

    /** Filter used to exclude cards by set/expansion */
    public static final String FILT_SET = "filt_set";
    /** Filter used to exclude cards by cost (in coins) */
    public static final String FILT_COST = "filt_cost";
    /** Filter used to exclude cards by debt cost */
    public static final String FILT_DEBT = "filt_debt";
    /** Filter used to exclude cards that require a potion */
    public static final String FILT_POTION = "filt_potion";
    /** Filter used to exclude curse-giving cards */
    public static final String FILT_CURSE = "filt_curse";
    /** Filter used to exclude specific cards */
    public static final String FILT_CARD = "filt_card";
    /** Filter used to specify required cards. */
    public static final String REQ_CARDS = "req_cards";

    /** Filter used to provide the correct card translation for each set.
     *  This is computed from {@link #FILT_LANG} and {@link #APP_LANG}
     *  when those preferences change. */
    public static final String COMP_LANG = "comp_lang";
    /** Contains the preferred language for each set. */
    public static final String FILT_LANG = "filt_lang";
    /** Language used by the app for ui elements.
     *  If the language changes, then the default language for each set changes. */
    public static final String APP_LANG = "app_lang";

    /** Used to sort cards before they are displayed.
     *  This is an sql ORDER BY clause, derived from {@link #SORT_CARD} */
    public static final String COMP_SORT_CARD = "comp_sort_card";
    /** Used to sort sets in the rules screen.
     *  This is an sql ORDER BY clause, derived from {@link #SORT_CARD} */
    public static final String COMP_SORT_SET = "comp_sort_set";
    /** The order in which things should be sorted. This is stored as a list
     *  of numbers, so that database updates don't break the sort order. */
    public static final String SORT_CARD = "sort_card";

    /////////// Obsolete preference keys \\\\\\\\\\\
    /** The old separator used in the deprecated MultiSelectImagePreference. */
    private static final String OLD_SEP = "\u0001\u0007\u001D\u0007\u0001";
    /** Deprecated key used to identify the version 0 preferences. */
    @Deprecated
    private static final String FILT_SET_BASE = "filt_set_base";
    /** Old attempt to store preference version from v1. Never worked properly. */
    @Deprecated
    public static final String PREF_VERSION = "pref_version";
    /** Selected cards - used in preferences before v6 */
    @Deprecated
    public static final String SELECTIONS = "selections";

    /** Listener used to update the computed preferences when they change */
    public static final Listener prefUpdater = new Listener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch(key) {
                case APP_LANG: case FILT_LANG:
                     updateLanguage(appContext);
                     break;
                case SORT_CARD:
                     updateSort(appContext);
                     break;
            }
        }
    };


    //////////// Routine methods - used everywhere \\\\\\\\\\\\
    /** Get the preferences for this app. */
    public static SharedPreferences get(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /** Edit the preferences for this app. */
    public static SharedPreferences.Editor edit(Context context) {
        return get(context).edit();
    }

    /** Get the application context of this app (this is not the context of the UI thread).
     *  Warning: This context is different from an Activity's context and should only
     *  be used with care when no other contexts are available. */
    public static Context getAppContext() {
        return appContext;
    }

    /** Register a listener to receive preference change notifications. */
    public static synchronized void addListener(Listener listener) {
        get(appContext).registerOnSharedPreferenceChangeListener(listener);
    }

    /** Unregister a listener. */
    public static synchronized void removeListener(Listener listener) {
        get(appContext).unregisterOnSharedPreferenceChangeListener(listener);
    }

    /** Retrieves the current language filter ({@link #COMP_LANG}). */
    public static String languageFilter(Context context) {
        return get(context).getString(COMP_LANG, "");
    }

    /** Retrieves the current card sort order ({@link #COMP_SORT_CARD}). */
    public static String cardSort(Context context) {
        return get(context).getString(COMP_SORT_CARD, "");
    }

    /** Retrieves the current card sort order ({@link #COMP_SORT_SET}). */
    public static String setSort(Context context) {
        return get(context).getString(COMP_SORT_SET, "");
    }


    //////////// Preference setup (called on application start) \\\\\\\\\\\\
    /** Set the preferences to default values, call for updates if needed and
     *  retrieve the application context.
     *  @param context A context within DominionPicker. */
    public static void setup(Context context) {
        appContext = context.getApplicationContext();
        Resources res = context.getResources();
        SharedPreferences pref = get(context);

        // Update the preferences as needed.
        int oldVersion = getVersion(pref);
        setDefaultValues(context);
        final int newVersion = res.getInteger(R.integer.pref_version);
        switch(oldVersion) {
            case -1: break; // preferences have been set for the first time
            case 0: update0(pref);
            case 1: update1(pref);
            case 2: update2(pref);
            case 3: // v3 -> v4 adds filt_lang. Setting default values is all that is needed.
            case 4: // v4 -> v5 adds sort_card. Setting default values is all that is needed.
            case 5: update5(pref);
        }
        pref.edit().putInt(VERSION, newVersion).commit();

        // Compute all computed preferences and add the listener.
        updateLanguage(context);
        updateSort(context);
        addListener(prefUpdater);
    }


    /** Verify that the language has not changed.
     *  If it has, the language filter will be updated. */
    public static void checkLanguage(Context context) {
        if(!context.getString(R.string.language)
                .equals(get(context).getString(APP_LANG, "")))
            updateLanguage(context);
    }


    /** Update {@link #COMP_LANG} so it reflects {@link #APP_LANG}. */
    private static void updateLanguage(Context context) {
        // Determine current state
        final Resources res = context.getResources();
        final SharedPreferences pref = get(context);
        final String[] defTrans  = res.getStringArray(R.array.def_trans);
        String[] rawTrans = pref.getString(FILT_LANG, res.getString(R.string.filt_lang_def))
                                .split(",");

        // Update the app language if needed.
        final String oldLang = pref.getString(APP_LANG, "");
        final String newLang = res.getString(R.string.language);
        if(!oldLang.equals(newLang))
            pref.edit().putString(APP_LANG, newLang).commit();

        // Replace "0" values with the default language for the set
        for(int i=0; i<rawTrans.length; i++)
            if(rawTrans[i].equals("0"))
                rawTrans[i] = defTrans[i];

        // Group sets together by language
        HashMap<String, String> transMap = new HashMap<>(2);
        for(int set=0; set<rawTrans.length; set++) {
            String lang = rawTrans[set];
            if(!transMap.containsKey(lang))
                transMap.put(lang, "");
            transMap.put(lang, transMap.get(lang)+","+set);
        }

        // Create an sql filter from transMap
        String compTrans = TableCard._LANG + "=NULL";
        for(String lang : transMap.keySet())
            compTrans += " OR (" + TableCard._LANG + "='" + lang
                         + "' AND " + TableCard._SET_ID + " IN ("
                         + transMap.get(lang).substring(1) + "))";
        compTrans = "("+compTrans+")";

        // Apply that filter to the COMP_LANG preference.
        if(!compTrans.equals(pref.getString(COMP_LANG, "")))
            pref.edit()
                .putString(COMP_LANG, compTrans)
                .commit();
    }


    /** Update {@link #COMP_SORT_CARD} and {@link #COMP_SORT_SET}
     *  so it matches the other preferences. */
    private static void updateSort(Context context) {
        Resources res = context.getResources();
        SharedPreferences pref = get(context);
        String[] rawSort = pref.getString(SORT_CARD, context.getString(R.string.sort_card_def))
                               .split(",");
        String[] colCard = res.getStringArray(R.array.sort_card_col);
        String[] colSet  = res.getStringArray(R.array.sort_set_col);
        String sortCard = "";
        String sortSet  = "";

        // If we have any sort rules, add them
        if(rawSort.length != 0 && !"".equals(rawSort[0])) {
            for(String rawId : rawSort) {
                int id = Integer.parseInt(rawId);
                // Sorting stops at the card name column
                if(id == 1) break;
                // Add a new card sort column
                sortCard += colCard[id]+",";
                // Not all sort ids apply to card sets
                if(!colSet[id].equals(""))
                    sortSet += colSet[id]+", ";
            }
        }

        // The final sort category is by name
        sortCard += colCard[1];
        sortSet +=  colSet[0];

        // Check if either has changed before writing them to memory
        SharedPreferences.Editor edit = pref.edit();
        if(!sortCard.equals(pref.getString(COMP_SORT_CARD, "")))
            edit.putString(COMP_SORT_CARD, sortCard);
        if(!sortSet.equals(pref.getString(COMP_SORT_SET, "")))
            edit.putString(COMP_SORT_SET, sortSet);
        edit.commit();
    }


    /** Set the default preference values (current version) */
    private static void setDefaultValues(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor edit = prefs.edit();
        Resources res = c.getResources();

        if(!prefs.contains(FILT_SET))
            edit.putString(FILT_SET, res.getString(R.string.filt_set_def));
        if(!prefs.contains(FILT_POTION))
            edit.putBoolean(FILT_POTION, res.getBoolean(R.bool.filt_pot_def));
        if (!prefs.contains(FILT_COST))
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
        if(!prefs.contains(REQ_CARDS))
            edit.putString(REQ_CARDS, "");
        if(!prefs.contains(ACTIVE_TAB))
            edit.putInt(ACTIVE_TAB, res.getInteger(R.integer.def_tab));
        edit.commit();
    }


    /** Determine which version the preferences are on.
     *  @return The preference version, or -1 for new preferences. */
    @SuppressWarnings("deprecation")
    private static int getVersion(SharedPreferences prefs) {
        // filt_set_base only in v0
        if(prefs.contains(FILT_SET_BASE)) return 0;
        // filt_set in all versions except v0
        if(!prefs.contains(FILT_SET)) return -1;
        // The following distinguish v1
        if(prefs.contains(PREF_VERSION)) return 1;
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
    @SuppressWarnings("deprecation")
    private static void update5(SharedPreferences prefs) {
        SharedPreferences.Editor edit = prefs.edit();

        // Add another language to FILT_LANG for the "Summon" card set
        String pref = prefs.getString(FILT_LANG, "");
        edit.putString(FILT_LANG, pref+",0");
        // Set the sort order to mirror previous versions
        edit.putString(SORT_CARD, "0,1,2,3,4,5,6");
        // Set the active tab to be the cards tab
        edit.putInt(ACTIVE_TAB, 1);

        // Change the set filter from a NOT IN filter to a IN filter.
        String[] filt_set = prefs.getString(FILT_SET, "").split(",");
        HashSet<Integer> not_filt_set = new HashSet<>(filt_set.length);
        for (String s : filt_set) {
            try{ not_filt_set.add(Integer.parseInt(s));
            } catch (NumberFormatException ignored){}
        }
        HashSet<Integer> new_filt_set = new HashSet<>(16 - filt_set.length);
        for(int i=0; i<16; i++)
            if(!not_filt_set.contains(i)) new_filt_set.add(i);
        edit.putString(FILT_SET, Utils.join(",", new_filt_set));

        // Update the cost & potions filter
        String filt_cost = prefs.getString(FILT_COST, "");
        edit.putBoolean(FILT_POTION, !filt_cost.contains("0"));
        HashSet<Integer> new_filt_cost = new HashSet<>();
        for(String s : filt_cost.split(",")) {
            try {
                int val = Integer.parseInt(s);
                if(val == 9)      new_filt_cost.add(8);
                else if(val != 0) new_filt_cost.add(val);
            } catch (NumberFormatException ignored){}
        }
        edit.putString(FILT_COST, Utils.join(",", new_filt_cost));

        // create a filt_set_arr so access is quicker
        boolean[] filt_set_arr = new boolean[16];
        for(int i=0; i<filt_set_arr.length; i++)
            filt_set_arr[i] = new_filt_set.contains(i);

        // Change the card filter from a IN filter to a NOT IN filter.
        String[] filt_sel_split = prefs.getString(SELECTIONS, "").split(",");
        HashSet<Integer> filt_card = new HashSet<>();
        // Get the selected cards
        HashSet<Integer> filt_sel = new HashSet<>(filt_sel_split.length);
        for(String card : filt_sel_split) {
            try{ filt_sel.add(Integer.parseInt(card));
            } catch (NumberFormatException ignored){}
        }
        // Invert the selection. Unselected sets are not added to filt_card.
        for(int i=1; i<257; i++) {
            if(within(i, 6, 30) && !filt_set_arr[0])
                i= 30;  // Base set
            else if(within(i, 31, 42) && !filt_set_arr[1])
                i= 42;  // Alchemy
            else if(within(i, 43, 67) && !filt_set_arr[9])
                i= 67;  // Intrigue
            else if(within(i, 68, 92) && !filt_set_arr[11])
                i= 92;  // Prosperity
            else if(within(i, 93, 118) && !filt_set_arr[12])
                i=118;  // Seaside
            else if(within(i, 119, 153) && !filt_set_arr[4])
                i=153;  // Dark Ages
            else if(within(i, 154, 166) && !filt_set_arr[3])
                i=166;  // Cornucopia
            else if(within(i, 167, 179) && !filt_set_arr[7])
                i=179;  // Guilds
            else if(within(i, 180, 205) && !filt_set_arr[8])
                i=205;  // Hinterlands
            else if(within(i, 207, 256) && !filt_set_arr[15])
                i=256;  // Adventures
            else if(i==1 && !filt_set_arr[2])
                i=1;  // Black Market
            else if(i==2 && !filt_set_arr[13])
                i=2;  // Stash
            else if(i==3 && !filt_set_arr[5])
                i=3;  // Envoy
            else if(i==4 && !filt_set_arr[14])
                i=4;  // Walled Village
            else if(i==5 && !filt_set_arr[6])
                i=5;  // Governor
            else if(i==206 && !filt_set_arr[10])
                i=206;  // Prince
            else if(!filt_sel.contains(i)) // Add the card if needed - it's set is not filtered
                filt_card.add(i);
        }
        edit.putString(FILT_CARD, Utils.join(",", filt_card));
        edit.remove(SELECTIONS);

        edit.commit();
    }

    /** Check if a value is within a given range */
    private static boolean within(int val, int startRange, int endRange) {
        return startRange <= val && val <= endRange;
    }


    /** Listeners added to {@link Pref} will be notified when it learns of preference changes. */
    public interface Listener extends SharedPreferences.OnSharedPreferenceChangeListener {}
}