package ca.marklauman.dominionpicker;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import ca.marklauman.dominionpicker.cardadapters.AdapterCardsSupply;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.database.TableSupply;
import ca.marklauman.dominionpicker.database.TimestampFormatter;
import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.tools.QueryDialogBuilder;
import ca.marklauman.tools.QueryDialogBuilder.QueryListener;
import ca.marklauman.tools.Utils;

/** Activity for displaying the supply piles for a new game.
 *  @author Mark Lauman */
public class ActivitySupply extends AppCompatActivity
                            implements Prefs.Listener {
    /** Key used to pass a supply id to this activity.
     *  The supply will load from the supply table. */
    public static final String PARAM_SUPPLY_ID = "supplyId";
    /** Key used to pass a history timestamp to this activity.
     *  The supply will load from the history table. */
    public static final String PARAM_HISTORY_ID = "historyId";

    /** The loader that gets the supply when an id is provided. */
    private final SupplyLoader supplyLoader = new SupplyLoader();
    /** The loader that gets the cards when the supply is loaded */
    private final CardLoader cardLoader = new CardLoader();
    /** Formatter used to make strings out of timestamps */
    private TimestampFormatter tFormat;

    /** The adapter used to display the supply cards. */
	private AdapterCardsSupply adapter;
	/** The TextView used to display the resource cards. */
	private TextView resView;
    /** {@code true} if the supply is from the sample table */
    private boolean sampleSupply = false;
	/** The supply on display. */
	private Supply supply;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Prefs.setup(this);

		setContentView(R.layout.activity_supply);
        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setDisplayHomeAsUpEnabled(true);
        Bundle params = getIntent().getExtras();

        // Basic view setup
		ListView card_list = (ListView) findViewById(R.id.card_list);
		View loading = findViewById(android.R.id.progress);
		card_list.setEmptyView(loading);
		resView = (TextView) findViewById(R.id.resources);
		resView.setVisibility(View.GONE);
        View timeFrame = findViewById(R.id.time_frame);
        TextView timeView = (TextView)findViewById(R.id.time);

        // Time formatter
        tFormat = new TimestampFormatter(this);
		
		// Setup the adapter
		adapter = new AdapterCardsSupply(this);
		adapter.changeCursor(null);
		card_list.setAdapter(adapter);

        // We have no supply, check for a history id
        if(params == null) return;
        long supplyId = params.getLong(PARAM_HISTORY_ID, -1);
        if(supplyId != -1) {
            timeView.setText(tFormat.formatShort(supplyId));
            Bundle args = new Bundle();
            args.putLong(PARAM_HISTORY_ID, supplyId);
            LoaderManager lm = getSupportLoaderManager();
            lm.initLoader(LoaderId.SUPPLY_OBJECT, args, supplyLoader);
            return;
        }
        
        // We still have no supply, check for a sample supply id.
        supplyId = params.getLong(PARAM_SUPPLY_ID, -1);
        if(supplyId != -1) {
            timeFrame.setVisibility(View.GONE);
            sampleSupply = true;
            Bundle args = new Bundle();
            args.putLong(PARAM_SUPPLY_ID, supplyId);
            LoaderManager lm = getSupportLoaderManager();
            lm.initLoader(LoaderId.SUPPLY_OBJECT, args, supplyLoader);
        }
	}
	
	
	/** Called to inflate the ActionBar */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.supply, menu);
		return true;
	}
	
	
	/** Called just before the ActionBar is displayed. */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		// Apply any changes super wants to apply.
		boolean changed = super.onPrepareOptionsMenu(menu);
        if(supply == null) return changed;
		
		/* Check if the black market icon is in/visible when it shouldn't be.
		 * If a change is needed, apply the change. */
		MenuItem marketButton = menu.findItem(R.id.action_market);
        if(supply.blackMarket() != marketButton.isVisible()) {
			marketButton.setVisible(supply.blackMarket());
			changed = true;
		}

        // No favorite button on sample
        if(sampleSupply) return changed;

        // Show/hide the correct favorite buttons
        final boolean isFavorite = supply.name != null;
        MenuItem favButton = menu.findItem(R.id.action_favorite);
        if(isFavorite != favButton.isVisible()) {
            favButton.setVisible(isFavorite);
            changed = true;
        }
        MenuItem notFavButton = menu.findItem(R.id.action_notFavorite);
        if(isFavorite == notFavButton.isVisible()) {
            notFavButton.setVisible(!isFavorite);
            changed = true;
        }

		return changed;
	}
	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // android "back" button on the action bar
        case android.R.id.home:
            finish();
            return true;
        case R.id.action_market:
        	Intent resAct = new Intent(this, ActivityMarket.class);
        	resAct.putExtra(ActivityMarket.PARAM_SUPPLY,
        					supply.cards);
			startActivityForResult(resAct, -1);
			return true;
        case R.id.action_notFavorite:
            FavDialog dialog = new FavDialog(this);
            dialog.create().show();
            return true;
        case R.id.action_favorite:
            // Wipe the supply name
            supply.name = null;
            ActionBar ab = getSupportActionBar();
            if(ab != null) ab.setTitle(R.string.supply_no_name);
            invalidateOptionsMenu();

            // Save the wipe to the database
            ContentValues values = new ContentValues();
            values.putNull(TableSupply._NAME);
            getContentResolver().update(Provider.URI_HIST, values,
                                        TableSupply._ID + "=?",
                                        new String[]{"" + supply.time});
            return true;
        }
        return super.onOptionsItemSelected(item);
	}
	
	
	/** Set the supply on display in this activity.
	 *  This triggers some UI changes, and should
	 *  be called on the UI thread.
	 *  @param supply The new supply to display. */
	private void setSupply(Supply supply) {
		this.supply = supply;
        sampleSupply = supply.sample;
		// Start loading the supply
        getSupportLoaderManager()
                .restartLoader(LoaderId.SUPPLY_CARDS, null, cardLoader);
        // Now that we have a supply, redo the action bar
		supportInvalidateOptionsMenu();
	}


    /** Used by subclasses to access the activity context */
    private ActivitySupply getActivity() {
        return this;
    }


    @Override
    public void prefChanged(String key) {
        switch(key) {
            case Prefs.FILT_LANG: case Prefs.SORT_CARD:
                getSupportLoaderManager().restartLoader(LoaderId.SUPPLY_CARDS, null, cardLoader);
                break;
        }
    }


    /** Used to load the cards once the supply is loaded. */
    private class CardLoader implements LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // Display the loading icon, hide the resources
            resView.setVisibility(View.GONE);
            adapter.changeCursor(null);

            // Basic loader
            CursorLoader c = new CursorLoader(getActivity());
            c.setUri(Provider.URI_CARD_ALL);
            c.setProjection(AdapterCardsSupply.COLS_USED);
            c.setSortOrder(Prefs.sort_card);

            // Selection string (sql WHERE clause)
            // _id IN (1,2,3,4)
            String cards = TableCard._ID+" IN ("+ Utils.join(",",supply.cards)+")";
            c.setSelection("("+cards+") AND "+ Prefs.filt_lang);

            return c;
        }


        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // In case the load finishes as the app closes
            if(data == null) return;

            // display the supply cards
            adapter.changeCursor(data);
            adapter.setBane(supply.bane);

            // display the resource cards
            String output = "";
            if(supply.high_cost)
                output += getString(R.string.supply_colonies);
            if(supply.shelters)
                output += "\n" + getString(R.string.supply_shelters);
            output = output.trim();
            if(! "".equals(output)) {
                resView.setText(output);
                resView.setVisibility(View.VISIBLE);
            }
        }


        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if(loader == null) return;
            adapter.changeCursor(null);
            resView.setVisibility(View.GONE);
        }
    }


    /** Used to load the supply from an id */
    private class SupplyLoader implements LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // Display the loading icon, hide the resources
            resView.setVisibility(View.GONE);
            adapter.changeCursor(null);

            // Basic loader
            CursorLoader c = new CursorLoader(getActivity());
            c.setProjection(new String[]{TableSupply._ID, TableSupply._NAME, TableSupply._BANE,
                                         TableSupply._HIGH_COST, TableSupply._SHELTERS, TableSupply._CARDS});
            
            // Load from the history table
            long supply_id = args.getLong(PARAM_HISTORY_ID, -1);
            if(supply_id != -1) {
                c.setUri(Provider.URI_HIST);
                c.setSelection(TableSupply._ID + "=?");
                c.setSelectionArgs(new String[]{"" + supply_id});
                return c;
            }
            // Load from the sample table
            supply_id = args.getLong(PARAM_SUPPLY_ID, -1);
            if(supply_id != -1) {
                c.setUri(Provider.URI_SUPPLY);
                c.setSelection(TableSupply._ID+"=? AND "+Prefs.filt_lang);
                c.setSelectionArgs(new String[]{""+supply_id});
                return c;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(data == null || data.getCount() < 1) return;

            // Build the supply object
            data.moveToFirst();
            Supply s = new Supply(data);
            s.sample = sampleSupply;

            // Display the appropriate title
            ActionBar bar = getSupportActionBar();
            if(bar != null && s.name != null)
                bar.setTitle(s.name);

            // Finish up
            setSupply(s);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    }

    /** Used to ask for the name of the new favorite. */
    private class FavDialog extends QueryDialogBuilder
                            implements QueryListener {
        final private TextView txt;
        final private Context mContext;

        public FavDialog(Context context) {
            super(context);
            mContext = context;
            setPositiveButton(R.string.save);
            setNegativeButton(android.R.string.no);
            setQueryListener(this);
            View v = View.inflate(context, R.layout.dialog_favorite, null);
            txt = (TextView) v.findViewById(R.id.name);
            setView(v);
        }

        @Override
        public void onDialogClose(boolean save) {
            if(!save) return;
            if(txt == null) return;

            // Get the name, default to a time if blank
            String name = "" + txt.getText();
            if(name.length() < 1)
                name = tFormat.formatLong(supply.time);

            // Display the new name, switch display to favorite.
            supply.name = name;
            ActionBar bar = getSupportActionBar();
            if(bar != null) bar.setTitle(name);
            invalidateOptionsMenu();

            // Save the new name to the database.
            ContentValues values = new ContentValues();
            values.put(TableSupply._NAME, name);
            mContext.getContentResolver()
                    .update(Provider.URI_HIST, values,
                            TableSupply._ID+"=?", new String[]{""+supply.time});
        }
    }
}