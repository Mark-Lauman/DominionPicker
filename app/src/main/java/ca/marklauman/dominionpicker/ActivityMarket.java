package ca.marklauman.dominionpicker;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;

/** Activity used to wrap the {@link FragmentMarket} when it is invoked from inside a supply.
 *  @author Mark Lauman */
public class ActivityMarket extends AppCompatActivity {
	/** Key used to pass the supply pool to this activity (optional) */
	public static final String PARAM_SUPPLY = FragmentMarket.PARAM_SUPPLY;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        App.updateInfo(this);

        FrameLayout swapPanel = new FrameLayout(this);
        swapPanel.setId(R.id.content_frame);
        setContentView(swapPanel);
        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState != null) return;

        Bundle appExtras = getIntent().getExtras();
        FragmentMarket market = new FragmentMarket();
        Bundle args = new Bundle();
        args.putLongArray(FragmentMarket.PARAM_SUPPLY,
                          appExtras.getLongArray(PARAM_SUPPLY));
        market.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.content_frame, market)
                                   .commit();
	}

    @Override
    public void onStart() {
        super.onStart();
        App.updateInfo(this);
    }
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // anything but the back button returns normal
        if(android.R.id.home != item.getItemId())
            return super.onOptionsItemSelected(item);
        // the back button closes the activity
        finish();
        return true;
	}
}