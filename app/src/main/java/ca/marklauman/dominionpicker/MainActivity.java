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

import ca.marklauman.dominionpicker.settings.ActivitySettings;
import ca.marklauman.tools.ExpandedArrayAdapter;
import ca.marklauman.tools.MultiSelectImagePreference;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
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

    /** Called when the activity is first created */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

    /** Save the current state of this activity. */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_ACTIVE, navAdapt.getSelection());
        super.onSaveInstanceState(outState);
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
                long[] selections = ((FragmentPicker)active).getSupplySelections();
                // Check for insufficient cards
                if(selections == null) return true;

                Intent resAct = new Intent(this, ActivitySupply.class);
                resAct.putExtra(ActivitySupply.PARAM_CARDS, selections);
                startActivityForResult(resAct, -1);
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
        PreferenceManager.setDefaultValues(this, R.xml.pref_version, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_filters, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Update settings from version 0
        if(prefs.contains("filt_set_base")) {
            // interpret old settings
            String[] set_names = getResources().getStringArray(R.array.card_sets);
            ArrayList<String> filt_set = new ArrayList<>();
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