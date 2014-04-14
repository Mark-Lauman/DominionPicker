package ca.marklauman.dominionpicker.settings;

import ca.marklauman.dominionpicker.R;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsFragment extends PreferenceFragment {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        String category = getArguments().getString("category");
        if("filters".equals(category))
        	addPreferencesFromResource(R.xml.pref_filters);
	}
}
