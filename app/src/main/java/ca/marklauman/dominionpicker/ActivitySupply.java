package ca.marklauman.dominionpicker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import ca.marklauman.dominionpicker.database.CardDb;
import ca.marklauman.dominionpicker.database.DataDb;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;

/** Activity for displaying the supply piles for a new game.
 *  @author Mark Lauman */
public class ActivitySupply extends ActionBarActivity {
    /** Key used to pass the supply to this activity.
     *  Alternative to {@link #PARAM_SUPPLY_ID}. */
    public static final String PARAM_SUPPLY_OBJ = "supply";
    /** Key used to pass a supply timestamp to this activity.
     *  Alternative to {@link #PARAM_SUPPLY_OBJ}.
     *  The supply will load from the history table. */
    public static final String PARAM_SUPPLY_ID = "supplyId";

    /** The loader that gets the supply when an id is provided. */
    private final SupplyLoader supplyLoader = new SupplyLoader();
    /** The loader that gets the cards when the supply is loaded */
    private final CardLoader cardLoader = new CardLoader();

    /** The adapter used to display the supply cards. */
	private CardAdapter adapter;
	/** The TextView used to display the resource cards. */
	private TextView resView;
	/** The supply on display. */
	private Supply supply;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_supply);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle params = getIntent().getExtras();

        // Basic view setup
		ListView card_list = (ListView) findViewById(R.id.card_list);
		View loading = findViewById(android.R.id.progress);
		card_list.setEmptyView(loading);
		resView = (TextView) findViewById(R.id.resources);
		resView.setVisibility(View.GONE);
		
		// Setup the adapter
		adapter = new CardAdapter(this);
		adapter.changeCursor(null);
		card_list.setAdapter(adapter);

        // Try to get a full supply object (from restore or from params)
        if(supply != null) return;
        // First check the savedInstanceState
        if(savedInstanceState != null)
            supply = savedInstanceState.getParcelable(PARAM_SUPPLY_OBJ);
        // Then check the passed parameters
        else if(params != null)
            supply = params.getParcelable(PARAM_SUPPLY_OBJ);

        // If we have the supply, set it and return
        if(supply != null) {
            setSupply(supply);
            return;
        }

        // We have no supply, check for an id
        long supplyId = -1;
        if(params != null)
            supplyId = params.getLong(PARAM_SUPPLY_ID, -1);
        if(supplyId < 0) return;

        // We have an id, load the supply.
        Bundle args = new Bundle();
        args.putLong(PARAM_SUPPLY_ID, supplyId);
        LoaderManager lm = getSupportLoaderManager();
        lm.initLoader(LoaderId.SUPPLY_S, args, supplyLoader);
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(PARAM_SUPPLY_OBJ, supply);
	}
	
	
	/** Called to inflate the ActionBar */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.supply, menu);
		return true;
	}
	
	
	/** Called just before the ActionBar is displayed. */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		// Apply any changes super wants to apply.
		boolean orig = super.onPrepareOptionsMenu(menu);
		
		/* Check if the black market icon is in/visible
		 * when it shouldn't be. If a change is needed,
		 * apply the change.                         */
		MenuItem marketButton = menu.findItem(R.id.action_market);
        if(supply != null && supply.blackMarket() != marketButton.isVisible()) {
			marketButton.setVisible(supply.blackMarket());
			return true;
		}
		
		// We didn't change anything
		return orig;
	}
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // android "back" button on the action bar
        case android.R.id.home:
            finish();
            return true;
        case R.id.action_market:
        	Intent resAct = new Intent(this, ActivityMarket.class);
        	resAct.putExtra(ActivityMarket.PARAM_SUPPLY,
        					supply.cards);
			startActivityForResult(resAct, -1);
			return true;
        }
        return super.onOptionsItemSelected(item);
	}
	
	
	/** Set the supply on display in this activity.
	 *  This triggers some UI changes, and should
	 *  be called on the UI thread.
	 *  @param supply The new supply to display. */
	private void setSupply(Supply supply) {
		this.supply = supply;
		
		// Start loading the supply
		LoaderManager lm = getSupportLoaderManager();
		lm.initLoader(LoaderId.SUPPLY_C, null, cardLoader);

		supportInvalidateOptionsMenu();
	}


    /** Used by subclasses to access the activity context */
    private ActionBarActivity getActivity() {
        return this;
    }

    /** Used to load the cards once the supply is loaded. */
    private class CardLoader implements LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // Display the loading icon, hide the resources
            resView.setVisibility(View.GONE);
            adapter.changeCursor(null);

            // Basic loader
            CursorLoader c = new CursorLoader(getActivity());
            c.setUri(Provider.URI_CARDS);

            // Selection string (sql WHERE clause)
            String sel = "";
            for(long ignored : supply.cards)
                sel += " OR " + CardDb._ID + "=?";
            sel = sel.substring(4);
            c.setSelection(sel);

            // Selection arguments (the numbers)
            String[] selArgs = new String[supply.cards.length];
            for(int i=0; i<supply.cards.length; i++)
                selArgs[i] = "" + supply.cards[i];
            c.setSelectionArgs(selArgs);

            return c;
        }


        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // In case the load finishes as the app closes
            if(data == null) return;

            // display the supply cards
            adapter.changeCursor(data);
            adapter.setBane(supply.bane);

            // display the resource cards
            String output = "";
            if(supply.high_cost)
                output += getString(R.string.supply_colonies);
            if(supply.shelters)
                output += "\n" + getString(R.string.supply_shelters);
            output = output.trim();
            if("".equals(output))
                output = getString(R.string.supply_normal);
            resView.setText(output);
            resView.setVisibility(View.VISIBLE);
        }


        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if(loader == null) return;
            adapter.changeCursor(null);
            resView.setVisibility(View.GONE);
        }
    }


    /** Used to load the supply from an id */
    private class SupplyLoader implements LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // Display the loading icon, hide the resources
            resView.setVisibility(View.GONE);
            adapter.changeCursor(null);

            // Basic loader
            CursorLoader c = new CursorLoader(getActivity());
            c.setUri(Provider.URI_HIST);
            c.setSelection(DataDb._H_TIME + "=?");
            long time = args.getLong(PARAM_SUPPLY_ID);
            c.setSelectionArgs(new String[]{""+time});
            return c;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(data == null || data.getCount() < 1) return;

            // Column indexes
            int _time = data.getColumnIndex(DataDb._H_TIME);
            int _name = data.getColumnIndex(DataDb._H_NAME);
            int _bane = data.getColumnIndex(DataDb._H_BANE);
            int _cost = data.getColumnIndex(DataDb._H_HIGH_COST);
            int _shelters = data.getColumnIndex(DataDb._H_SHELTERS);
            int _cards = data.getColumnIndex(DataDb._H_CARDS);
            data.moveToFirst();

            // Build the supply object
            Supply s = new Supply();
            s.time = data.getLong(_time);
            s.name = data.getString(_name);
            s.bane = data.getLong(_bane);
            s.high_cost = data.getInt(_cost) != 0;
            s.shelters = data.getInt(_shelters) != 0;
            String[] cardList = data.getString(_cards).split(",");
            s.cards = new long[cardList.length];
            for(int i=0; i<cardList.length; i++)
                s.cards[i] = Long.parseLong(cardList[i]);

            // Finish up
            data.close();
            setSupply(s);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    }
}