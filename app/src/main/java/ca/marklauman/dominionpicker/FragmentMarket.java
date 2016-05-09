package ca.marklauman.dominionpicker;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.LinkedList;

import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.dominionpicker.userinterface.recyclerview.AdapterCards;
import ca.marklauman.dominionpicker.userinterface.recyclerview.AdapterCards.ViewHolder;
import ca.marklauman.tools.Utils;
import ca.marklauman.tools.recyclerview.ListDivider;

/** Governs all the Black Market shuffler screens.
 *  @author Mark Lauman */
public class FragmentMarket extends Fragment
                            implements LoaderCallbacks<Cursor>, AdapterCards.Listener,
                                       Prefs.Listener {

    /** Key used to pass the supply pool to this fragment (optional) */
    public static final String PARAM_SUPPLY = "supply";

    /** Key used to save store stock to savedInstanceState. */
    private static final String KEY_STOCK = "stock";
    /** Key used to save current choices to savedInstanceState. */
    private static final String KEY_CHOICES = "choices";

    /** Index of the startup panel in {@link #vPanels}. */
    private static final int PANEL_STARTUP = 0;
    /** Index of the "draw card" panel in {@link #vPanels}. */
    private static final int PANEL_DRAW = 1;
    /** Index of the "choose card" panel in {@link #vPanels} */
    private static final int PANEL_CHOOSE = 2;
    /** Index of the "sold out" panel in {@link #vPanels} */
    private static final int PANEL_SOLD_OUT = 3;

    /** If the stock is reshuffled, this becomes true until the user is notified. */
    private boolean hasNewStock = false;
    /** The various panels displayed to the user. */
    private View[] vPanels;
    /** The currently visible panel in {@link #vPanels} */
    private int activePanel = PANEL_STARTUP;
    /** Adapter used to display the list of cards. */
    private AdapterCards adapter;

    /** Stores the stock of the market. (in drawing order) If null, the stock is being retrieved.
     *  If empty then no stock is left. */
    private LinkedList<Long> stock = null;
    /** The cards the user may choose from this round.
     *  Is null if the cards have not yet been drawn. */
    private long[] choices = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Prefs.addListener(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Prefs.removeListener(this);
    }


    @Override
    public void onStart() {
        // Display a message if the
        super.onStart();
        if(hasNewStock)
            Toast.makeText(getContext(), R.string.market_begin, Toast.LENGTH_LONG)
                 .show();
        hasNewStock = false;
    }


    /** Called to create this fragment's view for the first time.
     *  This is called first - before the fragment's state is restored.
     *  So the default view created by this is one where the stock has not been loaded. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_market, container, false);

        // Setup the panels
        vPanels = new View[4];
        vPanels[PANEL_STARTUP] = view.findViewById(R.id.loading);
        vPanels[PANEL_DRAW] = view.findViewById(R.id.market_draw);
        vPanels[PANEL_DRAW].setOnClickListener(new DrawListener());
        vPanels[PANEL_CHOOSE] = view.findViewById(R.id.market_choices);
        vPanels[PANEL_SOLD_OUT] = view.findViewById(R.id.market_sold_out);
        View but_pass = view.findViewById(R.id.market_pass);
        but_pass.setOnClickListener(new PassListener());

        // Setup card list and its adapter
        RecyclerView card_list = (RecyclerView) view.findViewById(R.id.card_list);
        card_list.setLayoutManager(new LinearLayoutManager(getContext()));
        card_list.addItemDecoration(new ListDivider(getContext()));
        adapter = new AdapterCards(card_list);
        adapter.setListener(this);
        card_list.setAdapter(adapter);

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Restore to previous state, if available.
        if(savedInstanceState != null) {
            final long[] arrStock = savedInstanceState.getLongArray(KEY_STOCK);
            if(arrStock != null) {
                stock = new LinkedList<>();
                for(long id : arrStock) stock.add(id);
                choices = savedInstanceState.getLongArray(KEY_CHOICES);
            }
        }

        // Determine the active panel
        if(stock != null) {
            if(choices == null) setActivePanel(PANEL_DRAW);
            else if(choices.length != 0) getActivity().getSupportLoaderManager()
                                                      .initLoader(LoaderId.MARKET_SHOW, null, this);
            else setActivePanel(PANEL_SOLD_OUT);
        // Draw some market stock if needed.
        } else getActivity().getSupportLoaderManager().restartLoader(LoaderId.MARKET_SHUFFLE, null, this);
    }


    /** Set the indicated panel as the active panel and display it.
     *  @param panelIndex The index of the new active panel. Should be one of the
     *                    static PANEL values provided by this class. */
    private void setActivePanel(int panelIndex) {
        if(panelIndex == activePanel) return;
        if(vPanels == null) activePanel = panelIndex;
        else {
            vPanels[activePanel].setVisibility(View.GONE);
            vPanels[panelIndex].setVisibility(View.VISIBLE);
            activePanel = panelIndex;
        }
    }


    @Override
    public void prefChanged(String key) {
        switch(key) {
            case Prefs.FILT_SET: case Prefs.FILT_COST: case Prefs.FILT_POTION:
            case Prefs.FILT_CURSE: case Prefs.REQ_CARDS: case Prefs.FILT_CARD:
                setActivePanel(PANEL_STARTUP);
                getActivity().getSupportLoaderManager()
                             .restartLoader(LoaderId.MARKET_SHUFFLE, null, this);
                break;
            case Prefs.FILT_LANG: case Prefs.SORT_CARD:
                if(choices == null) return;
                setActivePanel(PANEL_STARTUP);
                getActivity().getSupportLoaderManager()
                             .restartLoader(LoaderId.MARKET_SHOW, null, this);
                break;
        }
    }


    /** Called before the fragment is destroyed if its state should be preserved */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        long[] arrStock = new long[stock.size()];
        int i = 0;
        for(long card : stock) {
            arrStock[i] = card;
            i++;
        }
        outState.putLongArray(KEY_STOCK, arrStock);
        outState.putLongArray(KEY_CHOICES, choices);
        super.onSaveInstanceState(outState);
    }


    /** Selects which card was purchased. Called when a card is clicked in the choice panel.
     *  @param id The row id of the item that was clicked. */
    @Override
    public void onItemClick(ViewHolder holder, int position, long id, boolean longClick) {
        Toast.makeText(getActivity(), adapter.getName(position), Toast.LENGTH_SHORT)
             .show();
        // Unused choices return to the stock bottom
        for (long card : choices)
            if (card != id) stock.add(card);

        // Clear the choices
        adapter.changeCursor(null);
        if (stock.size() == 0) {
            // If we are out of stock, switch to the sold out panel
            choices = new long[0];
            setActivePanel(PANEL_SOLD_OUT);
        } else {
            // Otherwise, prepare to draw another card
            setActivePanel(PANEL_DRAW);
            choices = null;
        }
    }


    /** Start loading the shuffle or the choices */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle loadArgs) {
        CursorLoader c = new CursorLoader(getActivity());
        switch (id) {
            case LoaderId.MARKET_SHUFFLE:
                // Announce the new market
                hasNewStock = true;

                // Filter out cards not visible in the picker, and event cards
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
                String sel = FragmentPicker.getFilter(pref)
                             +" AND "+TableCard._TYPE_EVENT+"=0";

                // Filter out cards excluded by the card list
                String filt_card = pref.getString(Prefs.FILT_CARD, "");

                // Get the supply passed to this fragment
                Bundle args = getArguments();
                long[] supply_arr = (args == null) ? null : args.getLongArray(PARAM_SUPPLY);
                if(supply_arr == null) supply_arr = new long[0];

                // If supply cards have been provided, exclude them.
                if(0 < supply_arr.length && 0 < filt_card.length())
                    filt_card += ",";
                if(0 < supply_arr.length)
                    filt_card += Utils.join(",", supply_arr);

                // If no supply cards are provided, filter out required cards.
                // They are required to be in the supply
                else {
                    String req_cards = pref.getString(Prefs.REQ_CARDS, "");
                    if(0 < req_cards.length() && 0 < filt_card.length())
                        filt_card += ",";
                    if(0 < req_cards.length())
                        filt_card += req_cards;
                }

                // Add the card filter to the selection
                if(0 < filt_card.length())
                    sel += " AND "+TableCard._ID+" NOT IN ("+filt_card+")";

                // Build the cursor loader
                c.setUri(Provider.URI_CARD_DATA);
                c.setProjection(new String[]{TableCard._ID});
                c.setSelection(sel);
                c.setSortOrder("random()");
                return c;

            case LoaderId.MARKET_SHOW:
                c.setUri(Provider.URI_CARD_ALL);
                c.setProjection(AdapterCards.COLS_USED);
                c.setSortOrder(Prefs.sort_card);

                // no choices, no cards
                if(choices == null || choices.length == 0) {
                    c.setSelection(TableCard._ID + "=?");
                    c.setSelectionArgs(new String[]{"-1"});
                    return c;
                }

                // Generate selection string
                String cardSel = "";
                for(long ignored : choices)
                    cardSel += ",?";
                cardSel = TableCard._ID+" IN ("+cardSel.substring(1)+")";
                c.setSelection("("+cardSel+") AND "+Prefs.filt_lang);

                // selection args
                String[] strChoices = new String[choices.length];
                for(int i=0; i<choices.length; i++)
                    strChoices[i] = "" + choices[i];
                c.setSelectionArgs(strChoices);
                return c;
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LoaderId.MARKET_SHUFFLE:
                stock = new LinkedList<>();
                int _id = data.getColumnIndex(TableCard._ID);
                data.moveToPosition(-1);
                while(data.moveToNext()) {
                    stock.add(data.getLong(_id));
                }

                if(stock.size() == 0) {
                    choices = new long[0];
                    setActivePanel(PANEL_SOLD_OUT);
                } else setActivePanel(PANEL_DRAW);
                break;

            case LoaderId.MARKET_SHOW:
                adapter.changeCursor(data);
                if(data.getCount() == 0) setActivePanel(PANEL_SOLD_OUT);
                else setActivePanel(PANEL_CHOOSE);
                break;
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(LoaderId.MARKET_SHOW != loader.getId()) return;
        adapter.changeCursor(null);
        setActivePanel(PANEL_STARTUP);
    }


    /** Used so subclasses can access this fragment */
    private FragmentMarket getFragment() {
        return this;
    }


    /** Draws the next 3 cards to be picked.
     *  Invoked when the "draw" button is pressed. */
    private class DrawListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            // Determine the size of the next draw
            if(stock.size() < 3) choices = new long[stock.size()];
            else choices = new long[3];

            // draw the cards
            for(int i=0; i<choices.length; i++)
                choices[i] = stock.removeFirst();

            // update the view and start loading cards
            getActivity().getSupportLoaderManager()
                         .restartLoader(LoaderId.MARKET_SHOW, null, getFragment());
            setActivePanel(PANEL_STARTUP);
        }
    }

    /** Pass on this set of cards.
     *  Invoked when the "pass" button is pressed. */
    private class PassListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            // put the cards back
            for(long card : choices)
                stock.add(card);

            // wipe the choice list
            choices = null;
            adapter.changeCursor(null);
            setActivePanel(PANEL_DRAW);
        }
    }
}