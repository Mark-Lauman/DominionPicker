package ca.marklauman.dominionpicker.userinterface.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.tools.Utils;

/** Adapter designed to mark cards as filtered or required.
 *  Short press selects/deselects. Long press requires.
 *  @author Mark Lauman */
public class AdapterCardsFilter extends AdapterCards
                                implements AdapterCards.Listener {

    /** Cards that are deselected and will be filtered out. */
    private final HashSet<Long> mDeselected;
    /** Cards that are hard selected and are considered required. */
    private final HashSet<Long> mRequired;


    /** Basic constructor.
     *  @param view The ListView this adapter oversees.
     *  @param filtered The filtered card ids, separated by commas
     *                  (such as what would come out of {@link #getFilter()}).
     *  @param required The required card ids, separated by commas
     *                  (such as what would come out of {@link #getRequired()}). */
    public AdapterCardsFilter(RecyclerView view, String filtered, String required){
        super(view);
        setListener(this);
        mDeselected = new HashSet<>();
        mRequired = new HashSet<>();
        readString(mDeselected, filtered);
        readString(mRequired, required);
    }


    /** Read a string of long values into a HashSet.
     *  @param destination Where the values in the string will be stored.
     *  @param toRead The string to read into the HashSet.
     *                Formatted as comma separated long values. */
    private static void readString(HashSet<Long> destination, String toRead) {
        if(toRead == null || toRead.equals("")) return;
        final String[] split = toRead.split(",");
        for(String val : split) {
            try{ destination.add(Long.parseLong(val));
            } catch(NumberFormatException ignored) {}
        }
    }


    /** Get a comma separated list of the filtered cards */
    public String getFilter() {
        return Utils.join(",", mDeselected);
    }


    /** Get a comma separated list of the required cards */
    public String getRequired() {
        return Utils.join(",", mRequired);
    }


    /** Toggles the item at the given position.
     *  @param position The position of the card in this list.
     *  @param id The id of the card.
     *  @param longClick If the click was long or not. */
    @Override
    public void onItemClick(ViewHolder holder, int position, long id, boolean longClick) {
        // If the item was required, it changes to selected regardless of click length
        if(!mRequired.remove(id)) {
            // A long click when not required, makes an item required.
            if(longClick) {
                mDeselected.remove(id);
                mRequired.add(id);
            // A short click when not required toggles between selected and deselected
            } else if(!mDeselected.remove(id))
                mDeselected.add(id);
        }
        notifyItemRangeChanged(position, 1);
    }


    /** If one or more cards are deselected, those cards become selected.
     *  If no cards are deselected, all cards are deselected (including required ones).
     *  Cards that aren't in the cursor will not be affected. */
    public void toggleAll() {
        if(allCardsSelected()) deselectAll();
        else selectAll();
    }


    /** Check that no cards from the current list are deselected. */
    private boolean allCardsSelected() {
        // If nothing is deselected, then we're good
        if(mDeselected.size() == 0) return true;
        /* If cards are deselected, they may not be in the list.
         * We need to check that the deselected cards are in the list. */
        mCursor.moveToPosition(-1);
        while(mCursor.moveToNext()) {
            if(mDeselected.contains(mCursor.getLong(_id)))
                return false;
        }
        // None of the items in the cursor was in mDeselected
        return true;
    }


    /** Deselect all cards in the list. Required cards become deselected as well.
     *  Cards not in the list are unchanged. */
    private void deselectAll() {
        long id;
        mCursor.moveToPosition(-1);
        while(mCursor.moveToNext()) {
            id = mCursor.getLong(_id);
            mRequired.remove(id);
            mDeselected.add(id);
        }
        notifyDataSetChanged();
    }


    /** Select all cards in the list. Required cards are unchanged.
     *  Cards not in the list are unchanged. */
    private void selectAll() {
        mCursor.moveToPosition(-1);
        while(mCursor.moveToNext())
            mDeselected.remove(mCursor.getLong(_id));
        notifyDataSetChanged();
    }


    @Override @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder result = super.onCreateViewHolder(parent, viewType);
        result.extra.setText(R.string.req_card);
        return result;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        // Determine and apply the selection status to the view
        final long id = mCursor.getLong(_id);
        int background = R.color.list_item_sel;
        int visibility = View.GONE;
        if(mDeselected.contains(id)) background = R.color.background;
        else if(mRequired.contains(id)) {
            background = R.color.list_item_sel_hard;
            visibility = View.VISIBLE;
        }
        holder.background.setBackgroundResource(background);
        holder.extra.setVisibility(visibility);
    }
}