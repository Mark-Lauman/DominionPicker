package ca.marklauman.dominionpicker.settings;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import ca.marklauman.dominionpicker.R;

/** Very simple activity for the options screen.
 *  @author Mark Lauman  */
public class ActivityOptions extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setDisplayHomeAsUpEnabled(true);
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