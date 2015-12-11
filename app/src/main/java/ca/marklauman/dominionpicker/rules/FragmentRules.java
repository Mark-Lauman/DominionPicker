package ca.marklauman.dominionpicker.rules;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import ca.marklauman.dominionpicker.App;
import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableCard;

/** The fragment governing the Rules screen.
 *  @author Mark Lauman */
public class FragmentRules extends Fragment {
    /** Card sets available to be filtered */
    private Cursor cardSets;

    /** View used to show this fragment is loading */
    private View viewLoading;
    /** Show this view when all data is loaded */
    private ListView viewLoaded;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity act = (FragmentActivity) context;
        act.getSupportLoaderManager()
           .initLoader(LoaderId.RULES_EXP, null, new SetLoader());
    }

    /** Called to create this fragment's view for the first time.  */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rules, container, false);
        viewLoading = view.findViewById(R.id.loading);
        viewLoaded = (ListView)view.findViewById(R.id.loaded);
        updateView();
        return view;
    }


    private void updateView() {
        if(viewLoaded == null || cardSets == null) return;
        RulesAdapter adapter = new RulesAdapter(getContext(), cardSets);
        viewLoaded.setAdapter(adapter);
        viewLoaded.setOnItemClickListener(adapter);
        viewLoading.setVisibility(View.GONE);
        viewLoaded.setVisibility(View.VISIBLE);
    }


    private class SetLoader implements LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), Provider.URI_CARD_SET,
                                    new String[]{TableCard._SET_ID, TableCard._SET_NAME,
                                                 TableCard._PROMO},
                                    App.transFilter, null,
                                    TableCard._PROMO+", "+TableCard._SET_NAME);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            cardSets = data;
            updateView();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            cardSets = null;
        }
    }
}