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

/** Adapter used to display cards from the {@link CardList}
 *  ContentProvider.
 *  @author Mark Lauman                                  */
public class CardAdapter extends CursorSelAdapter
						 implements OnItemClickListener,
						 			ViewBinder {
	
	/** Maps expansion names to expansion icons */
	private static HashMap<String, Integer> exp_icons = null;
	
	/** Index of the {@link CardList#_DESC} column. */
	private int col_desc = -1;
	/** Index of the {@link CardList#_COST} column. */
	private int col_cost = -1;
	/** Index of the {@link CardList#_POTION} column. */
	private int col_potion = -1;
	/** Index of the {@link CardList#_EXP} column. */
	private int col_expansion = -1;
	/** Index of the {@link CardList#_GOLD} column. */
	private int col_gold = -1;
	/** Index of the {@link CardList#_VICTORY} column. */
	private int col_vict = -1;
	/** Index of the {@link CardList#_BUY} column. */
	private int col_buy = -1;
	/** Index of the {@link CardList#_DRAW} column. */
	private int col_draw = -1;
	/** Index of the {@link CardList#_ACTION} column. */
	private int col_act = -1;
	/** Index of the {@link CardList#_ID} column. */
	private int col_id = -1;
	
	/** Number of viable young witch targets selected */
	private int qty_yw_targets = 0;
	/** The bane card of the young witch (-1 if no bane) */
	private long yw_bane = -1;
	
	public CardAdapter(Context context) {
		super(context, R.layout.list_item_card,
			  new String[]{CardList._ID,
						   CardList._NAME,
						   CardList._COST,
						   CardList._POTION,
						   CardList._EXP,
						   CardList._CATEGORY,
						   CardList._GOLD,
						   CardList._BUY,
						   CardList._DESC,
						   CardList._VICTORY},
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
		staticSetup(context);
		this.setViewBinder(this);
	}
	
	/** Retrieve static resources if needed (these only
	 *  need to be retrieved once per context).
	 *  @param c The context of the application.     */
	private static void staticSetup(Context c) {
		if(exp_icons != null)
			return;
		exp_icons = new HashMap<String, Integer>();
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
		col_cost = cursor.getColumnIndex(CardList._COST);
		col_potion = cursor.getColumnIndex(CardList._POTION);
		col_expansion = cursor.getColumnIndex(CardList._EXP);
		col_gold = cursor.getColumnIndex(CardList._GOLD);
		col_vict = cursor.getColumnIndex(CardList._VICTORY);
		col_buy = cursor.getColumnIndex(CardList._BUY);
		col_draw = cursor.getColumnIndex(CardList._DRAW);
		col_act = cursor.getColumnIndex(CardList._ACTION);
		col_desc = cursor.getColumnIndex(CardList._DESC);
		col_id = cursor.getColumnIndex(CardList._ID);
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
				|| col_vict == columnIndex) {
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
			if(!"0".equals(val)) res += ", +" + val + " buy";
			val = cursor.getString(col_draw);
			if(!"0".equals(val)) res += ", +" + val + " card";
			val = cursor.getString(col_act);
			if(!"0".equals(val)) res += ", +" + val + " action";
			if(!"0".equals(col_gold)
					&& !"0".equals(col_vict)
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
	
	@Override
	public void selectItem(int position) {
		super.selectItem(position);
		updateStats(position, true);
	}
	
	@Override
	public void deselectItem(int position) {
		super.deselectItem(position);
		updateStats(position, false);
	}
	
	@Override
	public void selectAll() {
		super.selectAll();
		clearStats();
		if(mCursor == null) return;
		for(int card=0; card<mCursor.getCount(); card++)
			updateStats(card, true);
	}
	
	@Override
	public void deselectAll() {
		super.deselectAll();
		clearStats();
	}
	
	@Override
	public boolean toggleAll() {
		boolean res = super.toggleAll();
		clearStats();
		for(int card : mSelected)
			updateStats(card, true);
		return res;
	}
	
	
	/** Update the statistics variables. Called when
	 *  one selection is changed.
	 *  @param cardPosition Position of the card to
	 *  add/remove from the statistics.
	 * @param add {@code true} of this card is added,
	 * {@code false} if this card is to be removed. */
	private void updateStats(int cardPosition, boolean add) {
		// update stats from the card
		if(mCursor == null
				|| !mCursor.moveToPosition(cardPosition))
			return;
		String cost = mCursor.getString(col_cost);
		if("2".equals(cost) || "3".equals(cost)) {
			if(add) qty_yw_targets++;
			else qty_yw_targets--;
		}
	}
	
	/** Reset all statistics variables to 0 */
	private void clearStats() {
		qty_yw_targets = 0;
	}
	
	/** Check how many young witch targets are available */
	public int checkYWitchTargets() {
		return qty_yw_targets;
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
	 *  are in the same order as {@link CardList#COLS}. */
	public String[] getCard(long id) {
		String[] cols = CardList.COLS;
		int position = getPosition(id);
		this.mCursor.moveToPosition(position);
		ArrayList<String> data = new ArrayList<String>(cols.length);
		for(int i = 0; i < cols.length; i++) {
			int index = mCursor.getColumnIndex(cols[i]);
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
	public static int[] getDrawables(Context c, int resourceId) {
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