package ca.marklauman.dominionpicker.cardadapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;

/** Card adapter designed to display a supply.
 *  A bane card can be set, and clicking anywhere on the card opens the details panel.
 *  @author Mark Lauman */
public class AdapterCardsSupply extends AdapterCards implements AdapterCards.Listener {

    /** Id of the bane card */
    private long bane = -1;
    /** _id column index */
    private int col_id = -1;

    public AdapterCardsSupply(Context context) {
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
    }

    public void setBane(long newBane) {
        bane = newBane;
        notifyDataSetChanged();
    }


    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        col_id = (cursor == null) ? -1 : cursor.getColumnIndex(TableCard._ID);
    }


    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        switch(view.getId()) {
            case R.id.card_extra:
                if(columnIndex != col_id) break;
                if(bane == cursor.getLong(columnIndex)) {
                    ((TextView)view).setText(R.string.young_witch_bane);
                    view.setVisibility(View.VISIBLE);
                } else view.setVisibility(View.GONE);
                return true;
            case android.R.id.background:
                super.setViewValue(view, cursor, columnIndex);
                if(columnIndex != col_id) break;
                if(bane == cursor.getLong(columnIndex))
                    view.setBackgroundResource(R.color.type_curse);
                else view.setBackgroundResource(R.color.background);
                return true;
        }
        return super.setViewValue(view, cursor, columnIndex);
    }

    @Override
    public void onItemClick(int position, long id, boolean longClick) {
        launchDetails(id);
    }
}