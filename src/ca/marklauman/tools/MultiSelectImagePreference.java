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
	
    private static final String SEPARATOR = "\u0001\u0007\u001D\u0007\u0001";
    
    private ArrayCheckAdapter<CharSequence> adapt;
    private int[] icons = null;

    public MultiSelectImagePreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        
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
    }

    public MultiSelectImagePreference(Context context) {
        this(context, null);
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
    
    
    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        restoreCheckedEntries();
        
        
        ListView list = new ListView(getContext());
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
        
        if (saved == null || "".equals(saved)) {
        	adapt.deselectAll();
        	return;
        }
        
        List<CharSequence> savedList = Arrays.asList(saved);
        CharSequence[] values = getEntryValues();
        ArrayList<Integer> selections = new ArrayList<Integer>(savedList.size());
        for (int i = 0; i < values.length; i++) {
        	if(savedList.contains(values[i]))
        		selections.add(i);
        }
        adapt.setSelections(selections);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
    	if(!positiveResult) return;
    	
    	CharSequence[] entryValues = getEntryValues();
        Integer[] select = adapt.getSelections();
        List<CharSequence> values = new ArrayList<CharSequence>();
        if (select != null) {
        	for(int id : select)
        		values.add(entryValues[id]);
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

    private CharSequence prepareSummary(List<CharSequence> joined) {
        List<String> titles = new ArrayList<String>();
        CharSequence[] entryTitle = getEntries();
        CharSequence[] entryValues = getEntryValues();
        int ix = 0;
        for (CharSequence value : entryValues) {
            if (joined.contains(value)) {
                titles.add((String) entryTitle[ix]);
            }
            ix += 1;
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
    		 .putString(key, join(values, SEPARATOR));
    }
    
    /** Extract the values stored by a
     *  {@code MultiSelectImagePreference}.
     *  (Retrieve the values as a String and pass
     *  them to this)
     *  @param val The value stored in the preferences
     *  @return The values stored inside that.      */
    public static CharSequence[] getValues(CharSequence val) {
        if (val == null || "".equals(val)) {
            return new CharSequence[0];
        } else {
            return ((String) val).split(SEPARATOR);
        }
    }
}