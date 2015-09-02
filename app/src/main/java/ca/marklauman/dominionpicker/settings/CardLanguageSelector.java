package ca.marklauman.dominionpicker.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.CardDb;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.tools.SingleItemSelector;
import ca.marklauman.tools.Utils;

/** Governs the Card Language setting screen.
 *  Allows users to set which languages each card set will use.
 *  @author Mark Lauman */
public class CardLanguageSelector extends AppCompatActivity
                                  implements LoaderCallbacks<Cursor>, ListView.OnItemClickListener {
    /** Set of all language codes used by this selector */
    private String[] language_codes;
    /** Maps language codes to display names. */
    private HashMap<String, String> languages;
    /** The ListView used to display the preferences. */
    private ListView list;
    /** The adapter containing all the preferences. */
    private PrefAdapter adapter;
    /** Maps set ids to their preference objects. */
    private HashMap<Integer, LangPreference> setMap;
    /** Value actually saved to preferences */
    private String[] prefVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // View and action bar setup
        list = new ListView(this);
        list.setOnItemClickListener(this);
        setContentView(list);
        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setDisplayHomeAsUpEnabled(true);

        // Load the language display strings
        final Resources res = getResources();
        language_codes = res.getStringArray(R.array.language_codes);
        final String[] keyNames = res.getStringArray(R.array.language_names);
        languages = new HashMap<>(language_codes.length);
        for(int i=0; i<language_codes.length; i++)
            languages.put(language_codes[i], keyNames[i]);

        // Load values of the preferences
        String[] set_names = res.getStringArray(R.array.filter_set);
        int[] icons = Utils.getDrawableResources(this, R.array.card_set_icons);
        int[] set_ids = res.getIntArray(R.array.filter_set_ids);
        prefVal = PreferenceManager.getDefaultSharedPreferences(this)
                                   .getString(Prefs.FILT_LANG,
                                           res.getString(R.string.filt_lang_def))
                                   .split(",");

        // Setup the preferences and the id map.
        LangPreference[] preferences = new LangPreference[set_names.length];
        setMap = new HashMap<>(set_ids.length);
        for(int i=0; i<set_names.length; i++) {
            preferences[i] = new LangPreference();
            preferences[i].name = set_names[i];
            preferences[i].set_id = set_ids[i];
            preferences[i].icon = icons[set_ids[i]];
            preferences[i].val = prefVal[set_ids[i]];
            setMap.put(set_ids[i], preferences[i]);
        }

        // Adapter is not assigned to the view until the choices are loaded.
        adapter = new PrefAdapter(this, preferences);
        // Load the choices for each set
        getSupportLoaderManager().initLoader(LoaderId.LANGUAGE, null, this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() != android.R.id.home)
            return false;
        finish();
        return true;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort by set_id then language_codes order
        String sort_by = CardDb._SET_ID+", CASE "+CardDb._LANG;
        for(int i=0;i<language_codes.length; i++)
            sort_by += " WHEN '"+language_codes[i]+"' THEN "+i;
        sort_by += " END";
        // Get all set_id and language combinations in the card table.
        return new CursorLoader(this, Provider.URI_CARD_ALL,
                                new String[]{CardDb._SET_ID, CardDb._LANG},
                                CardDb._LANG+" NOT NULL GROUP BY "
                                +CardDb._SET_ID+", "+CardDb._LANG, null, sort_by);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null) {
            list.setAdapter(adapter);
            return;
        }

        // Loop setup
        final int col_set_id = data.getColumnIndex(CardDb._SET_ID);
        final int col_lang = data.getColumnIndex(CardDb._LANG);
        int set_id;
        String[] options;
        ArrayList<String> available = new ArrayList<>(languages.size());

        // First loop
        data.moveToFirst();
        set_id = data.getInt(col_set_id);
        available.add("0");
        available.add(data.getString(col_lang));

        // Subsequent loops.
        while(data.moveToNext()) {
            // If the set id has changed, we are out of languages for this set.
            if(set_id != data.getInt(col_set_id)) {
                // Add these options to the appropriate preference
                options = new String[available.size()];
                available.toArray(options);
                setMap.get(set_id).opt = options;
                // move to the next set id
                set_id = data.getInt(col_set_id);
                available.clear();
                available.add("0");
            }

            // add this row's language to the loaded languages
            available.add(data.getString(col_lang));
        }

        // Final set
        options = new String[available.size()];
        available.toArray(options);
        setMap.get(set_id).opt = options;

        // Activate the adapter
        list.setAdapter(adapter);
        data.close();
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LangPreference pref = adapter.getPreference(position);

        // Launch a SingleItem selector to choose the language for this set.
        Intent intent = new Intent(this, SingleItemSelector.class);
        intent.putExtra(SingleItemSelector.PARAM_TITLE, pref.name);
        intent.putExtra(SingleItemSelector.PARAM_RETURN, pref.opt);
        String[] displayNames = new String[pref.opt.length];
        for(int i=0; i<displayNames.length; i++)
            displayNames[i] = languages.get(pref.opt[i]);
        intent.putExtra(SingleItemSelector.PARAM_DISPLAY, displayNames);
        startActivityForResult(intent, pref.set_id);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Get the result from the SingleItemSelector (discard no result)
        if(resultCode != RESULT_OK || data == null) return;
        Bundle ext = data.getExtras();
        if(ext == null) return;
        String res = ext.getString(SingleItemSelector.RES_CODE);
        if(res == null) return;

        // We don't need to worry if there is no change.
        if(prefVal[requestCode].equals(res)) return;

        // Otherwise we update the preference and display
        setMap.get(requestCode).val = res;
        adapter.notifyDataSetChanged();
        list.invalidate();
        prefVal[requestCode] = res;
        String out = Utils.join(",", prefVal);
        PreferenceManager.getDefaultSharedPreferences(this)
                         .edit()
                         .putString(Prefs.FILT_LANG, out)
                         .commit();
    }


    private class LangPreference {
        /** The display string. */
        public String name;
        /** The icon used for this preference */
        public int icon;
        /** The internal set_id. */
        public int set_id;
        /** The current value */
        public String val;
        /** Options available for this translation */
        public String[] opt;
    }


    private class PrefAdapter extends ArrayAdapter<LangPreference> {
        /** The values on display */
        final private LangPreference[] values;

        public PrefAdapter(Context c, LangPreference[] values) {
            super(c, R.layout.card_language_pref, values);
            this.values = values;
        }

        public LangPreference getPreference(int position) {
            return values[position];
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
                convertView = View.inflate(getContext(), R.layout.card_language_pref, null);

            // Overridden items have grey backgrounds
            if(values[position].val.startsWith("0"))
                convertView.setBackgroundColor(ContextCompat.getColor(getContext(),
                                                                      android.R.color.transparent));
            else convertView.setBackgroundColor(ContextCompat.getColor(getContext(),
                                                                       R.color.card_list_select));

            ImageView icon = (ImageView) convertView.findViewById(android.R.id.icon2);
            icon.setImageResource(values[position].icon);
            TextView txt = (TextView) convertView.findViewById(android.R.id.text1);
            txt.setText(values[position].name);
            txt = (TextView) convertView.findViewById(android.R.id.text2);
            txt.setText(languages.get(values[position].val));
            return convertView;
        }
    }
}