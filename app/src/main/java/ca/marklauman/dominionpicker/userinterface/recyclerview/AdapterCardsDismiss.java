package ca.marklauman.dominionpicker.userinterface.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import ca.marklauman.dominionpicker.R;

/** A variation to the basic {@link AdapterCards} that allows
 *  swipe to dismiss and setting a bane card.
 *  @author Mark Lauman */
public class AdapterCardsDismiss extends AdapterCards
                                 implements AdapterCards.Listener {

    /** Id of the bane card */
    private long mBane = -1;
    /** A listener to be notified when an item is dismissed. */
    private Listener mListener = null;

    public AdapterCardsDismiss(RecyclerView view) {
        // TODO: enable swipe
        super(view, false);
        super.setListener(this);
    }


    /** Set the bane card on this adapter.
     *  If the bane card does not appear in the list, there will be no visible bane card.
     *  @param bane The card id of the bane card. */
    public void setBane(long bane) {
        mBane = bane;
        notifyDataSetChanged();
    }


    @Override @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = super.onCreateViewHolder(parent, viewType);
        holder.extra.setText(R.string.young_witch_bane);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if(mBane == mCursor.getLong(_id)) {
            holder.background.setBackgroundResource(R.color.type_curse);
            holder.extra.setVisibility(View.VISIBLE);
        } else {
            holder.background.setBackgroundResource(R.color.background);
            holder.extra.setVisibility(View.GONE);
        }
    }


    @Override
    public void onItemClick(ViewHolder holder, int position, long id, boolean longClick) {
        if(mListener != null && mCursor.moveToPosition(position))
            mListener.onItemClick(holder, position, id, longClick);
    }


    @Override
    public void onDismiss(int position) {
        if(mListener != null && mCursor.moveToPosition(position))
            mListener.onDismiss(position, mCursor.getLong(_id));
    }


    @Override @Deprecated
    public void setListener(AdapterCards.Listener listener) {}


    /** Set the listener who will be notified about dismiss events. There can be only one. */
    public void setListener(Listener listener) {
        mListener = listener;
    }


    public interface Listener extends AdapterCards.Listener {
        /** Called when a card is dismissed from this list. */
        void onDismiss(int position, long id);
    }
}