package ca.marklauman.dominionpicker.userinterface.recyclerview.rules;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.tools.preferences.Preference.PreferenceListener;
import ca.marklauman.tools.preferences.SmallNumberPreference;

/** Rule for {@link ca.marklauman.dominionpicker.userinterface.recyclerview.AdapterRules}
 *  that controls a number value in the range 0-99.
 *  The value set to a SmallNumberPreference is a String array.
 *  @author Mark Lauman. */
public class RuleNumber extends Rule implements PreferenceListener {

    /** The preference that is actually displayed in this rule */
    private final SmallNumberPreference pref;

    /** Construct a new RuleCheckbox for the parent.
     *  @param parent The parent RecyclerView that this Rule will be inserted into. */
    public RuleNumber(RecyclerView parent) {
        super(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_numpreference, parent, false));
        pref = (SmallNumberPreference) itemView;
        pref.setListener(this);
    }

    /** Set the value on display in this rule.
     *  @param newValue A string array of the format {@code {key, text}}.
     *                  The key is the preference key that will contain this rule's value.
     *                  The text is displayed as a label for the preference. */
    @Override
    public void setValue(Object newValue) {
        String[] value = (String[])newValue;
        pref.setKey(value[0]);
        pref.setText(value[1]);
    }

    /** When the preference is updated, notify any listeners. */
    @Override
    public void preferenceChanged(int i) {
        Prefs.notifyChange(itemView.getContext(), pref.getKey());
    }
}