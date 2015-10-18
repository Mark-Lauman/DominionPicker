package ca.marklauman.dominionpicker.cardlist;

import android.content.Context;
import android.database.Cursor;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.CardDb;

/** Adapter used to display lists of cards with color backgrounds and a bane.
 *  @author Mark Lauman */
public class AdapterColorCards extends AdapterCards {
    /** Columns required for this adapter to work */
    public static final String[] COLS_USED
            = {CardDb._ID, CardDb._NAME, CardDb._SET_NAME, CardDb._TYPE,
               CardDb._DESC, CardDb._SET_ID, CardDb._COST, CardDb._POT,
               CardDb._BUY, CardDb._ACT, CardDb._CARD, CardDb._COIN,
               CardDb._VICTORY, CardDb._REQ, CardDb._LANG,
               CardDb._TYPE_ACT, CardDb._TYPE_TREAS, CardDb._TYPE_VICTORY, // colorFactory required.
               CardDb._TYPE_DUR, CardDb._TYPE_REACT, CardDb._TYPE_RESERVE,
               CardDb._TYPE_RUINS, CardDb._TYPE_CURSE, CardDb._TYPE_EVENT};

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
                new String[]{CardDb._NAME, CardDb._COST, CardDb._POT, CardDb._SET_ID,
                             CardDb._SET_NAME, CardDb._REQ, CardDb._COIN, CardDb._TYPE,
                             CardDb._VICTORY, CardDb._LANG, CardDb._DESC,
                             CardDb._ID, CardDb._TYPE_ACT},
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
        col_id = cursor.getColumnIndex(CardDb._ID);
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