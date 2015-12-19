package ca.marklauman.dominionpicker.cardlist;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
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
                                implements ListView.OnItemClickListener {
    private final Context mContext;
    /** Index of the "_id" column */
    private int col_id;
    /** Set of all cards that are NOT selected */
    private final HashSet<Long> deselected;

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
        String rawDeselect = pref.getString(Prefs.FILT_CARD, "");
        if(rawDeselect.equals(""))
            deselected = new HashSet<>();
        else {
            String[] toDeselect = rawDeselect.split(",");
            deselected = new HashSet<>(toDeselect.length);
            for(String card : toDeselect)
                deselected.add(Long.parseLong(card));
        }
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
            if(deselected.contains(cursor.getLong(col_id)))
                view.setBackgroundResource(android.R.color.transparent);
            else view.setBackgroundResource(R.color.card_list_select);
            return true;
        }
        return super.setViewValue(view, cursor, columnIndex);
    }


    /** Select all items in the cursor */
    private void selectAll() {
        deselected.clear();
        saveUpdate(Prefs.FILT_CARD);
        notifyDataSetChanged();
    }


    private void deselectAll() {
        Cursor cursor = getCursor();
        if(cursor == null) return;
        cursor.moveToPosition(-1);
        while(cursor.moveToNext())
            deselected.add(cursor.getLong(col_id));
        saveUpdate(Prefs.FILT_CARD);
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
        if(deselected.contains(cardId))
            deselected.remove(cardId);
        else deselected.add(cardId);
        saveUpdate(Prefs.FILT_CARD);
        notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        toggleItem(id);
    }


    /** Save an update to the given preference */
    private void saveUpdate(String key) {
        switch(key) {
            case Prefs.FILT_CARD:
                PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                                 .putString(Prefs.FILT_CARD, Utils.join(",", deselected))
                                 .commit();
                Prefs.notifyChange(mContext, Prefs.FILT_CARD);
                break;
        }
    }
}