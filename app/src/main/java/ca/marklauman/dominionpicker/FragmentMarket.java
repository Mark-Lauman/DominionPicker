package ca.marklauman.dominionpicker;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import ca.marklauman.dominionpicker.database.CardDb;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;

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

    /** The ListView used to display the cards */
    ListView card_view;
    /** The button to show the next pick */
    private View but_draw;
    /** The list displaying the choices */
    private View choice_panel;
    /** The adapter used to display the choices. */
    private CardAdapter adapter;
    /** The notice for when there is no stock */
    private View sold_out;

    /** Stores the stock of the market. (in drawing order)
     *  If null, the stock is being retrieved.
     *  If empty then no stock is left.    */
    private LinkedList<Long> stock;
    /** The cards the user may choose from this round.
     *  Is null if the cards have not yet been drawn. */
    private long[] choices;

    /** The cursor containing current choices */
    private Cursor loaded;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("market", "onCreate");

        // Default fragment state if no data is passed
        stock = null;
        choices = null;
        loaded = null;

        // Restore to previous state, if available.
        if(savedInstanceState != null) {
            long[] arrStock = savedInstanceState.getLongArray(KEY_STOCK);
            if(arrStock == null) return;
            stock = new LinkedList<>();
            for(long id : arrStock) stock.add(id);
            choices = savedInstanceState.getLongArray(KEY_CHOICES);
        }
        Log.d("market", "onCreate done");
    }

    @Override
    public final void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("market", "onAttach");
        /* First initialization of the loader must be called
         * early, or the loader will not function.        */
        LoaderManager lm = ((FragmentActivity) activity).getSupportLoaderManager();
        lm.initLoader(LoaderId.MARKET_DISP, null, this);
        // load the market stock if none has been restored
        if(stock == null)
            lm.restartLoader(LoaderId.MARKET_SHUFFLE, null, this);
        Log.d("market", "onAttach done");
    }


    /** Called to create this fragment's view for the first time.
     *  We cannot guarantee that the cards have been loaded or shuffled. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d("market", "onCreateView");
        FragmentActivity act = getActivity();
        View view = inflater.inflate(R.layout.fragment_market, container, false);
        Log.d("market", "view inflated");

        // Get View items
        but_draw = view.findViewById(R.id.market_draw);
        but_draw.setOnClickListener(new DrawListener());
        View but_pass = view.findViewById(R.id.market_pass);
        but_pass.setOnClickListener(new PassListener());
        choice_panel = view.findViewById(R.id.market_choices);
        sold_out = view.findViewById(R.id.market_sold_out);
        Log.d("market", "views retrieved");

        // Setup the list & adapter
        card_view = (ListView) view.findViewById(R.id.card_list);
        adapter = new CardAdapter(act);
        adapter.changeCursor(loaded);
        if(stock != null) card_view.setAdapter(adapter);
        card_view.setOnItemClickListener(this);
        Log.d("market", "adapter setup");

        updateView();
        Log.d("market", "onCreateView done");
        return view;
    }


    /** Updates the view to reflect the current market state */
    private void updateView() {
        // No update if we don't have views or stock
        if(but_draw == null || stock == null) {
            Log.d("market", "updateView skipped");
            return;
        }
        Log.d("market", "updateView");

        // Cursor changes happen first
        adapter.changeCursor(loaded);
        Log.d("market", "cursor changed");

        if(card_view.getAdapter() == null)
            card_view.setAdapter(adapter);

        // If no cards have been drawn
        if(choices == null || choices.length == 0) {
            // and we have stock
            if(0 < stock.size()) {
                but_draw.setVisibility(View.VISIBLE);
                choice_panel.setVisibility(View.GONE);
                sold_out.setVisibility(View.GONE);
            // and we're out of stock
            } else {
                but_draw.setVisibility(View.GONE);
                choice_panel.setVisibility(View.GONE);
                sold_out.setVisibility(View.VISIBLE);
            }
        // cards have been drawn
        } else {
            // and loaded
            if(loaded != null) {
                but_draw.setVisibility(View.GONE);
                choice_panel.setVisibility(View.VISIBLE);
                sold_out.setVisibility(View.GONE);
            // but not loaded
            } else {
                but_draw.setVisibility(View.GONE);
                choice_panel.setVisibility(View.GONE);
                sold_out.setVisibility(View.GONE);
                /* Start loading data. If the data is currently being loaded,
                 * initLoader throws an exception. In this case, we just wait
                 * for the load to finish.                                 */
                try { getActivity().getSupportLoaderManager()
                                   .initLoader(LoaderId.MARKET_DISP, null, this);
                } catch (Exception ignored) {}
            }
        }
        Log.d("market", "updateView done");
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("market", "onSaveInstanceState");
        long[] arrStock = new long[stock.size()];
        int i = 0;
        for(long card : stock) {
            arrStock[i] = card;
            i++;
        }
        outState.putLongArray(KEY_STOCK, arrStock);
        outState.putLongArray(KEY_CHOICES, choices);
        super.onSaveInstanceState(outState);
        Log.d("market", "onSaveInstanceState done");
    }


    /** Selects which card was purchased.
     *  Called when a card is clicked in the choice panel.
     *  @param parent The AdapterView where the click happened.
     *  @param view The view within the AdapterView that was
     *  clicked (this will be a view provided by the adapter)
     *  @param position The position of the view in the adapter.
     *  @param id The row id of the item that was clicked.    */
    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        Toast.makeText(getActivity(), adapter.getName(position), Toast.LENGTH_SHORT)
             .show();

        // Unused choices return to the stock bottom
        for(long card : choices)
            if(card != id)
                stock.add(card);

        // clear the choices
        choices = null;
        loaded = null;
        updateView();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("market", "oCreateLoader " + id);
        CursorLoader c = new CursorLoader(getActivity());
        switch (id) {
            case LoaderId.MARKET_SHUFFLE:
                c.setUri(Provider.URI_CARD_DATA);

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
                        if (card != CardDb.ID_BLACK_MARKET && !supply.contains(card))
                            deck.add(card);
                }

                // Build the cursor loader from the deck
                c.setProjection(new String[]{CardDb._ID});
                String sel = "";
                String[] selArgs = new String[deck.size()];
                for(int i=0; i<deck.size(); i++) {
                    sel += " OR "+CardDb._ID+"=?";
                    selArgs[i]=""+deck.get(i);
                }
                sel = sel.substring(4);
                Log.d("marketSel",""+sel);
                Log.d("marketSelArgs", Arrays.toString(selArgs));
                c.setSelection(sel);
                c.setSelectionArgs(selArgs);
                c.setSortOrder("random()");
                Log.d("market", "oCreateLoader done");
                return c;

            case LoaderId.MARKET_DISP:
                c.setUri(Provider.URI_CARD_ALL);

                // TODO: Limit what columns are loaded to what we need

                // no choices, no cards
                if(choices == null || choices.length == 0) {
                    c.setSelection(CardDb._ID + "=?");
                    c.setSelectionArgs(new String[]{"-1"});
                    Log.d("market", "oCreateLoader nothing");
                    return c;
                }

                // Generate selection string
                String cardSel = "";
                for(long ignored : choices)
                    cardSel += " OR " + CardDb._ID + "=?";
                // remove the " OR " at the beginning
                cardSel = cardSel.substring(4);
                c.setSelection(MainActivity.language+" AND ("+cardSel+")");

                // selection args
                String[] strChoices = new String[choices.length];
                for(int i=0; i<choices.length; i++)
                    strChoices[i] = "" + choices[i];
                c.setSelectionArgs(strChoices);

                Log.d("marketSel",""+cardSel);
                Log.d("marketSelArgs", Arrays.toString(strChoices));

                updateView();
                Log.d("market", "oCreateLoader done");
                return c;
        }
        Log.d("market", "oCreateLoader done null");
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("market", "onLoadFinished "+loader.getId());
        switch (loader.getId()) {
            case LoaderId.MARKET_SHUFFLE:
                stock = new LinkedList<>();
                int _id = data.getColumnIndex(CardDb._ID);
                data.moveToPosition(-1);
                while(data.moveToNext()) {
                    stock.add(data.getLong(_id));
                }
                data.close();
                FragmentActivity a = getActivity();
                if(a != null) a.getSupportLoaderManager()
                               .restartLoader(LoaderId.MARKET_DISP, null, this);
                break;
            case LoaderId.MARKET_DISP:
                loaded = data;
                updateView();
                break;
        }
        Log.d("market", "onLoadFinished done");
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(LoaderId.MARKET_DISP != loader.getId()) return;
        loaded = null;
        updateView();
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
                         .restartLoader(LoaderId.MARKET_DISP, null, getFragment());
            updateView();
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
            loaded = null;
            updateView();
        }
    }
}