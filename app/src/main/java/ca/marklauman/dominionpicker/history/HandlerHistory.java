package ca.marklauman.dominionpicker.history;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.TextView;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.DataDb;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TimestampFormatter;
import ca.marklauman.tools.CursorHandler;
import ca.marklauman.tools.CursorSelAdapter;
import ca.marklauman.tools.Utils;

/** Handler for loading and displaying shuffles from the {@link DataDb}.
 *  @author Mark Lauman */
class HandlerHistory extends CursorSelAdapter
                            implements CursorHandler {
    /** Used to format the supply times */
    private final TimestampFormatter tFormat;
    /** True if only favorites are to be displayed */
    private final boolean onlyFav;

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

    /** Default constructor.
     *  @param context The context that the handler is used in.
     *  @param onlyFavorites Only loads favorite shuffles if true. */
    public HandlerHistory(Context context, boolean onlyFavorites) {
        super(context, R.layout.list_item_supply,
                new String[]{DataDb._H_NAME, DataDb._H_CARDS},
                new int[]{R.id.name, R.id.desc});
        setChoiceMode(CHOICE_MODE_NONE);
        setViewBinder(this);
        tFormat = new TimestampFormatter();
        onlyFav = onlyFavorites;
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
                // Not Favorite, display timestamp as name
                txt.setText(tFormat.format(cursor.getLong(_time)));
                txt.setTypeface(Typeface.create(type, Typeface.NORMAL));
            }
            return true;

        // The description field.
        } else if(columnIndex == _cards) {
            // Count the number of cards
            int cards = Utils.countChar(cursor.getString(_cards), ',')+1;
            String desc = mContext.getResources()
                                  .getQuantityString(R.plurals.hist_card, cards, cards);

            // Get the special conditions
            boolean high_cost = cursor.getInt(_high_cost) != 0;
            boolean shelters = cursor.getInt(_shelters) != 0;
            if(high_cost)                desc += " | " + mContext.getString(R.string.hist_plat);
            if(shelters)                 desc += " | " + mContext.getString(R.string.hist_shelter);

            txt.setText(desc);
            return true;
        }
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader c = new CursorLoader(mContext);
        c.setProjection(new String[]{DataDb._H_TIME, DataDb._H_NAME, DataDb._H_CARDS,
                                     DataDb._H_HIGH_COST, DataDb._H_SHELTERS});
        c.setUri(Provider.URI_HIST);
        c.setSortOrder(DataDb._H_TIME + " DESC");
        if(onlyFav) c.setSelection(DataDb._H_NAME + " NOT NULL");
        return c;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        changeCursor(null);
    }
}