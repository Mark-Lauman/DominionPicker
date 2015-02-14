/* Copyright (c) 2015 Mark Christopher Lauman
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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/** A simple array adapter with icons and selection. The icons
  * will be placed to the left of the {@code android.R.id.text1}
  * {@link TextView}. The background of that TextView will change
  * color when selected.                                       */
@SuppressWarnings("SameParameterValue")
public class ExpandedArrayAdapter<T> extends ArrayAdapter<T> {

    private int[] icons;
    private int selection;
    private int selection_back;

    public ExpandedArrayAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
        icons = null;
        selection = -1;
    }

    public void setIcons(int... iconResId) {
        this.icons = iconResId;
    }

    public void setSelBack(int resId) {
        selection_back = resId;
    }

    public void setSelection(int position) {
        selection = position;
        notifyDataSetChanged();
    }

    public int getSelection() {
        return selection;
    }

    public View getView (int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        TextView txt = (TextView) v.findViewById(android.R.id.text1);
        if(icons != null && position < icons.length)
            txt.setCompoundDrawablesWithIntrinsicBounds(icons[position],0,0,0);
        if(selection == position) txt.setBackgroundResource(selection_back);
        else txt.setBackgroundResource(android.R.color.transparent);
        return v;
    }
}