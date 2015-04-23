package ca.marklauman.dominionpicker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

import ca.marklauman.dominionpicker.database.CardDb;
import ca.marklauman.dominionpicker.database.DataDb;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.tools.Utils;

/** Task for shuffling the supply. Assumes card ids
 *  are valid, but does not assume a supply is possible
 *  with the provided cards.
 *  @author Mark Lauman */
class SupplyShuffler extends AsyncTask<Long, Void, Void> {

    /** When the shuffler is done, an intent of this type broadcasts
     *  the results back to the activity.                         */
    public static final String MSG_INTENT = "ca.marklauman.dominionpicker.shuffler";
    /** The extra in the result intent containing the result id.
     *  Will be a constant defined by this class starting with "RES_" */
    public static final String MSG_RES = "result";
    /** Extra containing card shortfall in the event
     *  of {@link #RES_MORE} or {@link #RES_MORE_K}.
     *  String formatted as "X/Y" cards.  */
    public static final String MSG_SHORT = "shortfall";
    /** The extra containing the supply. Only available on {@link #RES_OK}. */
    public static final String MSG_SUPPLY ="supply";

    /** Shuffle succeeded. Supply available in {@link #MSG_SUPPLY} */
    public static final int RES_OK = 0;
    /** Shuffle failed. Insufficient cards. Shortfall in {@link #MSG_SHORT}. */
    public static final int RES_MORE = 1;
    /** Shuffle failed. No young witch targets. */
    public static final int RES_NO_YW = 2;
    /** Shuffle failed. Insufficient kingdom cards.
     *  Shortfall in {@link #MSG_SHORT}. */
    public static final int RES_MORE_K = 3;
    /** Shuffle cancelled by outside source. */
    @SuppressWarnings("WeakerAccess")
    public static final int RES_CANCEL = 100;

    /** The set of all event card ids. */
    private static HashSet<Long> eventSet;

    /** The number of kingdom cards in the supply. */
    private int minKingdom;
    /** The max number of event cards in the supply. */
    private final int maxEvent;


    /** @param kingdom The suggested number of kingdom cards in this game.
     *  This number may be increased by some cards, such as Young Witch.
     *  @param event The max number of events in this game.          */
    public SupplyShuffler(int kingdom, int event) {
        super();
        minKingdom = kingdom;
        maxEvent = event;

        if(eventSet == null) {
            int[] eventList = MainActivity.getStaticContext()
                    .getResources()
                    .getIntArray(R.array.cards_events);
            eventSet = new HashSet<>(eventList.length);
            for(int e : eventList) eventSet.add(0L + e);
        }
    }

