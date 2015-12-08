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

import java.util.HashMap;
import java.util.Set;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.tools.SingleItemSelector;
import ca.marklauman.tools.Utils;

/** Governs the Card Language setting screen.
 *  Allows users to set which languages each card set will use.
 *  @author Mark Lauman */
public class CardLanguageSelector extends AppCompatActivity
                                  implements LoaderCallbacks<Cursor>, ListView.OnItemClickListener {

    /** The ListView used to display the language preferences. */
    private ListView list;
    /** The adapter containing all the preferences. */
    private PrefAdapter adapter;
    /** Maps set ids to their preference objects. */
    private LangPreference[] prefMap;

    /** Language codes sorted in display order. */
    private String[] languageCodes;
    /** Maps language codes to their display names. */
    private HashMap<String, String> languageNames;

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

        // Load the language codes and names
        final Resources res = getResources();
        languageCodes = res.getStringArray(R.array.language_codes);
        final String[] keyNames = res.getStringArray(R.array.language_names);
        languageNames = new HashMap<>(languageCodes.length);
        for(int i=0; i< languageCodes.length; i++)
            languageNames.put(languageCodes[i], keyNames[i]);

        // Load value of this preference
        prefVal = PreferenceManager.getDefaultSharedPreferences(this)
                                   .getString(Prefs.FILT_LANG,
                                              res.getString(R.string.filt_lang_def))
                                   .split(",");

        // Start to load the order of the sets.
        getSupportLoaderManager().initLoader(LoaderId.LANG_ORDER, null, this);
    }


    /** Create the preferences and adapter with the given sort order.
     *  @param sortOrder Cursor containing set_ids sorted as they should be displayed. */
    private void createPreferences(Cursor sortOrder) {
        // Load the necessary resources
        final int[] icons = Utils.getResourceArray(this, R.array.card_set_icons);
        final String[] def_trans = getResources().getStringArray(R.array.def_trans);

        // Create an array of preferences (for the adapter) and the prefMap
        int num_sets = sortOrder.getCount();
        LangPreference[] pref = new LangPreference[num_sets];
        prefMap = new LangPreference[num_sets];
        int col_id = sortOrder.getColumnIndex(TableCard._SET_ID);

        // Create all the preferences and add them to the map
        sortOrder.moveToFirst();
        int id, pos = 0;
        do {
            id = sortOrder.getInt(col_id);
            pref[pos] = new LangPreference();
            pref[pos].set_id = id;
            pref[pos].icon = icons[id];
            pref[pos].def = def_trans[id];
            pref[pos].val = prefVal[id];
            prefMap[id] = pref[pos];
            pos++;
        } while(sortOrder.moveToNext());

        // Create the adapter. Do not assign it to the ListView
        // until we have loaded the choices for each preference.
        adapter = new PrefAdapter(this, pref);
        // Start loading the choices
        getSupportLoaderManager().initLoader(LoaderId.LANG_CHOICES, null, this);
    }


    /** Set the choices available for each expansion, and attach the adapter to the ListView.
     *  @param choices Cursor covering the choices available.
     *                 Must include the {@link TableCard#_SET_ID}, {@link TableCard#_SET_NAME}
     *                 and {@link TableCard#_LANG} columns. */
    private void setChoices(Cursor choices) {
        // Column indexes
        int _id = choices.getColumnIndex(TableCard._SET_ID);
        int _lang = choices.getColumnIndex(TableCard._LANG);
        int _name = choices.getColumnIndex(TableCard._SET_NAME);

        // Copy the choices to their preferences.
        choices.moveToFirst();
        do {
            int set_id = choices.getInt(_id);
            prefMap[set_id].choices.put(choices.getString(_lang),
                                       choices.getString(_name));
        } while(choices.moveToNext());

        // Apply the adapter.
        list.setAdapter(adapter);
    }


    /** Close the Activity when the back button is pressed on the ActionBar. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item == null || item.getItemId() != android.R.id.home)
            return false;
        finish();
        return true;
    }


    /** Start loading the set order or language options */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id) {
            case LoaderId.LANG_ORDER:
                return new CursorLoader(this, Provider.URI_CARD_SET,
                                        new String[]{TableCard._SET_ID},
                                        TableCard._LANG+"=?", new String[]{"en"},
                                        TableCard._PROMO+", "+TableCard._RELEASE);
            case LoaderId.LANG_CHOICES:
                return new CursorLoader(this, Provider.URI_CARD_SET,
                                        new String[]{TableCard._SET_ID, TableCard._LANG,
                                        TableCard._SET_NAME}, null, null, null);
        }
        return null;
    }


    /** Pass the cursors to the appropriate methods when they finish loading */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch(loader.getId()) {
            case LoaderId.LANG_ORDER: createPreferences(data); break;
            case LoaderId.LANG_CHOICES: setChoices(data);
        }
    }


    /** Cursors are not retained */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}


    /** When a preference is clicked, launch a selector to choose that set's language */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LangPreference pref = adapter.getPreference(position);

        // Setup the choice array, add the "default" option
        Set<String> choices = pref.choices.keySet();
        String[] langCodes = new String[choices.size()+1];
        String[] langNames = new String[langCodes.length];
        langCodes[0] = "0";
        langNames[0] = getString(R.string.system_default);

        // Load the languages available for this set
        int i = 1;
        for(String code : languageCodes) {
            if(choices.contains(code)) {
                langCodes[i] = code;
                langNames[i] = languageNames.get(code);
                i++;
            }
        }

        // Launch a SingleItem selector to choose the language for this set.
        Intent intent = new Intent(this, SingleItemSelector.class);
        intent.putExtra(SingleItemSelector.PARAM_TITLE, pref.name);
        intent.putExtra(SingleItemSelector.PARAM_RETURN, langCodes);
        intent.putExtra(SingleItemSelector.PARAM_DISPLAY, langNames);
        startActivityForResult(intent, pref.set_id);
    }


    /** When a new language is chosen, apply the change. */
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
        prefMap[requestCode].val = res;
        adapter.notifyDataSetChanged();
        list.invalidate();
        prefVal[requestCode] = res;
        String out = Utils.join(",", prefVal);
        PreferenceManager.getDefaultSharedPreferences(this)
                         .edit()
                         .putString(Prefs.FILT_LANG, out)
                         .commit();
    }


    /** Simple class containing all data needed for one preference */
    private class LangPreference {
        /** Current name on display (set by the adapter) */
        private String name;
        /** The icon used for this preference */
        public int icon;
        /** The internal set_id. */
        public int set_id;
        /** The default language code. */
        public String def;
        /** The current value */
        public String val;
        /** Available choices of val, mapped to their display counterparts. */
        public HashMap<String, String> choices = new HashMap<>();
    }


    /** Adapter used to display the preferences */
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
            // View setup
            if(convertView == null)
                convertView = View.inflate(getContext(), R.layout.card_language_pref, null);

            // Load preference state
            LangPreference pref = values[position];
            String lang; int back;
            if(pref.val.startsWith("0")) {
                lang = pref.def; back = android.R.color.transparent;
            } else {
                lang = pref.val; back = R.color.card_list_select;
            }
            pref.name = pref.choices.get(lang);

            // Apply preference state to views
            convertView.setBackgroundColor(ContextCompat.getColor(getContext(), back));
            TextView txt = (TextView) convertView.findViewById(android.R.id.text1);
            txt.setText(pref.name);
            txt = (TextView) convertView.findViewById(android.R.id.text2);
            txt.setText(languageNames.get(values[position].val));
            ImageView icon = (ImageView) convertView.findViewById(android.R.id.icon2);
            icon.setImageResource(pref.icon);

            return convertView;
        }
    }
}