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
import java.util.StringTokenizer;

import ca.marklauman.dominionpicker.settings.SettingsActivity;
import ca.marklauman.tools.MultiSelectImagePreference;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ListView;
import android.widget.Toast;

/** Execution starts here. This activity allows you to pick
 *  the cards to use in your deck.
 *  @author Mark Lauman                                  */
public class MainActivity extends SherlockFragmentActivity
						  implements LoaderCallbacks<Cursor> {
	
	/** Key used to save selections to the preferences. */
	public static final String KEY_SELECT = "selections";
	
	/** The view associated with the card list. */
	ListView card_list;
	/** The adapter for the card list. */
	CardAdapter adapter;
	/** Used to store the currently selected cards. */
	long[] selections;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		card_list = (ListView) findViewById(R.id.card_list);
		selections = null;
		
		// Setup default preferences
		setupPreferences();
		
		// load last selections
		String store = PreferenceManager.getDefaultSharedPreferences(this)
						                .getString(KEY_SELECT, null);
		if(store == null) return;
		StringTokenizer tok = new StringTokenizer(store, ",");
		selections = new long[tok.countTokens()];
		for(int i=0; i<selections.length; i++)
			selections[i] = Long.parseLong(tok.nextToken());
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
			selections = adapter.getSelections();
		
		// save the selections to permanent storage
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < selections.length; i++)
		    str.append(selections[i]).append(",");
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
			selections = adapter.getSelections();
			return true;
		case R.id.action_filters:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_submit:
			selections = adapter.getSelections();
			if(selections.length < 10) {
				String more = getResources().getString(R.string.more);
				Toast.makeText(this, more + " (" + selections.length + "/10)", Toast.LENGTH_LONG).show();
				return true;
			}
			
			Intent resAct = new Intent(this, SupplyActivity.class);
			resAct.putExtra(SupplyActivity.PARAM_CARDS, selections);
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
		String sel_args1 = PreferenceManager.getDefaultSharedPreferences(this)
											.getString("filt_set", "");
		String[] sel_args = MultiSelectImagePreference.getValues(sel_args1);
		
		String sel = "";
		for(int i=0; i<sel_args.length; i++)
			sel += "AND " + CardList._EXP + "!=? ";
		if(sel_args.length != 0)
			sel = sel.substring(4);
		
		c.setSelection(sel);
		c.setSelectionArgs(sel_args);
		
		return c;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter = new CardAdapter(this);
		adapter.setChoiceMode(CardAdapter.CHOICE_MODE_MULTIPLE);
		adapter.changeCursor(data);
		if(selections != null)
			adapter.setSelections(selections);
		card_list.setAdapter(adapter);
		card_list.setOnItemClickListener(adapter);
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		selections = adapter.getSelections();
		card_list.setAdapter(null);
		adapter = null;
	}
	
	
	/** Setup the preferences and update old
	 *  preferences to the new standard   */
	private void setupPreferences() {
		PreferenceManager.setDefaultValues(this, R.xml.pref_version, false);
		PreferenceManager.setDefaultValues(this, R.xml.pref_filters, false);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Update settings from version 0
		if(prefs.contains("filt_set_base")) {
			// interpret old settings
			String[] set_names = getResources().getStringArray(R.array.card_sets);
			ArrayList<String> filt_set = new ArrayList<String>();
			if(!prefs.getBoolean("filt_set_base", true))
				filt_set.add(set_names[0]);
			if(!prefs.getBoolean("filt_set_alchemy", true))
				filt_set.add(set_names[1]);
			if(!prefs.getBoolean("filt_set_black_market", true))
				filt_set.add(set_names[2]);
			if(!prefs.getBoolean("filt_set_cornucopia", true))
				filt_set.add(set_names[3]);
			if(!prefs.getBoolean("filt_set_dark_ages", true))
				filt_set.add(set_names[4]);
			if(!prefs.getBoolean("filt_set_envoy", true))
				filt_set.add(set_names[5]);
			if(!prefs.getBoolean("filt_set_governor", true))
				filt_set.add(set_names[6]);
			if(!prefs.getBoolean("filt_set_guilds", true))
				filt_set.add(set_names[7]);
			if(!prefs.getBoolean("filt_set_hinterlands", true))
				filt_set.add(set_names[8]);
			if(!prefs.getBoolean("filt_set_intrigue", true))
				filt_set.add(set_names[9]);
			if(!prefs.getBoolean("filt_set_prosperity", true))
				filt_set.add(set_names[10]);
			if(!prefs.getBoolean("filt_set_seaside", true))
				filt_set.add(set_names[11]);
			if(!prefs.getBoolean("filt_set_stash", true))
				filt_set.add(set_names[12]);
			if(!prefs.getBoolean("filt_set_walled_village", true))
				filt_set.add(set_names[13]);
			
			// write them in the new format
			MultiSelectImagePreference.saveValue(prefs, "filt_set", filt_set);
			
			// remove old settings
			Editor edit = prefs.edit();
			edit.remove("filt_set_base");
			edit.remove("filt_set_alchemy");
			edit.remove("filt_set_black_market");
			edit.remove("filt_set_cornucopia");
			edit.remove("filt_set_dark_ages");
			edit.remove("filt_set_envoy");
			edit.remove("filt_set_governor");
			edit.remove("filt_set_guilds");
			edit.remove("filt_set_hinterlands");
			edit.remove("filt_set_intrigue");
			edit.remove("filt_set_prosperity");
			edit.remove("filt_set_seaside");
			edit.remove("filt_set_stash");
			edit.remove("filt_set_walled_village");
			edit.commit();
		}
	}
}