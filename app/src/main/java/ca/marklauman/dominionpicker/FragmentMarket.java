package ca.marklauman.dominionpicker;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import ca.marklauman.dominionpicker.cardlist.AdapterColorCards;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableCard;

/** Governs all the Black Market shuffler screens.
 *  @author Mark Lauman */
public class FragmentMarket extends Fragment
                            implements LoaderCallbacks<Cursor>,
                                       OnItemClickListener {

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

    /** The various panels displayed to the user. */
    private View[] vPanels;
    /** The currently visible panel in {@link #vPanels} */
    private int activePanel;
    /** Adapter used to display the list of cards. */
    private AdapterColorCards adapter;

    /** Stores the stock of the market. (in drawing order) If null, the stock is being retrieved.
     *  If empty then no stock is left. */
    private LinkedList<Long> stock = null;
    /** The cards the user may choose from this round.
     *  Is null if the cards have not yet been drawn. */
    private long[] choices = null;

    /** The display language of the current choices.
     *  Checked each time this fragment is started to ensure it is current. */
    private String transId;

    /** Called to create this fragment's view for the first time.
     *  This is called first - before the fragment's state is restored.
     *  So the default view created by this is one where the stock has not been loaded. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_market, container, false);

        // Setup the panels
        activePanel = PANEL_STARTUP;
        vPanels = new View[4];
        vPanels[PANEL_STARTUP] = view.findViewById(R.id.loading);
        vPanels[PANEL_DRAW] = view.findViewById(R.id.market_draw);
        vPanels[PANEL_DRAW].setOnClickListener(new DrawListener());
        vPanels[PANEL_CHOOSE] = view.findViewById(R.id.market_choices);
        vPanels[PANEL_SOLD_OUT] = view.findViewById(R.id.market_sold_out);
        View but_pass = view.findViewById(R.id.market_pass);
        but_pass.setOnClickListener(new PassListener());

        // Setup card list and its adapter
        ListView card_list = (ListView) view.findViewById(R.id.card_list);
        adapter = new AdapterColorCards(getActivity());
        card_list.setAdapter(adapter);
        card_list.setOnItemClickListener(this);

        return view;
    }


    /** Set the indicated panel as the active panel and display it.
     *  @param panelIndex The index of the new active panel. Should be one of the
     *                    static PANEL values provided by this class. */
    private void setActivePanel(int panelIndex) {
        if(activePanel == panelIndex) return;
        if(vPanels == null || vPanels[0] == null) return;
        vPanels[activePanel].setVisibility(View.GONE);
        vPanels[panelIndex].setVisibility(View.VISIBLE);
        activePanel = panelIndex;
    }


    /** Only called on fragment creation and reattachment - not on a restart.
     *  Used to restore loaded information. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Restore to previous state, if available.
        if(savedInstanceState != null) {
            final long[] arrStock = savedInstanceState.getLongArray(KEY_STOCK);
            if(arrStock == null) return;
            stock = new LinkedList<>();
            for(long id : arrStock) stock.add(id);
            choices = savedInstanceState.getLongArray(KEY_CHOICES);
        }
    }


    /** Called just before the fragment is drawn to the screen.
     *  Loaders are started/restarted here. */
    @Override
    public void onStart() {
        super.onStart();
        App.updateInfo(getActivity());
        LoaderManager lm = getActivity().getSupportLoaderManager();

        // Restore current market state
        if(stock == null) {
            // If we have no market, shuffle up some stock
            lm.restartLoader(LoaderId.MARKET_SHUFFLE, null, this);
        } else if(choices == null) {
            // We have a market, but no cards have been drawn
            setActivePanel(PANEL_DRAW);
        } else if(stock.size() == 0 && choices.length == 0) {
            // We have a market, but it's sold out
            setActivePanel(PANEL_SOLD_OUT);
        } else if(!App.transId.equals(transId)) {
            // We have a market and have drawn cards
            // Force the cards to reload if the language has changed
            lm.restartLoader(LoaderId.MARKET_SHOW, null, this);
        } else {
            // The language has not changed, just refresh the cursor
            lm.initLoader(LoaderId.MARKET_SHOW, null, this);
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
     *  @param position The position of the view in the adapter.
     *  @param id The row id of the item that was clicked. */
    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader c = new CursorLoader(getActivity());
        switch (id) {
            case LoaderId.MARKET_SHUFFLE:
                // load the supply (if provided)
                Bundle fragArgs = getArguments();
                HashSet<Long> supply = new HashSet<>();
                long[] supply_arr = null;
                if(fragArgs != null) supply_arr = fragArgs.getLongArray(PARAM_SUPPLY);
                if(supply_arr != null)
                    for(long cardID : supply_arr)
                        supply.add(cardID);

                // load the deck of available cards and eliminate supply cards.
                Long[] deck_arr = FragmentPicker.loadSelections(getActivity());
                ArrayList<Long> deck = new ArrayList<>();
                if(deck_arr != null) {
                    deck = new ArrayList<>(deck_arr.length);
                    for (long card : deck_arr)
                        if (card != TableCard.ID_BLACK_MARKET && !supply.contains(card))
                            deck.add(card);
                }

                // Turn the available cards into a select statement
                String sel = "";
                String[] selArgs = new String[deck.size()];
                for(int i=0; i<deck.size(); i++) {
                    sel += ",?";
                    selArgs[i]=""+deck.get(i);
                }
                if(0 < sel.length()) sel = TableCard._ID+" IN ("+sel.substring(1)+")";
                else sel = TableCard._ID+"=NULL";

                // Build the cursor loader
                c.setUri(Provider.URI_CARD_DATA);
                c.setProjection(new String[]{TableCard._ID});
                // Forbid events and ruins, restrict to selections
                c.setSelection(TableCard._TYPE_EVENT+"=0 AND "+sel);
                c.setSelectionArgs(selArgs);
                c.setSortOrder("random()");
                return c;

            case LoaderId.MARKET_SHOW:
                c.setUri(Provider.URI_CARD_ALL);
                c.setProjection(AdapterColorCards.COLS_USED);
                c.setSortOrder(App.sortOrder);
                transId = App.transId;

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
                c.setSelection("("+cardSel+") AND "+App.transFilter);

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
                data.close();

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