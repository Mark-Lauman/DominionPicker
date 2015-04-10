/* Copyright (c) 2014 Mark Christopher Lauman
 * 
 * Licensed under the The MIT License (MIT)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.                                                                  */
package ca.marklauman.dominionpicker;

import java.util.ArrayList;
import java.util.HashMap;

import ca.marklauman.dominionpicker.database.CardDb;
import ca.marklauman.tools.CursorSelAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

/** Adapter used to display cards from the {@link ca.marklauman.dominionpicker.database.Provider}
 *  ContentProvider.
 *  @author Mark Lauman                                  */
class CardAdapter extends CursorSelAdapter
						 implements OnItemClickListener,
						 			ViewBinder {
	
	/** Maps expansion names to expansion icons */
	private static HashMap<String, Integer> exp_icons = null;

    /** Format string for +1 Action */
    private final String formatAct;
    /** Format string for more than +1 Action */
    private final String formatActs;
    /** Format string for +1 Buy */
    private final String formatBuy;
    /** Format string for more than +1 Buy */
    private final String formatBuys;
    /** Format string for +1 Card */
    private final String formatCard;
    /** Format string for more than +1 Card */
    private final String formatCards;
	
	/** Index of the {@link ca.marklauman.dominionpicker.database.CardDb#_DESC} column. */
	private int col_desc = -1;
	/** Index of the {@link ca.marklauman.dominionpicker.database.CardDb#_COST} column. */
	private int col_cost = -1;
	/** Index of the {@link ca.marklauman.dominionpicker.database.CardDb#_POTION} column. */
	private int col_potion = -1;
	/** Index of the {@link ca.marklauman.dominionpicker.database.CardDb#_EXP} column. */
	private int col_expansion = -1;
	/** Index of the {@link ca.marklauman.dominionpicker.database.CardDb#_GOLD} column. */
	private int col_gold = -1;
	/** Index of the {@link ca.marklauman.dominionpicker.database.CardDb#_VICTORY} column. */
	private int col_victory = -1;
	/** Index of the {@link ca.marklauman.dominionpicker.database.CardDb#_BUY} column. */
	private int col_buy = -1;
	/** Index of the {@link ca.marklauman.dominionpicker.database.CardDb#_DRAW} column. */
	private int col_draw = -1;
	/** Index of the {@link ca.marklauman.dominionpicker.database.CardDb#_ACTION} column. */
	private int col_act = -1;
	/** Index of the {@link ca.marklauman.dominionpicker.database.CardDb#_ID} column. */
	private int col_id = -1;

	/** The bane card of the young witch (-1 if no bane) */
	private long yw_bane = -1;
	
	public CardAdapter(Context context) {
		super(context, R.layout.list_item_card,
			  new String[]{CardDb._ID,
						   CardDb._NAME,
						   CardDb._COST,
						   CardDb._POTION,
						   CardDb._EXP,
						   CardDb._CATEGORY,
						   CardDb._GOLD,
						   CardDb._BUY,
						   CardDb._DESC,
						   CardDb._VICTORY},
			  new int[]{R.id.card_special,
						R.id.card_title,
				        R.id.card_cost,
				        R.id.card_potion,
				        R.id.card_set,
				        R.id.card_cat,
				        R.id.card_res_gold,
				        R.id.card_res,
				        R.id.card_desc,
				        R.id.card_res_victory});

        // format strings
        formatAct   = context.getString(R.string.format_act);
        formatActs  = context.getString(R.string.format_acts);
        formatBuy   = context.getString(R.string.format_buy);
        formatBuys  = context.getString(R.string.format_buys);
        formatCard  = context.getString(R.string.format_card);
        formatCards = context.getString(R.string.format_cards);

		staticSetup(context);
		this.setViewBinder(this);
	}
	
	/** Retrieve static resources if needed (these only
	 *  need to be retrieved once per context).
	 *  @param c The context of the application.     */
	private static void staticSetup(Context c) {
		if(exp_icons != null)
			return;
		exp_icons = new HashMap<>();
		String[] sets = c.getResources()
						 .getStringArray(R.array.card_sets);
		
		int[] icons = getDrawables(c, R.array.card_set_icons);
		int len = sets.length;
		if(icons.length < len) len = icons.length;
		for(int i = 0; i < len; i++)
			exp_icons.put(sets[i], icons[i]);
	}
	
	
	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		if(cursor == null) return;
		// get the columns from the cursor
		col_cost = cursor.getColumnIndex(CardDb._COST);
		col_potion = cursor.getColumnIndex(CardDb._POTION);
		col_expansion = cursor.getColumnIndex(CardDb._EXP);
		col_gold = cursor.getColumnIndex(CardDb._GOLD);
		col_victory = cursor.getColumnIndex(CardDb._VICTORY);
		col_buy = cursor.getColumnIndex(CardDb._BUY);
		col_draw = cursor.getColumnIndex(CardDb._DRAW);
		col_act = cursor.getColumnIndex(CardDb._ACTION);
		col_desc = cursor.getColumnIndex(CardDb._DESC);
		col_id = cursor.getColumnIndex(CardDb._ID);
	}
	
	
	/** Binds the contents of the Cursor to View elements
	 *  provided by this adapter. When a value is bound
	 *  by this method, it returns {@code true}, so
	 *  that no other binding is performed. If it
	 *  returns false, the value is bound by the default
	 *  SimpleCursorAdapter methods.
	 *  @param view the view to bind the data to.
	 *  @param cursor the cursor to get the data from
	 *  (it has been moved to the appropriate position
	 *  for this row).
	 *  @param columnIndex the index of the column
	 *  that is being bound right now.
	 *  @return {@code true} if that column was bound to
	 *  the view, {@code false} otherwise.            */
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(col_cost == columnIndex
				|| col_gold == columnIndex
				|| col_victory == columnIndex) {
			if("0".equals(cursor.getString(columnIndex)))
				view.setVisibility(View.GONE);
			else view.setVisibility(View.VISIBLE);
			return false;
			
		} else if(col_potion == columnIndex) {
			if(1 > cursor.getInt(columnIndex))
				view.setVisibility(View.GONE);
			else view.setVisibility(View.VISIBLE);
			return true;
			
		} else if(col_expansion == columnIndex) {
			String val = cursor.getString(col_expansion);
			ImageView v = (ImageView) view;
			v.setContentDescription(val);
			Integer icon_id = exp_icons.get(val);
			if(icon_id == null) icon_id = R.drawable.ic_set_unknown;
			v.setImageResource(icon_id);
			return true;
			
		} else if(col_buy == columnIndex) {
			String res = "";
			String val = cursor.getString(col_buy);
            if(!"0".equals(val)) {
                if("1".equals(val)) res += ", " + String.format(formatBuy, val);
                else res += ", " + String.format(formatBuys, val);
            }
			val = cursor.getString(col_draw);
            if(!"0".equals(val)) {
                if("1".equals(val)) res += ", " + String.format(formatCard, val);
                else res += ", " + String.format(formatCards, val);
            }
			val = cursor.getString(col_act);
            if(!"0".equals(val)) {
                if("1".equals(val)) res += ", " + String.format(formatAct, val);
                else res += ", " + String.format(formatActs, val);
            }
			if(0!= col_gold
					&& 0 != col_victory
					&& res.length() > 2)
				res = res.substring(2);
			if("".equals(res))
				view.setVisibility(View.GONE);
			else {
				view.setVisibility(View.VISIBLE);
				TextView v = (TextView) view;
				v.setText(res);
			}
			return true;
			
		} else if(col_desc == columnIndex) {
			String desc = cursor.getString(columnIndex);
			if("".equals(desc)) {
				view.setVisibility(View.GONE);
				return true;
			}
			view.setVisibility(View.VISIBLE);
			return false;
			
		} else if(col_id == columnIndex) {
			TextView v = (TextView) view;
			if(yw_bane != cursor.getLong(col_id))
				v.setVisibility(View.GONE);
			else v.setVisibility(View.VISIBLE);
			return true;
		}
		
		return false;
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		toggleItem(position);
	}
	
	/** Set the bane card in this adapter and select it */
	public void setBane(long card_id) {
		if(mCursor == null) return;
		
		mCursor.moveToPosition(-1);
		int pos = 0;
		while(mCursor.moveToNext()) {
			if(mCursor.getLong(col_id) == card_id) {
				yw_bane = card_id;
				setChoiceMode(CHOICE_MODE_SINGLE);
				selectItem(pos);
				return;
			}
			pos++;
		}
	}
	
	
	/** Get the card with the specified id and return it.
	 *  @param id The SQL id of the card in question.
	 *  @return The card as a String array. The parameters
	 *  are in the same order as {@link ca.marklauman.dominionpicker.database.CardDb#COLS}. */
	public String[] getCard(long id) {
		String[] cols = CardDb.COLS;
		int position = getPosition(id);
		mCursor.moveToPosition(position);
		ArrayList<String> data = new ArrayList<>(cols.length);
        for (String col : cols) {
            int index = mCursor.getColumnIndex(col);
            data.add(mCursor.getString(index));
        }
		String[] res = new String[data.size()];
		data.toArray(res);
		return res;
	}
	
	/** Retrieve an array of drawable resources from
	 *  the xml of the provided {@link Context}.
	 *  @param c The {@code Context} to search for the array.
	 *  @param resourceId The resource id of an
	 *  {@code <array>} containing a list of drawables.
	 *  @return The resource ids of all the drawables
	 *  in the array, in the order in which they appear
	 *  in the xml.                                  */
	@SuppressWarnings("SameParameterValue")
    private static int[] getDrawables(Context c, int resourceId) {
		TypedArray ta = c.getResources()
				 		 .obtainTypedArray(resourceId);
		if(ta == null) return null;
		
    	int[] res = new int[ta.length()];
    	for(int i=0; i<ta.length(); i++)
    		res[i] = ta.getResourceId(i, -1);
    	
    	ta.recycle();
    	return res;
	}
}