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
	
	/** The adapter used to display the supply cards. */
	CardAdapter adapter;
	/** The supply chosen for this deck. */
	private long[] supply;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		ListView card_list = (ListView) findViewById(R.id.card_list);
		
		// Setup the adapter
		adapter = new CardAdapter(this);
		adapter.changeCursor(null);
		card_list.setAdapter(adapter);
		
		// Get the chosen supply cards
		supply = null;
		if(savedInstanceState != null)
			supply = savedInstanceState.getLongArray(KEY_SUPPLY);
		if(supply == null)
			chooseCards(getIntent().getExtras()
								   .getLongArray(PARAM_CARDS));
		
		// Start loading the cards
		LoaderManager lm = getSupportLoaderManager();
		lm.initLoader(2, null, this);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLongArray(KEY_SUPPLY, supply);
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
		String[] strSupply = new String[supply.length];
		for(int i=0; i<supply.length; i++)
			strSupply[i] = "" + supply[i];
		c.setSelectionArgs(strSupply);
		String selection = "";
		for(int i = 0; i < supply.length; i++)
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
	
	/** Choose 10 cards at random from the input pool make them
	 *  the new {@link #supply}. 
	 *  @param in_pool Each string in this array is the id #
	 *  of a card. The supply will be chosen from these ids. */
	private void chooseCards(long[] in_pool) {
		ArrayList<Long> pool = new ArrayList<Long>(in_pool.length);
		for(long card : in_pool)
			pool.add(card);
		
		supply = new long[10];
		for(int i=0; i<10; i++) {
			int pick = (int) (Math.random() * pool.size());
			long pick_val = pool.get(pick);
			pool.remove(pick);
			supply[i] = pick_val;
		}
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
}