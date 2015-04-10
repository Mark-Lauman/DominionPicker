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
import java.util.Collection;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import ca.marklauman.dominionpicker.database.CardDb;
import ca.marklauman.dominionpicker.database.Provider;

/** Task for choosing the supply cards. This task
 *  assumes a valid supply IS possible.
 *  @author Mark Lauman                        */
class SupplyShufflerOld extends AsyncTask<Long, Void, Supply> {

    public interface SupplyListener {
        /** Called when the SupplyShuffler is finished. */
        public void shuffleComplete(Supply supply);
    }
	

    /** The content resolver used to access the database. */
    private final ContentResolver resolver;
	
	
	/** String tied to the Prosperity set */
	private final String set_prosperity;
	/** String tied to the Dark Ages set */
	private final String set_dark;
	
	
	/** Default Constructor */
	public SupplyShufflerOld(Context c) {
		super();
        resolver = c.getContentResolver();
		set_prosperity = c.getString(R.string.set_prosperity);
		set_dark  = c.getString(R.string.set_dark_ages);
	}
	
	
	/** Attempt to choose the supply.
	 *  @param possibleSupply All cards available for use
	 *  in the supply. This input is NOT sanitized. It is
	 *  assumed that by this point all invalid supply
	 *  combos have been checked.                      */
	@Override
	protected Supply doInBackground(Long... possibleSupply) {
		// Turn the possibleSupply into an ArrayList pool
		ArrayList<Long> pool = new ArrayList<>(possibleSupply.length);
        pool.addAll(Arrays.asList(possibleSupply));
		
		Supply supply = new Supply();
		
		// Choose the cards for the supply
		int supply_size = 10;
		ArrayList<Long> cards = new ArrayList<>(supply_size);
		for(int i=0; i<supply_size; i++) {
            // break off if this is cancelled
            if(isCancelled()) return null;

			// Take a card from the pool
			int pick = (int) (Math.random() * pool.size());
			long pick_val = pool.get(pick);
			pool.remove(pick);
			cards.add(pick_val);

			// If it is the young witch
			if(pick_val == CardDb.ID_YOUNG_WITCH) {
				// Try to pick a bane card from the pool
				long bane = pickBane(pool);
				if(bane != -1) {
					// we have a valid bane card. Pick it.
					pool.remove(pool.indexOf(bane));
					cards.add(bane);
					i++;
					supply.bane = bane;
				} else {
					// All available bane cards are in the
					// supply already
					supply.bane = pickBane(cards);
				}
				// add one supply pile for the bane
				supply_size++;
			}
		}
		
		// Set the supply cards
		supply.cards = new long[cards.size()];
		for(int i=0; i<supply_size; i++)
			supply.cards[i] = cards.get(i);
		
		/* Choose two cards from the supply.
		 * The cards' sets will indicate whether
		 * colonies and/or shelters are used. */
		String[] resources = new String[2];
		int pick = (int) (Math.random() * supply.cards.length);
		resources[0] = "" + supply.cards[pick];
		pick = (int) (Math.random() * supply.cards.length);
		resources[1] = "" + supply.cards[pick];

        // Due to multi-threading, callback could have
        // been deleted before this point.
        //noinspection ConstantConditions
//        if(callback == null) return null;
		
		// Get the sets the resource cards are from
		// and replace the resource strings with them
		Cursor c = resolver.query(Provider.URI_CARDS,
					              new String[]{CardDb._ID, CardDb._EXP},
					              CardDb._ID + "=? OR " + CardDb._ID + "=?",
					              resources, null);
		int col_id = c.getColumnIndex(CardDb._ID);
		int col_set = c.getColumnIndex(CardDb._EXP);
		c.moveToPosition(-1);
		while(c.moveToNext()) {
			if(resources[0].equals("" + c.getLong(col_id)))
				resources[0] = c.getString(col_set);
			else
				resources[1] = c.getString(col_set);
		}
        c.close();
		
		// Determine colonies and shelters from the sets
		if(set_prosperity.equals(resources[0]))
			supply.high_cost = true;
		if(set_dark.equals(resources[1]))
			supply.shelters = true;
		
		return supply;
	}
	
	
	/** Apply the supply to the calling
	 *  {@link ActivitySupply}.
	 *  @param supply The supply chosen by
	 *  {@link #doInBackground(Long...)}. */
	@Override
	protected void onPostExecute(Supply supply) {
        // Due to multi-threading, callback could have
        // been deleted before this point.
        //noinspection ConstantConditions
//        if(isCancelled() || callback == null) return;
//		callback.shuffleComplete(supply);
	}
	
	
	/** Pick a bane card from the provided pool.
	 *  @param pool The pool of card ids to choose from.
	 *  @return The id of the bane card or -1 if no
	 *  valid bane could be found.                    */
    long pickBane(Collection<Long> pool) {
		/* Selection string
		 * format: (cost=? OR cost=? AND (id=? OR ...) */
		String sel = "";
        for (Long ignored : pool) {
            sel += " OR " + CardDb._ID + "=?";
        }
		sel = "(" + CardDb._COST + "=? OR "
					  + CardDb._COST + "=?) AND ("
				  + sel.substring(4) + ")";
		
		// Selection arguments
		String[] selArgs = new String[pool.size() +2];
		// Card cost
		selArgs[0] = "2";
		selArgs[1] = "3";
		// Card ids
		int i = 2;
		for(long id : pool) {
			selArgs[i] = "" + id;
			i++;
		}
		
		// Run a query for matching items
		Cursor c = resolver.query(Provider.URI_CARDS,
                new String[]{CardDb._ID},
                sel, selArgs, null);
		
		// No result = no bane available.
		if(c.getCount() < 1)
			return -1;
		
		// Choose a bane from the available cards
		int id_col = c.getColumnIndex(CardDb._ID);
		int bane_pos = (int) (Math.random() * c.getCount());
		c.moveToPosition(bane_pos);
        long res = c.getLong(id_col);
        c.close();
		return res;


	}
}