package ca.marklauman.dominionpicker.rules;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.tools.Utils;
import ca.marklauman.tools.preferences.Preference.PreferenceListener;
import ca.marklauman.tools.preferences.SmallNumberPreference;

/** Adapter used to display the simple rules list.
 *  This adapter directly handles the setting of the {@link Prefs#FILT_CURSE} and
 *  {@link Prefs#FILT_SET} values as well. */
class RulesAdapter extends ArrayAdapter<View>
                   implements OnItemClickListener, PreferenceListener {
    /** Keys updated by this adapter. */
    private static final String[] PREF_KEYS = {Prefs.LIMIT_SUPPLY, Prefs.FILT_SET,
                                               Prefs.LIMIT_EVENTS, Prefs.FILT_CURSE};

    /** The views available for this adapter */
    final private ArrayList<View> views;

    /** SharedPreference object of this context */
    final private SharedPreferences prefs;
    /** The current value of filt_set */
    final private HashSet<Integer> filt_set;

    /** Position of the first expansion in the adapter */
    final private int setStart;
    /** Position of the last expansion in the adapter */
    final private int setEnd;
    /** Position of the first promo in this adapter */
    final private int promoStart;
    /** Position of the last promo in this adapter */
    final private int promoEnd;
    /** Position of the filt_curse checkbox */
    final private int curseCheckPos;

    public RulesAdapter(Context context, Cursor cardSets) {
        super(context, R.layout.fragment_rules);

        // Load the preferences managed by this adapter
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String filt_set_raw = prefs.getString(Prefs.FILT_SET,
                                              context.getString(R.string.filt_set_def));
        String[] filt_set_arr = (filt_set_raw.length()==0) ? new String[0]
                                                           : filt_set_raw.split(",");
        filt_set = new HashSet<>(filt_set_arr.length);
        for(String s : filt_set_arr)
            filt_set.add(Integer.parseInt(s));

        boolean filt_curse = prefs.getBoolean(Prefs.FILT_CURSE, true);

        // Get the preferred list item height
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        TypedValue tv = new TypedValue();
        context.getTheme()
                .resolveAttribute(android.R.attr.listPreferredItemHeight, tv, true);
        int prefHeight = (int)(tv.getDimension(metrics)+0.5);

        // Get the expansion icons
        int[] icons = Utils.getResourceArray(context, R.array.card_set_icons);

        // Start to build the list of views
        views = new ArrayList<>();
        views.add(newNumPref(context, 0, Prefs.LIMIT_SUPPLY, R.string.limit_supply, prefHeight));

        // Add the expansions
        views.add(newSeparator(context, R.string.rules_expansions));
        setStart = views.size();
        int _id = cardSets.getColumnIndex(TableCard._SET_ID);
        int _name = cardSets.getColumnIndex(TableCard._SET_NAME);
        int _promo = cardSets.getColumnIndex(TableCard._PROMO);
        cardSets.moveToFirst();
        CheckedTextView view;
        do {
            int id = cardSets.getInt(_id);
            view = newChecked(context, cardSets.getString(_name), icons[id]);
            if(filt_set.contains(id)) view.toggle();
            view.setTag(id);
            views.add(view);
        } while(cardSets.moveToNext() && cardSets.getInt(_promo) == 0);
        setEnd = views.size() -1;

        // Add the promo sets
        views.add(newSeparator(context, R.string.rules_promo));
        promoStart = views.size();
        do {
            int id = cardSets.getInt(_id);
            view = newChecked(context, cardSets.getString(_name), icons[id]);
            if(filt_set.contains(id)) view.toggle();
            view.setTag(id);
            views.add(view);
        } while(cardSets.moveToNext());
        promoEnd = views.size()-1;

        // Add the event filter
        views.add(newSeparator(context, R.string.rules_other));
        views.add(newNumPref(context, 2, Prefs.LIMIT_EVENTS, R.string.limit_event, prefHeight));

        // Add the curse filter
        view = newChecked(context, context.getString(R.string.filter_curse),
                          R.drawable.ic_dom_curse);
        if(filt_curse) view.toggle();
        views.add(view);
        curseCheckPos = views.size() -1;

        // Add an extra 8dp to the top list item
        int dp8 = (int)(8 * metrics.density + 0.5f);
        views.get(0).setPadding(0, dp8, 0, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return views.get(position);
    }

    @Override
    public int getCount() {
        return views.size();
    }

    protected SmallNumberPreference newNumPref(Context c, int id, String key,
                                               int textRes, int height) {
        SmallNumberPreference res = new SmallNumberPreference(c);
        res.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                                                         height));
        res.setText(textRes);
        res.setKey(key);
        res.setListener(this, id);
        return res;
    }

    protected View newSeparator(Context c, int textRes) {
        View res = View.inflate(c, R.layout.list_item_seperator, null);
        TextView txt = (TextView)res.findViewById(android.R.id.text1);
        txt.setText(textRes);
        return res;
    }

    protected CheckedTextView newChecked(Context c, String text, int imgRes) {
        CheckedTextView res = (CheckedTextView) View.inflate(c, R.layout.rule_checkbox, null);
        res.setText(text);
        if (imgRes != 0)
            Utils.setDrawables(res, imgRes, 0, 0, 0);
        return res;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // If its the curse giver's checkbox
        if(position == curseCheckPos) {
            CheckedTextView check = (CheckedTextView) view;
            check.toggle();
            prefs.edit().putBoolean(Prefs.FILT_CURSE, check.isChecked()).commit();
            Prefs.notifyChange(getContext(), Prefs.FILT_CURSE);
            notifyDataSetChanged();
        }

        // If its a card set checkbox
        if((setStart <= position && position <= setEnd)
                || (promoStart <= position && position <= promoEnd)) {
            CheckedTextView tv = (CheckedTextView) views.get(position);
            tv.toggle();
            int set_id = (Integer)tv.getTag();
            if(tv.isChecked()) filt_set.add(set_id);
            else filt_set.remove(set_id);
            prefs.edit().putString(Prefs.FILT_SET, Utils.join(",", filt_set)).commit();
            Prefs.notifyChange(getContext(), Prefs.FILT_SET);
            notifyDataSetChanged();
        }
    }

    @Override
    public void preferenceChanged(int id) {
        Prefs.notifyChange(getContext(), PREF_KEYS[id]);
    }
}