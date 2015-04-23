package ca.marklauman.dominionpicker;

import java.util.HashMap;

import ca.marklauman.dominionpicker.database.CardDb;
import ca.marklauman.tools.CursorSelAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

/** Adapter used to display cards from the {@link CardDb}.
 *  @author Mark Lauman */
class CardAdapter extends CursorSelAdapter
						 implements OnItemClickListener,
						 			ViewBinder {
	
	/** Maps expansion names to expansion icons */
	private static HashMap<String, Integer> exp_icons = null;

    /** Resources used to format strings */
    private final Resources resources;
	
	/** Index of the {@link CardDb#_DESC} column. */
	private int col_desc = -1;
	/** Index of the {@link CardDb#_COST} column. */
	private int col_cost = -1;
	/** Index of the {@link CardDb#_POTION} column. */
	private int col_potion = -1;
	/** Index of the {@link CardDb#_EXP} column. */
	private int col_expansion = -1;
	/** Index of the {@link CardDb#_GOLD} column. */
	private int col_gold = -1;
	/** Index of the {@link CardDb#_VICTORY} column. */
	private int col_victory = -1;
	/** Index of the {@link CardDb#_BUY} column. */
	private int col_buy = -1;
	/** Index of the {@link CardDb#_DRAW} column. */
	private int col_draw = -1;
	/** Index of the {@link CardDb#_ACTION} column. */
	private int col_act = -1;
	/** Index of the {@link CardDb#_ID} column. */
	private int col_id = -1;
    /** Index of the {@link CardDb#_NAME} column. */
    private int col_name = -1;

	/** The bane card of the young witch (-1 if no bane) */
	private long yw_bane = -1;
	
	public CardAdapter(Context context) {
		super(context, R.layout.list_item_card,
			  new String[]{CardDb._ID, CardDb._NAME, CardDb._COST, CardDb._POTION,
						   CardDb._EXP, CardDb._CATEGORY, CardDb._GOLD, CardDb._BUY,
						   CardDb._DESC, CardDb._VICTORY},
			  new int[]{R.id.card_special, R.id.card_title, R.id.card_cost, R.id.card_potion,
				        R.id.card_set, R.id.card_cat, R.id.card_res_gold, R.id.card_res,
				        R.id.card_desc, R.id.card_res_victory});
        this.setViewBinder(this);
        resources = context.getResources();
        setSelectionColor(context.getResources().getColor(R.color.card_list_select));

        // Load the expansion icons if they haven't been loaded.
        if(exp_icons != null) return;
        exp_icons = new HashMap<>();
        String[] sets = resources.getStringArray(R.array.card_sets);
        int[] icons = getDrawables(context, R.array.card_set_icons);
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
        col_name = cursor.getColumnIndex(CardDb._NAME);
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
        // Basic string mapping, with hide on "0" (values like "3*" exist)
		if(col_cost == columnIndex || col_gold == columnIndex
				|| col_victory == columnIndex) {
			if("0".equals(cursor.getString(columnIndex)))
				view.setVisibility(View.GONE);
			else view.setVisibility(View.VISIBLE);
			return false;

        // Basic hide/show
		} else if(col_potion == columnIndex) {
			if(cursor.getInt(columnIndex) != 0)
                view.setVisibility(View.VISIBLE);
            else view.setVisibility(View.GONE);
			return true;

        // map expansion to icon
		} else if(col_expansion == columnIndex) {
			String val = cursor.getString(col_expansion);
			ImageView v = (ImageView) view;
			v.setContentDescription(val);
			Integer icon_id = exp_icons.get(val);
			if(icon_id == null) icon_id = R.drawable.ic_set_unknown;
			v.setImageResource(icon_id);
			return true;

        // All the resources after the icon bonuses
		} else if(col_buy == columnIndex) {
			String res = "";
            // + buy
			int val = cursor.getInt(col_buy);
            if(val != 0) res += ", " + resources.getQuantityString(R.plurals.format_buy, val, val);
            // + card
			val = cursor.getInt(col_draw);
            if(val != 0) res += ", " + resources.getQuantityString(R.plurals.format_card, val, val);
            // + action
			val = cursor.getInt(col_act);
            if(val != 0) res += ", " + resources.getQuantityString(R.plurals.format_act, val, val);
			// Only show the result if we have a result.
			if(2 < res.length()) {
                // Trim off the first ", "
                res = res.substring(2);
                view.setVisibility(View.VISIBLE);
                ((TextView) view).setText(res);
            } else view.setVisibility(View.GONE);
			return true;

        // Basic string mapping with hide on ""
		} else if(col_desc == columnIndex) {
			if("".equals(cursor.getString(columnIndex)))
				view.setVisibility(View.GONE);
            else view.setVisibility(View.VISIBLE);
			return false;

        // Show only on match
		} else if(col_id == columnIndex) {
			if(yw_bane != cursor.getLong(col_id))
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
	
	
//	/** Get the card with the specified id and return it.
//	 *  @param id The SQL id of the card in question.
//	 *  @return The card as a String array. The parameters
//	 *  are in the same order as {@link CardDb#COLS}. */
//	public String[] getCard(long id) {
//		String[] cols = CardDb.COLS;
//		int position = getPosition(id);
//		mCursor.moveToPosition(position);
//		ArrayList<String> data = new ArrayList<>(cols.length);
//        for (String col : cols) {
//            int index = mCursor.getColumnIndex(col);
//            data.add(mCursor.getString(index));
//        }
//		String[] res = new String[data.size()];
//		data.toArray(res);
//		return res;
//	}


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