package ca.marklauman.dominionpicker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Calendar;

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
    /** Extra containing card shortfall in the event of
     *  {@link #RES_MORE} or {@link #RES_MORE_K}.
     *  String formatted as "X/Y" cards.  */
    public static final String MSG_SHORT = "shortfall";
    /** The extra containing the supply id. Only available on {@link #RES_OK}. */
    public static final String MSG_SUPPLY_ID ="supply";

    /** Shuffle succeeded. Supply available in {@link #MSG_SUPPLY_ID} */
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
        if(event < 0) maxEvent = 0;
        else maxEvent = event;
    }

    @Override
    protected Void doInBackground(Long... cardIds) {
        // The result message we will send on finish
        Intent res = new Intent(MSG_INTENT);

        // If our supply consists of 0 cards, we can end early
        if(minKingdom < 1) {
            Supply s = new Supply();
            s.cards = new long[0];
            s.time = Calendar.getInstance().getTimeInMillis();
            insertSupply(s);
            res.putExtra(MSG_RES, RES_OK);
            res.putExtra(MSG_SUPPLY_ID, s);
            return sendMsg(res);
        }

        // If we don't have enough cards we can end early
        if(cardIds.length < minKingdom) {
            res.putExtra(MSG_RES, RES_MORE);
            int size = cardIds.length;
            res.putExtra(MSG_SHORT, size +"/"+ minKingdom);
            return sendMsg(res);
        }

        // Prepare the select statement
        String sel="";
        String[] selArgs = new String[cardIds.length];
        for(int i=0; i<selArgs.length; i++) {
            sel += ",?";
            selArgs[i] = ""+cardIds[i];
        }
        sel = CardDb._TYPE_RUINS+"=0 AND "+CardDb._ID+" IN ("+sel.substring(1)+")";

        // Variables that must live through the try statement
        Supply supply = new Supply();
        boolean done = false;
        Cursor c = null;
        ArrayList<Long> kingdom = new ArrayList<>(minKingdom);
        ArrayList<Long> events = new ArrayList<>(maxEvent);

        // The rest of this process can throw errors. So it must be tried.
        try {
            // Load the available cards
            c = shuffleCards(new String[]{CardDb._ID, CardDb._TYPE_EVENT, CardDb._SET_ID},
                    sel, selArgs);
            int _id = c.getColumnIndex(CardDb._ID);
            int _event = c.getColumnIndex(CardDb._TYPE_EVENT);
            int _set = c.getColumnIndex(CardDb._SET_ID);

            // Variables used in the main shuffle loop
            // The id of the loaded card
            long id;
            // True if that card is a kingdom card
            boolean isKingdomCard;
            // True if the young witch is in this supply
            boolean youngWitch = false;
            // True if we need a bane card right now
            boolean needBane = false;
            // random cards chosen for the high cost & shelters variables
            int costCard = (int)(Math.random() * minKingdom)+1;
            int shelterCard = (int)(Math.random() * minKingdom)+1;

            // Main Shuffle loop
            c.moveToPosition(-1);
            while(!done && c.moveToNext()) {
                // Stop on cancel
                if (isCancelled()) {
                    res.putExtra(MSG_RES, RES_CANCEL);
                    return sendMsg(res);
                }
                // Load the card
                id = c.getLong(_id);
                isKingdomCard = (0 == c.getInt(_event));
                if(isKingdomCard) {
                    kingdom.add(id);
                    // Determine if high cost game
                    if(kingdom.size() == costCard)
                        supply.high_cost = (c.getInt(_set) == CardDb.SET_PROSPERITY);
                    // Determine if shelters game
                    if(kingdom.size() == shelterCard)
                        supply.shelters = (c.getInt(_set) == CardDb.SET_DARK_AGES);
                    // If we need a bane, this is our bane
                    if(needBane) {
                        supply.bane = id;
                        needBane = false;
                    }
                    // If this is the young witch, we need a bane card.
                    if(id == CardDb.ID_YOUNG_WITCH) {
                        needBane = true;
                        youngWitch = true;
                        minKingdom++;
                    }
                // If this is an event, add it if it is needed.
                } else if(events.size() < maxEvent) events.add(id);
                // We are done if we don't need a bane and have our required kingdom cards.
                done = !needBane && (minKingdom <= kingdom.size());
            }

            // The shuffle is complete - but did it complete successfully?
            // If we aren't done, we ran out of cards.
            if(!done) {
                // Couldn't find a valid bane card before the end.
                if(needBane || (youngWitch && kingdom.size() == minKingdom-1)) {
                    res.putExtra(MSG_RES, RES_NO_YW);
                    return sendMsg(res);
                // We just don't have enough kingdom cards
                } else {
                    res.putExtra(MSG_RES, RES_MORE_K);
                    res.putExtra(MSG_SHORT, kingdom.size() +"/"+ minKingdom);
                    return sendMsg(res);
                }
            }

        } catch (Exception ignored){
        } finally {
            try{ if (c != null) c.close();
            } catch(Exception ignored){}
        }

        // If the shuffle loop failed, it was due to a resource being removed.
        // Which means the main activity doesn't exist anymore.
        // We can consider ourselves cancelled.
        if(!done) {
            res.putExtra(MSG_RES, RES_CANCEL);
            return sendMsg(res);
        }

        // The shuffle was a success. Gather all the cards.
        long[] cards = new long[kingdom.size() + events.size()];
        for(int i=0; i<kingdom.size(); i++)
            cards[i] = kingdom.get(i);
        for(int i=0; i<events.size(); i++)
            cards[i+kingdom.size()] = events.get(i);
        supply.cards = cards;

        // Insert the supply into the history table
        supply.time = Calendar.getInstance().getTimeInMillis();
        insertSupply(supply);

        // Finish and return supply
        res.putExtra(MSG_RES, RES_OK);
        res.putExtra(MSG_SUPPLY_ID, supply.time);
        return sendMsg(res);
    }


    /** Broadcast a given message back to the activity */
    @SuppressWarnings("SameReturnValue")
    private Void sendMsg(Intent msg) {
        try {
            LocalBroadcastManager.getInstance(App.staticContext)
                                 .sendBroadcast(msg);
        } catch(Exception ignored) {}
        return null;
    }


    /** Get the selected cards from the cardData table in random order.
     *  @return The result, or null if the query failed. */
    private static Cursor shuffleCards(String[] projection,
                                       String selection, String[] selectionArgs) {
        // I retain nothing because I don't know if the contentResolver changes.
        return App.staticContext
                  .getContentResolver()
                  .query(Provider.URI_CARD_DATA, projection,
                         selection, selectionArgs, "random()");
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

        try{ App.staticContext
                .getContentResolver()
                .insert(Provider.URI_HIST, values);
        } catch(Exception ignored) {}
    }
}