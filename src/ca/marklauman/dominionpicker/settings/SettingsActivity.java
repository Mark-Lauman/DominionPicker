package ca.marklauman.dominionpicker.settings;

import java.util.List;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import ca.marklauman.dominionpicker.R;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;

/** Handles the preferences for this activity.
 *  @author Mark Lauman                     */
public class SettingsActivity extends SherlockPreferenceActivity {
	final static String PREF_FILTERS = "ca.marklauman.dominionpicker.PREF_FILTERS";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
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
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers_3, target);
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
}
