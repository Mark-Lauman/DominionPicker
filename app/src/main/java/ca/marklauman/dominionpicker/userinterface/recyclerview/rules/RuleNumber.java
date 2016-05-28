package ca.marklauman.dominionpicker.userinterface.recyclerview.rules;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.tools.preferences.Preference.PreferenceListener;
import ca.marklauman.tools.preferences.SmallNumberPreference;

/**
 * Created by Mark on 2016-05-25.
 */
public class RuleNumber extends Rule implements PreferenceListener {

    private final SmallNumberPreference pref;

    public RuleNumber(RecyclerView parent) {
        super(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_numpreference, parent, false));
        pref = (SmallNumberPreference) itemView;
        pref.setListener(this);
    }

    @Override
    public void setValue(Object newValue) {
        String[] value = (String[])newValue;
        pref.setKey(value[0]);
        pref.setText(value[1]);
    }

    @Override
    public void preferenceChanged(int i) {
        Prefs.notifyChange(itemView.getContext(), pref.getKey());
    }
}