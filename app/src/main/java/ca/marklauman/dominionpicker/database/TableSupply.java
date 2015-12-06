package ca.marklauman.dominionpicker.database;

import android.provider.BaseColumns;

/** Describes the supply tables that are accessible through the ContentProvider.
 *  @author Mark Lauman */
public abstract class TableSupply {
    /** The view containing all supply info */
    static final String VIEW = "supply";

    /** Unique id for the supply. */
    public static final String _ID = BaseColumns._ID;
    /** The name of the supply. */
    public static final String _NAME = "name";
    /** The set that this supply came from. */
    public static final String _SET_ID = TableCard._SET_ID;
    /** The name of the set this supply comes from. */
    public static final String _SET_NAME = TableCard._SET_NAME;
    /** Cards in this supply. */
    public static final String _CARDS = "cards";
    /** Column storing if this shuffle is a high cost game.
     *  (with colonies and shelters)  */
    public static final String _HIGH_COST = "high_cost";
    /** Column storing if this shuffle uses shelters.<br/>
     *  History Table, Java String */
    public static final String _SHELTERS = "shelters";
    /** Column storing the id of the bane card.
     *  The card is set to -1 if there is no bane card. */
    public static final String _BANE = "bane";
}