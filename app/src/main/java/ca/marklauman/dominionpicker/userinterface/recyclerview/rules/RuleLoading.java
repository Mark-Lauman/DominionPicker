package ca.marklauman.dominionpicker.userinterface.recyclerview.rules;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import ca.marklauman.dominionpicker.R;


/** This rule is used to display the loading icon. */
public class RuleLoading extends Rule {

    public RuleLoading(RecyclerView parent) {
        super(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_loading, parent, false));
    }

    /* These two methods should affect nothing */
    @Override
    public void setValue(Object newValue) {}
    @Override
    public void setLast(boolean isLast) {}
}