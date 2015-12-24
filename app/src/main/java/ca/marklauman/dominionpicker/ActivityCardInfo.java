package ca.marklauman.dominionpicker;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

// TODO: Actually implement this class
/** Activity used to display detailed card information.
 *  This goes into detail on ONE card. No other cards are shown.
 *  @author Mark Lauman */
public class ActivityCardInfo extends AppCompatActivity {

    public static final String PARAM_ID = "card_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_info);
        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setDisplayHomeAsUpEnabled(true);

        if(ab != null) ab.setTitle("Card Name");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(android.R.id.home == item.getItemId()) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
