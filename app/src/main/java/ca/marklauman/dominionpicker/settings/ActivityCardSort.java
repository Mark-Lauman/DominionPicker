package ca.marklauman.dominionpicker.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.tools.Utils;

/** Activity used to change the card sort order.
 *  @author Mark Lauman */
public class ActivityCardSort extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Prefs.setup(this);
        setContentView(R.layout.activity_card_sort);
        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setDisplayHomeAsUpEnabled(true);

        DragSortListView view = (DragSortListView) findViewById(android.R.id.list);
        CardSortAdapter adapter = new CardSortAdapter(this);
        view.setAdapter(adapter);
        view.setDropListener(adapter);
        DragSortController controller = new DragSortController(view, android.R.id.background, 0, 0);
        view.setFloatViewManager(controller);
        view.setOnTouchListener(controller);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item == null || item.getItemId() != android.R.id.home)
            return false;
        finish();
        return true;
    }


    /** Adapter used to actually change the sort order.
     *  Also saves the new order when it changes. */
    private static class CardSortAdapter extends SimpleCursorAdapter
                                         implements SimpleCursorAdapter.ViewBinder,
                                                    DragSortListView.DropListener {
        /** Context used by this adapter */
        private final Context context;
        /** Current sort order of the adapter */
        private ArrayList<Integer> sortOrder;

        public CardSortAdapter(Context context) {
            super(context, R.layout.list_item_sort, makeCursor(context),
                    new String[]{"txt1", "txt2"}, new int[]{android.R.id.text1, android.R.id.text2});
            setViewBinder(this);
            this.context = context;
            loadSortOrder();
        }

        /** Create the cursor used to display the list */
        private static MatrixCursor makeCursor(Context context) {
            MatrixCursor cursor = new MatrixCursor(new String[]{"_id","txt1","txt2"});
            String[] sort = context.getResources()
                                   .getStringArray(R.array.sort_card_names);
            String sortEnd = context.getString(R.string.sort_card_end);
            cursor.addRow(new Object[]{0,sort[0], null});
            cursor.addRow(new Object[]{1,sort[1], sortEnd});
            for(Integer i=2; i<sort.length; i++)
                cursor.addRow(new Object[]{i, sort[i], null});
            return cursor;
        }

        /** Load the current sort order from the preferences */
        private void loadSortOrder() {
            // Load the sort order
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String[] orderStr = prefs.getString(Prefs.SORT_CARD,
                                                context.getString(R.string.sort_card_def))
                                     .split(",");

            // Parse the sort order
            try{
                sortOrder = new ArrayList<>(orderStr.length);
                for(String val : orderStr)
                    sortOrder.add(Integer.parseInt(val));
            } catch(Exception ignored) {}

            // Reset to default pref value if this one is the wrong length
            if(getCount() != sortOrder.size()) {
                prefs.edit()
                     .putString(Prefs.SORT_CARD, context.getString(R.string.sort_card_def))
                     .commit();
                Prefs.notifyChange(context, Prefs.SORT_CARD);
                loadSortOrder();
            }
        }

        @Override
        public void drop(int from, int to) {
            Integer i = sortOrder.remove(from);
            sortOrder.add(to, i);
            notifyDataSetChanged();

            // Save the changed sort order
            PreferenceManager.getDefaultSharedPreferences(context)
                             .edit()
                             .putString(Prefs.SORT_CARD, Utils.join(",", sortOrder))
                             .commit();
            Prefs.notifyChange(context, Prefs.SORT_CARD);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return super.getView(sortOrder.get(position), convertView, parent);
        }

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            String val = cursor.getString(columnIndex);
            if(val == null) view.setVisibility(View.GONE);
            else view.setVisibility(View.VISIBLE);
            return false;
        }
    }
}