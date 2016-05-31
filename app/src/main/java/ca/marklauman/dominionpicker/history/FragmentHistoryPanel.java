package ca.marklauman.dominionpicker.history;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import ca.marklauman.dominionpicker.ActivitySupply;
import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.settings.Pref;
import ca.marklauman.tools.CursorHandler;

/** Represents a single panel in the History screen.
 *  Different Handlers display different things.
 *  @author Mark Lauman */
public class FragmentHistoryPanel extends Fragment
                                  implements LoaderCallbacks<Cursor>, ListView.OnItemClickListener,
                                             Pref.Listener {

    /** Parameter indicating the type of panel to use. */
    public static final String PARAM_TYPE = "type";

    // Variables that must be set externally before onCreate is called
    /** Loader id used to get the supply data. */
    private int loaderId;
    /** The handler that loads the data and acts as adapter. */
    private CursorHandler handler;

    // Variables that are set internally.
    /** True if we are loading data */
    private boolean loading = true;
    /** The view for the history list */
    private ListView listView;
    /** The view for if the list is empty */
    private View empty_view;
    /** The view for if the list is loading. */
    private View load_view;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pref.addListener(this);

        // Select the appropriate handler and loader id
        Bundle args = getArguments();
        int type = -1;
        if(args != null) type = args.getInt(PARAM_TYPE, -1);
        switch (type) {
            case 0: loaderId = LoaderId.SAMPLE_SUPPLY;
                    handler  = new HandlerSamples(getActivity());
                    break;
            case 1: loaderId = LoaderId.FAVORITES;
                    handler  = new HandlerHistory(getActivity(), true);
                    break;
            case 2: loaderId = LoaderId.HISTORY;
                    handler  = new HandlerHistory(getActivity(), false);
                    break;
            default: throw new IllegalArgumentException("panel type not set");
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if(loaderId == LoaderId.SAMPLE_SUPPLY)
             getActivity().getSupportLoaderManager()
                          .restartLoader(loaderId, null, this);
        else getActivity().getSupportLoaderManager()
                          .initLoader(loaderId, null, this);
    }


    /** Called to create this fragment's view for the first time.  */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Basic view setup and retrieval
        View view = inflater.inflate(R.layout.fragment_history_panel, container, false);
        listView = (ListView) view.findViewById(R.id.card_list);
        empty_view = view.findViewById(android.R.id.empty);
        load_view = view.findViewById(android.R.id.progress);

        // View configuration
        listView.setAdapter(handler);
        listView.setOnItemClickListener(this);
        updateEmpty();
        return view;
    }


    @Override
    public void onDestroy() {
        Pref.removeListener(this);
        super.onDestroy();
    }


    /** Updates the empty list view to reflect if we are loading or not */
    private void updateEmpty() {
        if(listView == null || empty_view == null || load_view == null)
            return;
        empty_view.setVisibility(View.GONE);
        load_view.setVisibility(View.GONE);
        if(loading) listView.setEmptyView(load_view);
        else listView.setEmptyView(empty_view);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        loading = true;
        updateEmpty();
        return handler.onCreateLoader(id, args);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        handler.onLoadFinished(loader, data);
        loading = false;
        updateEmpty();
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        handler.onLoaderReset(loader);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(getActivity(), ActivitySupply.class);
        if(loaderId == LoaderId.SAMPLE_SUPPLY) i.putExtra(ActivitySupply.PARAM_SUPPLY_ID, id);
        else i.putExtra(ActivitySupply.PARAM_HISTORY_ID, id);
        getActivity().startActivity(i);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if(Pref.COMP_LANG.equals(key))
            getActivity().getSupportLoaderManager()
                         .restartLoader(loaderId, null, this);
    }
}