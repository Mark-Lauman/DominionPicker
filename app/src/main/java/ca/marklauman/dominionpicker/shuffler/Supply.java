package ca.marklauman.dominionpicker.shuffler;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.database.TableSupply;


/** The basic information needed to represent a supply for a game of Dominion.
 *  @author Mark Lauman */
public class Supply implements Parcelable {

    /** The timestamp of this supply (maps to its database id) */
    public long time;
    /** The name of this supply (optional, may be null) */
    public String name;
    /** The cards in the supply. */
    public final ArrayList<Long> cards;
    /** {@code true} if colonies + platinum are in use. */
    public boolean high_cost;
    /** {@code true} if shelters are in use. */
    public boolean shelters;
    /** {@code true} if this is from the sample database. */
    public boolean sample;
    /** The id of the bane card, or -1 if there isn't one. */
    public long bane;


    Supply() {
        cards = new ArrayList<>();
    }


    public Supply(Cursor c) {
        time  = c.getLong(  c.getColumnIndex(TableSupply._ID));
        name  = c.getString(c.getColumnIndex(TableSupply._NAME));
        bane = c.getLong(c.getColumnIndex(TableSupply._BANE));
        high_cost = c.getInt(c.getColumnIndex(TableSupply._HIGH_COST)) != 0;
        shelters = c.getInt(c.getColumnIndex(TableSupply._SHELTERS)) != 0;
        String[] cardList = c.getString(c.getColumnIndex(TableSupply._CARDS))
                             .split(",");
        cards = new ArrayList<>(cardList.length);
        for(String id : cardList)
            try{ cards.add(Long.parseLong(id));
            } catch(NumberFormatException ignored) {}
    }


    /** Constructor for unpacking a parcel into a {@code Supply} */
    Supply(Parcel in) {
        time = in.readLong();
        cards = new ArrayList<>();
        in.readList(cards, null);
        boolean[] booleans = in.createBooleanArray();
        high_cost = booleans[0];
        shelters = booleans[1];
        sample = booleans[2];
        bane = in.readLong();
    }


    /** String for debugging */
    @Override
    public String toString (){
        String res = "Supply {";
        res += high_cost ? "high cost, " : "low cost, ";
        res += shelters ? "shelters, " : "no shelters, ";
        res += sample ? "sample, " : "history, ";
        res += "bane=" + bane + ",  ";
        res += cards + "}";
        return res;
    }


    /** Returns {@code true} if a Black Market is in the supply. */
    public boolean blackMarket() {
        if(cards == null) return false;
        for(long card : cards)
            if(card == TableCard.ID_BLACK_MARKET) return true;
        return false;
    }


    @Override
    public int describeContents() {
    	/* As far as I can tell, this method has no function. */
        return 0;
    }


    /** Flatten this Supply in to a Parcel.
     *  @param out The Parcel in which the Supply
     *  should be written.
     *  @param flags Parameter is ignored */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(time);
        out.writeList(cards);
        out.writeBooleanArray(new boolean[]{high_cost, shelters, sample});
        out.writeLong(bane);
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