    @Override
    protected Void doInBackground(Long... cardIds) {
        // The result message we will send on finish
        Intent res = new Intent(MSG_INTENT);

        // Quick sanity check for number of cards
        if(cardIds.length < minKingdom) {
            res.putExtra(MSG_RES, RES_MORE);
            int size = cardIds.length;
            res.putExtra(MSG_SHORT, size +"/"+ minKingdom);
            return sendMsg(res);
        }

        // Create the card pool and base for a supply
        ArrayList<Long> pool = new ArrayList<>(Arrays.asList(cardIds));
        ArrayList<Long> kingdom = new ArrayList<>(minKingdom);
        ArrayList<Long> events = new ArrayList<>(maxEvent);
        long bane = -1;

        // Main Shuffle loop
        for(int i=0; i< minKingdom; i++) {
            // stop on cancel
            if (isCancelled()) {
                res.putExtra(MSG_RES, RES_CANCEL);
                return sendMsg(res);
            }

            /* Not enough kingdom cards. (can happen if minKingdom increases
             * or events are in the shuffle. */
            if (pool.size() < 1) {
                res.putExtra(MSG_RES, RES_MORE_K);
                res.putExtra(MSG_SHORT, kingdom.size() + "/" + minKingdom);
                return sendMsg(res);
            }

            // Pick a card from the pool at random
            long pick = pool.remove((int) (Math.random() * pool.size()));

            // If we just picked the young witch, we need an extra bane kingdom card.
            if (pick == CardDb.ID_YOUNG_WITCH) {
                bane = pickBane(cardIds);
                // Bane picking is resource intensive. Check we aren't cancelled.
                if (isCancelled()) {
                    res.putExtra(MSG_RES, RES_CANCEL);
                    return sendMsg(res);
                }
                // If no bane is available.
                if (bane < 0) {
                    // Invalidate this pick
                    pick = -1;
                    // If we don't have enough cards to finish, return
                    if (kingdom.size() + countKingdom(pool) < minKingdom) {
                        res.putExtra(MSG_RES, RES_NO_YW);
                        return sendMsg(res);
                    }
                // A bane is available! Add it to the kingdom pile if necessary
                } else {
                    minKingdom++;
                    if(! kingdom.contains(bane)) {
                        pool.remove(bane);
                        kingdom.add(bane);
                        i++;
                    }
                }
            }

            /* Place the card in the pile it belongs to. If the
             * pick is < 0, it was invalidated after drawing. */
            if (0 <= pick) {
                if (eventSet.contains(pick)) {
                    i--;  // events don't count to the kingdom card count
                    if (events.size() < maxEvent) events.add(pick);
                    // handle the card if its a kingdom card
                } else kingdom.add(pick);
            } else i--;
        }

        // Shuffle finished successfully. Start to build the supply object.
        Supply supply = new Supply();
        supply.cards = new long[kingdom.size() + events.size()];
        int i = 0;
        for(long card : kingdom) {
            supply.cards[i] = card;
            i++;
        }
        for(long card : events) {
            supply.cards[i] = card;
            i++;
        }
        supply.bane = bane;

        // Set the conditions on the supply (high cost, shelters)
        setConditions(supply);

        // Insert the supply into the history table
        supply.time = Calendar.getInstance().getTimeInMillis();
        insertSupply(supply);

        // Finish and return supply
        res.putExtra(MSG_RES, RES_OK);
        res.putExtra(MSG_SUPPLY, supply);
        return sendMsg(res);
    }


    /** Broadcast a given message back to the activity */
    @SuppressWarnings("SameReturnValue")
    private Void sendMsg(Intent msg) {
        try {
            LocalBroadcastManager.getInstance(MainActivity.getStaticContext())
                                 .sendBroadcast(msg);
        } catch(Exception ignored) {}
        return null;
    }


    /** Perform a query using the main content resolver.
     *  @return The result, or null if the query failed. */
    private static Cursor query(String[] projection,
                                String selection, String[] selectionArgs,
                                String sortBy) {
        // I retain nothing because I don't know if the contentResolver changes.
        return MainActivity.getStaticContext()
                .getContentResolver()
                .query(Provider.URI_CARDS, projection,
                        selection, selectionArgs, sortBy);
    }


    /** Pick a bane card from the available cards.
     *  @param pool The pool of all cards available for selection.
     *  @return The id of the bane card, or -1 if none could be found. */
    private long pickBane(Long... pool) {
        if(pool == null || pool.length == 0)
            return -1;

        // Make the selection string and arguments for the query
        String sel = "";
        String[] selArgs = new String[pool.length];
        for(int i=0; i<selArgs.length; i++) {
            selArgs[i] = "" + pool[i];
            sel += " OR " + CardDb._ID + "=?";
        }
        // select cards that cost 2 or 3 and are in the card pool
        sel = "(" + CardDb._COST + "=2 OR "
                  + CardDb._COST + "=3) "
              + "AND (" + sel.substring(4) + ")";

        // query for the bane card and return it.
        long res = -1;
        try {
            Cursor c = query(new String[]{CardDb._ID}, sel, selArgs, "random() LIMIT 1");
            if(c == null) return -1;
            if(c.getCount() < 1) {
                c.close();
                return -1;
            }
            int id = c.getColumnIndex(CardDb._ID);
            c.moveToFirst();
            res = c.getLong(id);
            c.close();
        } catch(Exception ignored) {}
        return res;
    }


