package ca.marklauman.dominionpicker;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
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
	
	public CardAdapter(Context context) {
		super(context, R.layout.list_item_card,
			  new String[]{CardList._NAME,
						   CardList._COST,
						   CardList._POTION,
						   CardList._EXP,
						   CardList._CATEGORY,
						   CardList._GOLD,
						   CardList._BUY,
						   CardList._DESC,
						   CardList._VICTORY},
			  new int[]{R.id.card_title,
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
		int[] icons = {R.drawable.ic_set_base,
					   R.drawable.ic_set_alchemy,
					   R.drawable.ic_set_black_market,
					   R.drawable.ic_set_cornucopia,
					   R.drawable.ic_set_dark_ages,
					   R.drawable.ic_set_envoy,
					   R.drawable.ic_set_governor,
					   R.drawable.ic_set_hinterlands,
					   R.drawable.ic_set_intrigue,
					   R.drawable.ic_set_prosperity,
					   R.drawable.ic_set_seaside,
					   R.drawable.ic_set_stash,
					   R.drawable.ic_set_walled_village};
		int len = sets.length;
		if(icons.length < len) len = icons.length;
		for(int i = 0; i < len; i++)
			exp_icons.put(sets[i], icons[i]);
	}
	
	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		if(cursor == null) return;
		for(int i = 0; i < cursor.getCount(); i++)
			selectItem(i);
		col_cost = cursor.getColumnIndex(CardList._COST);
		col_potion = cursor.getColumnIndex(CardList._POTION);
		col_expansion = cursor.getColumnIndex(CardList._EXP);
		col_gold = cursor.getColumnIndex(CardList._GOLD);
		col_vict = cursor.getColumnIndex(CardList._VICTORY);
		col_buy = cursor.getColumnIndex(CardList._BUY);
		col_draw = cursor.getColumnIndex(CardList._DRAW);
		col_act = cursor.getColumnIndex(CardList._ACTION);
		col_desc = cursor.getColumnIndex(CardList._DESC);
	}
	
	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if(col_cost == columnIndex
				|| col_gold == columnIndex
				|| col_vict == columnIndex) {
			if("0".equals(cursor.getString(columnIndex)))
				view.setVisibility(View.GONE);
			else view.setVisibility(View.VISIBLE);
			return false;
		}
		if(col_potion == columnIndex) {
			if(1 > cursor.getInt(columnIndex))
				view.setVisibility(View.GONE);
			else view.setVisibility(View.VISIBLE);
			return true;
		}
		if(col_expansion == columnIndex) {
			String val = cursor.getString(col_expansion);
			ImageView v = (ImageView) view;
			v.setContentDescription(val);
			Integer icon_id = exp_icons.get(val);
			if(icon_id == null) icon_id = R.drawable.ic_set_unknown;
			v.setImageResource(icon_id);
			return true;
		}
		if(col_buy == columnIndex) {
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
		}
		if(col_desc == columnIndex) {
			String desc = cursor.getString(columnIndex);
			if("".equals(desc)) {
				view.setVisibility(View.GONE);
				return true;
			}
			view.setVisibility(View.VISIBLE);
			return false;
		}
		return false;
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		toggleItem(position);
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
}