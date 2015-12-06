package ca.marklauman.dominionpicker.history;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ca.marklauman.dominionpicker.App;
import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableSupply;
import ca.marklauman.tools.CursorHandler;
import ca.marklauman.tools.Utils;

/** Handler for loading and displaying shuffles from {@link TableSupply}.
 *  @author Mark Lauman */
class HandlerSamples extends SimpleCursorAdapter
                     implements CursorHandler {

    /** The context this handler is in */
    private final Context mContext;
    /** Maps expansion names to expansion icons */
    private int[] exp_icons = null;

    /** Column index for set id */
    private int _set_id;
    /** Column index for set name */
    private int _set_name;
    /** Column index for cards. */
    private int _cards;
    /** Column index for shelters. */
    private int _shelters;
    /** Column index for high_cost. */
    private int _high_cost;

    public HandlerSamples(Context context) {
        super(context, R.layout.list_item_supply, null,
                new String[]{TableSupply._NAME, TableSupply._SET_ID, TableSupply._SET_NAME, TableSupply._CARDS},
                new int[]{R.id.name, R.id.set, R.id.set, R.id.desc},
                0);
        mContext = context;
        setViewBinder(this);
        exp_icons = Utils.getResourceArray(context, R.array.card_set_icons);
    }

    @Override
    public void changeCursor(Cursor c) {
        super.changeCursor(c);
        if(c == null) return;
        _set_id = c.getColumnIndex(TableSupply._SET_ID);
        _cards = c.getColumnIndex(TableSupply._CARDS);
        _high_cost = c.getColumnIndex(TableSupply._HIGH_COST);
        _shelters = c.getColumnIndex(TableSupply._SHELTERS);
        _set_name = c.getColumnIndex(TableSupply._SET_NAME);
    }

    /** Used to map the set id and supply details to their views */
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if(columnIndex == _set_id) {
            // Map the set id to an image
            int setImg = 0;
            try{ setImg = exp_icons[cursor.getInt(_set_id)];
            } catch (Exception ignored) {}
            if (setImg == 0) setImg = R.drawable.ic_set_unknown;
            view.setVisibility(View.VISIBLE);
            ((ImageView)view).setImageResource(setImg);
            return true;

        } else if(columnIndex == _set_name) {
            view.setContentDescription(cursor.getString(_set_name));
            return true;

        } else if(columnIndex == _cards) {
            // Count the number of cards
            int cards = Utils.countChar(cursor.getString(_cards),',')+1;
            String desc = mContext.getResources()
                    .getQuantityString(R.plurals.hist_card, cards, cards);

            // Get the special conditions
            boolean high_cost = cursor.getInt(_high_cost) != 0;
            boolean shelters = cursor.getInt(_shelters) != 0;
            if(high_cost)                desc += " | " + mContext.getString(R.string.hist_plat);
            if(shelters)                 desc += " | " + mContext.getString(R.string.hist_shelter);

            ((TextView)view).setText(desc);
            return true;
        }
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // start the query going
        CursorLoader c = new CursorLoader(mContext);
        c.setProjection(new String[]{TableSupply._ID, TableSupply._NAME, TableSupply._SET_ID,
                                     TableSupply._SET_NAME, TableSupply._CARDS, TableSupply._SHELTERS,
                                     TableSupply._HIGH_COST});
        c.setUri(Provider.URI_SUPPLY);
        c.setSortOrder(TableSupply._ID);
        c.setSelection(App.transFilter);
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