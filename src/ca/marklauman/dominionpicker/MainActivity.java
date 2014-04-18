package ca.marklauman.dominionpicker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

import ca.marklauman.dominionpicker.settings.SettingsActivity;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends SherlockFragmentActivity
						  implements LoaderCallbacks<Cursor> {
	
	public static final String KEY_SELECT = "selections";
	
	ListView card_list;
	CardAdapter adapter;
	long[] last_select;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		card_list = (ListView) findViewById(R.id.card_list);
		last_select = null;
		
		// Setup default settings
		PreferenceManager.setDefaultValues(this, R.xml.pref_filters, false);
		
		// load last selections
		String store = PreferenceManager.getDefaultSharedPreferences(this)
						                .getString(KEY_SELECT, null);
		if(store == null) return;
		StringTokenizer tok = new StringTokenizer(store, ",");
		last_select = new long[tok.countTokens()];
		for(int i=0; i<last_select.length; i++)
			last_select[i] = Long.parseLong(tok.nextToken());
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		LoaderManager lm = getSupportLoaderManager();
		lm.restartLoader(1, null, this);
	}
	
	@Override
	protected void onStop() {
		if(adapter != null)
			last_select = adapter.getSelections();
		
		// save the selections to permanent storage
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < last_select.length; i++)
		    str.append(last_select[i]).append(",");
		PreferenceManager.getDefaultSharedPreferences(this)
		 				 .edit()
		 				 .putString(KEY_SELECT, str.toString())
		 				 .commit();
		
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_toggle_all:
			adapter.toggleAll();
			last_select = adapter.getSelections();
			return true;
		case R.id.action_filters:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_submit:
			last_select = adapter.getSelections();
			if(last_select.length < 10) {
				String more = getResources().getString(R.string.more);
				Toast.makeText(this, more + " (" + last_select.length + "/10)", Toast.LENGTH_LONG).show();
				return true;
			}
			
			ArrayList<Long> pool = new ArrayList<Long>();
			for(long id : last_select)
				pool.add(id);
			
			String[] cards = new String[10];
			for(int i=0; i<10; i++) {
				int pick = (int) (Math.random() * pool.size());
				long pick_id = pool.get(pick);
				pool.remove(pick);
				cards[i] = "" + pick_id;
			}
			
			Intent resAct = new Intent(this, ResActivity.class);
			resAct.putExtra(ResActivity.PARAM_CARDS, cards);
			startActivityForResult(resAct, -1);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader c = new CursorLoader(this);
		c.setUri(CardList.URI);
		
		// Filter by set
		ArrayList<String> sel_args = new ArrayList<String>();
		Resources res = getResources();
		String[] sets = res.getStringArray(R.array.card_sets);
		HashSet<String> vis_sets = getVisibleSets(PreferenceManager.getDefaultSharedPreferences(this),
												  res);
		for(String set : sets) {
			if(!vis_sets.contains(set))
				sel_args.add(set);
		}
		String sel = "";
		for(int i=0; i<sel_args.size(); i++)
			sel += "AND " + CardList._EXP + "!=? ";
		if(sel_args.size() != 0)
			sel = sel.substring(4);
		
		c.setSelection(sel);
		String[] sel_args2 = new String[sel_args.size()];
		sel_args.toArray(sel_args2);
		c.setSelectionArgs(sel_args2);
		
		return c;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter = new CardAdapter(this);
		adapter.setChoiceMode(CardAdapter.CHOICE_MODE_MULTIPLE);
		adapter.changeCursor(data);
		if(last_select != null)
			adapter.setSelections(last_select);
		card_list.setAdapter(adapter);
		card_list.setOnItemClickListener(adapter);
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		last_select = adapter.getSelections();
		card_list.setAdapter(null);
		adapter = null;
	}
	
	public static HashSet<String> getVisibleSets(SharedPreferences prefs, Resources res) {
		HashSet<String> out = new HashSet<String>();
		if(prefs.getBoolean("filt_set_base", false))
			out.add(res.getString(R.string.set_base));
		if(prefs.getBoolean("filt_set_alchemy", false))
			out.add(res.getString(R.string.set_alchemy));
		if(prefs.getBoolean("filt_set_black_market", false))
			out.add(res.getString(R.string.set_black_market));
		if(prefs.getBoolean("filt_set_cornucopia", false))
			out.add(res.getString(R.string.set_cornucopia));
		if(prefs.getBoolean("filt_set_dark_ages", false))
			out.add(res.getString(R.string.set_dark_ages));
		if(prefs.getBoolean("filt_set_envoy", false))
			out.add(res.getString(R.string.set_envoy));
		if(prefs.getBoolean("filt_set_governor", false))
			out.add(res.getString(R.string.set_governor));
		if(prefs.getBoolean("filt_set_hinterlands", false))
			out.add(res.getString(R.string.set_hinterlands));
		if(prefs.getBoolean("filt_set_intrigue", false))
			out.add(res.getString(R.string.set_intrigue));
		if(prefs.getBoolean("filt_set_prosperity", false))
			out.add(res.getString(R.string.set_prosperity));
		if(prefs.getBoolean("filt_set_seaside", false))
			out.add(res.getString(R.string.set_seaside));
		if(prefs.getBoolean("filt_set_stash", false))
			out.add(res.getString(R.string.set_stash));
		if(prefs.getBoolean("filt_set_walled_village", false))
			out.add(res.getString(R.string.set_walled_village));
		return out;
	}
}