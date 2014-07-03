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
package ca.marklauman.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ca.marklauman.dominionpicker.R;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.ListPreference;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

/** An implementation of MultiSelectImagePreference
 *  for Android 2 with icons included beside
 *  the list items.
 *  @author Mark Lauman & Krzysztof Suszynski.
 *  This is a heavily modified version of
 *  Krzysztof Suszynski's MultiSelectListPreference:
 *  https://gist.github.com/cardil/4754571&nbsp;. */
public class MultiSelectImagePreference extends ListPreference {
	
	/** Separator between list entries */
    private static final String SEPARATOR = "\u0001\u0007\u001D\u0007\u0001";
    /** Adapter used to track selections */
    private ArrayCheckAdapter<CharSequence> adapt;
    /** Icon resources used by the adapter */
    private int[] icons = null;
    /** If this preference is inverted, it saves the
     *  items that are NOT selected */
    private boolean inverted = false;
    
    
    public MultiSelectImagePreference(Context context) {
        this(context, null);
    }
    
    public MultiSelectImagePreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        
        // load inversion settings
        isInverted(attributeSet);
        
        // setup the adapter
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        if (entries == null || entryValues == null
                || entries.length != entryValues.length) {
            throw new IllegalStateException(
                    "MultiSelectImagePreference requires an entries array and an entryValues "
                            + "array which are both the same length");
        }
        adapt = new ArrayCheckAdapter<CharSequence>(getContext(),
        											R.layout.list_item_check,
        											entries);
        adapt.setChoiceMode(ArrayCheckAdapter.CHOICE_MODE_MULTIPLE);
        adapt.setIcons(getEntryIcons(attributeSet));
        
