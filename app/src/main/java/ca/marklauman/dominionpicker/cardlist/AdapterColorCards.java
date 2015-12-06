package ca.marklauman.dominionpicker.cardlist;

import android.content.Context;
import android.database.Cursor;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;

/** Adapter used to display lists of cards with color backgrounds and a bane.
 *  @author Mark Lauman */
public class AdapterColorCards extends AdapterCards {
    /** Columns required for this adapter to work */
    public static final String[] COLS_USED
            = {TableCard._ID, TableCard._NAME, TableCard._SET_NAME, TableCard._TYPE,
               TableCard._DESC, TableCard._SET_ID, TableCard._COST, TableCard._POT,
               TableCard._BUY, TableCard._ACT, TableCard._CARD, TableCard._COIN,
               TableCard._VICTORY, TableCard._REQ, TableCard._LANG,
               TableCard._TYPE_ACT, TableCard._TYPE_TREAS, TableCard._TYPE_VICTORY, // colorFactory required.
               TableCard._TYPE_DUR, TableCard._TYPE_REACT, TableCard._TYPE_RESERVE,
               TableCard._TYPE_CURSE, TableCard._TYPE_EVENT};

    /** 2 dp in the current context */
    private final int dp2;

    /** Factory responsible for coloring the card backgrounds */
    private CardColorFactory colorFactory;
    /** Column id for the "_id" column */
    private int col_id;
    /** Id of the bane card */
    private long bane = -1L;


    public AdapterColorCards(Context context) {
        super(context, R.layout.list_item_card_bane,
                new String[]{TableCard._NAME, TableCard._COST, TableCard._POT, TableCard._SET_ID,
                             TableCard._SET_NAME, TableCard._REQ, TableCard._COIN, TableCard._TYPE,
                             TableCard._VICTORY, TableCard._LANG, TableCard._DESC,
                             TableCard._ID, TableCard._TYPE_ACT},
                   new int[]{R.id.card_name, R.id.card_cost, R.id.card_potion, R.id.card_set,
                             R.id.card_set, R.id.card_requires, R.id.card_res_gold, R.id.card_type,
                             R.id.card_res_victory, R.id.card_res, R.id.card_desc,
                             R.id.card_frame, R.id.card_back});
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        dp2 = (int)(0.5 + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, dm));
    }


    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        if(cursor == null) return;
        colorFactory = new CardColorFactory(resources, cursor);
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
        // Handle the default view binding
        if(super.setViewValue(view, cursor, columnIndex))
            return true;

        // The id column is used to identify the bane card
        if(columnIndex == col_id) {
            if(bane == cursor.getLong(columnIndex)) {
                view.setPadding(2*dp2, 0, 2*dp2, dp2);
                view.findViewById(R.id.card_special)
                    .setVisibility(View.VISIBLE);
            } else {
                view.setPadding(0, 0, 0, 0);
                view.findViewById(R.id.card_special)
                    .setVisibility(View.GONE);
            }
            return true;

        // the typeAction column is used to bind the card background
        } else if(columnIndex == colorFactory.getActionColumn()) {
            colorFactory.updateBackground(view, cursor);
            return true;
        }

        // Not bound by this class
        return false;
    }


    /** Set the bane card used by this card adapter */
    public void setBane(long baneId) {
        bane = baneId;
    }
}