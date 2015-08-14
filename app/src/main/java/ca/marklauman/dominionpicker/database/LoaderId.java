package ca.marklauman.dominionpicker.database;

/** The unique identifiers for each content loader in the app.
 *  @author Mark Lauman */
public abstract class LoaderId {
    /** The picker's card loader. */
    public static final int PICKER = 1;
    /** The supply fragment's history loader. */
    public static final int SUPPLY_HIST = 2;
    /** The supply fragment's card loader. */
    public static final int SUPPLY_CARDS = 3;
    /** The market's shuffle loader. */
    public static final int MARKET_SHUFFLE = 4;
    /** The market's display loader. */
    public static final int MARKET_SHOW = 5;
    /** The favorites loader. */
    public static final int FAVORITES = 6;
    /** The history loader. */
    public static final int HISTORY = 7;
}