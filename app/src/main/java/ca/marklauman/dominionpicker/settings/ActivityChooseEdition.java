package ca.marklauman.dominionpicker.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.userinterface.recyclerview.AdapterSortCard;
import ca.marklauman.tools.SingleItemSelector;
import ca.marklauman.tools.Utils;
import ca.marklauman.tools.recyclerview.ListDivider;


public class ActivityChooseEdition extends AppCompatActivity
                                    implements LoaderManager.LoaderCallbacks<Cursor>, ListView.OnItemClickListener {
    /**
     * The ListView used to display the language preferences.
     */
    private ListView list;
    /**
     * The adapter containing all the preferences.
     */
    private ActivityChooseEdition.PrefAdapter adapter;
    /**
     * Maps set ids to their preference objects.
     */
    private ActivityChooseEdition.ExpansionEditionsPreference[] prefMap;

    /**
     * Value actually saved to preferences
     */
    private Integer[] prefVal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // View and action bar setup
        list = new ListView(this);
        list.setOnItemClickListener(this);
        setContentView(list);
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

        final Resources res = getResources();

        // Load value of this preference
        String val[] = Pref.get(this)
                .getString(Pref.EXPANSION_EDITIONS,
                        res.getString(R.string.expansion_editions_def))
                .split(",");
        prefVal = new Integer[val.length];
        for (int i = 0; i < val.length; i++) {
            try {
                prefVal[i] = Integer.parseInt(val[i]);
            } catch (NumberFormatException ignored)  {
                prefVal[i] = 1; //If we cannot parse the default values, set it to 1 (1st edition)
            }
        }

        // Start to load the order of the sets.
        getSupportLoaderManager().initLoader(LoaderId.EXP_EDITIONS_ORDER, null, this);
    }


    /**
     * Create the preferences and adapter with the given sort order.
     *
     * @param sortOrder Cursor containing set_ids sorted as they should be displayed.
     */
    private void createPreferences(Cursor sortOrder) {
        // Load the necessary resources
        final int[] icons = Utils.getResourceArray(this, R.array.card_set_icons);

        String def_exp_list = getResources().getString(R.string.expansion_editions_def);
        String def_exp_arr[] = def_exp_list.split(",");
        Integer def_exp_editions[] = new Integer[def_exp_arr.length];
        for (int i = 0; i < def_exp_arr.length; i++) {
            try {
                def_exp_editions[i] = Integer.parseInt(def_exp_arr[i]);
            } catch (NumberFormatException ignored)  {
                def_exp_editions[i] = 1; //If we cannot parse the default values, set it to 1 (1st edition)
            }
        }

        // Create an array of preferences (for the adapter) and the prefMap
        int num_items = sortOrder.getCount();
        prefMap = new ActivityChooseEdition.ExpansionEditionsPreference[num_items];
        ExpansionEditionsPreference pref[] = new ExpansionEditionsPreference[num_items];

        int col_id = sortOrder.getColumnIndex(TableCard._SET_ID);
        int _name = sortOrder.getColumnIndex(TableCard._SET_NAME);
        int _lang = sortOrder.getColumnIndex(TableCard._LANG);

        HashMap<Integer, String> setNames = new HashMap<>();
        String expLangPref[] = Pref.get(this).getString(Pref.FILT_LANG, getResources().getString(R.string.filt_lang_def)).split(",");
        String languageCodes[] = languageCodes = getResources().getStringArray(R.array.language_codes);

        // Create all the preferences and add them to the map
        sortOrder.moveToFirst();
        int id, pos = 0;
        do {
            id = sortOrder.getInt(col_id);
            pref[pos] = new ActivityChooseEdition.ExpansionEditionsPreference();
            pref[pos].set_id = id;
            pref[pos].icon = icons[id];
            pref[pos].def = def_exp_editions[id];
            pref[pos].val = prefVal[id];
            pref[pos].name = sortOrder.getString(_name);
            prefMap[id] = pref[pos];
            pos++;

        } while (sortOrder.moveToNext());




       // prefMap = pref.toArray(new ActivityChooseEdition.ExpansionEditionsPreference[pref.size()]);

        // Create the adapter. Do not assign it to the ListView
        // until we have loaded the choices for each preference.
        adapter = new ActivityChooseEdition.PrefAdapter(this, pref);
        // Start loading the choices
        getSupportLoaderManager().initLoader(LoaderId.EXP_EDITION_CHOICES, null, this);
    }


    /**
     * Set the choices available for each expansion, and attach the adapter to the ListView.
     *
     * @param choices Cursor covering the choices available.
     *                Must include the {@link TableCard#_SET_ID}, {@link TableCard#_SET_NAME}
     *                and {@link TableCard#_LANG} columns.
     */
    private void setChoices(Cursor choices) {
        // Column indexes
        int _id = choices.getColumnIndex(TableCard._SET_ID);
        int _edition = choices.getColumnIndex(TableCard._EDITION);

        // Copy the choices to their preferences.
        choices.moveToFirst();
        do {
            int set_id = choices.getInt(_id);
            prefMap[set_id].choices.add(choices.getInt(_edition));
        } while (choices.moveToNext());

        // Apply the adapter.
        list.setAdapter(adapter);
    }


    /**
     * Close the Activity when the back button is pressed on the ActionBar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == null || item.getItemId() != android.R.id.home)
            return false;
        finish();
        return true;
    }


    /**
     * Start loading the set order or language options
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LoaderId.EXP_EDITIONS_ORDER:
                return new CursorLoader(this, Provider.URI_CARD_SET,
                        new String[]{TableCard._SET_ID, TableCard._SET_NAME, TableCard._LANG},
                        Pref.languageFilter(this), null,
                        TableCard._PROMO + ", " + TableCard._RELEASE);
            case LoaderId.EXP_EDITION_CHOICES:
                return new CursorLoader(this, Provider.URI_SET_EDITION,
                        new String[]{TableCard._SET_ID, TableCard._EDITION}, null, null, null);
        }
        return null;
    }


    /**
     * Pass the cursors to the appropriate methods when they finish loading
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LoaderId.EXP_EDITIONS_ORDER:
                createPreferences(data);
                break;
            case LoaderId.EXP_EDITION_CHOICES:
                setChoices(data);
                break;
        }
    }


    /**
     * Cursors are not retained
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    /**
     * When a preference is clicked, launch a selector to choose that set's language
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ActivityChooseEdition.ExpansionEditionsPreference pref = adapter.getPreference(position);

        // Setup the choice array, add the "default" option
        String[] editionCodes = new String[pref.choices.size() + 1];
        String[] editionNames = new String[editionCodes.length];
        editionCodes[0] = String.valueOf(getResources().getInteger(R.integer.expansion_editions_all));
        editionNames[0] = getString(R.string.expansion_editions_all_desc);

        // Load the languages available for this set
        int i = 1;
        for (Integer edition : pref.choices) {
            editionCodes[i] = String.valueOf(edition);
            editionNames[i] = getResources().getStringArray(R.array.expansion_edition_names)[edition - 1];
            i++;
        }

        // Launch a SingleItem selector to choose the language for this set.
        Intent intent = new Intent(this, SingleItemSelector.class);
        intent.putExtra(SingleItemSelector.PARAM_TITLE, pref.name);
        intent.putExtra(SingleItemSelector.PARAM_RETURN, editionCodes);
        intent.putExtra(SingleItemSelector.PARAM_DISPLAY, editionNames);
        startActivityForResult(intent, pref.set_id);
    }


    /**
     * When a new language is chosen, apply the change.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Get the result from the SingleItemSelector (discard no result)
        if (resultCode != RESULT_OK || data == null) return;
        Bundle ext = data.getExtras();
        if (ext == null) return;
        String res = ext.getString(SingleItemSelector.RES_CODE);
        if (res == null) return;

        // We don't need to worry if there is no change.
        if (prefVal[requestCode].equals(res)) return;

        // Otherwise we update the preference and display
        try { prefMap[requestCode].val = Integer.parseInt(res); } catch (NumberFormatException ignored) {}
        adapter.notifyDataSetChanged();
        list.invalidate();
        try {
            prefVal[requestCode] = Integer.parseInt(res);
            String out = Utils.join(",", prefVal);
            Pref.edit(this)
                    .putString(Pref.EXPANSION_EDITIONS, out)
                    .commit();
        }  catch (NumberFormatException ignored) {}

    }

    /**
     * Simple class containing all data needed for one preference
     */
    private class ExpansionEditionsPreference {
        /**
         * Current name on display (set by the adapter)
         */
        private String name;
        /**
         * The icon used for this preference
         */
        public int icon;
        /**
         * The internal set_id.
         */
        public int set_id;
        /**
         * The default edition.
         */
        public int def;
        /**
         * The current value
         */
        public int val;
        /**
         * Available choices of val, mapped to their display counterparts.
         */
        final public Set<Integer> choices = new HashSet<>();
    }


    /**
     * Adapter used to display the preferences
     */
    private class PrefAdapter extends ArrayAdapter<ActivityChooseEdition.ExpansionEditionsPreference> {
        /**
         * The values on display
         */
        final private ActivityChooseEdition.ExpansionEditionsPreference[] values;

        public PrefAdapter(Context c, ActivityChooseEdition.ExpansionEditionsPreference[] values) {
            super(c, R.layout.card_language_pref, values);
            this.values = values;
        }

        public ActivityChooseEdition.ExpansionEditionsPreference getPreference(int position) {
            return values[position];
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // View setup
            if (convertView == null)
                convertView = View.inflate(getContext(), R.layout.card_language_pref, null);

            // Load preference state
            ActivityChooseEdition.ExpansionEditionsPreference pref = values[position];

            TextView txt = (TextView) convertView.findViewById(android.R.id.text1);
            txt.setText(pref.name);
            txt = (TextView) convertView.findViewById(android.R.id.text2);

            int val = values[position].val;
            if (val == getResources().getInteger(R.integer.expansion_editions_all))
                txt.setText(getResources().getString(R.string.expansion_editions_all_desc));
            else
                txt.setText(getResources().getStringArray(R.array.expansion_edition_names)[values[position].val - 1]);

            ImageView icon = (ImageView) convertView.findViewById(android.R.id.icon2);
            icon.setImageResource(pref.icon);

            return convertView;
        }
    }
}
