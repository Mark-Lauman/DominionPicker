package ca.marklauman.dominionpicker.shuffler;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.settings.Pref;

/** Class devoted to keeping track of the rules involved in a shuffle.
 *  This class does not do the shuffle itself. That is done by
 *  TODO: Name the shuffler.
 *  @author Mark Lauman */
public class SupplyBuilder extends Supply {

    /** Minimum amount of kingdom cards in a shuffle. */
    public final int minKingdom;
    /** Maximum amount of special cards in a shuffle (events and landmarks) */
    public final int maxSpecial;
    /** The basic filter used to exclude cards that will NEVER be in the supply.
     *  This filter will always contain something. You can join it to other clauses
     *  with AND or OR safely. */
    public final String baseFilter;
    /** Cards deselected in FragmentPicker */
    public final String banCards;
    /** Discarded cards that were swiped out of the shuffle */
    public final ArrayList<Long> disCards;


    /** Load the standard shuffle state from the preferences. */
    public SupplyBuilder(SharedPreferences pref) {
        minKingdom = pref.getInt(Pref.LIMIT_SUPPLY, 0);
        maxSpecial = pref.getInt(Pref.LIMIT_EVENTS, 0);
        baseFilter = getBulkFilter(pref);
        banCards = pref.getString(Pref.FILT_CARD, "");
        disCards = new ArrayList<>(5);
    }


    /** Retrieve the combined total of all
     *  @param pref The filter will be constructed from these preferences.
     *  @return The basic filter. This is never empty or null, and can safely be joined
     *  to other clauses using AND/OR. */
    @NonNull
    public static String getBulkFilter(SharedPreferences pref) {
        // Filter out sets (the set filter is always present)
        String curSel = pref.getString(Pref.FILT_SET, "");
        String filter = (curSel.length()==0) ? TableCard._SET_ID+"=NULL"
                                             : TableCard._SET_ID+" IN ("+curSel+")";
        // Filter out potions
        if(!pref.getBoolean(Pref.FILT_POTION, true))
            filter += " AND "+TableCard._POT+"=0";

        // Filter out coins
        curSel = pref.getString(Pref.FILT_COST, "");
        if(0 < curSel.length())
            filter += " AND "+TableCard._COST_VAL+" NOT IN ("+curSel+")";

        // Filter out debt
        curSel = pref.getString(Pref.FILT_DEBT, "");
        if(0 < curSel.length())
            filter += " AND "+TableCard._DEBT+" NOT IN ("+curSel+")";

        // Filter out cursers
        if(!pref.getBoolean(Pref.FILT_CURSE, true))
            filter += " AND " + TableCard._META_CURSER + "=0";

        return filter;
    }

    public static final Parcelable.Creator<Supply> CREATOR
            = new Parcelable.Creator<Supply>() {
        public Supply createFromParcel(Parcel in) {
            return new Supply(in);
        }

        public Supply[] newArray(int size) {
            return new Supply[size];
        }
    };
}