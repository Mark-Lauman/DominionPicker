package ca.marklauman.dominionpicker.userinterface.recyclerview.rules;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.settings.Pref;
import ca.marklauman.dominionpicker.userinterface.DrawableLoader;
import ca.marklauman.dominionpicker.userinterface.icons.Icon;

/** Rule for {@link ca.marklauman.dominionpicker.userinterface.recyclerview.AdapterRules}
 *  that contains 1 checkbox, an icon and text.
 *  The value set to a RuleCheckbox is an instance of {@link RuleCheckbox.Data}.
 *  @author Mark Lauman */
public class RuleCheckbox extends Rule
                          implements View.OnClickListener {

    /** View that displays the icon. */
    @BindView(android.R.id.icon) public ImageView vIcon;
    /** View that displays the text and the checkbox. */
    @BindView(android.R.id.checkbox) public CheckedTextView vText;
    /** The data on display in this RuleCheckbox. */
    private RuleCheckbox.Data data;


    /** Construct a new RuleCheckbox for the parent.
     *  @param parent The parent RecyclerView that this Rule will be inserted into. */
    public RuleCheckbox(@NonNull RecyclerView parent) {
        super(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.rule_checkbox, parent, false));
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
    }


    /** Set the value on display in this RuleCheckbox.
     *  @param newValue An instance of {@link RuleCheckbox.Data}. */
    @Override
    public void setValue(Object newValue) {
        data = (Data)newValue;
        if(data.icon != null)
            vIcon.setImageDrawable(data.icon);
        else DrawableLoader.into(vIcon)
                           .place(data.iconRes);
        vText.setText(data.text);
        vText.setChecked( data.keySet == null
                              ? data.inverted != Pref.get(itemView.getContext())
                                                     .getBoolean(data.key, true)
                              : data.inverted != data.keySet.contains(data.key));
    }


    /** Toggles the checkbox and saves the changed value to where it belongs. */
    @Override
    public void onClick(View v) {
        vText.toggle();
        if(data.keySet != null) {
            if(data.inverted != vText.isChecked()) data.keySet.add(data.key);
            else data.keySet.remove(data.key);
        } else Pref.edit(itemView.getContext())
                   .putBoolean(data.key, data.inverted != vText.isChecked())
                   .commit();
    }


    /** Defines the setup for a {@link RuleCheckbox}. The two constructors define the two
     *  basic setups for a {@link RuleCheckbox}. */
    public static class Data {

        /** Icon resource used for the icon and text. */
        public final Drawable icon;
        /** Image resource id used for the icon (fallback if icon is null). */
        public final int iconRes;
        /** String used for the display text. */
        public final String text;
        /** True if this checkbox should be inverted.
         *  (Checked if the preference is false, or the key is not in {@link #keySet}). */
        public final boolean inverted;
        /** Key used to save the value of this checkbox. */
        public final String key;
        /** If a keySet is provided, this key will be placed into this set when selected.
          * For no set, the key is the preference key that this checkbox will be saved to. */
        public final HashSet<String> keySet;


        /** Build this checkbox from an Icon resource.
         *  @param icon The icon to use as the basis for this rule. Provides text and image.
         * @param inverted If true, this rule will be checked if its preference is false or
         *                 its key is not in keySet.
         * @param key The key bound to this {@link RuleCheckbox}. Used to save Rule's value.
         * @param keySet If a keySet is provided, then the presence of the key is looked for in
         *               the keySet and the rule is checked based off of its presence.
         *               If no keySet is provided, then the key is used as a preference key
         *               to a boolean value. */
        public Data(@NonNull Icon icon, boolean inverted,
                    @NonNull String key, @Nullable HashSet<String> keySet) {
            this.icon = icon;
            iconRes = 0;
            text = icon.getDescription(null);
            this.inverted = inverted;
            this.key = key;
            this.keySet = keySet;
        }


        /** Build this checkbox from a Drawable resource id.
         *  @param iconRes Resource id of the drawable to use as the icon for this preference.
         *  @param text The text to display in this checkbox.
         *  @param inverted If true, this rule will be checked if its preference is false or
         *                  its key is not in keySet.
         *  @param key The key bound to this {@link RuleCheckbox}. Used to save Rule's value.
         *  @param keySet If a keySet is provided, then the presence of the key is looked for in
         *                the keySet and the rule is checked based off of its presence.
         *                If no keySet is provided, then the key is used as a preference key
         *                to a boolean value. */
        public Data(int iconRes, @NonNull String text, boolean inverted,
                    @NonNull String key, @Nullable HashSet<String> keySet) {
            this.icon = null;
            this.iconRes = iconRes;
            this.text = text;
            this.inverted = inverted;
            this.key = key;
            this.keySet = keySet;
        }
    }
}