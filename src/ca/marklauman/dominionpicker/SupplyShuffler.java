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

import java.util.ArrayList;
import java.util.Arrays;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

public class SupplyShuffler extends AsyncTask<Long, Void, Supply> {
	
	
	/** The activity that needs the supply. */
	SupplyActivity callback;
	
	
	/** String tied to the Prosperity set */
	String set_prosp;
	/** String tied to the Dark Ages set */
	String set_dark;
	
	
	/** Default Constructor
	 *  @param activity The activity that needs the supply.
	 */
	public SupplyShuffler(SupplyActivity activity) {
		super();
		callback = activity;
		set_prosp = activity.getString(R.string.set_prosperity);
		set_dark  = activity.getString(R.string.set_dark_ages);
	}
	
	
	/** Attempt to choose the supply.
	 *  @param possibleSupply All cards available for use
	 *  in the supply. This input is NOT sanitized. It is
	 *  assumed that by this point all invalid supply
	 *  combos have been checked.                      */
	@Override
	protected Supply doInBackground(Long... possibleSupply) {
		// Turn the poosibleSupply into an ArrayList pool
		ArrayList<Long> pool = new ArrayList<Long>(possibleSupply.length);
		for(long card : possibleSupply)
			pool.add(card);
		
		Supply supply = new Supply();
		
		// Choose the cards for the supply
		long[] cards = new long[10];
		for(int i=0; i<10; i++) {
			int pick = (int) (Math.random() * pool.size());
			long pick_val = pool.get(pick);
			pool.remove(pick);
			cards[i] = pick_val;
		}
		supply.cards = cards;
		
		/* Choose two cards from the supply.
		 * The cards' sets will indicate whether
		 * colonies and/or shelters are used. */
		String[] resources = new String[2];
		int pick = (int) (Math.random() * cards.length);
		resources[0] = "" + cards[pick];
		pick = (int) (Math.random() * cards.length);
		resources[1] = "" + cards[pick];
		
		// Get the sets the resource cards are from
		// and replace the resources strings with them
		if(callback == null) return null;
		Cursor c = callback.getContentResolver()
				.query(CardList.URI,
					   new String[]{CardList._ID,
									CardList._EXP},
					   CardList._ID + "=? OR "
					   + CardList._ID + "=?",
					   resources, null);
		int col_id = c.getColumnIndex(CardList._ID);
		int col_set = c.getColumnIndex(CardList._EXP);
		c.moveToPosition(-1);
		while(c.moveToNext()) {
			if(resources[0].equals("" + c.getLong(col_id)))
				resources[0] = c.getString(col_set);
			else
				resources[1] = c.getString(col_set);
		}
		
		// Determine colonies and shelters from the sets
		if(set_prosp.equals(resources[0]))
			supply.high_cost = true;
		if(set_dark.equals(resources[1]))
			supply.shelters = true;
		
		return supply;
	}
	
	
	/** Apply the supply to the calling
	 *  {@link SupplyActivity}.      
	 *  @param supply The supply chosen by
	 *  {@link #doInBackground(Long...)}. */
	@Override
	protected void onPostExecute(Supply supply) {
		if(callback == null) return;
		
		callback.setSupply(supply);
		
		Log.d("supply", "" + Arrays.toString(supply.cards));
		Log.d("colonies", "" + supply.high_cost);
		Log.d("shelters", "" + supply.shelters);
		Log.d("bane", "" + "" + supply.bane);
	}
}