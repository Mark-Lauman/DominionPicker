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
package ca.marklauman.dominionpicker.settings;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import ca.marklauman.dominionpicker.R;

import android.os.Bundle;

/** Handles the preferences for this activity.
 *  @author Mark Lauman                     */
public class SettingsActivity extends SherlockPreferenceActivity {
	final static String PREF_FILTERS = "ca.marklauman.dominionpicker.PREF_FILTERS";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
//        	setupLegacy();
        setupLegacy();
    }
	
	@SuppressWarnings("deprecation")
	public void setupLegacy() {
		addPreferencesFromResource(R.xml.pref_filters);
//		String category = getIntent().getAction();
//		if(PREF_FILTERS.equals(category))
//			addPreferencesFromResource(R.xml.pref_filters);
//		else
//			addPreferencesFromResource(R.xml.pref_headers_1);
	}
	
	
//	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
//	@Override
//    public void onBuildHeaders(List<Header> target) {
//        loadHeadersFromResource(R.xml.pref_headers_3, target);
//    }
	
	
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
	protected boolean isValidFragment (String fragmentName) {
		if(SettingsFragment.class.getName().equals(fragmentName))
			return true;
		return super.isValidFragment(fragmentName);
	}
}