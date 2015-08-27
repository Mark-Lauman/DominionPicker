package ca.marklauman.dominionpicker.history;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.tools.SlidingTabLayout;

/** Governs the History screen, and all three of its panels.
 *  @author Mark Lauman */
public class FragmentHistory extends Fragment {
    /** Key used to save the active tab to savedInstanceState */
    private static final String ACTIVE_KEY = "activeTab";
    /** The currently active tab */
    private int activeTab = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
            activeTab = savedInstanceState.getInt(ACTIVE_KEY);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ACTIVE_KEY, activeTab);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Tab setup
        ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapter(getActivity(), getChildFragmentManager()));
        pager.setCurrentItem(activeTab);
        SlidingTabLayout tabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
        tabs.setViewPager(pager);

        return view;
    }

    /** Adapter used to switch panels */
    private class PagerAdapter extends FragmentStatePagerAdapter {
        /** The tab names */
        final String[] titles;

        public PagerAdapter(Context c, FragmentManager fm) {
            super(fm);
            titles = c.getResources().getStringArray(R.array.historyTypes);
        }

        @Override
        public Fragment getItem(int position) {
            activeTab = position;
            FragmentHistoryPanel res = new FragmentHistoryPanel();
            switch (position) {
                case 0: res.loaderId = LoaderId.SAMPLE_SUPPLY;
                        res.handler = new HandlerSamples(getActivity());
                        return res;
                case 1: res.loaderId = LoaderId.FAVORITES;
                        res.handler = new HandlerHistory(getActivity(), true);
                        return res;
                case 2: res.loaderId = LoaderId.HISTORY;
                        res.handler = new HandlerHistory(getActivity(), false);
                        return res;
                default: return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}