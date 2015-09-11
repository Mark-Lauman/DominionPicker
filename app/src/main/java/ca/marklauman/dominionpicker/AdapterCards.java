package ca.marklauman.dominionpicker;

import ca.marklauman.dominionpicker.database.CardDb;
import ca.marklauman.tools.CursorSelAdapter;
import ca.marklauman.tools.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

import java.util.HashMap;

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
               CardDb._VICTORY, CardDb._REQ, CardDb._LANG};
	
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
    /** Index of the {@link CardDb#_LANG} column. */
    private int col_lang = -1;

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
                new String[]{CardDb._ID, CardDb._ID, CardDb._NAME, CardDb._COST,
                             CardDb._POT, CardDb._SET_ID, CardDb._SET_NAME, CardDb._TYPE,
                             CardDb._REQ, CardDb._COIN, CardDb._VICTORY,
                             CardDb._LANG, CardDb._DESC},
                new int[]{R.id.card_special, R.id.card_special_2, R.id.card_name, R.id.card_cost,
                          R.id.card_potion, R.id.card_set, R.id.card_set, R.id.card_type,
                          R.id.card_requires, R.id.card_res_gold, R.id.card_res_victory,
                          R.id.card_res, R.id.card_desc});
        this.setViewBinder(this);
        resources = context.getResources();
        setSelectionColor(ContextCompat.getColor(context, R.color.card_list_select));

        // Load the expansion icons.
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
        col_lang = cursor.getColumnIndex(CardDb._LANG);
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
        } else if (col_lang == columnIndex) {
            loopStr = getPlusString(cursor, cursor.getString(col_lang));
            if(! "".equals(loopStr)) {
                view.setVisibility(View.VISIBLE);
                ((TextView) view).setText(loopStr);
            } else view.setVisibility(View.GONE);
            return true;

        // Hide if empty.
        } else if (col_desc == columnIndex || col_req == columnIndex) {
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


    /** Create the display string for +X buy, +X action and +X cards
     * @param cursor The cursor positioned at the current item
     * @param lang The language that this card is in.
     * @return A string containing the "+X Action, +X Buy, +X Cards" values of this card. */
    private String getPlusString(@NonNull Cursor cursor, String lang) {
        Integer langId = mapLanguage.get(lang);
        if(langId == null) langId = 0;

        String res = "";
        res += getPlusCol(cursor, col_plusAct, resAct[langId]);
        res += getPlusCol(cursor, col_plusBuy, resBuy[langId]);
        res += getPlusCol(cursor, col_plusCard, resCard[langId]);

        if(res.length() < 2) return res;
        return res.substring(2);
    }


    private String getPlusCol(@NonNull Cursor cursor, int colIndex, int formatRes) {
        loopStr = cursor.getString(colIndex);
        try{ loopInt = Integer.parseInt(loopStr);
        }catch(Exception e){ loopInt = 1; }
        if(loopStr.length() == 0 || "0".equals(loopStr)) return "";
        return ", " + resources.getQuantityString(formatRes, loopInt, loopStr);
    }
}