package ca.marklauman.dominionpicker.cardlist;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;

/** Adapter used to select cards.
 *  Does not use card color as background.
 *  @author Mark Lauman */
public class AdapterSelCards extends AdapterCards {

    /** Index of the "_id" column */
    private int col_id;
    /** Set of all selected cards */
    private final HashSet<Long> mSelected;

    public AdapterSelCards(Context context) {
        super(context, R.layout.list_item_card,
                new String[]{TableCard._NAME, TableCard._COST, TableCard._POT, TableCard._SET_ID,
                             TableCard._SET_NAME, TableCard._REQ, TableCard._COIN, TableCard._TYPE,
                             TableCard._VICTORY, TableCard._LANG, TableCard._DESC, TableCard._ID},
                   new int[]{R.id.card_name, R.id.card_cost, R.id.card_potion, R.id.card_set,
                             R.id.card_set, R.id.card_requires, R.id.card_res_gold, R.id.card_type,
                             R.id.card_res_victory, R.id.card_res, R.id.card_desc, R.id.card_back});
        mSelected = new HashSet<>();
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
        if(super.setViewValue(view, cursor, columnIndex))
            return true;
        if(columnIndex == col_id) {
            if(mSelected.contains(cursor.getLong(columnIndex))) {
                view.setBackgroundResource(R.color.card_list_select);
            } else view.setBackgroundResource(android.R.color.transparent);
            return true;
        }
        return false;
    }

    /** Set what cards are selected. Cards can be selected even if they
     *  aren't in the cursor. */
    public void setSelections(Long... selectIds) {
        mSelected.clear();
        if(selectIds == null || selectIds.length == 0) return;
        Collections.addAll(mSelected, selectIds);
        notifyDataSetChanged();
    }

    /** Get the selected cards, and verify that all those cards
     *  are visible in the cursor. */
    public Long[] getSelections() {
        Cursor mCursor = getCursor();
        if(mCursor == null || !mCursor.moveToFirst())
            return new Long[0];

        ArrayList<Long> sel = new ArrayList<>(mSelected.size());
        do {
            long id = mCursor.getLong(col_id);
            if(mSelected.contains(id)) sel.add(id);
        } while(mCursor.moveToNext());

        Long[] res = new Long[sel.size()];
        sel.toArray(res);
        return res;
    }

    /** Select all items in the cursor */
    public void selectAll() {
        mSelected.clear();
        Cursor cursor = getCursor();
        if(cursor == null || !cursor.moveToFirst()) return;
        do {
            mSelected.add(cursor.getLong(col_id));
        } while(cursor.moveToNext());
        notifyDataSetChanged();
    }


    /** All items in the list are selected. If they are already all selected,
      *  then everything is deselected instead. */
    public void toggleAll() {
        Cursor cursor = getCursor();
        if(cursor == null || !cursor.moveToFirst()) return;
        boolean selected;
        do {
            selected = mSelected.contains(cursor.getLong(col_id));
        } while(selected && cursor.moveToNext());

        if(selected) mSelected.clear();
        else selectAll();
        notifyDataSetChanged();
    }

    /** Select an item if its unselected. Deselect it if it is selected. */
    public void toggleItem(long cardId) {
        if(mSelected.contains(cardId))
            mSelected.remove(cardId);
        else mSelected.add(cardId);
        notifyDataSetChanged();
    }
}