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
    /** ID used for the market loader */
    public static final int MARKET = 4;
    /** ID used for the history loader */
    public static final int HISTORY = 5;
}
