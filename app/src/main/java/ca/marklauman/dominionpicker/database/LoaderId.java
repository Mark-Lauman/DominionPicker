package ca.marklauman.dominionpicker.database;

/** The unique identifiers for each content loader in the app.
 *  @author Mark Lauman */
public abstract class LoaderId {
    /** The Card picker's list. */
    public static final int PICKER = 1;
    /** The supply fragment's supply loader. */
    public static final int SUPPLY_S = 2;
    /** The supply fragment's card list. */
    public static final int SUPPLY_C = 3;
    /** ID used for the market display loader */
    public static final int MARKET_DISP = 4;
    /** ID used for the favorites loader */
    public static final int FAVORITES = 5;
    /** ID used for the history loader */
    public static final int HISTORY = 6;
    /** ID used for the market shuffle loader */
    public static final int MARKET_SHUFFLE = 7;
}
