package ca.marklauman.dominionpicker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.marklauman.dominionpicker.settings.Pref;
import ca.marklauman.dominionpicker.userinterface.recyclerview.AdapterRules;
import ca.marklauman.tools.recyclerview.ListDivider;

/** The fragment governing the Rules screen.
 *  @author Mark Lauman */
public class FragmentRules extends Fragment
                           implements Pref.Listener {
    /** Adapter used to load and display the rules */
    private AdapterRules adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pref.addListener(this);
    }

    @Override
    public void onDestroy() {
        Pref.removeListener(this);
        super.onDestroy();
    }

    /** Called to create this fragment's view for the first time.  */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rules, container, false);
        RecyclerView listView = view.findViewById(R.id.loaded);
        listView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        listView.addItemDecoration(new ListDivider(container.getContext()));

        adapter = new AdapterRules(listView);
        listView.setAdapter(adapter);
        return view;
    }

    /** Save the rules to the preferences. */
    public void save() {
        if(adapter != null) adapter.save();
    }


    @Override
    public void onPause() {
        save();
        super.onPause();
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        switch(key) {
            case Pref.COMP_LANG: case Pref.COMP_SORT_SET:
                if(adapter != null) adapter.reload();
                break;
        }
    }
}