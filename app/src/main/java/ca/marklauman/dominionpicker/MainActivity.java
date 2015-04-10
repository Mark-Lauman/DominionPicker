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

import ca.marklauman.dominionpicker.settings.ActivitySettings;
import ca.marklauman.tools.ExpandedArrayAdapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/** Execution starts here. This activity is the hub you first
 *  see upon entering the app. Individual screens are covered
 *  by their own Fragments.
 *  @author Mark Lauman                                  */
public class MainActivity extends ActionBarActivity
                          implements ListView.OnItemClickListener {

    /** Key used to save the id of the selected fragment to savedInstanceState. */
    private static final String KEY_ACTIVE = "active";

    /** Used by external threads to access the context */
    private static Context staticContext;

    /** The name of the app */
    private String app_name;
    /** The names of the navigation drawer entries */
    private String[] navNames;

    /** Layout for the navigation drawer */
    private DrawerLayout navLayout;
    /** Listens to and toggles the navigation drawer */
    private NavToggle navToggle;
    /** ListView for the navigation drawer */
    private ListView navView;
    /** The adapter for the navigation drawer's ListView */
    private ExpandedArrayAdapter<String> navAdapt;

    /** The current active fragment. */
    private Fragment active;
    /** Handler used to manage the shuffler */
    private ShuffleManager shuffler;

    /** Called when the activity is first created */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        staticContext = getApplicationContext();
        shuffler = new ShuffleManager();
		setContentView(R.layout.activity_main);
        navLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navView = (ListView) findViewById(R.id.left_drawer);
		
		// Setup default preferences
		setupPreferences();

        // Get the strings for the nav drawer
        app_name = getString(R.string.app_name);
        navNames = getResources().getStringArray(R.array.navNames);

        // Display nav drawer icon on the action bar
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeAsUpIndicator(R.drawable.ic_navdrawer);
        bar.setHomeButtonEnabled(true);

        // Setup the navigation drawer
        navLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        navToggle = new NavToggle(this, navLayout);
        navLayout.setDrawerListener(navToggle);
        String[] headers = getResources().getStringArray(R.array.navNames);
        navAdapt = new ExpandedArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, headers);
        navAdapt.setIcons(R.drawable.ic_action_send, R.drawable.ic_action_market,
                R.drawable.ic_action_settings);
        navAdapt.setSelBack(R.color.nav_drawer_sel);
        navView.setAdapter(navAdapt);
        navView.setOnItemClickListener(this);

        // setup the active fragment
        FragmentManager fm = getSupportFragmentManager();
        if(savedInstanceState == null) {
            // For the first setup, FragmentPicker is selected
            navAdapt.setSelection(0);
            getSupportActionBar().setTitle(navNames[0]);
            active = new FragmentPicker();
            fm.beginTransaction()
              .replace(R.id.content_frame, active)
              .commit();
        } else {
            // subsequent setups use the stored fragment
            int sel = savedInstanceState.getInt(KEY_ACTIVE, 0);
            navAdapt.setSelection(sel);
            getSupportActionBar().setTitle(navNames[sel]);
            active = fm.findFragmentById(R.id.content_frame);
        }
	}

    /** Get the context for the process. (accessible outside the display thread) */
    public static Context getStaticContext() {
        return staticContext;
    }

    /** Save the current state of this activity. */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_ACTIVE, navAdapt.getSelection());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        shuffler.unregister();
        super.onDestroy();
    }

    /** Setup the ActionBar's menu. Only called once at application start */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        // Needs to be dynamic for multiple tabs
        getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    /** Prepare the ActionBar's menu. Called every time the menu
     *  is displayed - including after each invalidation.
     *  Useful to hide/display menu items.             */
    public boolean onPrepareOptionsMenu(Menu menu) {
        /* Only show items if the FragmentPicker is active and
         * the navigation drawer is not open.       */
        boolean picker_active = navAdapt != null && navAdapt.getSelection() == 0;
        boolean show = picker_active && !navLayout.isDrawerOpen(navView);
        menu.findItem(R.id.action_toggle_all)
            .setVisible(show);
        menu.findItem(R.id.action_submit)
            .setVisible(show);
        return super.onPrepareOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        // Handle navigation bar requests first
        if(navToggle.onOptionsItemSelected(item))
            return true;

        // Then handle our menu items
		switch(item.getItemId()) {
            case R.id.action_toggle_all:
                ((FragmentPicker)active).toggleAll();
                return true;
            case R.id.action_submit:
                Long[] cards = ((FragmentPicker)active).getLongSelections();
                shuffler.startShuffle(cards);

                // TODO: Remove section
//                // Check for insufficient cards
//                if(selections == null) return true;
//
//                Intent resAct = new Intent(this, ActivitySupply.class);
//                resAct.putExtra(ActivitySupply.PARAM_CARDS, selections);
//                startActivityForResult(resAct, -1);
                return true;
		}

        // Not an item we created
		return super.onOptionsItemSelected(item);
	}


    /** Called when an item in the navigation bar is selected */
    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        // The case where you selected the same thing again.
        if(position == navAdapt.getSelection()) {
            navLayout.closeDrawer(navView);
            return;
        }

        // never select filters
        if(position != 2) navAdapt.setSelection(position);

        // save selections before swap
        if(active instanceof FragmentPicker)
            ((FragmentPicker)active).saveSelections(this);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        switch(position) {
            case 0: active = new FragmentPicker();
                    t.replace(R.id.content_frame, active);
                    break;
            case 1: Toast.makeText(this, R.string.market_begin,
                                   Toast.LENGTH_LONG).show();
                    long[] cards = ((FragmentPicker)active).getSelections();
                    active = new FragmentMarket();
                    Bundle args = new Bundle();
                    args.putLongArray(FragmentMarket.PARAM_CARDS, cards);
                    active.setArguments(args);
                    t.replace(R.id.content_frame, active);
                    break;
            case 2: Intent intent = new Intent(this, ActivitySettings.class);
                    startActivity(intent);
        }
        t.commit();

        navLayout.closeDrawer(navView);
    }


    /** Handles the navigation bar's opening and closing. */
    private class NavToggle extends ActionBarDrawerToggle {
        public NavToggle(Activity activity, DrawerLayout drawerLayout) {
            super(activity, drawerLayout,
                  R.string.menu_open, R.string.menu_close);
        }

        /** Called when a drawer has settled in a completely closed state. */
        public void onDrawerClosed(View view) {
            // make the current selection the active title
            getSupportActionBar().setTitle(navNames[navAdapt.getSelection()]);
            invalidateOptionsMenu();
            super.onDrawerClosed(view);
        }

        /** Called when a drawer has settled in a completely open state. */
        public void onDrawerOpened(View view) {
            // make the application name the active title
            getSupportActionBar().setTitle(app_name);
            invalidateOptionsMenu();
            super.onDrawerOpened(view);
        }
    }


    /** Setup the preferences and update old preferences to the new standard. */
    private void setupPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.pref_filters, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Update settings from version 0
        if(prefs.contains("filt_set_base")) {
            // interpret old settings
            String newFilt = "";
            if(!prefs.getBoolean("filt_set_base", true)) newFilt += ",0";
            if(!prefs.getBoolean("filt_set_alchemy", true)) newFilt += ",1";
            if(!prefs.getBoolean("filt_set_black_market", true)) newFilt += ",2";
            if(!prefs.getBoolean("filt_set_cornucopia", true)) newFilt += ",3";
            if(!prefs.getBoolean("filt_set_dark_ages", true)) newFilt += ",4";
            if(!prefs.getBoolean("filt_set_envoy", true)) newFilt += ",5";
            if(!prefs.getBoolean("filt_set_governor", true)) newFilt += ",6";
            if(!prefs.getBoolean("filt_set_guilds", true)) newFilt += ",7";
            if(!prefs.getBoolean("filt_set_hinterlands", true)) newFilt += ",8";
            if(!prefs.getBoolean("filt_set_intrigue", true)) newFilt += ",9";
            // Prince not in version 0
            if(!prefs.getBoolean("filt_set_prosperity", true)) newFilt += ",11";
            if(!prefs.getBoolean("filt_set_seaside", true)) newFilt += ",12";
            if(!prefs.getBoolean("filt_set_stash", true)) newFilt += ",13";
            if(!prefs.getBoolean("filt_set_walled_village", true)) newFilt += ",14";
            // trim off the comma at the beginning
            if(newFilt.length() > 1) newFilt = newFilt.substring(1);

            // write them in the new format
            prefs.edit().putString("filt_set", newFilt);

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
        } else {
            // The separator used in MultiSelectImagePreference
            String old_sep = "\u0001\u0007\u001D\u0007\u0001";

            // update sets to newest version
            String filt = prefs.getString("filt_set", "");
            if(filt.contains(old_sep)) {
                String newSets = "";
                if(filt.contains("Base")) newSets += ",0";
                if(filt.contains("Alchemy")) newSets += ",1";
                if(filt.contains("Black Market")) newSets += ",2";
                if(filt.contains("Cornucopia")) newSets += ",3";
                if(filt.contains("Dark Ages")) newSets += ",4";
                if(filt.contains("Envoy")) newSets += ",5";
                if(filt.contains("Governor")) newSets += ",6";
                if(filt.contains("Guilds")) newSets += ",7";
                if(filt.contains("Hinterlands")) newSets += ",8";
                if(filt.contains("Intrigue")) newSets += ",9";
                if(filt.contains("Prince")) newSets += ",10";
                if(filt.contains("Prosperity")) newSets += ",11";
                if(filt.contains("Seaside")) newSets += ",12";
                if(filt.contains("Stash")) newSets += ",13";
                if(filt.contains("Walled Village")) newSets += ",14";
                if(newSets.length() > 1) newSets = newSets.substring(1);
                prefs.edit().putString("filt_set", newSets);
            }

            // update costs to newest version
            filt = prefs.getString("filt_cost", "");
            if(filt.contains(old_sep)) {
                String newCost = "";
                if(filt.contains("Potion")) newCost += ",0";
                if(filt.contains("1")) newCost += ",1";
                if(filt.contains("2")) newCost += ",2";
                if(filt.contains("3")) newCost += ",3";
                if(filt.contains("4")) newCost += ",4";
                if(filt.contains("5")) newCost += ",5";
                if(filt.contains("6")) newCost += ",6";
                if(filt.contains("7")) newCost += ",7";
                if(filt.contains("8")) newCost += ",8";
                if(filt.contains("8*")) newCost += ",9";

                if(newCost.length() > 1) newCost = newCost.substring(1);
                prefs.edit().putString("filt_cost", newCost);
            }
        }
    }

    private class ShuffleManager extends BroadcastReceiver {
        private SupplyShuffler shuffler;

        public ShuffleManager() {
            super();
            shuffler = null;
            LocalBroadcastManager.getInstance(getStaticContext())
                    .registerReceiver(this, new IntentFilter(SupplyShuffler.MSG_INTENT));
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            shuffler = null;
            Log.d("Intent action", "" + intent.getAction());

            int res = intent.getIntExtra(SupplyShuffler.MSG_RES, -100);
            switch(res) {
                case SupplyShuffler.RES_OK:
                    Supply s = intent.getParcelableExtra(SupplyShuffler.MSG_SUPPLY);
                    Log.d("Shuffler", s + "");
                    return;
                case SupplyShuffler.RES_MORE:
                    Log.d("Shortfall",
                          ""+intent.getStringExtra(SupplyShuffler.MSG_SHORT));
                    return;
                case SupplyShuffler.RES_MORE_K:
                    Log.d("K Shortfall",
                            ""+intent.getStringExtra(SupplyShuffler.MSG_SHORT));
                    return;
                case SupplyShuffler.RES_NO_YW:
                    Log.d("Young Witch", "No targets");
                    return;
                default: // Do nothing
            }
        }

        /** Start a shuffle and register this receiver.
         *  Also cancels any shuffles in progress. */
        public void startShuffle(Long... cards) {
            if(cards == null) cards = new Long[0];
            cancelShuffle();
            shuffler = new SupplyShuffler(10, 2);
            shuffler.execute(cards);
        }

        /** Stop a shuffle if it is in session. */
        public void cancelShuffle() {
            if(shuffler != null) shuffler.cancel(true);
            shuffler = null;
        }

        /** Unregister this manager and shut down open shuffles */
        public void unregister() {
            LocalBroadcastManager.getInstance(getStaticContext())
                                 .unregisterReceiver(this);
            cancelShuffle();
        }
    }
}