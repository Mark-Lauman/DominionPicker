package ca.marklauman.dominionpicker;

import java.security.InvalidParameterException;
import java.util.HashSet;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/** Selection Utility class I made because I hate android's
 *  selection implementation. This should work for all
 *  versions of Android down to API v4.
 *  @author Mark Lauman                                  */
public class CursorSelAdapter extends SimpleCursorAdapter {
	
	/** Normal adapter that does not indicate choices. */
	public static final int CHOICE_MODE_NONE = ListView.CHOICE_MODE_NONE;
	/** The adapter allows up to one choice. */ 
	public static final int CHOICE_MODE_SINGLE = ListView.CHOICE_MODE_SINGLE;
	/** The adapter allows multiple choices.  */
	public static final int CHOICE_MODE_MULTIPLE = ListView.CHOICE_MODE_MULTIPLE;
	
	/** Background color of a tab in its normal state */
	private static int COLOR_NORM = -1;
	/** Background color of a tab when selected */
	private static int COLOR_SELECT = -1;
	
	
	/** Current choice mode.   */
	private int mChoiceMode = CHOICE_MODE_NONE;
	/** Current selections.    */
	private HashSet<Integer> mSelected = new HashSet<Integer>();
	
	
	/** Standard constructor.
	 *  @param context The context where the ListView
	 *  associated with this CursorSelAdapter is
	 *  running.
	 *  @param layout resource identifier of a layout file
	 *  that defines the views for this list item. The
	 *  layout file should include at least those named
	 *  views defined in "to"
	 *  @param from A list of column names representing
	 *  the data to bind to the UI. Can be null if the
	 *  cursor is not available yet.
	 *  @param to The views that should display column in
	 *  the "from" parameter. These should all be
	 *  TextViews. The first N views in this list are given
	 *  the values of the first N columns in the from
	 *  parameter. Can be null if the cursor is not
	 *  available yet.                                 */
	public CursorSelAdapter(Context context, int layout,
			String[] from, int[] to) {
		super(context, layout, null, from, to, 0);
		if(COLOR_NORM == -1) {
			COLOR_NORM	 = context.getResources()
								  .getColor(android.R.color.transparent);
			COLOR_SELECT = context.getResources()
								  .getColor(R.color.list_activated_holo);
		}
	}
	
	
	/** Gets the view for a specified position in the list.
	 *  In {@link SimpleCursorAdapter}s, this method is
	 *  not responsible for the inflation of the view, just
	 *  its retrieval and refreshing. Inflation is done in
	 *  {@link #newView(Context, Cursor, ViewGroup)}.
	 *  @param position The position of the item within the
	 *  adapter's data set of the item whose view we want.
	 *  @param convertView The old view to reuse, if possible.
	 *  @param parent The parent that this view will
	 *  eventually be attached to
	 *  @return A View corresponding to the data at
	 *  the specified position.                             */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View res = super.getView(position, convertView, parent);
		if(mSelected.contains(position))
			res.setBackgroundColor(COLOR_SELECT);
		else
			res.setBackgroundColor(COLOR_NORM);
		return res;
	}
	
	
	/** Get the item position paired with this id.
	 *  @param id The sql id of the item.
	 *  (from the "_id" column)
	 *  @return The position of that item in the list,
	 *  or -1 if no items exist with that id.       */
	public int getPosition(long id) {
		if(mCursor == null || !mCursor.moveToFirst())
			return -1;
		do {
			if(id == mCursor.getLong(mRowIDColumn))
				return mCursor.getPosition();
		} while(mCursor.moveToNext());
		return -1;
	}
	
	
	/** <p>Defines the choice behavior for the Adapter. By
	 *  default, Adapters do not have any choice behavior
	 *  ({@link #CHOICE_MODE_NONE}).
	 *  By setting the choiceMode to
	 *  {@link #CHOICE_MODE_SINGLE}, the Adapter allows up
	 *  to one item to be in a chosen state. By setting the
	 *  choiceMode to {@link #CHOICE_MODE_MULTIPLE}, the
	 *  list allows any number of items to be chosen.</p>
	 *  <p>Calling this method will clear all current
	 *  selections. Be sure to call {@link #getSelections()}
	 *  before this if you want to preserve your selections.</p>
	 *  @param choiceMode  One of
	 *  {@link #CHOICE_MODE_NONE},
	 *  {@link CHOICE_MODE_SINGLE}, or
	 *  {@link CHOICE_MODE_MULTIPLE}.				*/
	public void setChoiceMode(int choiceMode) {
		switch(choiceMode) {
		case CHOICE_MODE_NONE:		case CHOICE_MODE_SINGLE:
		case CHOICE_MODE_MULTIPLE:	break;
		default:	throw new InvalidParameterException("Invalid choice mode");
		}
		mChoiceMode = choiceMode;
		mSelected.clear();
		notifyDataSetChanged();
	}
	
	
	/** Gets the current ChoiceMode of this CursorSelAdapter.
	 *  @return One of {@link #CHOICE_MODE_NONE},
	 *  {@link CHOICE_MODE_SINGLE}, or
	 *  {@link CHOICE_MODE_MULTIPLE}.                      */
	public int getChoiceMode() {
		return mChoiceMode;
	}
	
	
	/** Select this item. If it is already selected
	 *  it remains so.
	 *  @param position The position of the row in the
	 *  list.										*/
	public void selectItem(int position) {
		switch(mChoiceMode) {
		case CHOICE_MODE_NONE:
			return;
		case CHOICE_MODE_SINGLE:
			mSelected.clear();
		case CHOICE_MODE_MULTIPLE: break;
		default: 	throw new IllegalStateException("Choice Mode is an invalid value: " + mChoiceMode);
		}
		mSelected.add(position);
		notifyDataSetChanged();
	}
	
	
	/** Deselects this item. If it is already deselected
	 *  then it remains so.
	 *  @param position The position of the row in the
	 *  list.                                        */
	public void deselectItem(int position) {
		mSelected.remove(position);
		notifyDataSetChanged();
	}
	
	
	/** If the list item at the given location is selected,
	 *  deselects it. If it is not selected, selects it.
	 *  @param position The position of the row in the
	 *  list.
	 *  @return {@code true} if the item is selected,
	 *  {@code false} otherwise.                   */
	public boolean toggleItem(int position) {
		switch(mChoiceMode) {
		case CHOICE_MODE_NONE:
			return false;
		case CHOICE_MODE_SINGLE:
			mSelected.clear();
		case CHOICE_MODE_MULTIPLE: break;
		default: 	throw new IllegalStateException("Choice Mode is an invalid value: " + mChoiceMode);
		}
		
		if(mSelected.contains(position))
			mSelected.remove(position);
		else mSelected.add(position);
		
		notifyDataSetChanged();
		return mSelected.contains(position);
	}
	
	
	/** Gets all selected items.
	 *  @return The ids of each selected item. There is
	 *  no guaranteed order to this list, users must sort
	 *  it themselves if necessary.                    */
	public long[] getSelections() {
		long[] res = new long[mSelected.size()];
		int i = 0;
		for(Integer pos : mSelected) {
			res[i] = getItemId(pos);
			i++;
		}
		return res;
	}
	
	
	/** Sets the selected items. Be sure to set the choice
	 *  mode (using {@link #setChoiceMode(int)})
	 *  before calling this!
	 *  @param selections The sql ids of the items to select.
	 *  If {@link #CHOICE_MODE_NONE}, nothing is selected.
	 *  If {@link #CHOICE_MODE_SINGLE}, only the last valid
	 *  item is selected.
	 *  If {@link #CHOICE_MODE_MULTIPLE}, all valid items
	 *  are selected.
	 *  Items are considered valid if they
	 *  are in the list.                                 */
	public void setSelections(long... selections) {
		clearSelections();
		if(selections == null
				|| selections.length == 0
				|| mChoiceMode == CHOICE_MODE_NONE)
			return;
		for(long sel : selections) {
			int pos = getPosition(sel);
			if(pos >= 0) selectItem(pos);
		}
	}
	
	
	/** All items are deselected. Selection mode is unchanged */
	public void clearSelections() {
		mSelected.clear();
		notifyDataSetChanged();
	}
}