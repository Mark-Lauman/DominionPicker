package ca.marklauman.dominionpicker;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Calendar;

import ca.marklauman.dominionpicker.database.DataDb;
import ca.marklauman.dominionpicker.database.Provider;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.settings.Prefs;
import ca.marklauman.tools.Utils;

/** This task is used to shuffle new supplies.
 *  It reads the current setting configuration when called, and attempts to create a supply
 *  with the available cards.
 *  The result of the shuffle is communicated to the main activity with broadcast intents.
 *  @author Mark Lauman */
class SupplyShuffler extends AsyncTask<Void, Void, Void> {

    /** When the shuffler is done, an intent of this type broadcasts
     *  the results back to the activity.                         */
    public static final String MSG_INTENT = "ca.marklauman.dominionpicker.shuffler";
    /** The extra in the result intent containing the result id.
     *  Will be a constant defined by this class starting with "RES_" */
    public static final String MSG_RES = "result";
    /** Extra containing card shortfall in the event of {@link #RES_MORE}.
     *  String formatted as "X/Y" cards.  */
    public static final String MSG_SHORT = "shortfall";
    /** The extra containing the supply id. Only available on {@link #RES_OK}. */
    public static final String MSG_SUPPLY_ID ="supply";

    /** Shuffle succeeded. Supply available in {@link #MSG_SUPPLY_ID} */
    public static final int RES_OK = 0;
    /** Shuffle failed. No young witch targets. */
    public static final int RES_NO_YW = 1;
    /** Shuffle failed. Insufficient kingdom cards.
     *  Shortfall in {@link #MSG_SHORT}. */
    public static final int RES_MORE = 2;
    /** Shuffle cancelled by outside source. */
    @SuppressWarnings("WeakerAccess")
    public static final int RES_CANCEL = 100;



    @Override
    protected Void doInBackground(Void... ignored) {
        // Create the supply we will populate, and do a check for minKingdoms == 0
        ShuffleSupply supply = new ShuffleSupply();
        if(!supply.needsKingdom())
            return successfulResult(supply);

        // load applicable filters.
        SharedPreferences pref = getPreferences();
        String filt_pre = loadFilter(pref);
        String filt_req = pref.getString(Prefs.REQ_CARDS, "");
        String filt_card = pref.getString(Prefs.FILT_CARD, "");

        // Load the required cards into the supply
        if(0 < filt_req.length())
            loadCards(supply, filt_pre + TableCard._ID+" IN ("+filt_req+")", true);
        if(isCancelled())
            return cancelResult();
        if (!supply.needsKingdom())
            return successfulResult(supply);

        // Filter out both required and excluded cards
        String filt = filt_req+filt_card;
        if(0 < filt_req.length() && 0 < filt_card.length())
            filt = filt_req + "," + filt_card;
        filt = (filt.length() == 0) ? "TRUE"
                                    : TableCard._ID+" NOT IN ("+filt+")";

        // Shuffle the remaining cards into the supply
        if(filt.length() != 0)
            loadCards(supply, filt_pre + filt, false);
        if(isCancelled())
            return cancelResult();
        if (!supply.needsKingdom())
            return successfulResult(supply);

        // Shuffle has failed.
        Intent msg = new Intent(MSG_INTENT);
        int shortfall = supply.getShortfall();
        // Shuffle failed because there were no bane cards for the young witch
        if(supply.waitingForBane() && shortfall == 1) {
            msg.putExtra(MSG_RES, RES_NO_YW);
            return sendMsg(msg);
        } else {
            msg.putExtra(MSG_RES, RES_MORE);
            msg.putExtra(MSG_SHORT, supply.minKingdom-shortfall+"/"+supply.minKingdom);
            return sendMsg(msg);
        }
    }


