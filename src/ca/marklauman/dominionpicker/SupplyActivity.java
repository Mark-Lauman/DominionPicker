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

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
	/** Key used to store the chosen supply cards in
	 *  a savedInstanceState.                     */
	private static final String KEY_SUPPLY = "supply";
	/** Key used to store the chosen resouces cards in
	 *  a savedInstanceState.                     */
	private static final String KEY_RES = "resources";
	
	/** ID of the loader for the supply cards. */
	private static final int LOADER_SUPPLY = 2;
	
	/** The adapter used to display the supply cards. */
	CardAdapter adapter;
	/** The TextView used to display the resource cards. */
	TextView resView;
	/** The supply chosen for this deck. */
	private long[] supply;
	/** The resource cards chosen for this game
	 *  (If {@code resources[0]} is from Prosperity,
	 *  add Colonies & Platinum. If {@code resources[1]}
	 *  is from Dark Ages, add Shelters.)       */
	private long[] resources;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_supply);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		ListView card_list = (ListView) findViewById(R.id.card_list);
		resView = (TextView) findViewById(R.id.resources);
		
		// Setup the adapter
		adapter = new CardAdapter(this);
		adapter.changeCursor(null);
		card_list.setAdapter(adapter);
		
		// Get the chosen supply cards
		supply = null;
		if(savedInstanceState != null) {
			supply = savedInstanceState.getLongArray(KEY_SUPPLY);
			resources = savedInstanceState.getLongArray(KEY_RES);
		}
		if(supply == null)
			chooseCards(getIntent().getExtras()
								   .getLongArray(PARAM_CARDS));
		
		// Start loading the cards
		LoaderManager lm = getSupportLoaderManager();
		lm.initLoader(LOADER_SUPPLY, null, this);
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLongArray(KEY_SUPPLY, supply);
		outState.putLongArray(KEY_RES, resources);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(blackMarket()) {
			getSupportMenuInflater().inflate(R.menu.supply, menu);
			return true;
		}
		return super.onCreateOptionsMenu(menu);
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
        					supply);
			startActivityForResult(resAct, -1);
			return true;
        }
        return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader c = new CursorLoader(this);
		c.setUri(CardList.URI);
		
		// Selection string
		String sel = "";
		for(int i = 0; i < supply.length; i++)
			sel += " OR " + CardList._ID + "=?";
		sel = sel.substring(4);
		c.setSelection(sel);
		
		// Selection arguments
		String[] selArgs = new String[supply.length];
		for(int i=0; i<supply.length; i++)
			selArgs[i] = "" + supply[i];
		c.setSelectionArgs(selArgs);
		
		return c;
	}
	
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if(data == null) return;
		
		// display the resource cards
		String[] res = new String[]{"", ""};
		int col_exp = data.getColumnIndex(CardList._EXP);
		int col_id = data.getColumnIndex(CardList._ID);
		data.moveToPosition(-1);
		while(data.moveToNext()) {
			if(resources[0] == data.getLong(col_id))
				res[0] = data.getString(col_exp);
			if(resources[1] == data.getLong(col_id))
				res[1] = data.getString(col_exp);
		}
		displayResourceCards(res[0], res[1]);
		
		// display the supply cards
		adapter.changeCursor(data);
	}
	
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if(loader == null) return;
		switch(loader.getId()) {
		case LOADER_SUPPLY:
			adapter.changeCursor(null);
			break;
		}
	}
	
	
	/** Choose 10 cards at random from the input pool make them
	 *  the new {@link #supply}. 
	 *  @param in_pool Each string in this array is the id #
	 *  of a card. The supply will be chosen from these ids. */
	private void chooseCards(long[] in_pool) {
		ArrayList<Long> pool = new ArrayList<Long>(in_pool.length);
		for(long card : in_pool)
			pool.add(card);
		
		// Choose the supply
		supply = new long[10];
		for(int i=0; i<10; i++) {
			int pick = (int) (Math.random() * pool.size());
			long pick_val = pool.get(pick);
			pool.remove(pick);
			supply[i] = pick_val;
		}
		
		// Choose the resources from the supply
		resources = new long[2];
		int pick = (int) (Math.random() * supply.length);
		resources[0] = supply[pick];
		pick = (int) (Math.random() * supply.length);
		resources[1] = supply[pick];
	}
	
	/** Returns {@code true} if a Black Market is in
	 *  the supply.                               */
	private boolean blackMarket() {
		if(supply == null) return false;
		for(long card : supply)
			if(card == CardList.BLACK_MARKET_ID)
				return true;
		return false;
	}
	
	
	/** Display the resource cards in use for this
	 *  game (Colony, Platinum and Shelters).
	 *  @param colony_set If this set is Prosperity,
	 *  colonies and Platinum are used.
	 *  @param shelter_set If this set is Dark Ages,
	 *  Shelters are used.                        */
	public void displayResourceCards(String colony_set, String shelter_set) {
		String output = "";
		String set_prosp = getString(R.string.set_prosperity);
		String set_dark = getString(R.string.set_dark_ages);
		if(set_prosp.equals(colony_set))
			output += getString(R.string.supply_colonies);
		if(set_dark.equals(shelter_set))
			output += "\n" + getString(R.string.supply_shelters);
		output = output.trim();
		if("".equals(output))
			output = getString(R.string.supply_normal);
		resView.setText(output);
	}
}