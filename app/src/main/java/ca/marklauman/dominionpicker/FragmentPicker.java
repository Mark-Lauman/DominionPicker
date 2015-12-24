package ca.marklauman.dominionpicker;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import ca.marklauman.dominionpicker.cardadapters.AdapterCardsFilter;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.settings.Prefs;

/** Fragment governing the card list screen.
 *  @author Mark Lauman */
public class FragmentPicker extends Fragment
                            implements LoaderCallbacks<Cursor>, Prefs.Listener {

    /** The view associated with the card list. */
    private ListView card_list;
    /** The adapter for the card list. */
    private AdapterCardsFilter adapter;
    /** The view associated with an empty list */
    private View empty;
    /** The view associated with a loading list */
    private View loading;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Prefs.addListener(this);
        adapter = new AdapterCardsFilter(getContext());
        getActivity().getSupportLoaderManager()
                     .restartLoader(LoaderId.PICKER, null, this);
    }


    @Override
    public void onDestroy() {
        Prefs.removeListener(this);
        super.onDestroy();
    }


    /** Called when a preference's value has changed */
    @Override
    public void prefChanged(String key) {
        switch(key) {
            // These preferences affect the picker
            case Prefs.FILT_SET:  case Prefs.FILT_COST:
            case Prefs.FILT_LANG: case Prefs.SORT_CARD:
                FragmentActivity act = getActivity();
                if(act == null) return;
                act.getSupportLoaderManager()
                   .restartLoader(LoaderId.PICKER, null, this);
                break;
        }
    }


    /** Called to create this fragment's view for the first time.  */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picker, container, false);
        card_list = (ListView) view.findViewById(R.id.card_list);
        empty = view.findViewById(android.R.id.empty);
        loading = view.findViewById(android.R.id.progress);

        if(adapter.getCursor() != null)
            card_list.setEmptyView(empty);
        else card_list.setEmptyView(loading);
        return view;
    }


    public void toggleAll() {
        adapter.toggleAll();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Show the loading icon if we have views
        if(card_list != null) {
            empty.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            card_list.setEmptyView(loading);
        }
        adapter.changeCursor(null);

        // Basic setup
        CursorLoader c = new CursorLoader(getActivity());
        c.setUri(Provider.URI_CARD_ALL);
        c.setProjection(AdapterCardsFilter.COLS_USED);
        String sel = "";
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ArrayList<CharSequence> sel_args = new ArrayList<>();

        // Sort order
        String[] sort_col = getResources().getStringArray(R.array.sort_card_col);
        String[] sort_pref = pref.getString(Prefs.SORT_CARD, "").split(",");
        String sort = "";
        for(String s : sort_pref) {
            int i = Integer.parseInt(s);
            if(i == 1) break;
            sort += sort_col[i] + ", ";
        }
        c.setSortOrder(sort + sort_col[1]);

        // Filter out sets
        String curSel = pref.getString("filt_set", "");
        if(0 < curSel.length())
            sel += " AND "+ TableCard._SET_ID+" IN ("+curSel+")";

        // TODO: Cost filters
//        // Filter out potions
//        String[] costs = getActivity().getResources()
//                                      .getStringArray(R.array.filt_cost);
//        String filt_cost = pref.getString("filt_cost", "");
//        ArrayList<CharSequence> split_cost = new ArrayList<>(Arrays.asList(
//                        MultiSelectPreference.mapValues(filt_cost, null, costs)));
//        String potion = getResources().getStringArray(R.array.filt_cost)[0];
//        if(0 < split_cost.size() && potion.equals(split_cost.get(0))) {
//            sel += " AND " + TableCard._POT + "=?";
//            sel_args.add("0");
//            split_cost.remove(0);
//        }
//
//        // Filter out costs
//        curSel = "";
//        for(CharSequence s : split_cost) {
//            sel_args.add(s);
//            curSel += ",?";
//        }
//        if(0 < curSel.length()) sel += " AND "+ TableCard._COST+" NOT IN ("+curSel.substring(1)+")";

        // Filter out cursers
        boolean filt_curse = pref.getBoolean("filt_curse", true);
        if(!filt_curse) {
            sel += " AND " + TableCard._META_CURSER + "=?";
            sel_args.add("0");
        }

        // Translation filter
        sel = (sel+" AND "+Prefs.filt_lang).substring(5);

        c.setSelection(sel);
        String[] sel_args_final = new String[sel_args.size()];
        for(int i=0; i<sel_args.size(); i++)
            sel_args_final[i] = sel_args.get(i).toString();
        c.setSelectionArgs(sel_args_final);

        return c;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);

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
        }
        adapter.changeCursor(null);
    }
}