        // setup the summary
        CharSequence[] values = getValues(getValue());
        ArrayList<String> listVals = new ArrayList<String>(values.length);
        for(CharSequence val : values)
        	listVals.add("" + val);
        setSummary(prepareSummary(listVals));
    }
    
    
    /** Load the icons used for the list entries
     *  into {@link #icons}. After this is called,
     *  {@link #getEntryIcons()} will return the same
     *  as this function - and run faster. Before it
     *  is called, {@link #getEntryIcons()} always
     *  returns {@code null}.
     *  @param attrs The attributes provided
     *  to this {@code MultiSelectImagePreference}.
     *  The icons are retrieved from this parameter.
     *  @return The resource ids of each icon in the
     *  list. These icons appear in the order of
     *  the entries themselves.                   */
    private int[] getEntryIcons(AttributeSet attrs) {
    	icons = null;
    	if(attrs == null) return icons;
    	int list_id = attrs.getAttributeResourceValue(null, "icons", -1);
    	if(list_id < 0) return icons;
    	
    	TypedArray ta = getContext().getResources()
    								.obtainTypedArray(list_id);
    	if(ta == null) return icons;
    	icons = new int[ta.length()];
    	for(int i=0; i<ta.length(); i++) {
    		icons[i] = ta.getResourceId(i, -1);
    	}
    	ta.recycle();
    	return icons;
    }
    
    
    /** Get the icons used for the list entries.
     *  @return The resource ids of each icon in the
     *  list. These icons appear in the order of
     *  the entries themselves.                   */
    public int[] getEntryIcons() {
    	return icons;
    }
    
    /** <p>Check if this preference is inverted in its
     *  xml description. Inverted preferences save
     *  items that are NOT selected.</p>
     *  <p>After this is called once
     *  {@link #isInverted()} returns the same thing
     *  and is faster. Before this is called,
     *  {@link #isInverted()} returns {@code null}.</p>
     *  @param attrs The attributes provided
     *  to this {@code MultiSelectImagePreference}.
     *  The setting is retrieved from this parameter.
     *  @return {@code true} if this is inverted */
    private boolean isInverted(AttributeSet attrs) {
    	inverted = attrs.getAttributeBooleanValue(null, "inverted", false);
    	return inverted;
    }
    
    /** Check if this preference is inverted.
     *  Inverted preferences save items that are
     *  NOT selected.
     *  @return {@code true} if this is inverted */
    public boolean isInverted() {
    	return inverted;
    }
    
    
    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        restoreCheckedEntries();
        
        ListView list = new ListView(getContext());
        list.setBackgroundColor(Color.WHITE);
        list.setAdapter(adapt);
        OnItemClickListener listener = new OnItemClickListener() {
			public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
				adapt.toggleItem(position);
			}
        };
        list.setOnItemClickListener(listener);
        builder.setView(list);
    }
    

    private void restoreCheckedEntries() {
        // get preference state
        CharSequence[] saved = getValues(getValue());
        
        if (saved == null || saved.length == 0) {
        	if(inverted) adapt.selectAll();
        	else adapt.deselectAll();
        	return;
        }
        
        List<CharSequence> savedList = Arrays.asList(saved);
        CharSequence[] values = getEntryValues();
        ArrayList<Integer> selections = new ArrayList<Integer>(savedList.size());
        if(inverted) {
        	for (int i = 0; i < values.length; i++) {
            	if(!savedList.contains(values[i]))
                	selections.add(i);
            }
        } else {
        	for (int i = 0; i < values.length; i++) {
            	if(savedList.contains(values[i]))
                	selections.add(i);
            }
        }
        adapt.setSelections(selections);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
    	if(!positiveResult) return;
    	
    	CharSequence[] entryValues = getEntryValues();
    	Integer[] select;
    	if(inverted) select = adapt.getDeselections();
    	else select = adapt.getSelections();
        List<String> values = new ArrayList<String>();
        if (select != null) {
        	for(int id : select)
        		values.add("" + entryValues[id]);
            String value = join(values, SEPARATOR);
            setSummary(prepareSummary(values));
            setValueAndEvent(value);
        }
    }
    
    
    private void setValueAndEvent(String value) {
        if (callChangeListener(getValues(value))) {
            setValue(value);
        }
    }
    
    private CharSequence prepareSummary(List<String> joined) {
        List<String> titles = new ArrayList<String>();
        CharSequence[] entryTitle = getEntries();
        CharSequence[] entryValues = getEntryValues();
        int ix = 0;
        if(inverted) {
        	for (CharSequence value : entryValues) {
                if (!joined.contains(value)) {
                    titles.add((String) entryTitle[ix]);
                }
                ix += 1;
            }
        } else {
        	for (CharSequence value : entryValues) {
                if (joined.contains(value)) {
                    titles.add((String) entryTitle[ix]);
                }
                ix += 1;
            }
        }
        return join(titles, ", ");
    }

    @Override
    protected Object onGetDefaultValue(TypedArray typedArray, int index) {
        return typedArray.getTextArray(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue,
            Object rawDefaultValue) {
        String value = null;
        CharSequence[] defaultValue;
        if (rawDefaultValue == null) {
            defaultValue = new CharSequence[0];
        } else {
            defaultValue = (CharSequence[]) rawDefaultValue;
        }
        List<CharSequence> joined = Arrays.asList(defaultValue);
        String joinedDefaultValue = join(joined, SEPARATOR);
        if (restoreValue) {
            value = getPersistedString(joinedDefaultValue);
        } else {
            value = joinedDefaultValue;
        }

        setSummary(prepareSummary(Arrays.asList(getValues(value))));
        setValueAndEvent(value);
    }

    /** Joins array of object to single string by separator
     *  Credits to kurellajunior on this post
     *  http://snippets.dzone.com/posts/show/91
     *   @param iterable any kind of iterable
     *   ex.: <code>["a", "b", "c"]</code>
     *  @param separator separetes entries
     *  ex.: <code>","</code>
     *  @return joined string
     *  ex.: <code>"a,b,c"</code>                  */
    protected static String join(Iterable<?> iterable, String separator) {
        Iterator<?> oIter;
        if (iterable == null || (!(oIter = iterable.iterator()).hasNext()))
            return "";
        StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
        while (oIter.hasNext())
            oBuilder.append(separator).append(oIter.next());
        return oBuilder.toString();
    }
    
    @Override
    protected void onBindView(View view) {
    	super.onBindView(view);
    	TextView summary = (TextView) view.findViewById(android.R.id.summary);
    	if(summary != null) {
    		summary.setEllipsize(TruncateAt.END);
    		summary.setLines(2);
    	}
    }
    
    
    /** Save a value to memory using the
     *  {@code MultiSelectImagePreference} format.
     *  @param prefs The preferences to save the
     *  values into.
     *  @param key The key associated with this
     *  preference value.
     *  @param values The values you wish placed
     *  there.                                */
    public static void saveValue(SharedPreferences prefs, String key, Collection<? extends String> values) {
    	prefs.edit()
    		 .putString(key, join(values, SEPARATOR))
    		 .commit();
    }
    
    /** Extract the values stored by a
     *  {@code MultiSelectImagePreference}.
     *  (Retrieve the values as a String and pass
     *  them to this)
     *  @param val The value stored in the preferences
     *  @return The values stored inside that.      */
    public static String[] getValues(CharSequence val) {
        if (val == null || "".equals(val)) {
            return new String[0];
        } else {
            return ((String) val).split(SEPARATOR);
        }
    }
}