package ca.marklauman.dominionpicker.userinterface.recyclerview;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.LoaderId;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.settings.Pref;
import ca.marklauman.dominionpicker.userinterface.icons.CoinIcon;
import ca.marklauman.dominionpicker.userinterface.icons.DebtIcon;
import ca.marklauman.dominionpicker.userinterface.icons.IconDescriber;
import ca.marklauman.dominionpicker.userinterface.recyclerview.rules.Rule;
import ca.marklauman.dominionpicker.userinterface.recyclerview.rules.RuleCheckbox;
import ca.marklauman.dominionpicker.userinterface.recyclerview.rules.RuleNumber;
import ca.marklauman.dominionpicker.userinterface.recyclerview.rules.RuleSection;
import ca.marklauman.tools.Utils;

/** The card filtration rules are displayed and loaded in this adapter.
 *  This is a basic filter - it does not set odds of cards.
 *  @author Mark Lauman. */
public class AdapterRules extends Adapter<Rule>
                          implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Type for a new section */
    private static final int TYPE_SECTION = 0;
    /** Type for a number rule */
    private static final int TYPE_NUMBER = 1;
    /** Type for a checkbox rule. */
    private static final int TYPE_CHECK = 2;


    /** Context used to construct this adapter */
    private final AppCompatActivity mContext;
    /** RecyclerView covered by this adapter. */
    private final RecyclerView recycler;
    /** The type of each ViewHolder in the rule list */
    private final ArrayList<Integer> types = new ArrayList<>();
    /** The value assigned to each ViewHolder in the rule list */
    private final ArrayList<Object> values = new ArrayList<>();
    /** The IconDescriber used for the many icons in this adapter. */
    private final IconDescriber describer;

    /** Current value of the set filter. */
    private final HashSet<String> filt_set = new HashSet<>();
    /** Current value of the cost filter */
    private final HashSet<String> filt_cost = new HashSet<>();
    /** Current value of the curse filter */
    private final HashSet<String> filt_debt = new HashSet<>();

    /** True if this adapter has a scrollbar */
    private boolean hasScroll = false;
    /** Position of the last element in the fully loaded rule list */
    private int lastItem = -1;


    /** Default constructor for the adapter. */
    public AdapterRules(RecyclerView target) {
        mContext = (AppCompatActivity) target.getContext();
        recycler = target;
        describer = new IconDescriber(mContext);

        // hasScroll is set to true the moment a nonzero scroll happens
        target.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy == 0) return;
                hasScroll = true;
                recyclerView.removeOnScrollListener(this);
            }
        });

        // retrieve the basic preferences and reload the list of options.
        SharedPreferences pref = Pref.get(mContext);
        loadPref(pref, Pref.FILT_SET, filt_set);
        loadPref(pref, Pref.FILT_COST, filt_cost);
        loadPref(pref, Pref.FILT_DEBT, filt_debt);
        reload();
    }


    /** Load a preference into a HashSet of included values. */
    private void loadPref(SharedPreferences pref, String key, HashSet<String> destination) {
        String raw_filt = pref.getString(key, "");
        Collections.addAll(destination, raw_filt.length() < 1 ? new String[]{}
                                                              : raw_filt.split(","));
    }


    /** Save the rule values to memory. */
    public void save() {
        Pref.edit(mContext)
            .putString(Pref.FILT_SET, Utils.join(",", filt_set))
            .putString(Pref.FILT_COST, Utils.join(",", filt_cost))
            .putString(Pref.FILT_DEBT, Utils.join(",", filt_debt))
            .commit();
    }


    /** Retrieve the type of a the view at this position. */
    @Override
    public int getItemViewType(int position) {
        return types.get(position);
    }


    /** Create an empty rule for the given position in the adapter. */
    @Override @NonNull
    public Rule onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch(viewType) {
            case TYPE_CHECK:   return new RuleCheckbox(recycler);
            case TYPE_SECTION: return new RuleSection(recycler);
            case TYPE_NUMBER:  return new RuleNumber(recycler);
            default: throw new UnsupportedOperationException("Invalid rule type");
        }
    }


    /** Put data into the rule at the given position. */
    @Override
    public void onBindViewHolder(@NonNull Rule holder, int position) {
        holder.setLast(hasScroll && position == lastItem);
        holder.setValue(values.get(position));
    }


    @Override
    public int getItemCount() {
        return values.size();
    }


    /** Insert a rule at the provided position.
     *  Please note that this does not notify listeners the insertion occurred -
     *  for that you need to call {@link #notifyItemInserted(int)}.
     *  @param position Where to insert the rule.
     *  @param type The type of rule to insert.
     *  @param value The value assigned to that rule. */
    private void insertRule(int position, int type, Object value) {
        types.add(position, type);
        values.add(position, value);
    }


    /** Clear the adapter and begin to reload it. */
    public void reload() {
        // Clear the adapter
        int oldSize = types.size();
        types.clear();
        values.clear();

        // And the top few entries and the loading icon
        types.add(TYPE_NUMBER);
        values.add(new String[]{Pref.LIMIT_SUPPLY,
                   mContext.getString(R.string.limit_supply)});
        types.add(TYPE_SECTION);
        values.add(mContext.getString(R.string.rules_expansions));

        // Notify any listeners what has changed.
        if(3 < oldSize) {
            notifyItemChanged(2);
            notifyItemRangeRemoved(3, oldSize);
        } else notifyDataSetChanged();

        // Start loading the expansions
        startLoader(LoaderId.RULES_EXP);
    }


    /** Start loading the given set of data. */
    private void startLoader(int loaderId) {
        mContext.getSupportLoaderManager()
                .restartLoader(loaderId, null, this);
    }


    @Override @NonNull
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id) {
            case LoaderId.RULES_EXP:
                return new CursorLoader(mContext, Provider.URI_CARD_SET,
                        new String[]{TableCard._SET_ID, TableCard._SET_NAME, TableCard._PROMO},
                                     Pref.languageFilter(mContext), null,
                                     TableCard._PROMO+", "+ Pref.setSort(mContext));
            case LoaderId.RULES_COST:
                return new CursorLoader(mContext, Provider.URI_CARD_DATA_U,
                                        new String[]{TableCard._COST_VAL},
                                        null, null, TableCard._COST_VAL);
            case LoaderId.RULES_DEBT:
                return new CursorLoader(mContext, Provider.URI_CARD_DATA_U,
                                        new String[]{TableCard._DEBT},
                                        null, null, TableCard._DEBT);
        }
        throw new UnsupportedOperationException("No loader specified");
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int start = values.size();
        int inserted = 0;

        switch(loader.getId()) {
            case LoaderId.RULES_EXP:
                final int[] icons = Utils.getResourceArray(mContext, R.array.card_set_icons);
                final int _id = data.getColumnIndex(TableCard._SET_ID);
                final int _name = data.getColumnIndex(TableCard._SET_NAME);
                final int _promo = data.getColumnIndex(TableCard._PROMO);

                // Insert the expansions
                data.moveToFirst();
                do {
                    int id = data.getInt(_id);
                    insertRule(start+inserted, TYPE_CHECK,
                               new RuleCheckbox.Data(icons[id], data.getString(_name),
                                                     false, ""+id, filt_set));
                    inserted++;
                } while(data.moveToNext() && data.getInt(_promo) == 0);

                // Insert the promo cards
                insertRule(start+inserted, TYPE_SECTION, mContext.getString(R.string.rules_promo));
                inserted++;
                do {
                    int id = data.getInt(_id);
                    insertRule(start+inserted, TYPE_CHECK,
                               new RuleCheckbox.Data(icons[id], data.getString(_name),
                                                     false, ""+id, filt_set));
                    inserted++;
                } while(data.moveToNext());

                // Insert the cost section header
                insertRule(start+inserted, TYPE_SECTION,
                           mContext.getString(R.string.rules_cost));
                inserted++;
                startLoader(LoaderId.RULES_COST);
                break;


            case LoaderId.RULES_COST:
                // Insert the potion cost
                insertRule(start+inserted, TYPE_CHECK,
                           new RuleCheckbox.Data(R.drawable.ic_dom_potion,
                                                 mContext.getString(R.string.potion),
                                                 false, Pref.FILT_POTION, null));
                inserted++;

                final int _cost = data.getColumnIndex(TableCard._COST_VAL);
                data.moveToFirst();
                do {
                    String cost = data.getString(_cost);
                    insertRule(start+inserted, TYPE_CHECK,
                               new RuleCheckbox.Data(new CoinIcon(mContext, describer, cost),
                                                     true, cost, filt_cost));
                    inserted++;
                } while(data.moveToNext());
                startLoader(LoaderId.RULES_DEBT);
                break;

            case LoaderId.RULES_DEBT:
                // Insert the debt filters.
                final int _debt = data.getColumnIndex(TableCard._DEBT);
                data.moveToFirst();
                do {
                    String debt = data.getString(_debt);
                    insertRule(start+inserted, TYPE_CHECK,
                               new RuleCheckbox.Data(new DebtIcon(mContext, describer, debt),
                                                     true, debt, filt_debt));
                    inserted++;
                } while(data.moveToNext());

                // Insert the curse filter
                insertRule(start+inserted, TYPE_SECTION,
                           mContext.getString(R.string.rules_other));
                inserted++;
                insertRule(start+inserted, TYPE_CHECK,
                        new RuleCheckbox.Data(R.drawable.ic_dom_curse,
                                              mContext.getString(R.string.rules_curse),
                                              false, Pref.FILT_CURSE, null));
                lastItem = start+inserted;
                inserted++;
        }

        notifyItemRangeInserted(start, inserted);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}
}