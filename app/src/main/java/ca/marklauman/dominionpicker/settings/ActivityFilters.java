package ca.marklauman.dominionpicker.settings;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import ca.marklauman.dominionpicker.R;

/** Very simple activity for filtering cards.
 *  @author Mark Lauman  */
public class ActivityFilters extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // android "back" button on the action bar
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }
}