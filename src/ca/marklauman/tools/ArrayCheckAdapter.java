/* Copyright (c) 2014 Mark Christopher Lauman
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.                                        */
package ca.marklauman.tools;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/** Selection Utility class I made because I hate android's
 *  selection implementation. This should work for all
 *  versions of Android down to API v4.
 *  @author Mark Lauman                                  */
public class ArrayCheckAdapter<T> extends ArrayAdapter<T> {
	
	/** Normal adapter that does not indicate choices. */
	public static final int CHOICE_MODE_NONE = ListView.CHOICE_MODE_NONE;
	/** The adapter allows up to one choice. */ 
	public static final int CHOICE_MODE_SINGLE = ListView.CHOICE_MODE_SINGLE;
	/** The adapter allows multiple choices.  */
	public static final int CHOICE_MODE_MULTIPLE = ListView.CHOICE_MODE_MULTIPLE;

	/** Background color of a selected item. */
	private Integer color_select = null;
	/** Background color of a deselected item. */
	private Integer color_deselect = null;
	
	/** Current choice mode.   */
	private int mChoiceMode = CHOICE_MODE_NONE;
	/** Last selected item (used in single choice mode) */
	private int last_select = -1;
	/** Current selections.    */
	private ArrayList<Boolean> mSelected;
	/** Icon resource ids for the list entries. */
	private int[] mIcons;
	
	
	// DEFAULT SUPER CONSTRUCTORS \\
	//   all call setupSelect()   \\
	public ArrayCheckAdapter(Context context, int resource) {
		super(context, resource);
		setupSelect(0);
	}
	public ArrayCheckAdapter(Context context, int resource,
			int textViewResourceId) {
		super(context, resource, textViewResourceId);
		setupSelect(0);
	}
	public ArrayCheckAdapter(Context context, int resource,
			int textViewResourceId, List<T> objects) {
		super(context, resource, textViewResourceId, objects);
		if(objects == null) setupSelect(0);
		else setupSelect(objects.size());
	}
	public ArrayCheckAdapter(Context context, int resource,
			int textViewResourceId, T[] objects) {
		super(context, resource, textViewResourceId, objects);
		if(objects == null) setupSelect(0);
		else setupSelect(objects.length);
	}
	public ArrayCheckAdapter(Context context, int resource, List<T> objects) {
		super(context, resource, objects);
		if(objects == null) setupSelect(0);
		else setupSelect(objects.size());
	}
	public ArrayCheckAdapter(Context context, int resource, T[] objects) {
		super(context, resource, objects);
		if(objects == null) setupSelect(0);
		else setupSelect(objects.length);
	}
	private void setupSelect(int length) {
		mSelected = new ArrayList<Boolean>(length);
		for(int i=0; i<length; i++)
			mSelected.add(false);
	}
	// END CONSTRUCTORS \\
	
	
	@Override
	public void add(T object) {
		mSelected.add(false);
		super.add(object);
	}

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void addAll(Collection<? extends T> collection) {
		if(collection == null) return;
		ArrayList<Boolean> sels = new ArrayList<Boolean>(collection.size());
		for(int i=0; i<sels.size(); i++)
			sels.set(i, false);
		mSelected.addAll(sels);
		super.addAll(collection);
	}
	
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressWarnings("unchecked")
	public void addAll(T... items) {
		if(items == null) return;
		ArrayList<Boolean> sels = new ArrayList<Boolean>(items.length);
		for(int i=0; i<sels.size(); i++)
			sels.set(i, false);
		mSelected.addAll(sels);
		super.addAll(items);
	}
	
	@Override
	public void clear() {
		mSelected.clear();
		super.clear();
	}
	
	@Override
	public void insert(T object, int index) {
		mSelected.add(index, false);
		super.insert(object, index);
	}
	
	@Override
	public void remove(T object) {
		int pos = super.getPosition(object);
		mSelected.remove(pos);
		super.remove(object);
	}
	
	
	public void sort(Comparator<? super T> comparator) {
		throw new RuntimeException("sort() is not supported on ArrayCheckAdapter");
	}
	
	
	/** When selecting items, toggle the background
	 *  color of an item between these values. If this
	 *  function is not called, the background will
	 *  not be used as a selection indicator.
	 *  @param selected The color corresponding to
	 *  a selected item (not an android id value).
	 * @param deselected The color corresponding to
	 *  a deselected item (not an android id value). */
	public void setBackgroundColors(int selected, int deselected) {
		color_select   = selected;
		color_deselect = deselected;
	}
	
	
	/** Set an array of drawables to act as icons
	 *  for list items. If this is set and an
	 *  {@link ImageView} with the id
	 *  {@code @android:id/icon} is inside this
	 *  adapter's view, then that drawable will be
	 *  assigned to that {@code ImageView}. If not,
	 *  {@code @android:id/text1} will be given a
	 *  drawable to its left.
	 *  @param icons An array containing the resource
	 *  ids of the drawables to use (one per list
	 *  entry).                                    */
	public void setIcons(int[] icons) {
		mIcons = icons;
	}
	
	
	/** Gets the view for a specified position in the list.
	 *  @param convertView The old view to reuse, if possible.
	 *  @param parent The parent that this view will
	 *  eventually be attached to.
	 *  @return A View corresponding to the data at
	 *  the specified position.                             */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View res = super.getView(position, convertView, parent);
		
		// list entry icons
		if(mIcons != null && position < mIcons.length) {
			View icon = res.findViewById(android.R.id.icon);
			TextView text1 = (TextView) res.findViewById(android.R.id.text1);
			if(icon != null && icon instanceof ImageView) {
				((ImageView)icon).setImageResource(mIcons[position]);
			} else if(text1 != null) {
				text1.setCompoundDrawablesWithIntrinsicBounds(
						mIcons[position],0,0,0);
			}
		}
		
