/* Copyright (c) 2015 Mark Christopher Lauman
 *
 * Licensed under the The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.                                                                  */
package ca.marklauman.dominionpicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;

import ca.marklauman.tools.MultiSelectPreference;

/** Used to pick the cards used in all other shuffles.
 *  @author Mark Lauman                             */
public class FragmentPicker extends Fragment
                            implements LoaderCallbacks<Cursor>,
                                       ListView.OnItemClickListener {

    /** ID used for the picker's card loader */
    private static final int LOADER_PICKER = 1;

    /** Key used to save selections to the preferences. */
    @SuppressWarnings("WeakerAccess")
    public static final String KEY_SELECT = "selections";

    /** The view associated with the card list. */
    private ListView card_list;
    /** The adapter for the card list. */
    private CardAdapter adapter;
    /** The view associated with an empty list */
    private View empty;
    /** The view associated with a loading list */
    private View loading;

    /* These values are set in onCreate when it calls initLoader.
     * They are then checked at each startup to see if they have
     * changed. If they have, a reload is required.            */
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
        adapter = null;
        LoaderManager lm = getActivity().getSupportLoaderManager();
        lm.initLoader(LOADER_PICKER, null, this);
    }


    /** Called to create this fragment's view for the first time.  */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if(view != null)
            return view;
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

        // Restart the loader if the filters have changed
        // The try block prevents crashes if the loader is already restarting
        if(filtChanged()) {
            try { getActivity().getSupportLoaderManager()
                               .restartLoader(LOADER_PICKER, null, this);
            } catch (Exception ignored) {}
        }

    }

    /** Checks if any filters' value has changed */
    private boolean filtChanged() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String new_set = pref.getString("filt_set", "");
        String new_cost = pref.getString("filt_cost", "");
        boolean new_curse = pref.getBoolean("filt_curse", true);

        return !new_set.equals(filt_set) ||
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
        // Set the display to loading if the display exists
        if(card_list != null) {
            empty.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            card_list.setEmptyView(loading);
            card_list.setAdapter(null);
        }
        adapter = null;

        // Basic setup
        CursorLoader c = new CursorLoader(getActivity());
        c.setUri(CardList.URI);
        String sel = "";
        ArrayList<CharSequence> sel_args = new ArrayList<>();
        Resources res = getActivity().getResources();
        String[] sets  = res.getStringArray(R.array.card_sets);
        String[] costs = res.getStringArray(R.array.filt_cost);

        // Filter by set
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        filt_set = pref.getString("filt_set", "");
        CharSequence[] split_set = MultiSelectPreference.mapValues(filt_set, sets);
        Collections.addAll(sel_args, split_set);
        for (CharSequence ignored:split_set)
            sel += "AND " + CardList._EXP + "!=? ";

        // Filter by cost
        filt_cost = pref.getString("filt_cost", "");
        ArrayList<CharSequence> split_cost = new ArrayList<>(Arrays.asList(
                        MultiSelectPreference.mapValues(filt_cost, costs)));
        String potion = getResources().getStringArray(R.array.filt_cost)[0];
        if(0 < split_cost.size() && potion.equals(split_cost.get(0))) {
            sel += "AND " + CardList._POTION + "=? ";
            sel_args.add("0");
            split_cost.remove(0);
        }
        for(CharSequence s : split_cost)
            sel_args.add(s);
        for(int i=0; i<split_cost.size(); i++)
            sel += "AND " + CardList._COST + "!=? ";

        // Filter out cursors
        filt_curse = pref.getBoolean("filt_curse", true);
        if(!filt_curse) {
            sel += "AND " + CardList._CURSER + "=? ";
            sel_args.add("0");
        }

        if(sel_args.size() != 0)
            sel = sel.substring(4);

        c.setSelection(sel);
        String[] sel_args_final = new String[sel_args.size()];
        for(int i=0; i<sel_args.size(); i++)
            sel_args_final[i] = sel_args.get(i).toString();
        c.setSelectionArgs(sel_args_final);

        return c;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter = new CardAdapter(getActivity());
        adapter.setChoiceMode(CardAdapter.CHOICE_MODE_MULTIPLE);
        adapter.changeCursor(data);

        // Load and apply the last selections (if any)
        // Default to all selected if there are no selections.
        long[] selections = loadSelections(getActivity());
        if(selections != null)
            adapter.setSelections(selections);
        else adapter.selectAll();

        // display the loaded data
        if(card_list != null) {
            card_list.setAdapter(adapter);
            card_list.setOnItemClickListener(adapter);
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

    /** Get the currently selected cards. */
    public long[] getSelections() {
        return adapter.getSelectionIds();
    }

    /** Check the selected cards to see if they are a valid
     *  supply. Eliminate contradictions where they occur, and
     *  return the cleaned selection.
     *  @return The supply, or null if no valid supply could be made. */
    public long[] getSupplySelections() {
        // minimum allowable selection size (may increase due to some cards)
        int min_select = 10;
        // the result
        long[] res = adapter.getSelectionIds();

        // check for young witch
        int young_witch = -1;
        for(int i=0; i<res.length; i++) {
            if(res[i] == CardList.ID_YOUNG_WITCH)
                young_witch = i;
        }

        // Handle young witch
        if(young_witch != -1) {
            if(adapter.checkYWitchTargets() < 1) {
					/* Eliminate young witch, as it has no
					 * viable targets.                  */
                long[] new_sel = new long[res.length - 1];
                int b = 0;
                for(int a=0; a<new_sel.length; a++) {
                    if(b == young_witch)
                        b++;
                    new_sel[a] = res[b];
                    b++;
                }
                res = new_sel;
            } else min_select++;
        }

        if(min_select <= res.length) return res;

        String more = getActivity().getString(R.string.more);
        Toast.makeText(getActivity(),
                       more + " (" + res.length + "/" + min_select + ")",
                       Toast.LENGTH_LONG).show();
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(adapter != null) adapter.toggleItem(position);
    }


    /** Save currently selected items to the preferences */
    public void saveSelections(Context c) {
        if(c == null || adapter == null) return;
        long[] selections = adapter.getSelectionIds();
        StringBuilder str = new StringBuilder();
        for (long selection : selections)
            str.append(selection).append(",");
        PreferenceManager.getDefaultSharedPreferences(c)
                         .edit()
                         .putString(KEY_SELECT, str.toString())
                         .commit();
    }


    /** Loads the cards that were selected last.
     *  @param c The context of this activity - to be used
     *           to load the selections from the preferences.
     *  @return The card ids selected, or {@code null} if no
     *          cards were selected.                      */
    private static long[] loadSelections(Context c) {
        String store = PreferenceManager.getDefaultSharedPreferences(c)
                                        .getString(KEY_SELECT, null);
        if(store == null) return null;
        StringTokenizer tok = new StringTokenizer(store, ",");
        long[] selections = new long[tok.countTokens()];
        for(int i=0; i<selections.length; i++)
            selections[i] = Long.parseLong(tok.nextToken());
        return selections;
    }
}