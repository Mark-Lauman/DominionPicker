package ca.marklauman.dominionpicker.settings;

import ca.marklauman.dominionpicker.R;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/** Fragment used for settings in android > 3.0.
 *  @author Mark Lauman                       */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsFragment extends PreferenceFragment {
	@SuppressWarnings("WeakerAccess")
    final static String PREF_FILTERS = "ca.marklauman.dominionpicker.PREF_FILTERS";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        String category = getArguments().getString("category");
        if(PREF_FILTERS.equals(category))
        	addPreferencesFromResource(R.xml.pref_filters);
	}
}
