package ca.marklauman.dominionpicker.settings;

import ca.marklauman.dominionpicker.R;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/** Handles the preferences for this activity.
 *  @author Mark Lauman                     */
public class ActivitySettings extends PreferenceActivity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add action bar
        setContentView(R.layout.activity_settings);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        t.setTitle(R.string.filters);
        t.setNavigationIcon(R.drawable.ic_action_back);
        t.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // hide the drop shadow if its already there
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View shadow = findViewById(R.id.toolbar_shadow);
            shadow.setVisibility(View.GONE);
        }

        //noinspection deprecation
        addPreferencesFromResource(R.xml.pref_filters);
    }
	
	@Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
	protected boolean isValidFragment (String fragmentName) {
		return SettingsFragment.class.getName().equals(fragmentName)
			   || super.isValidFragment(fragmentName);
	}
}