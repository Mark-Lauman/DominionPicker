package ca.marklauman.dominionpicker.rules;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.settings.Prefs;

/** The fragment governing the Rules screen.
 *  @author Mark Lauman */
public class FragmentRules extends Fragment
                           implements Prefs.Listener {
    /** Adapter used to load and display the rules */
    private RulesAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Prefs.addListener(this);
    }

    @Override
    public void onDestroy() {
        Prefs.removeListener(this);
        super.onDestroy();
    }

    /** Called to create this fragment's view for the first time.  */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rules, container, false);
        ListView listView = (ListView)view.findViewById(R.id.loaded);
        adapter = new RulesAdapter(getContext(), listView);
        listView.setOnItemClickListener(adapter);
        listView.setAdapter(adapter);
        return view;
    }


    @Override
    public void prefChanged(String key) {
        switch(key) {
            case Prefs.FILT_LANG: case Prefs.SORT_CARD:
                if(adapter != null) adapter.rebuild();
                break;
        }
    }
}