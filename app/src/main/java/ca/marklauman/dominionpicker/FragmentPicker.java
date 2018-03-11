package ca.marklauman.dominionpicker;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.userinterface.recyclerview.AdapterCardsFilter;
import ca.marklauman.dominionpicker.settings.Pref;
import ca.marklauman.tools.recyclerview.ListDivider;

/** Fragment governing the card list screen.
 *  @author Mark Lauman */
public class FragmentPicker extends Fragment
                            implements LoaderCallbacks<Cursor>, Pref.Listener {

    /** The list of cards. */
    @BindView(android.R.id.list)     RecyclerView card_list;
    /** The loading screen. */
    @BindView(android.R.id.progress) View loading;
    /** The screen that is displayed when no cards are available. */
    @BindView(android.R.id.empty)    View empty;

    /** The adapter for the card list. */
    private AdapterCardsFilter adapter;
    /** The cursor containing the cards on display right now. */
    private Cursor mCursor = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentActivity activity = getActivity();
        if(activity != null)
            activity.getSupportLoaderManager()
                    .restartLoader(LoaderId.PICKER, null, this);
        Pref.addListener(this);
    }


    @Override
    public void onDestroy() {
        Pref.removeListener(this);
        super.onDestroy();
    }


    /** Called when a preference's value has changed */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        switch(key) {
            // These preferences affect the picker
            case Pref.FILT_SET:  case Pref.FILT_COST: case Pref.FILT_POTION:
            case Pref.FILT_CURSE: case Pref.COMP_SORT_CARD: case Pref.COMP_LANG:
                FragmentActivity act = getActivity();
                if(act == null) return;
                act.getSupportLoaderManager()
                   .restartLoader(LoaderId.PICKER, null, this);
                break;
        }
    }


    /** Called to create this fragment's view for the first time.  */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picker, container, false);
        ButterKnife.bind(this, view);
        card_list.setLayoutManager(new LinearLayoutManager(getContext()));
        card_list.addItemDecoration(new ListDivider(container.getContext()));

        // Disable flicker animation when an item changes
        // (otherwise items will flicker when selection state changes)
        ItemAnimator animator = card_list.getItemAnimator();
        if (animator instanceof SimpleItemAnimator)
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);

        final SharedPreferences pref = Pref.get(getContext());
        adapter = new AdapterCardsFilter(card_list, pref.getString(Pref.FILT_CARD, ""),
                                                    pref.getString(Pref.REQ_CARDS, ""));
        card_list.setAdapter(adapter);
        updateView();
        return view;
    }


    private void updateView() {
        if(adapter == null) return;
        adapter.changeCursor(mCursor);

        // Determine the active view: 1-Loading, 2-Empty, 3-List
        int activeView = 1;
        if(mCursor != null) {
            activeView++;
            if(mCursor.moveToFirst()) activeView++;
        }

        // Apply the active view
        loading.setVisibility(  activeView == 1 ? View.VISIBLE : View.GONE);
        empty.setVisibility(    activeView == 2 ? View.VISIBLE : View.GONE);
        card_list.setVisibility(activeView == 3 ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onDestroyView() {
        saveSelections();
        adapter = null;
        super.onDestroyView();
    }


    /** Save all selected/filtered cards to the preferences. */
    public void saveSelections() {
        if(adapter == null) return;
        Pref.edit(getContext())
            .putString(Pref.FILT_CARD, adapter.getFilter())
            .putString(Pref.REQ_CARDS, adapter.getRequired())
            .commit();
    }


    public void toggleAll() {
        adapter.toggleAll();
    }


    @Override @NonNull
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        assert getActivity() != null;
        // Show the loading icon if we have views
        mCursor = null;
        updateView();


        // Basic setup
        CursorLoader c = new CursorLoader(getActivity());
        c.setUri(Provider.URI_CARD_ALL);
        c.setProjection(AdapterCardsFilter.COLS_USED);
        String sel = getFilter(Pref.get(getContext()));
        sel += " AND "+ Pref.languageFilter(getContext());
        c.setSelection(sel);
        c.setSortOrder(Pref.cardSort(getContext()));

        return c;
    }


    /** Get the filter used by the picker to hide cards that will never be in the supply.
     *  This does not include individual deselected or required cards.
     *  @param pref The preferences used to retrieve filter values.
     *  @return The SQL selection statement that FragmentPicker uses to hide cards.
     *  This statement is never null or the empty string. There will be something in here. */
    public static String getFilter(SharedPreferences pref) {
        // Filter out sets (the set filter is always present)
        String curSel = pref.getString(Pref.FILT_SET, "");
        String sel = (curSel.length()==0) ? TableCard._SET_ID+"=NULL"
                                          : TableCard._SET_ID+" IN ("+curSel+")";

        // Filter out potions
        if(!pref.getBoolean(Pref.FILT_POTION, true))
            sel += " AND "+TableCard._POT+"=0";

        // Filter out coins
        curSel = pref.getString(Pref.FILT_COST, "");
        if(0 < curSel.length())
            sel += " AND "+TableCard._COST_VAL+" NOT IN ("+curSel+")";

        // Filter out debt
        curSel = pref.getString(Pref.FILT_DEBT, "");
        if(0 < curSel.length())
            sel += " AND "+TableCard._DEBT+" NOT IN ("+curSel+")";

        // Filter out cursers
        boolean filt_curse = pref.getBoolean(Pref.FILT_CURSE, true);
        if(!filt_curse)
            sel += " AND " + TableCard._META_CURSER + "=0";

        return sel;
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        updateView();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursor = null;
        updateView();
    }
}