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
import java.util.HashSet;
import java.util.LinkedList;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

/** Used to run a Black Market (see the Black Market card) 
 * @author Mark Lauman                                  */
public class MarketActivity extends SherlockFragmentActivity
							implements LoaderCallbacks<Cursor>,
									   OnItemClickListener {
	/** Key used to pass the card pool to this activity */
	public static final String PARAM_CARDS = "cards";
	/** Key used to pass the card pool to this activity */
	public static final String PARAM_SUPPLY = "supply";
	
	/** Key used to save store stock to savedInstanceState. */
	private static final String KEY_STOCK = "stock";
	/** Key used to save current choices to savedInstanceState. */
	private static final String KEY_CHOICES = "choices";
	
	/** The button to show the next pick */
	private View but_draw;
	/** The list displaying the coices */
	private View choice_panel;
	/** The adapter used to display the choices. */
	private CardAdapter adapter;
	/** The notice for when there is no stock */
	private View sold_out;
	
	/** The stock available in the market. */
	private LinkedList<Long> stock;
	/** The cards the user may choose from this round. */
	private long[] choices;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_market);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Get View items
		but_draw = findViewById(R.id.market_draw);
		choice_panel = findViewById(R.id.market_choices);
		sold_out = findViewById(R.id.market_sold_out);
		
		// Setup the list & adapter
		ListView card_list = (ListView) findViewById(R.id.card_list);
		adapter = new CardAdapter(this);
		adapter.changeCursor(null);
		card_list.setAdapter(adapter);
		card_list.setOnItemClickListener(this);
		
		// Restore app state or set it up
		stock = null;
		choices = null;
		if(savedInstanceState != null) {
			long[] arrStock = savedInstanceState.getLongArray(KEY_STOCK);
			stock = new LinkedList<Long>();
			for(long id : arrStock)
				stock.add(id);
			choices = savedInstanceState.getLongArray(KEY_CHOICES);
		} else
			setupMarketStall();
		
		// Setup the display based off of app state
		if(choices == null) {
			if(stock == null || stock.size() < 1) {
				but_draw.setVisibility(View.GONE);
				sold_out.setVisibility(View.VISIBLE);
			}
		} else {
			but_draw.setVisibility(View.GONE);
			choice_panel.setVisibility(View.VISIBLE);
			getSupportLoaderManager().restartLoader(4, null, this);
		}
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		long[] arrStock = new long[stock.size()];
		int i = 0;
		for(long card : stock) {
			arrStock[i] = card;
			i++;
		}
		outState.putLongArray(KEY_STOCK, arrStock);
		outState.putLongArray(KEY_CHOICES, choices);
	}
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // android "back" button on the action bar
        case android.R.id.home:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader c = new CursorLoader(this);
		c.setUri(CardList.URI);
		String[] strChoices = new String[choices.length];
		for(int i=0; i<choices.length; i++)
			strChoices[i] = "" + choices[i];
		c.setSelectionArgs(strChoices);
		String selection = "";
		for(int i = 0; i < choices.length; i++)
			selection += " OR " + CardList._ID + "=?";
		c.setSelection(selection.substring(4));
		return c;
	}
	
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.changeCursor(data);
	}
	
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.changeCursor(null);
	}
	
	
	/** Setup the black market. */
	private void setupMarketStall() {
		Bundle extras = getIntent().getExtras();
		
		// load the supply
		HashSet<Long> supply = new HashSet<Long>();
		long[] supply_arr = extras.getLongArray(PARAM_SUPPLY);
		if(supply_arr != null)
			for(long id : supply_arr)
				supply.add(id);
		supply_arr = null;
		
		// load the deck of available cards
		long[] deck_arr = extras.getLongArray(PARAM_CARDS);
		ArrayList<Long> deck = new ArrayList<Long>(deck_arr.length);
		for(int i=0; i<deck_arr.length; i++)
			if(!supply.contains(deck_arr[i]))
				deck.add(deck_arr[i]);
		deck_arr = null;
		
		// shuffle the deck to make the stock
		stock = new LinkedList<Long>();
		while(deck.size() > 0) {
			int pick = (int) (Math.random() * deck.size());
			stock.add(deck.get(pick));
			deck.remove(pick);
		}
	}
	
	
	/** Draw the next 3 cards to be picked.
	 *  Called by the "draw" button on press.
	 *  @param v The {@link View} of the draw button. */
	public void drawCards(View v) {
		if(stock.size() < 3)
			choices = new long[stock.size()];
		else choices = new long[3];
		for(int i=0; i<choices.length; i++)
			choices[i] = stock.removeFirst();
		but_draw.setVisibility(View.GONE);
		if(0 < choices.length) {
			choice_panel.setVisibility(View.VISIBLE);
			getSupportLoaderManager().restartLoader(3, null, this);
		} else
			sold_out.setVisibility(View.VISIBLE);
	}
	
	
	/** Pass this set of cards.
	 *  Called by the "pass" button on press.
	 *  @param v The {@link View} of the pass button. */
	public void passPick(View v) {
		for(long card : choices)
			stock.add(card);
		choices = null;
		choice_panel.setVisibility(View.GONE);
		adapter.changeCursor(null);
		but_draw.setVisibility(View.VISIBLE);
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
		Toast.makeText(this,
					   adapter.getCard(id)[1],
					   Toast.LENGTH_SHORT)
			 .show();
		
		// Unused choices return to the stock bottom
		for(long card : choices)
			if(card != id)
				stock.add(card);
		
		// Hide the choice panel and clear it
		choice_panel.setVisibility(View.GONE);
		adapter.changeCursor(null);
		
		// Show the "draw" button if cards can still be drawn
		if(choices.length > 1)
			but_draw.setVisibility(View.VISIBLE);
		// Or say we're sold out
		else
			sold_out.setVisibility(View.VISIBLE);
		choices = null;
	}
}