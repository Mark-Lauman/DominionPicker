/* Copyright (c) 2014 Mark Christopher Lauman
 * 
 * Licensed under the The MIT License (MIT)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.                                                                  */
package ca.marklauman.dominionpicker;

import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

/** Contains all information about a supply set.
 *  @author Mark Lauman                       */
public class Supply implements Parcelable {
	
	
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
		cards = null;
		high_cost = false;
		shelters = false;
		bane = -1L;
	}
	
	
	/** Constructor for unpacking a parcel
	 *  into a {@code Supply} */
    private Supply(Parcel in) {
    	cards = in.createLongArray();
    	boolean[] bools = in.createBooleanArray();
    	high_cost = bools[0];
    	shelters = bools[1];
    	bane = in.readLong();
    	this.toString();
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
	
    
    @Override
    public int describeContents() {
    	/* As far as I can tell, this method
    	 * has no function.               */
        return 0;
    }
    
    
    /** Flatten this Supply in to a Parcel.
     *  @param out The Parcel in which the Supply
     *  should be written. 
     *  @param flags Parameter is ignored */
    @Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLongArray(cards);
		boolean[] bools = new boolean[] {high_cost,
										 shelters };
		out.writeBooleanArray(bools);
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