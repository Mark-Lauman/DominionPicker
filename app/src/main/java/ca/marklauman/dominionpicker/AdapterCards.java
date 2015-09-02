package ca.marklauman.dominionpicker;

import ca.marklauman.dominionpicker.database.CardDb;
import ca.marklauman.tools.CursorSelAdapter;
import ca.marklauman.tools.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

/** Adapter used to display cards from the {@link CardDb}.
 *  @author Mark Lauman */
class AdapterCards extends CursorSelAdapter
						 implements OnItemClickListener,
						 			ViewBinder {

	/** The columns used by this adapter.
	 *  Any other columns provided will be ignored. */
	public static final String[] COLS_USED
			= {CardDb._ID, CardDb._NAME, CardDb._SET_NAME, CardDb._TYPE,
               CardDb._DESC, CardDb._SET_ID, CardDb._COST, CardDb._POT,
               CardDb._BUY, CardDb._ACT, CardDb._CARD, CardDb._COIN,
               CardDb._VICTORY, CardDb._REQ};
	
	/** Maps expansion names to expansion icons */
	private static int[] exp_icons = null;

    /** Resources used to format strings */
    private final Resources resources;
	
	/** Index of the {@link CardDb#_DESC} column. */
	private int col_desc = -1;
	/** Index of the {@link CardDb#_COST} column. */
	private int col_cost = -1;
	/** Index of the {@link CardDb#_POT} column. */
	private int col_potion = -1;
	/** Index of the {@link CardDb#_SET_ID} column. */
	private int col_set = -1;
    /** Index of the {@link CardDb#_SET_NAME} column. */
    private int col_setName = -1;
	/** Index of the {@link CardDb#_COIN} column. */
	private int col_plusCoin = -1;
	/** Index of the {@link CardDb#_VICTORY} column. */
	private int col_plusVP = -1;
	/** Index of the {@link CardDb#_BUY} column. */
	private int col_plusBuy = -1;
	/** Index of the {@link CardDb#_CARD} column. */
	private int col_plusCard = -1;
	/** Index of the {@link CardDb#_ACT} column. */
	private int col_plusAct = -1;
	/** Index of the {@link CardDb#_ID} column. */
	private int col_id = -1;
    /** Index of the {@link CardDb#_NAME} column. */
    private int col_name = -1;
	/** Index of the {@link CardDb#_REQ} column. */
	private int col_req = -1;

	/** The id of the young witch's bane card (-1 if no bane) */
	private long yw_bane = -1;

    /** In the main display loop this is used to store string values temporarily */
    private String loopStr;
    /** In the main display loop this is used to store int values temporarily */
    private int loopInt;

    /** Default constructor.
     *  @param context The application context
     *  @param showCardColor True if the background color of the card should be shown. */
	public AdapterCards(Context context, boolean showCardColor) {
		super(context, R.layout.list_item_card,
                new String[]{CardDb._ID, CardDb._ID, CardDb._NAME, CardDb._COST, CardDb._POT,
                        CardDb._SET_ID, CardDb._SET_NAME, CardDb._TYPE, CardDb._REQ,
                        CardDb._COIN, CardDb._VICTORY, CardDb._BUY,
                        CardDb._DESC},
                new int[]{R.id.card_special, R.id.card_special_2, R.id.card_name, R.id.card_cost, R.id.card_potion,
                        R.id.card_set, R.id.card_set, R.id.card_type, R.id.card_requires,
                        R.id.card_res_gold, R.id.card_res_victory, R.id.card_res,
                        R.id.card_desc});
        this.setViewBinder(this);
        resources = context.getResources();
        setSelectionColor(ContextCompat.getColor(context, R.color.card_list_select));

        // Load the expansion icons if they haven't been loaded.
        if(exp_icons == null)
			exp_icons = Utils.getDrawableResources(context, R.array.card_set_icons);
	}
	
	
	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		if(cursor == null) return;
		// get the columns from the cursor
        col_id = cursor.getColumnIndex(CardDb._ID);
        col_name = cursor.getColumnIndex(CardDb._NAME);
		col_cost = cursor.getColumnIndex(CardDb._COST);
		col_potion = cursor.getColumnIndex(CardDb._POT);
		col_set = cursor.getColumnIndex(CardDb._SET_ID);
        col_setName = cursor.getColumnIndex(CardDb._SET_NAME);
		col_plusCoin = cursor.getColumnIndex(CardDb._COIN);
		col_plusVP = cursor.getColumnIndex(CardDb._VICTORY);
		col_plusBuy = cursor.getColumnIndex(CardDb._BUY);
		col_plusCard = cursor.getColumnIndex(CardDb._CARD);
		col_plusAct = cursor.getColumnIndex(CardDb._ACT);
		col_desc = cursor.getColumnIndex(CardDb._DESC);
        col_req = cursor.getColumnIndex(CardDb._REQ);
	}
	
	
	/** Binds the contents of the Cursor to View elements provided by this adapter.
     *  When a value is bound by this method, it returns {@code true},
     *  so that no other binding is performed. If it returns false,
     *  the value is bound by the default SimpleCursorAdapter methods.
	 *  @param view the view to bind the data to.
	 *  @param cursor the cursor to get the data from
     *  (it has been moved to the appropriate position for this row).
	 *  @param columnIndex the index of the column that is being bound right now.
	 *  @return {@code true} if that column was bound to the view, {@code false} otherwise. */
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        // Basic string mapping, with hide on "0" (values like "3*" exist)
        if (col_cost == columnIndex || col_plusCoin == columnIndex
                || col_plusVP == columnIndex) {
            if ("0".equals(cursor.getString(columnIndex)))
                view.setVisibility(View.GONE);
            else view.setVisibility(View.VISIBLE);
            return false;

        // Basic hide/show
        } else if (col_potion == columnIndex) {
            if (cursor.getInt(columnIndex) != 0)
                view.setVisibility(View.VISIBLE);
            else view.setVisibility(View.GONE);
            return true;

        // map expansion to icon
        } else if (col_set == columnIndex) {
            loopInt = 0;
            try { loopInt = exp_icons[cursor.getInt(col_set)];
            } catch(Exception ignored){}
            if (loopInt == 0) loopInt = R.drawable.ic_set_unknown;
            ((ImageView)view).setImageResource(loopInt);
            return true;

        // Expansion name to icon description
        } else if(col_setName == columnIndex) {
            view.setContentDescription(cursor.getString(columnIndex));
            return true;

        // All the resources after the icon bonuses
        } else if (col_plusBuy == columnIndex) {
            loopStr = "";
            // + buy
            loopInt = cursor.getInt(col_plusBuy);
            if (loopInt != 0)
                loopStr += ", "+resources.getQuantityString(R.plurals.format_buy, loopInt, loopInt);
            // + card
            loopInt = cursor.getInt(col_plusCard);
            if (loopInt != 0)
                loopStr += ", "+resources.getQuantityString(R.plurals.format_card, loopInt, loopInt);
            // + action
            loopInt = cursor.getInt(col_plusAct);
            if (loopInt != 0)
                loopStr += ", "+resources.getQuantityString(R.plurals.format_act, loopInt, loopInt);
            // Only show the result if we have a result.
            if (2 < loopStr.length()) {
                // Trim off the first ", "
                loopStr = loopStr.substring(2);
                view.setVisibility(View.VISIBLE);
                ((TextView) view).setText(loopStr);
            } else view.setVisibility(View.GONE);
            return true;

        // Hide if empty.
        } else if (col_desc == columnIndex) {
            loopStr = cursor.getString(columnIndex);
            if (loopStr == null || "".equals(loopStr)) view.setVisibility(View.GONE);
            else view.setVisibility(View.VISIBLE);
            return false;

        // Show on match
        } else if (col_id == columnIndex) {
            if (yw_bane != cursor.getLong(col_id))
                view.setVisibility(View.GONE);
            else view.setVisibility(View.VISIBLE);
            return true;

        // Hide if empty, use format string
        } else if (col_req == columnIndex) {
            loopStr = cursor.getString(columnIndex);
            if (loopStr == null || "".equals(loopStr)) view.setVisibility(View.GONE);
            else view.setVisibility(View.VISIBLE);
            ((TextView) view).setText(String.format(mContext.getString(R.string.format_req),
                                      loopStr));
            return true;
        }

        // All other columns should be handled the default ways.
		return false;
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		toggleItem(position);
	}


	/** Set the bane card in this adapter. Note that if
     *  Set the cursor with {@link #changeCursor(Cursor)} first! */
	public void setBane(long card_id) {
        yw_bane = card_id;
        if(getChoiceMode() != CHOICE_MODE_MULTIPLE)
            setChoiceMode(CHOICE_MODE_MULTIPLE);
        selectItem(card_id);
	}

    /** Get the name of the card at the given position */
    public String getName(int position) {
        if(mCursor == null || !mCursor.moveToPosition(position))
            return null;
        return mCursor.getString(col_name);
    }
}