    /** Simple shorthand to get the preferences. */
    private static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(Prefs.getStaticContext());
    }


    /** Returns additional filters that should be used before _id IN (....) */
    private String loadFilter(SharedPreferences pref) {
        // Filter out sets
        String filt_set = pref.getString(Prefs.FILT_SET, "");
        String filter = (filt_set.length()==0) ? TableCard._SET_ID+"=NULL"
                                               : TableCard._SET_ID+" IN ("+filt_set+")";

        // Filter out curses
        if(!pref.getBoolean(Prefs.FILT_CURSE, true)) {
            if(filter.length() != 0) filter += " AND ";
            filter += TableCard._META_CURSER+"=0";
        }

        return (filter.length() == 0) ? filter : filter+" AND ";
    }


    /** Load all cards matching the filter and add them to the supply.
     *  @param s The supply object that you want to add to.
     *  @param filter The filter for the cards you wish to add.
     *  @param cardsRequired True if all matching cards must be in the supply.
     *  If this is false, cards will be added to the supply until it has enough kingdom cards. */
    private void loadCards(ShuffleSupply s, String filter, boolean cardsRequired) {
        // Query the cards in the database
        Cursor c = Prefs.getStaticContext()
                        .getContentResolver()
                        .query(Provider.URI_CARD_DATA,
                                new String[]{TableCard._ID, TableCard._TYPE_EVENT,
                                        TableCard._SET_ID, TableCard._COST},
                                filter, null, "random()");
        if(c == null) return;

        try {
            int _id = c.getColumnIndex(TableCard._ID);
            int _event = c.getColumnIndex(TableCard._TYPE_EVENT);
            int _cost = c.getColumnIndex(TableCard._COST);
            int _set_id = c.getColumnIndex(TableCard._SET_ID);

            c.moveToPosition(-1);
            while(c.moveToNext() && (cardsRequired || s.needsKingdom())) {
                if(isCancelled())
                    return;

                // We handle events and kingdom cards differently (events first)
                long id = c.getLong(_id);
                if(c.getInt(_event) != 0)
                    s.addEvent(id, cardsRequired);
                else s.addKingdom(id, c.getString(_cost), c.getInt(_set_id), cardsRequired);
            }
        } finally {
            c.close();
        }
    }


    /** Broadcast a given message back to the activity */
    @SuppressWarnings("SameReturnValue")
    private Void sendMsg(Intent msg) {
        try {
            LocalBroadcastManager.getInstance(Prefs.getStaticContext())
                                 .sendBroadcast(msg);
        } catch(Exception ignored) {}
        return null;
    }


    /** The shuffle was cancelled prematurely. */
    private Void cancelResult() {
        Intent cancel = new Intent(MSG_INTENT);
        cancel.putExtra(MSG_RES, RES_CANCEL);
        return sendMsg(cancel);
    }


    /** Generating the supply was successful.
     *  Write the result into the history database and tell the app its id number */
    private Void successfulResult(ShuffleSupply supply) {
        // Insert the new supply
        long time = Calendar.getInstance().getTimeInMillis();
        ContentValues values = new ContentValues();
        values.putNull(DataDb._H_NAME);
        values.put(DataDb._H_TIME,      time);
        values.put(DataDb._H_CARDS,     Utils.join(",", supply.getCards()));
        values.put(DataDb._H_BANE,      supply.getBane());
        values.put(DataDb._H_HIGH_COST, supply.high_cost);
        values.put(DataDb._H_SHELTERS,  supply.shelters);
        Prefs.getStaticContext()
             .getContentResolver()
             .insert(Provider.URI_HIST, values);

        // let the listeners know the result
        Intent msg = new Intent(MSG_INTENT);
        msg.putExtra(MSG_RES, RES_OK);
        msg.putExtra(MSG_SUPPLY_ID, time);
        return sendMsg(msg);
    }


    /** Represents a supply in the process of being shuffled. */
    private class ShuffleSupply {
        /** Possible value of {@link #baneStatus}. There is no young witch in the supply. */
        private static final int BANE_INACTIVE = 0;
        /** Possible value of {@link #baneStatus}.
         *  The young witch was drawn, but we haven't seen a bane yet */
        private static final int BANE_WAITING = 1;
        /** Possible value of {@link #baneStatus}. The bane and the young witch have been set */
        private static final int BANE_ACTIVE = 2;

        /** Minimum amount of kingdom cards needed for this supply to be complete. */
        public int minKingdom;
        /** Maximum amount of event cards allowed. */
        public final int maxEvent;
        /** If this is a high cost game or not. */
        public boolean high_cost = false;
        /** If this game uses shelters or not. */
        public boolean shelters = false;

        /** Position of the kingdom card that determines if this is a high cost game. */
        private final int costCard;
        /** Position of the kingdom card that determines if this game uses shelters. */
        private final int shelterCard;

        /** Kingdom cards in this supply */
        private final ArrayList<Long> kingdom;
        /** Event cards in this supply */
        private final ArrayList<Long> events;
        /** Current status of the bane card */
        private int baneStatus = BANE_INACTIVE;
        /** Id for a possible bane card */
        private long bane = -1L;


        public ShuffleSupply() {
            SharedPreferences prefs = getPreferences();
            minKingdom = prefs.getInt(Prefs.LIMIT_SUPPLY, 10);
            maxEvent = prefs.getInt(Prefs.LIMIT_EVENTS, 2);
            kingdom = new ArrayList<>(minKingdom);
            events = new ArrayList<>(maxEvent);
            costCard = (int)(Math.random() * minKingdom)+1;
            shelterCard = (int)(Math.random() * minKingdom)+1;
        }


        /** Add an event to the supply */
        public void addEvent(long id, boolean required) {
            if(required) events.add(id);
            else if(events.size() < maxEvent)
                events.add(id);
        }


        /** Check if this shuffler needs a kingdom card */
        public boolean needsKingdom() {
            return kingdom.size() < minKingdom;
        }


        /** Add a kingdom card to the supply */
        public void addKingdom(long id, String cost, int set_id, boolean required) {
            if(!required && minKingdom <= kingdom.size())
                return;

            // Special handling for the young witch
            if(id == TableCard.ID_YOUNG_WITCH) {
                if(bane == -1L) {
                    // Do not add the young witch, wait for a bane card first
                    baneStatus = BANE_WAITING;
                    return;
                } else {
                    // Add the young witch, we have a bane card
                    baneStatus = BANE_ACTIVE;
                    minKingdom++;
                }
            }

            // Special handling for the young witch's bane
            else if("2".equals(cost) || "3".equals(cost)) {
                bane = id;
                if(baneStatus == BANE_WAITING)
                    addKingdom(TableCard.ID_YOUNG_WITCH, "4", 3, true);
            }

            kingdom.add(id);

            // determine if this is a high cost/shelters game
            if(kingdom.size() == costCard)
                high_cost = set_id == TableCard.SET_PROSPERITY;
            if(kingdom.size() == shelterCard)
                shelters = set_id == TableCard.SET_DARK_AGES;
        }


        /** Get all cards in this supply */
        public long[] getCards() {
            long[] res = new long[kingdom.size() + events.size()];
            int i = 0;
            for(Long card : kingdom) {
                res[i] = card;
                i++;
            }
            for(Long card : events) {
                res[i] = card;
                i++;
            }
            return res;
        }

        /** Get the bane card of this supply */
        public long getBane() {
            if(baneStatus == BANE_ACTIVE) return bane;
            else return -1L;
        }


        /** Get how many more kingdom cards we need */
        public int getShortfall() {
            return minKingdom - kingdom.size();
        }


        /** Check if the supply is waiting for a valid bane card. */
        public boolean waitingForBane() {
            return baneStatus == BANE_WAITING;
        }
    }
}