		if(mSelected.get(position))
			selectView(res, true);
		else
			selectView(res, false);
		return res;
	}
	
	/** Make the provided View de/selected.
	 *  @param view The view to make de/selected.
	 *  @param select True if the view is selected.
	 *  False, if deselected.
	 *  @return The changed view.                */
	private void selectView(View view, boolean select) {
		View check = view.findViewById(android.R.id.checkbox);
		View text1 = view.findViewById(android.R.id.text1);
		
		// checkbox toggle
		if(check != null && check instanceof CheckBox)
			((CheckBox) check).setChecked(select);
		if(text1 != null && text1 instanceof CheckedTextView)
			((CheckedTextView) text1).setChecked(select);
		
		// background toggle
		if(select && color_select != null)
			view.setBackgroundColor(color_select);
		else if(!select && color_deselect != null)
			view.setBackgroundColor(color_deselect);
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
		deselectAll();
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
			if(0 <= last_select)
				mSelected.set(last_select, false);
			last_select = position;
		case CHOICE_MODE_MULTIPLE: break;
		default: 	throw new IllegalStateException("Choice Mode is an invalid value: " + mChoiceMode);
		}
		mSelected.set(position, true);
		notifyDataSetChanged();
	}
	
	
	/** Deselects this item. If it is already deselected
	 *  then it remains so.
	 *  @param position The position of the row in the
	 *  list.                                        */
	public void deselectItem(int position) {
		mSelected.set(position, false);
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
			deselectAll();
		case CHOICE_MODE_MULTIPLE: break;
		default: 	throw new IllegalStateException("Choice Mode is an invalid value: " + mChoiceMode);
		}
		
		mSelected.set(position, !mSelected.get(position));
		notifyDataSetChanged();
		return mSelected.get(position);
	}
	
	
	/** All items are selected.
	 *  Choice mode changes to
	 *  {@link #CHOICE_MODE_MULTIPLE}. */
	public void selectAll() {
		mChoiceMode = CHOICE_MODE_MULTIPLE;
		for(int i = 0; i < getCount(); i++)
			mSelected.set(i, true);
		notifyDataSetChanged();
	}
	
	
	/** All items are deselected. Choice mode is unchanged */
	public void deselectAll() {
		last_select = -1;
		for(int i = 0; i < getCount(); i++)
			mSelected.set(i, false);
		notifyDataSetChanged();
	}
	
	
	/** All items in the list are selected. If they are already
	 *  all selected, then everything is deselected instead.
	 *  Choice mode changes to {@link #CHOICE_MODE_MULTIPLE}
	 *  regardless.
	 * @return {@code true} if all items are selected.   */
	public boolean toggleAll() {
		mChoiceMode = CHOICE_MODE_MULTIPLE;
		
		// check all items are selected
		boolean selected = true;
		for(int i = 0; selected && i < getCount(); i++)
			selected = mSelected.get(i);
		
		// apply the change
		if(selected) deselectAll();
		else selectAll();
		return !selected;
	}
	
	/** Gets all selected items.
	 *  @return The positions of each selected item.
	 *  There is no guaranteed order to this list,
	 *  users must sort it themselves if necessary. */
	public Integer[] getSelections() {
		ArrayList<Integer> res = new ArrayList<Integer>();
		for(int i=0; i<mSelected.size(); i++) {
			if(mSelected.get(i))
				res.add(i);
		}
		
		Integer[] resArr = new Integer[res.size()];
		res.toArray(resArr);
		return resArr;
	}
	
	
	/** Gets all deselected items.
	 *  @return The positions of each deselected item.
	 *  There is no guaranteed order to this list,
	 *  users must sort it themselves if necessary. */
	public Integer[] getDeselections() {
		ArrayList<Integer> res = new ArrayList<Integer>();
		for(int i=0; i<mSelected.size(); i++) {
			if(!mSelected.get(i))
				res.add(i);
		}
		
		Integer[] resArr = new Integer[res.size()];
		res.toArray(resArr);
		return resArr;
	}
	
	
	/** Sets the selected items. Be sure to set the
	 *  choice mode (using {@link #setChoiceMode(int)})
	 *  before calling this!
	 *  @param selections The positions to select
	 *  If {@link #CHOICE_MODE_NONE}, nothing is selected.
	 *  If {@link #CHOICE_MODE_SINGLE}, only the last valid
	 *  item is selected.
	 *  If {@link #CHOICE_MODE_MULTIPLE}, all valid items
	 *  are selected.
	 *  Items are considered valid if they
	 *  are in the list.                                 */
	public void setSelections(int... selections) {
		deselectAll();
		if(selections == null
				|| selections.length == 0
				|| mChoiceMode == CHOICE_MODE_NONE)
			return;
		
		for(int sel : selections) 
			mSelected.set(sel, true);
		notifyDataSetChanged();
	}
	
	/** Sets the selected items. Be sure to set the
	 *  choice mode (using {@link #setChoiceMode(int)})
	 *  before calling this!
	 *  @param selections The positions to select
	 *  If {@link #CHOICE_MODE_NONE}, nothing is selected.
	 *  If {@link #CHOICE_MODE_SINGLE}, only the last valid
	 *  item is selected.
	 *  If {@link #CHOICE_MODE_MULTIPLE}, all valid items
	 *  are selected.
	 *  Items are considered valid if they
	 *  are in the list.                                 */
	public void setSelections(Collection<? extends Integer> selections) {
		deselectAll();
		if(selections == null
				|| selections.size() == 0
				|| mChoiceMode == CHOICE_MODE_NONE)
			return;
		for(int sel : selections) 
			mSelected.set(sel, true);
	}
}