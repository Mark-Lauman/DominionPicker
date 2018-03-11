package ca.marklauman.dominionpicker;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.marklauman.dominionpicker.userinterface.InfoTextView;
import ca.marklauman.dominionpicker.userinterface.icons.IconDescriber;
import ca.marklauman.dominionpicker.userinterface.icons.PriceIcon;
import ca.marklauman.dominionpicker.userinterface.imagefactories.CardColorFactory;
import ca.marklauman.dominionpicker.community.EmailButton;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.settings.Pref;
import ca.marklauman.tools.Utils;

/** Activity used to display detailed card information.
 *  This goes into detail on ONE card. No other cards are shown.
 *  @author Mark Lauman */
public class ActivityCardInfo extends AppCompatActivity
                              implements LoaderManager.LoaderCallbacks<Cursor>, Pref.Listener {

    /** Extra used to pass the card id to this activity */
    public static final String PARAM_ID = "card_id";
    /** Columns used by the loader */
    private static final String[] COLS_USED
            = {TableCard._ID, TableCard._NAME, TableCard._COST, TableCard._COST_VAL,
               TableCard._DEBT, TableCard._POT, TableCard._TYPE, TableCard._SET_ID,
               TableCard._SET_NAME, TableCard._TEXT, TableCard._RULES, TableCard._LANG,
               TableCard._TYPE_ACT, TableCard._TYPE_TREAS, TableCard._TYPE_VICTORY, // colorFactory
               TableCard._TYPE_DUR, TableCard._TYPE_REACT, TableCard._TYPE_RESERVE, // required rows
               TableCard._TYPE_CURSE, TableCard._TYPE_EVENT, TableCard._TYPE_LANDMARK};


    /** Icons used for the individual expansions. */
    private int[] expIcons;
    /** Used to generate the card color. */
    private CardColorFactory colorFactory;
    /** Holds the price of the card */
    private PriceIcon price;

    /** Actionbar for this activity */
    private ActionBar actionBar;
    /** View used to show the loaded card */
    @BindView(R.id.loaded)        View vLoaded;
    /** View used to show that the card is loading */
    @BindView(R.id.loading)       View vLoading;
    /** Color of the card */
    @BindView(R.id.card_color)    View vColor;
    /** Type of the card. */
    @BindView(R.id.card_type)     TextView vType;
    /** Used to display the card details */
    @BindView(android.R.id.text1) InfoTextView vInfo;
    /** Displayed if there is no info for this card. */
    @BindView(R.id.card_no_info)  View vNoInfo;
    /** View holding the set's name */
    @BindView(R.id.card_set_name) TextView vSetName;
    /** View holding the set's icon */
    @BindView(R.id.card_set)      ImageView vSetIcon;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pref.checkLanguage(this);

        // Set up the view
        setContentView(R.layout.activity_card_info);
        ButterKnife.bind(this);
        IconDescriber describer = new IconDescriber(this);

        vInfo.setDescriber(describer);
        price = new PriceIcon(this, describer);
        price.setHeight((int)(-vType.getPaint().ascent() + 0.5f));
        vLoaded.setVisibility(View.GONE);
        vLoading.setVisibility(View.VISIBLE);
        actionBar = getSupportActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up the icon providers
        colorFactory = new CardColorFactory(this);
        expIcons = Utils.getResourceArray(this, R.array.card_set_icons);

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
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if(!Pref.COMP_LANG.equals(key)) return;
        getSupportLoaderManager().restartLoader(LoaderId.INFO_CARD, null, this);
    }


    @Override @NonNull
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader c = new CursorLoader(this);
        c.setUri(Provider.URI_CARD_ALL);
        c.setProjection(COLS_USED);
        long card = getIntent().getLongExtra(PARAM_ID, -1);
        c.setSelection(TableCard._ID+"="+card+" AND "+Pref.languageFilter(this));
        return c;
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(data == null || !data.moveToFirst()) {
            Log.wtf("ca.marklauman.dominionpicker.ActivityCardInfo",
                    "There is no card "+getIntent().getLongExtra(PARAM_ID, -1));
            return;
        }
        Resources res = getResources();

        // Name of the card
        if(actionBar != null) actionBar.setTitle(getString(data, TableCard._NAME));

        // The text on the card
        String txt = getString(data, TableCard._TEXT);
        if(txt == null || "".equals(txt)) {
            vInfo.setVisibility(View.GONE);
            setupEmail(vNoInfo, res, getId(data),
                                     getString(data, TableCard._LANG));
        } else vInfo.setText(getString(data, TableCard._TEXT),
                             getString(data, TableCard._LANG));

        // Type and price of the card
        SpannableStringBuilder span = new SpannableStringBuilder(getString(data, TableCard._TYPE));
        price.setValue(getString(data, TableCard._COST),
                       getInt(data, TableCard._DEBT),
                       getInt(data, TableCard._POT),
                       getInt(data, TableCard._TYPE_LANDMARK));
        String desc = price.getDescription(getString(data, TableCard._LANG));
        span.insert(0, desc+" ");
        span.setSpan(new ImageSpan(price, ImageSpan.ALIGN_BASELINE), 0 , desc.length(), 0);
        vType.setText(span);

        // Card color
        colorFactory.changeCursor(data);
        colorFactory.updateBackground(vColor, data);

        // Name of the card set
        String setName = getString(data, TableCard._SET_NAME);
        vSetName.setText(setName);

        // Icon of the card set
        int expIcon = R.drawable.ic_set_unknown;
        try { expIcon = expIcons[getInt(data, TableCard._SET_ID)];
        } catch(Exception ignored){}
        vSetIcon.setImageResource(expIcon);

        // Show the card
        vLoading.setVisibility(View.GONE);
        vLoaded.setVisibility(View.VISIBLE);
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

        EmailButton button = vNoInfo.findViewById(R.id.email);
        button.setSubject(String.format(res.getString(R.string.card_mail_subject),
                                        cardId, lang));
    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}


    /** Extract a string from the cursor.
     *  @param cursor The cursor to extract the string from.
     *  @param columnName The name of the column you're looking for. */
    private static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    /** Extract an id form the cursor.
     *  @param cursor The cursor to extract the long from. */
    private static long getId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndex(TableCard._ID));
    }

    /** Extract an integer from the cursor.
     *  @param cursor The cursor to extract the integer from.
     *  @param columnName The name of the column you're looking for. */
    private static int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }
}