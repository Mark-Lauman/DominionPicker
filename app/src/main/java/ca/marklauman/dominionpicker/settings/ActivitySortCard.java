package ca.marklauman.dominionpicker.settings;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.userinterface.recyclerview.AdapterSortCard;
import ca.marklauman.tools.Utils;
import ca.marklauman.tools.recyclerview.ListDivider;

/** Activity used to change the card sort order.
 *  @author Mark Lauman */
public class ActivitySortCard extends AppCompatActivity {

    private AdapterSortCard mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pref.checkLanguage(this);
        setContentView(R.layout.activity_sort_card);
        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setDisplayHomeAsUpEnabled(true);

        RecyclerView list = (RecyclerView)findViewById(android.R.id.list);
        if(list == null) return;
        list.setLayoutManager(new LinearLayoutManager(this));
        list.addItemDecoration(new ListDivider(this));
        mAdapter = new AdapterSortCard(this, list);
        list.setAdapter(mAdapter);
        list.hasFixedSize();
    }

    @Override
    public void onStop() {
        super.onStop();
        Pref.edit(this)
            .putString(Pref.SORT_CARD, Utils.join(",", mAdapter.getSortOrder()))
            .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item != null && item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else return super.onOptionsItemSelected(item);
    }
}