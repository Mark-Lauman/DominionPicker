package ca.marklauman.dominionpicker.userinterface.recyclerview.rules;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.marklauman.dominionpicker.R;

/** Rule for {@link ca.marklauman.dominionpicker.userinterface.recyclerview.AdapterRules}
 *  that is used as a section separator.
 *  Accepts a single String as its value.
 *  @author Mark Lauman */
public class RuleSection extends Rule {

    /** View that displays the title of this section. */
    @BindView(android.R.id.text1) public TextView title;

    /** Construct a new RuleCheckbox for the parent.
     *  @param parent The parent RecyclerView that this Rule will be inserted into. */
    public RuleSection(RecyclerView parent) {
        super(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_seperator, parent, false));
        ButterKnife.bind(this, itemView);
    }

    /** Set the title of this RuleSection
     *  @param title A string that will become the new the new title of this section. */
    @Override
    public void setValue(Object title) {
        this.title.setText((String)title);
    }

    @Override
    public void setLast(boolean isLast) {
        // Do nothing - the section header should appear the same.
    }
}