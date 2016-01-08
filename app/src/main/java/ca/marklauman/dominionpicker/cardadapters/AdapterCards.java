package ca.marklauman.dominionpicker.cardadapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

import java.util.HashMap;

import ca.marklauman.dominionpicker.cardadapters.imagefactories.CardColorFactory;
import ca.marklauman.dominionpicker.cardadapters.imagefactories.CoinFactory;
import ca.marklauman.dominionpicker.ActivityCardInfo;
import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.tools.Utils;

/** Basic adapter used to display cards from {@link TableCard}.
 *  @author Mark Lauman */
public class AdapterCards extends SimpleCursorAdapter
                          implements ViewBinder {

    /** The columns used by this adapter.
     *  Any other columns provided will be ignored. */
    public static final String[] COLS_USED
            = {TableCard._ID, TableCard._NAME, TableCard._SET_NAME, TableCard._TYPE,
               TableCard._SET_ID, TableCard._COST, TableCard._POT, TableCard._REQ, TableCard._LANG,
               TableCard._TYPE_ACT, TableCard._TYPE_TREAS, TableCard._TYPE_VICTORY, // colorFactory
               TableCard._TYPE_DUR, TableCard._TYPE_REACT, TableCard._TYPE_RESERVE, // required
               TableCard._TYPE_CURSE, TableCard._TYPE_EVENT};                       // rows

    // Internal ids used to identify cursor indexes.
    /** Internal id for the _id column. */
    private static final int COL_ID = 0;
    /** Internal id for the name column */
    private static final int COL_NAME = 1;
    /** Internal id for the cost column */
    private static final int COL_COST = 2;
    /** Internal id for the potion column */
    private static final int COL_POTION = 3;
    /** Internal id for the set_id column */
    private static final int COL_SET_ID = 4;
    /** Internal id for the set_name column */
    private static final int COL_SET_NAME = 5;
    /** Internal id for the requires column */
    private static final int COL_REQ = 6;
    /** Internal id for the type column */
    private static final int COL_TYPE = 7;

    /** The context provided in this adapter's constructor. */
    private final Context mContext;
    /** Maps expansion names to expansion icons */
    private final int[] exp_icons;
    /** Maps card language to the correct coin description */
    private final HashMap<String, Integer> coinDesc;
    /** Constructs drawables reflecting the card's background color */
    private final CardColorFactory colorFactory;
    /** Constructs drawables for coins */
    private final CoinFactory coinFactory;

    /** Maps column indexes to column ids */
    private final SparseIntArray columnMap;
    /** Column index of the card's name */
    private int col_name;
    /** Column index of the card's language */
    private int col_lang;


    /** Internal listener used to listen to clicks on the card image. */
    private final View.OnClickListener imgListen = new View.OnClickListener() {
        public void onClick(View v) {
            launchDetails((long)v.getTag());
        }
    };
    /** Internal listener used to listen to clicks on the rest of the list item. */
    private final InternalClickListener clickListen = new InternalClickListener();
     /** Listener that is notified when a list item is clicked. */
    private Listener extListen = null;


    /** Default constructor.
     *  @param context The context of the display thread. */
    public AdapterCards(Context context) {
        this(context, R.layout.list_item_card,
             new String[]{TableCard._NAME, TableCard._COST, TableCard._POT, TableCard._SET_ID,
                          TableCard._SET_NAME, TableCard._REQ, TableCard._ID,
                          TableCard._ID, TableCard._ID, TableCard._TYPE, TableCard._TYPE},
                new int[]{R.id.card_name, R.id.card_cost, R.id.card_potion, R.id.card_set,
                          R.id.card_set, R.id.card_requires, android.R.id.background,
                          R.id.card_image, R.id.image_overlay, R.id.card_type, R.id.card_color});
    }


    /** Used by subclasses so they can bind their own columns.
     *  @param context The context of the display thread.
     *  @param viewResource Id of the view resource used for this adapter's views.
     *  @param columnNames The names of the columns to bind to views.
     *  @param viewIds The view ids bound to those names. */
    AdapterCards(Context context, int viewResource, String[] columnNames, int[] viewIds) {
        super(context, viewResource, null, columnNames, viewIds);
        setViewBinder(this);
        mContext = context;
        Resources res = context.getResources();
        colorFactory = new CardColorFactory(res);
        coinFactory = new CoinFactory(res);
        columnMap = new SparseIntArray();

        // Load the resources
        exp_icons = Utils.getResourceArray(context, R.array.card_set_icons);
        int[] form_coin = Utils.getResourceArray(context, R.array.format_coin);
        String[] lang = context.getResources().getStringArray(R.array.language_codes);
        coinDesc = new HashMap<>(lang.length);
        for(int i=0; i<lang.length; i++)
            coinDesc.put(lang[i], form_coin[i]);
    }


    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        if (cursor == null) return;
        colorFactory.changeCursor(cursor);
        col_name = cursor.getColumnIndex(TableCard._NAME);
        col_lang = cursor.getColumnIndex(TableCard._LANG);
        columnMap.clear();
        columnMap.put(col_name, COL_NAME);
        columnMap.put(cursor.getColumnIndex(TableCard._COST), COL_COST);
        columnMap.put(cursor.getColumnIndex(TableCard._POT), COL_POTION);
        columnMap.put(cursor.getColumnIndex(TableCard._SET_ID), COL_SET_ID);
        columnMap.put(cursor.getColumnIndex(TableCard._SET_NAME), COL_SET_NAME);
        columnMap.put(cursor.getColumnIndex(TableCard._REQ), COL_REQ);
        columnMap.put(cursor.getColumnIndex(TableCard._TYPE), COL_TYPE);
        columnMap.put(cursor.getColumnIndex(TableCard._ID), COL_ID);
    }


    /** Binds each column's value to its associated view.
     *  @param view The view paired to this value by the constructor.
     *  @param cursor The cursor, positioned at the right place to read the value.
     *  @param columnIndex The index of the column being bound.
     *  @return {@code true} if the view was bound successfully. If the view was not bound, the
     *  Android system will bind the value using its default methods. */
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        int colId = columnMap.get(columnIndex);
        switch(view.getId()) {
            case android.R.id.background:
                if(colId != COL_ID) return false;
                view.setTag(cursor.getPosition() + "," + cursor.getString(columnIndex));
                view.setOnClickListener(clickListen);
                view.setOnLongClickListener(clickListen);
                return true;

            case R.id.card_color:
                colorFactory.updateBackground(view, cursor);
                return true;

            case R.id.card_cost:
                if(colId != COL_COST) return false;
                String cost = cursor.getString(columnIndex);
                Integer qty = parseValue(cost);
                if(qty != null && qty == 0)
                    view.setVisibility(View.GONE);
                else {
                    Resources res = mContext.getResources();
                    if(qty == null) qty = 0;
                    String lang = cursor.getString(col_lang);
                    view.setVisibility(View.VISIBLE);
                    view.setContentDescription(res.getQuantityString(coinDesc.get(lang), qty, cost));
                    ((ImageView)view).setImageDrawable(coinFactory.getDrawable(cost, 0));
                }
                return true;

            case R.id.card_potion:
                if(colId != COL_POTION) return false;
                // Hide icon if equal to "0", show icon otherwise
                if (cursor.getInt(columnIndex) != 0)
                    view.setVisibility(View.VISIBLE);
                else view.setVisibility(View.GONE);
                return true;

            case R.id.card_set:
                switch(colId) {
                    case COL_SET_ID:
                        // Match the id to a set icon
                        int setIcon = 0;
                        try { setIcon = exp_icons[cursor.getInt(columnIndex)];
                        } catch(Exception ignored){}
                        if(setIcon == 0) setIcon = R.drawable.ic_set_unknown;
                        ((ImageView)view).setImageResource(setIcon);
                        return true;

                    case COL_SET_NAME:
                        // Label the set icon with the set name
                        view.setContentDescription(cursor.getString(columnIndex));
                        return true;
                }
                return false;

            case R.id.image_overlay:
                if(colId != COL_ID) return false;
                long id = cursor.getLong(columnIndex);
                view.setTag(id);
                view.setOnClickListener(imgListen);

            case R.id.card_image:
                if(colId != COL_ID) return false;
                // TODO: Bind image to this view
                return true;

            case R.id.card_requires:
                if(colId != COL_REQ) return false;
                // Default binding, but hide if equal to "" or null
                String req = cursor.getString(columnIndex);
                if(cursor.isNull(columnIndex) || "".equals(req))
                    view.setVisibility(View.GONE);
                else view.setVisibility(View.VISIBLE);
                return false;
        }
        // All other columns rely on the default binding
        return false;
    }


    /** Parse a string and determine its value.
     *  @param s The string to interpret
     *  @return The value of that string, or null if the value could not be determined */
    private Integer parseValue(String s) {
        // "" and null
        if(s == null || s.length() == 0) return null;
        // "1235"
        try{ return Integer.parseInt(s);
        } catch (NumberFormatException ignored){}
        // "5*"
        try{ return Integer.parseInt(s.substring(0, s.length() - 1));
        } catch (NumberFormatException ignored){}
        // "X"
        return null;
    }

    /** Get the name of the card at the given position */
    public String getName(int position) {
        Cursor cursor = getCursor();
        if(cursor == null || !cursor.moveToPosition(position))
            return null;
        return cursor.getString(col_name);
    }


    public void setListener(Listener listener) {
        extListen = listener;
        notifyDataSetChanged();
    }


    void launchDetails(long cardId) {
        Intent info = new Intent(mContext, ActivityCardInfo.class);
        info.putExtra(ActivityCardInfo.PARAM_ID, cardId);
        mContext.startActivity(info);
    }


    private class InternalClickListener implements View.OnClickListener, View.OnLongClickListener {
        @Override
        public void onClick(View v) {
            callExt(v, (String)v.getTag(), false);
        }

        @Override
        public boolean onLongClick(View v) {
            callExt(v, (String)v.getTag(), true);
            return true;
        }

        private void callExt(View v, String tag, boolean longClick) {
            if(extListen == null) return;
            int sep = tag.indexOf(',');
            extListen.onItemClick(v, Integer.parseInt(tag.substring(0, sep)),
                                     Long.parseLong(tag.substring(sep+1, tag.length())),
                                     longClick);
        }
    }

    public interface Listener {
        void onItemClick(View view, int position, long id, boolean longClick);
    }
}