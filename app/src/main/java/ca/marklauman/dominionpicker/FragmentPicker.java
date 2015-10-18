package ca.marklauman.dominionpicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;

import ca.marklauman.dominionpicker.cardlist.AdapterSelCards;
import ca.marklauman.dominionpicker.database.CardDb;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.tools.preferences.MultiSelectPreference;

/** Governs the Picker screen. Allows users to choose what cards they want.
 *  @author Mark Lauman */
public class FragmentPicker extends Fragment
                            implements LoaderCallbacks<Cursor>,
                                       ListView.OnItemClickListener {

    /** The view associated with the card list. */
    private ListView card_list;
    /** The adapter for the card list. */
    private AdapterSelCards adapter = null;
    /** The view associated with an empty list */
    private View empty;
    /** The view associated with a loading list */
    private View loading;

    /* These values are set in onCreate when it calls initLoader.
     * They are then checked at each startup to see if they have
     * changed. If they have, a reload is required.            */
    /** Current language loaded by this picker */
    private String transId;
    /** Value of the set filter */
    private String filt_set;
    /** Value of the cost filter */
    private String filt_cost;
    /** Value of the curse filter */
    private boolean filt_curse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start loading the card list
        LoaderManager lm = getActivity().getSupportLoaderManager();
        lm.initLoader(LoaderId.PICKER, null, this);
    }


    /** Called to create this fragment's view for the first time.  */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if(view != null) return view;

        view = inflater.inflate(R.layout.fragment_picker, container, false);
        card_list = (ListView) view.findViewById(R.id.card_list);
        card_list.setOnItemClickListener(this);
        empty = view.findViewById(android.R.id.empty);
        loading = view.findViewById(android.R.id.progress);

        if(adapter != null) {
            card_list.setAdapter(adapter);
            card_list.setEmptyView(empty);
        }
        else card_list.setEmptyView(loading);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Reload the cards if the display language has changed.
        App.updateInfo(getActivity());
        if(filterChanged())
            getActivity().getSupportLoaderManager()
                         .restartLoader(LoaderId.PICKER, null, this);

    }

    /** Checks if any filters' value has changed */
    private boolean filterChanged() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String new_set = pref.getString("filt_set", "");
        final String new_cost = pref.getString("filt_cost", "");
        final boolean new_curse = pref.getBoolean("filt_curse", true);

        return !App.transId.equals(transId) ||
               !new_set.equals(filt_set) ||
               !new_cost.equals(filt_cost) ||
               new_curse != filt_curse;
    }

    @Override
    public void onDestroyView() {
        card_list = null;
        empty = null;
        loading = null;
        super.onDestroyView();
    }


    /** Called when this fragment is no longer visible to the user */
    @Override
    public void onStop() {
        saveSelections(getActivity());
        super.onStop();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Show the loading icon if we have views
        if(card_list != null) {
            empty.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            card_list.setEmptyView(loading);
            card_list.setAdapter(null);
        }
        adapter = null;

        // Basic setup
        CursorLoader c = new CursorLoader(getActivity());
        c.setUri(Provider.URI_CARD_ALL);
        c.setProjection(AdapterSelCards.COLS_USED);
        c.setSortOrder(App.sortOrder);
        String sel = "";
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ArrayList<CharSequence> sel_args = new ArrayList<>();

        // Filter out sets
        String curSel = "";
        filt_set = pref.getString("filt_set", "");
        if(0 < filt_set.length()) {
            String[] split_set = filt_set.split(",");
            Collections.addAll(sel_args, split_set);
            for (CharSequence ignored : split_set) curSel += ",?";
            if (0 < curSel.length())
                sel += " AND "+CardDb._SET_ID+" NOT IN ("+curSel.substring(1)+")";
        }

        // Filter out potions
        String[] costs = getActivity().getResources()
                                      .getStringArray(R.array.filt_cost);
        filt_cost = pref.getString("filt_cost", "");
        ArrayList<CharSequence> split_cost = new ArrayList<>(Arrays.asList(
                        MultiSelectPreference.mapValues(filt_cost, null, costs)));
        String potion = getResources().getStringArray(R.array.filt_cost)[0];
        if(0 < split_cost.size() && potion.equals(split_cost.get(0))) {
            sel += " AND " + CardDb._POT + "=?";
            sel_args.add("0");
            split_cost.remove(0);
        }

        // Filter out costs
        curSel = "";
        for(CharSequence s : split_cost) {
            sel_args.add(s);
            curSel += ",?";
        }
        if(0 < curSel.length()) sel += " AND "+CardDb._COST+" NOT IN ("+curSel.substring(1)+")";

        // Filter out cursers
        filt_curse = pref.getBoolean("filt_curse", true);
        if(!filt_curse) {
            sel += " AND " + CardDb._META_CURSER + "=?";
            sel_args.add("0");
        }

        // Translation filter
        transId = App.transId;
        sel = (sel+" AND "+App.transFilter).substring(5);

        c.setSelection(sel);
        String[] sel_args_final = new String[sel_args.size()];
        for(int i=0; i<sel_args.size(); i++)
            sel_args_final[i] = sel_args.get(i).toString();
        c.setSelectionArgs(sel_args_final);

        return c;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter = new AdapterSelCards(getActivity());
        adapter.changeCursor(data);

        // Load and apply the last selections (if any)
        // Default to all selected if there are no selections.
        Long[] selections = loadSelections(getActivity());
        if(selections != null)
            adapter.setSelections(selections);
        else adapter.selectAll();

        // display the loaded data
        if(card_list != null) {
            card_list.setAdapter(adapter);
            empty.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            card_list.setEmptyView(empty);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(card_list != null) {
            empty.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            card_list.setEmptyView(loading);
            card_list.setAdapter(null);
        }
        adapter = null;
    }

    public void toggleAll() {
        adapter.toggleAll();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(adapter != null) adapter.toggleItem(id);
    }


    /** Save currently selected items to the preferences.
     *  Automatically triggered when this fragment is stopped. */
    public void saveSelections(Context c) {
        if(c == null || adapter == null) return;
        Long[] selections = adapter.getSelections();
        StringBuilder str = new StringBuilder();
        for (long selection : selections)
            str.append(selection).append(",");
        PreferenceManager.getDefaultSharedPreferences(c)
                         .edit()
                         .putString(Prefs.SELECTIONS, str.toString())
                         .commit();
    }


    /** Loads the cards that were selected last.
     *  @param c The context of this activity - to be used
     *           to load the selections from the preferences.
     *  @return The card ids selected, or {@code null} if no
     *          cards were selected.                      */
    public static Long[] loadSelections(Context c) {
        String store = PreferenceManager.getDefaultSharedPreferences(c)
                                        .getString(Prefs.SELECTIONS, null);
        if(store == null) return null;
        StringTokenizer tok = new StringTokenizer(store, ",");
        Long[] selections = new Long[tok.countTokens()];
        for(int i=0; i<selections.length; i++)
            selections[i] = Long.parseLong(tok.nextToken());
        return selections;
    }
}