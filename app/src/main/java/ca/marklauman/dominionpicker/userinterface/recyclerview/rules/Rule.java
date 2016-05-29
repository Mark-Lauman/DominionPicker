package ca.marklauman.dominionpicker.userinterface.recyclerview.rules;

/** General ViewHolder used by
 *  {@link ca.marklauman.dominionpicker.userinterface.recyclerview.AdapterRules}.
 *  @author Mark Lauman */
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

/** One rule in the adapter. Adds two new methods to the usual ViewHolder. */
public abstract class Rule extends RecyclerView.ViewHolder {

    /** True if this rule is the last in the list
     *  (see {@link #setLast(boolean)} for details). */
    private boolean isLast = false;

    /** Default constructor from ViewHolder */
    Rule(View itemView) {
        super(itemView);
    }

    /** Set the value on display in this rule. The value on display may change.
     *  This should be taken into account.
     *  @param newValue The new value to assign to this rule. Type varies from type to type. */
    public abstract void setValue(Object newValue);

    /** Tells this rule if it is the last one in the list
     *  (the last rule needs to be shifted to the left so its content isn't behind
     *  the FAB used to start a shuffle)
     *  @param isLast True if this is the last Rule in the adapter. */
    public void setLast(boolean isLast) {
        if(this.isLast == isLast) return;
        this.isLast = isLast;
        final int dp64 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64f,
                                                       itemView.getResources().getDisplayMetrics());
        if(isLast) ViewCompat.setPaddingRelative(itemView, 0, 0, dp64, 0);
        else       ViewCompat.setPaddingRelative(itemView, 0, 0, 0, 0);
    }
}