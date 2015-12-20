package ca.marklauman.dominionpicker.cardlist;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.HashSet;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.tools.Utils;

/** Adapter designed to select and filter out cards.
 *  Handles saving the filters to the preferences as well.
 *  @author Mark Lauman */
public class AdapterCardsFilter extends AdapterCards
                                implements ListView.OnItemClickListener,
                                           AdapterView.OnItemLongClickListener {

    /** Integer used to identify the card filter preference */
    private static final int PREF_FILT = 0;
    /** Integer used to identify the required card preference */
    private static final int PREF_REQ  = 1;

    private final Context mContext;
    /** Index of the "_id" column */
    private int col_id;
    /** Set of all cards that are NOT selected */
    private final HashSet<Long> deselected;
    /** Set of all cards that are "hard" selected (must be in the supply */
    private final HashSet<Long> hardSelected;

    public AdapterCardsFilter(Context context) {
        super(context, R.layout.list_item_card,
                new String[]{TableCard._NAME, TableCard._COST, TableCard._POT, TableCard._SET_ID,
                             TableCard._SET_NAME, TableCard._REQ, TableCard._COIN, TableCard._TYPE,
                             TableCard._VICTORY, TableCard._LANG, TableCard._DESC, TableCard._ID},
                new int[]{R.id.card_name, R.id.card_cost, R.id.card_potion, R.id.card_set,
                          R.id.card_set, R.id.card_requires, R.id.card_res_gold, R.id.card_type,
                          R.id.card_res_victory, R.id.card_res, R.id.card_desc, R.id.card_back});
        mContext = context;

        // Load the selections
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        deselected = parsePreference(pref, Prefs.FILT_CARD);
        hardSelected = parsePreference(pref, Prefs.REQ_CARDS);
    }


    private HashSet<Long> parsePreference(SharedPreferences pref, String key) {
        String rawPref = pref.getString(key, "");
        if(rawPref.equals(""))
            return new HashSet<>();
        String[] split = rawPref.split(",");
        HashSet<Long> res = new HashSet<>(split.length);
        for(String card : split)
            res.add(Long.parseLong(card));
        return res;
    }


    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        if(cursor == null) return;
        col_id = cursor.getColumnIndex(TableCard._ID);
    }

    /** Binds each column's value to its associated view.
     *  @param view The view paired to this value by the constructor.
     *  @param cursor The cursor, positioned at the right place to read the value.
     *  @param columnIndex The index of the column being bound.
     *  @return {@code true} if the view was bound successfully. If the view was not bound, the
     *  Android system will bind the value using its default methods. */
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if(columnIndex == col_id) {
            long id = cursor.getLong(col_id);
            if(deselected.contains(id))
                view.setBackgroundResource(android.R.color.transparent);
            else if(hardSelected.contains(id))
                view.setBackgroundResource(R.color.required_card);
            else view.setBackgroundResource(R.color.card_list_select);
            return true;
        }
        return super.setViewValue(view, cursor, columnIndex);
    }


    /** Select all items in the cursor */
    private void selectAll() {
        deselected.clear();
        saveUpdate(PREF_FILT);
        notifyDataSetChanged();
    }


    private void deselectAll() {
        Cursor cursor = getCursor();
        if(cursor == null) return;
        cursor.moveToPosition(-1);
        while(cursor.moveToNext())
            deselected.add(cursor.getLong(col_id));
        saveUpdate(PREF_FILT);
        notifyDataSetChanged();
    }


    /** All items in the list are selected. If they are already all selected,
      *  then everything is deselected instead. */
    public void toggleAll() {
        if(deselected.size() == 0) deselectAll();
        else selectAll();
    }

    /** Select an item if its unselected. Deselect it if it is selected. */
    private void toggleItem(long cardId) {
        // Remove the card from the hard selection if needed
        boolean hardSelect = hardSelected.remove(cardId);
        if(hardSelect) saveUpdate(PREF_REQ);
        // toggle the normal selection state
        if(hardSelect || !deselected.remove(cardId))
            deselected.add(cardId);
        saveUpdate(PREF_FILT);
        notifyDataSetChanged();
    }


    private void hardToggle(long cardId) {
        // The card is not hard selected
        if(!hardSelected.remove(cardId)) {
            hardSelected.add(cardId);
            if(deselected.remove(cardId))
                saveUpdate(PREF_FILT);
        // The card was hard selected
        } else {
            deselected.add(cardId);
            saveUpdate(PREF_FILT);
        }
        saveUpdate(PREF_REQ);
        notifyDataSetChanged();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        toggleItem(id);
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        hardToggle(id);
        return true;
    }


    /** Save an update to the given preference */
    private void saveUpdate(int prefId) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(mContext)
                                                         .edit();
        switch(prefId) {
            case PREF_FILT:
                edit.putString(Prefs.FILT_CARD, Utils.join(",", deselected))
                    .commit();
                Prefs.notifyChange(mContext, Prefs.FILT_CARD);
                break;
            case PREF_REQ:
                edit.putString(Prefs.REQ_CARDS, Utils.join(",", hardSelected))
                        .commit();
                Prefs.notifyChange(mContext, Prefs.REQ_CARDS);
                break;
        }
    }
}