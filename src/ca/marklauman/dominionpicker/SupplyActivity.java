/* Copyright (c) 2014 Mark Christopher Lauman
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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/** Activity for choosing and displaying the supply piles for
 *  a new game.
 *  @author Mark Lauman                                  */
public class SupplyActivity extends SherlockFragmentActivity
						 	implements LoaderCallbacks<Cursor> {
	
	
	/** Key used to pass the card pool to this activity */
	public static final String PARAM_CARDS = "cards";
	/** Key used to store the chosen supply
	 *  in a savedInstanceState.         */
	private static final String KEY_SUPPLY = "supply";
	/** ID of the loader for the supply cards. */
	private static final int LOADER_SUPPLY = 2;
	
	
	/** The adapter used to display the supply cards. */
	CardAdapter adapter;
	/** The TextView used to display the resource cards. */
	TextView resView;
	/** The supply chosen for this deck. */
	private Supply supply;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_supply);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		ListView card_list = (ListView) findViewById(R.id.card_list);
		View loading = findViewById(android.R.id.progress);
		card_list.setEmptyView(loading);
		resView = (TextView) findViewById(R.id.resources);
		resView.setVisibility(View.GONE);
		
		// Setup the adapter
		adapter = new CardAdapter(this);
		adapter.changeCursor(null);
		card_list.setAdapter(adapter);
		
		// Restore the supply if this is a restore
		supply = null;
		if(savedInstanceState != null) {
			Supply supply = (Supply) savedInstanceState.getParcelable(KEY_SUPPLY);
			if(supply != null)
				setSupply(supply);
			
		} else {
			// No supply? Shuffle one!
			SupplyShuffler shuffler = new SupplyShuffler(this);
			// Pool must be passed as type Long not long
			long[] cards = getIntent().getExtras()
					  				  .getLongArray(PARAM_CARDS);
			Long[] pool = new Long[cards.length];
			for(int i=0; i<cards.length; i++)
				pool[i] = cards[i];
			shuffler.execute(pool);
		}
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_SUPPLY, supply);
	}
	
	
	/** Called to inflate the ActionBar */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.supply, menu);
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
		boolean marketPresent = blackMarket();
		if(marketPresent != marketButton.isVisible()) {
			marketButton.setVisible(marketPresent);
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
        	Intent resAct = new Intent(this, MarketActivity.class);
        	resAct.putExtra(MarketActivity.PARAM_CARDS,
        					getIntent().getExtras()
        							   .getLongArray(PARAM_CARDS));
        	resAct.putExtra(MarketActivity.PARAM_SUPPLY,
        					supply.cards);
			startActivityForResult(resAct, -1);
			return true;
        }
        return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Display the loading icon, hide the resources
		resView.setVisibility(View.GONE);
		adapter.changeCursor(null);
		
		// Basic loader
		CursorLoader c = new CursorLoader(this);
		c.setUri(CardList.URI);
		
		// Selection string (sql WHERE clause)
		String sel = "";
		int numCards = supply.cards.length;
		for(int i = 0; i < numCards; i++)
			sel += " OR " + CardList._ID + "=?";
		sel = sel.substring(4);
		c.setSelection(sel);
		
		// Selection arguments (the numbers)
		String[] selArgs = new String[numCards];
		for(int i=0; i<numCards; i++)
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
		switch(loader.getId()) {
		case LOADER_SUPPLY:
			adapter.changeCursor(null);
			resView.setVisibility(View.GONE);
			break;
		}
	}
	
	
	/** Set the supply on display in this activity.
	 *  This triggers some UI changes, and should
	 *  be called on the UI thread.
	 *  @param supply The new supply to display. */
	public void setSupply(Supply supply) {
		this.supply = supply;
		
		// Start loading the supply
		LoaderManager lm = getSupportLoaderManager();
		lm.initLoader(LOADER_SUPPLY, null, this);
	}
	
	
	/** Returns {@code true} if a Black Market is in
	 *  the supply.                               */
	private boolean blackMarket() {
		if(supply == null) return false;
		for(long card : supply.cards)
			if(card == CardList.ID_BLACK_MARKET)
				return true;
		return false;
	}
	
	
	/** Display the resource cards in use for this
	 *  game (Colony, Platinum and Shelters).
	 *  @param colony_set If this set is Prosperity,
	 *  colonies and Platinum are used.
	 *  @param shelter_set If this set is Dark Ages,
	 *  Shelters are used.                        */
	public void displayResourceCards() {
		String output = "";
		if(supply.high_cost)
			output += getString(R.string.supply_colonies);
		if(supply.shelters)
			output += "\n" + getString(R.string.supply_shelters);
		output = output.trim();
		if("".equals(output))
			output = getString(R.string.supply_normal);
		resView.setText(output);
	}
}