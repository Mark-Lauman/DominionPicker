package ca.marklauman.dominionpicker.recyclerview;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.tools.recyclerview.dragdrop.BasicTouchAdapter;
import ca.marklauman.tools.recyclerview.dragdrop.TouchCallback;

/** Adapter used to change the card sort order used by the app.
 *  This adapter implements drag and drop for its primary purpose.
 *  @author Mark Lauman */
public class AdapterSortCard extends BasicTouchAdapter<AdapterSortCard.ViewHolder> {

    /** The current sort order (stores the sort ids in the order in which they are applied) */
    private final ArrayList<Integer> sortOrder;
    /** Names used to label the individual items. Sorted by sort id. */
    private final String[] names;


    public AdapterSortCard(Context context, RecyclerView view) {
        super(view, TouchCallback.forDragList());
        hasStableIds();
        Resources res = context.getResources();
        names = res.getStringArray(R.array.sort_card_names);

        // load the sort order
        String[] prefOrder = Prefs.get(context)
                                  .getString(Prefs.SORT_CARD, res.getString(R.string.sort_card_def))
                                  .split(",");
        sortOrder = new ArrayList<>(prefOrder.length);
        for(String id : prefOrder)
            sortOrder.add(Integer.parseInt(id));
    }


    public ArrayList<Integer> getSortOrder() {
        return sortOrder;
    }


    @Override
    public long getItemId(int position) {
        return sortOrder.get(position);
    }


    @Override
    public int getItemCount() {
        return sortOrder.size();
    }


    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(sortOrder, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }


    @Override
    public void onDismiss(int position) { /* Never called */ }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.list_item_sort, parent, false));
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int id = sortOrder.get(position);
        holder.text1.setText(names[id]);
        holder.text2.setVisibility(id==0 ? View.VISIBLE : View.GONE);
    }


    public class ViewHolder extends RecyclerView.ViewHolder
                            implements View.OnTouchListener {
        public final TextView text1;
        public final TextView text2;
        public final ImageView drag;

        public ViewHolder(View itemView) {
            super(itemView);
            drag = (ImageView) itemView.findViewById(android.R.id.icon);
            text1 = (TextView) itemView.findViewById(android.R.id.text1);
            text2 = (TextView) itemView.findViewById(android.R.id.text2);
            drag.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN)
                startDrag(this);
            return true;
        }
    }
}