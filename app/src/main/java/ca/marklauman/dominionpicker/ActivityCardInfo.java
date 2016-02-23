package ca.marklauman.dominionpicker;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import ca.marklauman.dominionpicker.cardadapters.InfoTextView;
import ca.marklauman.dominionpicker.cardadapters.imagefactories.CardColorFactory;
import ca.marklauman.dominionpicker.cardadapters.imagefactories.CoinFactory;
import ca.marklauman.dominionpicker.community.EmailButton;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.tools.Utils;

/** Activity used to display detailed card information.
 *  This goes into detail on ONE card. No other cards are shown.
 *  @author Mark Lauman */
public class ActivityCardInfo extends AppCompatActivity
                              implements LoaderManager.LoaderCallbacks<Cursor>, Prefs.Listener {
    /** Extra used to pass the card id to this activity */
    public static final String PARAM_ID = "card_id";
    /** Columns used by the loader */
    private static final String[] COLS_USED
            = {TableCard._ID, TableCard._NAME, TableCard._COST, TableCard._POT, TableCard._TYPE,
               TableCard._SET_ID, TableCard._SET_NAME, TableCard._TEXT, TableCard._RULES,
               TableCard._LANG,
               TableCard._TYPE_ACT, TableCard._TYPE_TREAS, TableCard._TYPE_VICTORY, // colorFactory
               TableCard._TYPE_DUR, TableCard._TYPE_REACT, TableCard._TYPE_RESERVE, // required
               TableCard._TYPE_CURSE, TableCard._TYPE_EVENT, TableCard._TYPE_ATK};  // rows


    /** Icons used for the individual expansions. */
    private int[] expIcons;
    /** Used to generate the card color. */
    private CardColorFactory colorFactory;
    /** Used to generate the cost icon in the bottom left. */
    private CoinFactory coinFactory;
    /** Maps card language to the correct coin description */
    private HashMap<String, Integer> coinDesc;

    /** View used to show the loaded card */
    private View loaded;
    /** View used to show that the card is loading */
    private View loading;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Prefs.setup(this);

        // Set up the view
        setContentView(R.layout.activity_card_info);
        loaded = findViewById(R.id.loaded);
        loading = findViewById(R.id.loading);
        loaded.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setDisplayHomeAsUpEnabled(true);

        // Set up the icon providers
        Resources res = getResources();
        coinFactory = new CoinFactory(res);
        colorFactory = new CardColorFactory(res);
        expIcons = Utils.getResourceArray(this, R.array.card_set_icons);

        // Load the coin descriptions
        int[] form_coin = Utils.getResourceArray(this, R.array.format_coin);
        String[] lang = res.getStringArray(R.array.language_codes);
        coinDesc = new HashMap<>(lang.length);
        for(int i=0; i<lang.length; i++)
            coinDesc.put(lang[i], form_coin[i]);

        // Start to load the card
        getSupportLoaderManager().restartLoader(LoaderId.INFO_CARD, null, this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(android.R.id.home == item.getItemId()) {
            finish();
            return true;
        } else return super.onOptionsItemSelected(item);
    }


    @Override
    public void prefChanged(String key) {
        if(!Prefs.FILT_LANG.equals(key)) return;
        getSupportLoaderManager().restartLoader(LoaderId.INFO_CARD, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader c = new CursorLoader(this);
        c.setUri(Provider.URI_CARD_ALL);
        c.setProjection(COLS_USED);
        long card = getIntent().getLongExtra(PARAM_ID, -1);
        c.setSelection(TableCard._ID+"="+card+" AND "+Prefs.filt_lang);
        return c;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null || data.getCount() == 0) {
            Log.wtf("ca.marklauman.dominionpicker.ActivityCardInfo",
                    "There is no card "+getIntent().getLongExtra(PARAM_ID, -1));
            return;
        }
        data.moveToFirst();
        Resources res = getResources();

        // Basic text mapping
        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setTitle(data.getString(data.getColumnIndex(TableCard._NAME)));
        TextView txtView = (TextView)findViewById(R.id.card_type);
        txtView.setText(data.getString(data.getColumnIndex(TableCard._TYPE)));

        // Text info
        InfoTextView info = (InfoTextView)findViewById(android.R.id.text1);
        String txt = data.getString(data.getColumnIndex(TableCard._TEXT));
        // Apply the descriptive text if its there
        if(txt != null && !"".equals(txt))
            info.setText(data.getString(data.getColumnIndex(TableCard._TEXT)),
                         data.getString(data.getColumnIndex(TableCard._LANG)));
        else {
            // It isn't there, show the "no info" panel
            info.setVisibility(View.GONE);
            setupEmail(findViewById(R.id.card_no_info), res,
                       data.getLong(data.getColumnIndex(TableCard._ID)),
                       data.getString(data.getColumnIndex(TableCard._LANG)));
        }

        // Potion cost
        ImageView imgView = (ImageView)findViewById(R.id.card_potion);
        String cost = data.getString(data.getColumnIndex(TableCard._POT));
        boolean potion = !"0".equals(cost);
        if(potion) imgView.setVisibility(View.VISIBLE);
        else imgView.setVisibility(View.GONE);

        // Cost Image
        imgView = (ImageView)findViewById(R.id.card_cost);
        cost = data.getString(data.getColumnIndex(TableCard._COST));
        if("0".equals(cost) && potion) {
            imgView.setVisibility(View.GONE);
        } else {
            imgView.setVisibility(View.VISIBLE);
            imgView.setImageDrawable(coinFactory.getDrawable(cost,
                    res.getDimensionPixelSize(R.dimen.card_info_bottom)));
        }

        // Cost Description
        int descRes = coinDesc.get(data.getString(data.getColumnIndex(TableCard._LANG)));
        imgView.setContentDescription(res.getQuantityString(descRes,
                                                            TableCard.parseVal(cost),
                                                            cost));

        // Card color
        colorFactory.changeCursor(data);
        colorFactory.updateBackground(findViewById(R.id.card_color), data);

        // Name of the card set
        String setName = data.getString(data.getColumnIndex(TableCard._SET_NAME));
        txtView = (TextView)findViewById(R.id.card_set_name);
        txtView.setText(setName);
        imgView = (ImageView)findViewById(R.id.card_set);
        imgView.setContentDescription(setName);

        // Icon of the card set
        int expIcon = R.drawable.ic_set_unknown;
        try { expIcon = expIcons[data.getInt(data.getColumnIndex(TableCard._SET_ID))];
        } catch(Exception ignored){}
        imgView.setImageResource(expIcon);

        // Show the card
        loading.setVisibility(View.GONE);
        loaded.setVisibility(View.VISIBLE);
    }

    /** Configure the email panel */
    private void setupEmail(View vNoInfo, Resources res, long cardId, String lang) {
        // Ge the id of the current language
        int langId = 0;
        String[] langCodes = res.getStringArray(R.array.language_codes);
        while(! lang.equals(langCodes[langId]))
            langId++;

        vNoInfo.setVisibility(View.VISIBLE);
        String str = res.getStringArray(R.array.card_no_info)[langId];
        ((TextView)vNoInfo.findViewById(R.id.card_no_info_msg))
                          .setText(str);

        EmailButton button = (EmailButton)vNoInfo.findViewById(R.id.email);
        button.setSubject(String.format(res.getString(R.string.card_mail_subject),
                                        cardId, lang));
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}
