package ca.marklauman.dominionpicker.cardadapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;

import java.util.HashSet;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.tools.Utils;

/** Adapter designed to select and filter out cards.
 *  Handles saving the filters to the preferences as well.
 *  @author Mark Lauman */
public class AdapterCardsFilter extends AdapterCards
                                implements AdapterCards.Listener {

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
        super(context,
              new String[]{TableCard._NAME, TableCard._COST, TableCard._POT, TableCard._SET_ID,
                           TableCard._SET_NAME, TableCard._REQ, TableCard._ID,
                           TableCard._ID, TableCard._ID, TableCard._TYPE, TableCard._TYPE,
                           TableCard._ID},
              new int[]{R.id.card_name, R.id.card_cost, R.id.card_potion, R.id.card_set,
                        R.id.card_set, R.id.card_requires, android.R.id.background,
                        R.id.card_image, R.id.image_overlay, R.id.card_type, R.id.card_color,
                        R.id.card_extra});
        setListener(this);
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
        long id;
        switch(view.getId()) {
            case R.id.card_extra:
                if(hardSelected.contains(cursor.getLong(columnIndex)))
                    view.setVisibility(View.VISIBLE);
                else view.setVisibility(View.GONE);
                return true;

            case android.R.id.background:
                if(columnIndex != col_id) return false;
                id = cursor.getLong(col_id);

                if(deselected.contains(id))
                    view.setBackgroundResource(R.color.background);
                else if(hardSelected.contains(id))
                    view.setBackgroundResource(R.color.list_item_sel_hard);
                else view.setBackgroundResource(R.color.list_item_sel);

            default: return super.setViewValue(view, cursor, columnIndex);
        }
    }


    /** Select all items in the cursor */
    private void selectAll() {
        deselected.clear();
        saveUpdate(PREF_FILT);
        notifyDataSetChanged();
    }


    private void deselectAll() {
        hardSelected.clear();
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
        // If this item is hard selected, it becomes selected
        if(hardSelected.remove(cardId))
            saveUpdate(PREF_REQ);
        else {
            // This item was not hard selected
            // Toggle deselected state.
            if (!deselected.remove(cardId))
                deselected.add(cardId);
            saveUpdate(PREF_FILT);
        }
        notifyDataSetChanged();
    }


    private void hardToggle(long cardId) {
        // If hard selected, revert to just selected
        if(!hardSelected.remove(cardId)) {
            // If not hard selected
            hardSelected.add(cardId);
            if(deselected.remove(cardId))
                saveUpdate(PREF_FILT);
        }
        saveUpdate(PREF_REQ);
        notifyDataSetChanged();
    }


    @Override
    public void onItemClick(int position, long id, boolean longClick) {
        if(longClick) hardToggle(id);
        else toggleItem(id);
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