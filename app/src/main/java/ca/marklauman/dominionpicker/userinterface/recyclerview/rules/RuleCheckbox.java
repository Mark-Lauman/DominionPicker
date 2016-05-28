package ca.marklauman.dominionpicker.userinterface.recyclerview.rules;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.dominionpicker.userinterface.icons.Icon;

/**
 * Created by Mark on 2016-05-26.
 */
public class RuleCheckbox extends Rule
                          implements View.OnClickListener {

    @BindView(android.R.id.icon) public ImageView vIcon;
    @BindView(android.R.id.checkbox) public CheckedTextView vText;


    /** True if items should be checked if they are NOT in the keySet/not true in the key. */
    private boolean inverted;
    /** Preference key that is bound to this checkbox. */
    private String key;
    /** Set of keys bound to this checkbox. (optional) */
    private HashSet<String> keySet;


    public RuleCheckbox(RecyclerView parent) {
        super(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.rule_checkbox, parent, false));
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
    }


    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object newValue) {
        Object[] in = (Object[])newValue;
        if(in[0] instanceof Icon) {
            // in[] = { icon, inverted, key, [keySet] }
            setDisplay((Icon)in[0]);
            setKey((boolean)in[1], (String)in[2],
                   in.length == 3 ? null : (HashSet<String>)in[3] );
        } else {
            // in[] = { icon, text, inverted, key, [keySet] }
            if(in[0] instanceof Drawable) setDisplay((Drawable)in[0], (String)in[1]);
            else setDisplay((int)in[0], (String)in[1]);
            setKey((boolean)in[2], (String)in[3],
                    in.length == 4 ? null : (HashSet<String>)in[4]);
        }
    }


    /** Show the image and text in this checkbox. */
    private void setDisplay(int imageResource, String text) {
        vIcon.setImageResource(imageResource);
        vText.setText(text);
    }


    /** Show the image and text in this checkbox. */
    private void setDisplay(Drawable drawable, String text) {
        vIcon.setImageDrawable(drawable);
        vText.setText(text);
    }


    /** Show the icon in this checkbox. */
    private void setDisplay(Icon icon) {
        vIcon.setImageDrawable(icon);
        vText.setText(icon.getDescription(null));
    }


    /** Set the key combination paired to this checkbox.
     *  @param inverted If the checkbox is selected, it will not be in the keySet and will be false.
     *  @param key The key assigned to this checkbox.
     *  @param keySet If this is null, then the value of this rule is saved to the preferences
     *                under the key. If not null, then the key is added to this set when the rule
     *                is selected/deselected. */
    void setKey(boolean inverted, String key, HashSet<String> keySet) {
        this.inverted = inverted;
        this.key = key;
        this.keySet = keySet;
        vText.setChecked(keySet == null ? inverted != Prefs.get(itemView.getContext())
                                                           .getBoolean(key, true)
                                        : inverted != keySet.contains(key));
    }

    @Override
    public void onClick(View v) {
        vText.toggle();

        if(keySet != null) {
            if(inverted != vText.isChecked()) keySet.add(key);
            else keySet.remove(key);
        } else Prefs.edit(itemView.getContext())
                    .putBoolean(key, inverted != vText.isChecked())
                    .commit();
    }
}