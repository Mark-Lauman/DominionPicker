package ca.marklauman.dominionpicker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import ca.marklauman.dominionpicker.database.DataDb;
import ca.marklauman.tools.CursorSelAdapter;

/** Adapter for displaying shuffles from the history table. */
public class HistoryAdapter extends CursorSelAdapter
                            implements ViewBinder {
    /** Column index for time. */
    private int _time;
    /** Column index for name. */
    private int _name;
    /** Column index for cards. */
    private int _cards;
    /** Column index for shelters. */
    private int _shelters;
    /** Column index for high_cost. */
    private int _high_cost;

    private DateFormat tFormat;

    public HistoryAdapter(Context context) {
        super(context, R.layout.list_item_shuffle,
                new String[]{DataDb._H_NAME, DataDb._H_CARDS},
                new int[]{R.id.name, R.id.desc});
        setChoiceMode(CHOICE_MODE_NONE);
        setViewBinder(this);
        tFormat = DateFormat.getDateTimeInstance();
    }


    @Override
    public void changeCursor(Cursor c) {
        super.changeCursor(c);
        if(c == null) return;
        _time = c.getColumnIndex(DataDb._H_TIME);
        _name = c.getColumnIndex(DataDb._H_NAME);
        _cards = c.getColumnIndex(DataDb._H_CARDS);
        _high_cost = c.getColumnIndex(DataDb._H_HIGH_COST);
        _shelters = c.getColumnIndex(DataDb._H_SHELTERS);
    }


    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        TextView txt = (TextView) view;

        // The name field
        if(columnIndex == _name) {
            Typeface type = txt.getTypeface();
            String name = cursor.getString(_name);
            if(name != null) {
                // Favorite, display bold name
                txt.setText(name);
                txt.setTypeface(Typeface.create(type, Typeface.BOLD));
            } else {
                // Not Favorite, display normal name
                String time = tFormat.format(new Date(cursor.getLong(_time)));
                txt.setText(time);
                txt.setTypeface(Typeface.create(type, Typeface.NORMAL));
            }
            return true;

        // The description field.
        } else if(columnIndex == _cards) {
            // Count the number of cards
            String desc;
            int cards = cursor.getString(_cards).split(",").length;
            if(cards < 2) desc = mContext.getString(R.string.hist_card);
            else desc = String.format(mContext.getString(R.string.hist_cards), cards);

            // Get the special conditions
            boolean high_cost = cursor.getInt(_high_cost) != 0;
            boolean shelters = cursor.getInt(_shelters) != 0;
            if(!(high_cost || shelters)) desc += " | " + mContext.getString(R.string.hist_normal);
            if(high_cost)                desc += " | " + mContext.getString(R.string.hist_plat);
            if(shelters)                 desc += " | " + mContext.getString(R.string.hist_shelt);

            txt.setText(desc);
            return true;
        }
        return false;
    }
}