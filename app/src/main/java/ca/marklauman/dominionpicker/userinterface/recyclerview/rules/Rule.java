package ca.marklauman.dominionpicker.userinterface.recyclerview.rules;

/**
 * Created by Mark on 2016-05-26.
 */
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

/** One rule in the adapter. Adds two new methods to the usual ViewHolder. */
public abstract class Rule extends RecyclerView.ViewHolder {

    /** Four dp in the current display resolution. */
    private final int dp64;

    /** True if this rule is the last in the list
     *  (see {@link #setLast(boolean)} for details). */
    private boolean isLast = false;

    /** Default constructor */
    public Rule(View itemView) {
        super(itemView);
        dp64 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64f,
                                              itemView.getResources().getDisplayMetrics());
    }

    /** Set the value on display in this rule. The value on display may change.
     *  This should be taken into account.
     *  @param newValue The new value to assign to this rule. */
    public abstract void setValue(Object newValue);

    /** Tells this rule if it is the last one in the list
     *  (the last rule needs to be shifted to the left so its content isn't behind
     *  the FAB used to start a shuffle)
     *  @param isLast True if this is the last Rule in the adapter. */
    public void setLast(boolean isLast) {
        if(this.isLast == isLast) return;
        this.isLast = isLast;
        if(isLast) ViewCompat.setPaddingRelative(itemView, 0, 0, dp64, 0);
        else       ViewCompat.setPaddingRelative(itemView, 0, 0, 0, 0);
    }
}