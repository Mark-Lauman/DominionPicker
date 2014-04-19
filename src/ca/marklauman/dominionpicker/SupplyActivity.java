package ca.marklauman.dominionpicker;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
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
	private String[] supply;
	
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
			supply = savedInstanceState.getStringArray(KEY_SUPPLY);
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
		outState.putStringArray(KEY_SUPPLY, supply);
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
		c.setSelectionArgs(supply);
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
		
		supply = new String[10];
		for(int i=0; i<10; i++) {
			int pick = (int) (Math.random() * pool.size());
			String pick_val = "" + pool.get(pick);
			pool.remove(pick);
			supply[i] = pick_val;
		}
	}
}