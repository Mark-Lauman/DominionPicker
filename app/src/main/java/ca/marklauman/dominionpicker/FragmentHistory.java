package ca.marklauman.dominionpicker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import ca.marklauman.dominionpicker.database.DataDb;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;

/** Governs the History and Favorites screens. Allows users to see previous shuffles.
 *  @author Mark Lauman */
public class FragmentHistory extends Fragment
                             implements LoaderCallbacks<Cursor>,
                                        ListView.OnItemClickListener {
    /** Only display the favorite shuffles */
    private boolean onlyFav = false;
    /** True if we are loading data */
    private boolean loading = true;

    /** The view for the history list */
    private ListView listView;
    /** The adapter for the ListView. */
    private HistoryAdapter adapter;
    /** The view for if the list is empty */
    private TextView empty_view;
    /** The view for if the list is loading. */
    private View load_view;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentActivity act = getActivity();
        adapter = new HistoryAdapter(act);

        // start loading the card list
        LoaderManager lm = act.getSupportLoaderManager();
        lm.initLoader(LoaderId.HISTORY, null, this);
    }


    /** Called to create this fragment's view for the first time.  */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Basic view setup and retrieval
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        listView = (ListView) view.findViewById(R.id.card_list);
        empty_view = (TextView) view.findViewById(android.R.id.empty);
        if(onlyFav) empty_view.setText(R.string.no_fav);
        load_view = view.findViewById(android.R.id.progress);

        // View configuration
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        updateEmpty();

        return view;
    }


    /** Called when a history entry is clicked */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent showSupply = new Intent(getActivity(), ActivitySupply.class);
        showSupply.putExtra(ActivitySupply.PARAM_SUPPLY_ID, id);
        startActivity(showSupply);
    }

    /** Used to launch the query to the history database */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // switch the fragment to load mode
        loading = true;
        updateEmpty();

        // start the query going
        CursorLoader c = new CursorLoader(getActivity());
        c.setUri(Provider.URI_HIST);
        c.setProjection(new String[]{DataDb._H_TIME, DataDb._H_NAME, DataDb._H_CARDS,
                                     DataDb._H_HIGH_COST, DataDb._H_SHELTERS});
        c.setSortOrder(DataDb._H_TIME + " DESC");
        if(onlyFav) c.setSelection(DataDb._H_NAME + " NOT NULL");
        return c;
    }


    /** When the query is finished, it returns here */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
        loading = false;
        updateEmpty();
    }


    /** When the history table changes, this is invoked. */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }


    /** Updates the empty list view to reflect if we are loading or not */
    private void updateEmpty() {
        if(listView == null) return;

        empty_view.setVisibility(View.GONE);
        load_view.setVisibility(View.GONE);
        if(loading) listView.setEmptyView(load_view);
        else listView.setEmptyView(empty_view);
    }
}