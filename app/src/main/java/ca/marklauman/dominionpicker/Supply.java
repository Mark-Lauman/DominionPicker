package ca.marklauman.dominionpicker;

import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

import ca.marklauman.dominionpicker.database.CardDb;

/** Contains all information about a supply set.
 *  @author Mark Lauman                       */
public class Supply implements Parcelable {

    /** The timestamp of this supply (maps to its database id) */
    public long time;
    /** The name of this supply (optional, may be null) */
    public String name;
	/** The cards in the supply. */
	public long[] cards;
	/** {@code true} if colonies + platinum are in use. */
	public boolean high_cost;
	/** {@code true} if shelters are in use. */
	public boolean shelters;
	/** The id of the bane card, or -1 if
	 *  there isn't one.               */
	long bane;
	

	/** Basic constructor */
	public Supply() {
        time = 0;
        name = null;
		cards = null;
		high_cost = false;
		shelters = false;
		bane = -1L;
	}
	
	
	/** Constructor for unpacking a parcel
	 *  into a {@code Supply} */
    private Supply(Parcel in) {
    	cards = in.createLongArray();
    	boolean[] booleans = in.createBooleanArray();
    	high_cost = booleans[0];
    	shelters = booleans[1];
    	bane = in.readLong();
    }
    
    
    /** String for debugging */
    @Override
    public String toString (){
    	String res = "Supply {";
    	if(high_cost) res += "high cost, ";
    	else		  res += "low cost, ";
    	if(shelters) res += "shelters, ";
    	else		 res += "no shelters, ";
    	res += "bane=" + bane + ",  ";
    	res += Arrays.toString(cards);
    	return res + "}";
    }


    /** Returns {@code true} if a Black Market is in the supply. */
    public boolean blackMarket() {
        if(cards == null) return false;
        for(long card : cards)
            if(card == CardDb.ID_BLACK_MARKET) return true;
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
		out.writeLongArray(cards);
		boolean[] booleans = new boolean[] {high_cost,
										 shelters };
		out.writeBooleanArray(booleans);
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