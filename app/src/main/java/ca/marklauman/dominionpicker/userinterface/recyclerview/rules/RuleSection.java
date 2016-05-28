package ca.marklauman.dominionpicker.userinterface.recyclerview.rules;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.marklauman.dominionpicker.R;

/**
 * Created by Mark on 2016-05-26.
 */
public class RuleSection extends Rule {

    @BindView(android.R.id.text1) public TextView title;

    public RuleSection(RecyclerView parent) {
        super(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_seperator, parent, false));
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void setValue(Object newValue) {
        title.setText((String)newValue);
    }

    @Override
    public void setLast(boolean isLast) {
        // Do nothing - the section header should appear the same.
    }
}