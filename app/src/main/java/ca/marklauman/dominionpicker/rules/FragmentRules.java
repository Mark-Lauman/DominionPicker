package ca.marklauman.dominionpicker.rules;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

import ca.marklauman.dominionpicker.R;

/**
 *
 * Created by Mark on 2015-11-28.
 */
public class FragmentRules extends Fragment {

    /** View used to show this fragment is loading */
    private View viewLoading;
    /** Show this view when all data is loaded */
    private View viewLoaded;
    /** View holding a list of all sets */
    private LinearLayout viewExp;

    /** Called to create this fragment's view for the first time.  */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rules_simple, container, false);
        viewLoading = view.findViewById(R.id.loading);
        viewLoaded = view.findViewById(R.id.loaded);
        viewExp = (LinearLayout) view.findViewById(R.id.list_expansion);
        updateView();
        return view;
    }

    private void updateView() {
        if(viewExp == null) return;

        Context c = getActivity();
        if(c == null) return;

        CheckedTextView check = (CheckedTextView)View.inflate(c, R.layout.rule_checkbox, null);
        check.setTag(1);
        viewExp.addView(check);
        Log.d("check1", "" + check.getTag());

        check = (CheckedTextView)View.inflate(c, R.layout.rule_checkbox, null);
        check.setTag(2);
        viewExp.addView(check);
        Log.d("check2", ""+check.getTag());

        viewLoading.setVisibility(View.GONE);
        viewLoaded.setVisibility(View.VISIBLE);
    }
}