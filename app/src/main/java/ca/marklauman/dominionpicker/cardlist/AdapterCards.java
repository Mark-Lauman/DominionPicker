package ca.marklauman.dominionpicker.cardlist;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import java.util.HashMap;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.CardDb;
import ca.marklauman.tools.Utils;

/** Basic adapter used to display cards from the {@link CardDb}.
 *  @author Mark Lauman */
public class AdapterCards extends SimpleCursorAdapter
                          implements ViewBinder {

    /** The columns used by this adapter.
     *  Any other columns provided will be ignored. */
    public static final String[] COLS_USED
            = {CardDb._ID, CardDb._NAME, CardDb._SET_NAME, CardDb._TYPE,
               CardDb._DESC, CardDb._SET_ID, CardDb._COST, CardDb._POT,
               CardDb._BUY, CardDb._ACT, CardDb._CARD, CardDb._COIN,
               CardDb._VICTORY, CardDb._REQ, CardDb._LANG};

    // Internal ids used to identify cursor indexes.
    /** Internal id for a column this adapter does not handle */
    private static final int ID_UNKNOWN = -1;
    /** Internal id for the name column */
    private static final int ID_NAME = 0;
    /** Internal id for the cost column */
    private static final int ID_COST = 1;
    /** Internal id for the potion column */
    private static final int ID_POTION = 2;
    /** Internal id for the set_id column */
    private static final int ID_SET_ID = 3;
    /** Internal id for the set_name column */
    private static final int ID_SET_NAME = 4;
    /** Internal id for the requires column */
    private static final int ID_REQ = 5;
    /** Internal id for the type column */
    private static final int ID_TYPE = 6;
    /** Internal id for the plus_coin column */
    private static final int ID_PLUS_COIN = 7;
    /** Internal id for the plus victory column */
    private static final int ID_PLUS_VICTORY = 8;
    /** Internal id for the language column */
    private static final int ID_LANG = 9;
    /** Internal id for the description column */
    private static final int ID_DESC = 10;

    /** Resources used to format strings */
    final Resources resources;

    /** Maps expansion names to expansion icons */
    private final int[] exp_icons;
    /** Maps language code to +X resource index. */
    private final HashMap<String, Integer> mapLanguage;
    /** Resource ids of the + action format strings */
    private final int[] resAct;
    /** Resource ids of the + buy format strings */
    private final int[] resBuy;
    /** Resource ids of the + card format strings */
    private final int[] resCard;
    /** Maps column indexes to column ids */
    private final SparseIntArray columnMap;

    /** Column index of the card's name */
    private int col_name;
    /** Column index of the plus_action column */
    private int col_plusAct;
    /** Column index of the plus_buy column */
    private int col_plusBuy;
    /** Column index of the plus_card column */
    private int col_plusCard;


//    /** Default constructor.
//     *  @param context The context of the display thread. */
//    public AdapterCards(Context context) {
//        this(context, R.layout.list_item_card,
//             new String[]{CardDb._NAME, CardDb._COST, CardDb._POT, CardDb._SET_ID,
//                          CardDb._SET_NAME, CardDb._REQ, CardDb._COIN, CardDb._TYPE,
//                          CardDb._VICTORY, CardDb._LANG, CardDb._DESC},
//                new int[]{R.id.card_name, R.id.card_cost, R.id.card_potion, R.id.card_set,
//                          R.id.card_set, R.id.card_requires, R.id.card_res_gold, R.id.card_type,
//                          R.id.card_res_victory, R.id.card_res, R.id.card_desc});
//    }


    /** Used by subclasses so they can bind their own columns.
     *  @param context The context of the display thread.
     *  @param viewResource Id of the view resource used for this adapter's views.
     *  @param columnNames The names of the columns to bind to views.
     *  @param viewIds The view ids bound to those names. */
    AdapterCards(Context context, int viewResource, String[] columnNames, int[] viewIds) {
        super(context, viewResource, null, columnNames, viewIds);
        setViewBinder(this);
        resources = context.getResources();
        columnMap = new SparseIntArray();

        // Load the expansion icons
        exp_icons = Utils.getResourceArray(context, R.array.card_set_icons);

        // Load the language map. Do not include "0" in the map.
        String[] lang = resources.getStringArray(R.array.language_codes);
        mapLanguage = new HashMap<>(lang.length);
        for(int i=1; i<lang.length; i++)
            mapLanguage.put(lang[i], i);

        // Load the format strings for the +X values.
        resAct = Utils.getResourceArray(context, R.array.format_act);
        resBuy = Utils.getResourceArray(context, R.array.format_buy);
        resCard = Utils.getResourceArray(context, R.array.format_card);
    }


    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        columnMap.clear();
        if (cursor == null) return;
        columnMap.put(cursor.getColumnIndex(CardDb._NAME), ID_NAME);
        columnMap.put(cursor.getColumnIndex(CardDb._COST), ID_COST);
        columnMap.put(cursor.getColumnIndex(CardDb._POT), ID_POTION);
        columnMap.put(cursor.getColumnIndex(CardDb._SET_ID), ID_SET_ID);
        columnMap.put(cursor.getColumnIndex(CardDb._SET_NAME), ID_SET_NAME);
        columnMap.put(cursor.getColumnIndex(CardDb._REQ), ID_REQ);
        columnMap.put(cursor.getColumnIndex(CardDb._TYPE), ID_TYPE);
        columnMap.put(cursor.getColumnIndex(CardDb._COIN), ID_PLUS_COIN);
        columnMap.put(cursor.getColumnIndex(CardDb._VICTORY), ID_PLUS_VICTORY);
        columnMap.put(cursor.getColumnIndex(CardDb._LANG), ID_LANG);
        columnMap.put(cursor.getColumnIndex(CardDb._DESC), ID_DESC);
        col_name = cursor.getColumnIndex(CardDb._NAME);
        col_plusAct = cursor.getColumnIndex(CardDb._ACT);
        col_plusBuy = cursor.getColumnIndex(CardDb._BUY);
        col_plusCard = cursor.getColumnIndex(CardDb._CARD);
    }


    /** Binds each column's value to its associated view.
     *  @param view The view paired to this value by the constructor.
     *  @param cursor The cursor, positioned at the right place to read the value.
     *  @param columnIndex The index of the column being bound.
     *  @return {@code true} if the view was bound successfully. If the view was not bound, the
     *  Android system will bind the value using its default methods. */
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        int colId = columnMap.get(columnIndex, ID_UNKNOWN);
        switch(colId) {
            case ID_COST: case ID_PLUS_COIN: case ID_PLUS_VICTORY:
                // Default binding, but hide if equal to "0"
                if ("0".equals(cursor.getString(columnIndex)))
                    view.setVisibility(View.GONE);
                else view.setVisibility(View.VISIBLE);
                return false;

            case ID_POTION:
                // Hide icon if equal to "0", show icon otherwise
                if (cursor.getInt(columnIndex) != 0)
                    view.setVisibility(View.VISIBLE);
                else view.setVisibility(View.GONE);
                return true;

            case ID_SET_ID:
                // Match the id to a set icon
                int setIcon = 0;
                try { setIcon = exp_icons[cursor.getInt(columnIndex)];
                } catch(Exception ignored){}
                if(setIcon == 0) setIcon = R.drawable.ic_set_unknown;
                ((ImageView)view).setImageResource(setIcon);
                return true;

            case ID_SET_NAME:
                // Label the set icon with the set name
                view.setContentDescription(cursor.getString(columnIndex));
                return true;

            case ID_REQ: case ID_DESC:
                // Default binding, but hide if equal to "" or null
                String req = cursor.getString(columnIndex);
                if(cursor.isNull(columnIndex) || "".equals(req))
                    view.setVisibility(View.GONE);
                else view.setVisibility(View.VISIBLE);
                return false;

            case ID_LANG:
                // Language is used to bind the additional "plus resource" columns
                Integer langId = mapLanguage.get(cursor.getString(columnIndex));
                if(langId == null) langId = 0;
                String plus = "";
                plus += getPlusCol(cursor, col_plusAct, resAct[langId]);
                plus += getPlusCol(cursor, col_plusBuy, resBuy[langId]);
                plus += getPlusCol(cursor, col_plusCard, resCard[langId]);
                if("".equals(plus))
                    view.setVisibility(View.GONE);
                else {
                    view.setVisibility(View.VISIBLE);
                    ((TextView) view).setText(plus.substring(2));
                }
                return true;
        }
        // All other columns rely on the default binding
        return false;
    }

    private String getPlusCol(Cursor cursor, int colIndex, int formatRes) {
        String str = cursor.getString(colIndex);
        int val = 1;
        try{ val = Integer.parseInt(str);
        } catch(Exception ignored){}
        if(str.length() == 0 || "0".equals(str)) return "";
        return ", " + resources.getQuantityString(formatRes, val, str);
    }

    /** Get the name of the card at the given position */
    public String getName(int position) {
        Cursor cursor = getCursor();
        if(cursor == null || !cursor.moveToPosition(position))
            return null;
        return cursor.getString(col_name);
    }
}