    /** Count the number of kingdom cards in the provided ArrayList.
     *  Please note that this requires a database query, and is thus
     *  I/O intensive. Only query if you need this.
     *  @param cardIds The ids of al the cards we're interested in.
     *  @return The number of kingdom cards in that set. 0 If no kingdom
     *  cards are found. */
    private int countKingdom(ArrayList<Long> cardIds) {
        if(cardIds == null || cardIds.size() == 0)
            return 0;

        // make sel and selArgs match all cards in cardIds
        String sel = "";
        String selArgs[] = new String[cardIds.size() + 1];
        for(int card=0; card<cardIds.size(); card++) {
            sel += " OR " + CardDb._ID + "=?";
            selArgs[card] = ""+cardIds.get(card);
        }
        if(sel.length() > 4) sel = sel.substring(4);

        // Make sel and selArgs not match event cards
        sel = "("+sel+") AND replace(" + CardDb._CATEGORY + ", ?, '') != " + CardDb._CATEGORY;
        selArgs[selArgs.length - 1] = Provider.TYPE_EVENT;

        // run the query, return the result. Default to none found.
        int count = 0;
        try {
            Cursor c = query(new String[]{"count(*) AS " + BaseColumns._ID},
                             sel, selArgs, null);
            if(c == null) return 0;
            if(c.getCount() == 0) {
                c.close();
                return 0;
            }
            int id = c.getColumnIndex(BaseColumns._ID);
            c.moveToFirst();
            count = c.getInt(id);
            c.close();
        } catch(Exception ignored){}
        return count;
    }


    /** Set the conditionals found in the supply.
     *  (high cost and shelters).
     *  Requires a database query. */
    private void setConditions(Supply supply) {
        // pick two cards, one for cost, one for shelter.
        String[] conditions = new String[2];
        int pick = (int)(Math.random() * supply.cards.length);
        conditions[0] = ""+supply.cards[pick];
        pick = (int)(Math.random() * supply.cards.length);
        conditions[1] = ""+supply.cards[pick];

        // Match the two cards to their two expansions.
        String[] exp = new String[]{"", ""};
        try {
            Cursor c = query(new String[]{CardDb._ID, CardDb._EXP},
                             CardDb._ID+"=? OR "+CardDb._ID+"=?", conditions,
                             null);
            int id = c.getColumnIndex(CardDb._ID);
            int exp_id = c.getColumnIndex(CardDb._EXP);
            c.moveToPosition(-1);
            while(c.moveToNext()) {
                if(conditions[0].equals(c.getString(id)))
                    exp[0] = c.getString(exp_id);
                else exp[1] = c.getString(exp_id);
            }
            c.close();
        } catch(Exception ignored){}

        // High cost
        String match = MainActivity.getStaticContext()
                                   .getString(R.string.set_prosperity);
        supply.high_cost = match.equals(exp[0]);

        // Shelters
        match = MainActivity.getStaticContext()
                            .getString(R.string.set_dark_ages);
        supply.shelters = match.equals(exp[1]);
    }

    /** Insert a given supply into the history table */
    private void insertSupply(Supply s) {
        ContentValues values = new ContentValues();
        values.put(DataDb._H_TIME,      s.time);
        values.put(DataDb._H_NAME,      s.name);
        values.put(DataDb._H_CARDS,     Utils.join(",", s.cards));
        values.put(DataDb._H_BANE,      s.bane);
        values.put(DataDb._H_HIGH_COST, s.high_cost);
        values.put(DataDb._H_SHELTERS,  s.shelters);

        try{ MainActivity.getStaticContext()
                         .getContentResolver()
                         .insert(Provider.URI_HIST, values);
        } catch(Exception ignored) {}
    }
}