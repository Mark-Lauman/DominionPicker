package ca.marklauman.dominionpicker;

/** Contains all information about a supply set.
 *  @author Mark Lauman                       */
public class Supply {
	
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
}
