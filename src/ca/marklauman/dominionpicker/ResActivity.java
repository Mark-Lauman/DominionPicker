package ca.marklauman.dominionpicker;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class ResActivity extends SherlockFragmentActivity
						 implements LoaderCallbacks<Cursor> {
	
	public static final String PARAM_CARDS = "cards";
	
	CardAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		ListView card_list = (ListView) findViewById(R.id.card_list);
		
		adapter = new CardAdapter(this);
		adapter.changeCursor(null);
		card_list.setAdapter(adapter);
		
		String[] in_cards = getIntent().getExtras()
				.getStringArray(PARAM_CARDS);
		Bundle out_cards = new Bundle();
		out_cards.putStringArray(PARAM_CARDS, in_cards);
		
		LoaderManager lm = getSupportLoaderManager();
		lm.initLoader(2, out_cards, this);
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
		String[] sel_args = args.getStringArray(PARAM_CARDS);
		c.setSelectionArgs(sel_args);
		String selection = "";
		for(int i = 0; i < sel_args.length; i++)
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
}
