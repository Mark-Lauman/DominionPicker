package ca.marklauman.dominionpicker.database;

import android.provider.BaseColumns;

/** Describes the card tables that are accessible through the ContentProvider.
 *  @author Mark Lauman */
public abstract class TableCard {
    /** The _ID value of the Black Market card. */
    public static final long ID_BLACK_MARKET = 1L;
    /** The _ID value of the Young Witch card */
    public static final long ID_YOUNG_WITCH = 161L;
    /** The _SET_ID value of the prosperity set */
    public static final int SET_PROSPERITY = 11;
    /** The _SET_ID value of the dark ages set */
    public static final int SET_DARK_AGES = 4;

    /** Table containing all card data */
    static final String TABLE_DATA = "cardData";
    /** Table containing all card translations */
    static final String TABLE_SET = "cardSet";
    /** View that spans all 3 card tables. */
    static final String VIEW_ALL = "cardAll";

    /** Unique identifier for an expansion. From the cardSet and cardData tables. */
    public static final String _SET_ID = "set_id";
    /** Column for expansion name. From the cardSet table. */
    public static final String _SET_NAME = "set_name";
    /** Column for release date. From the cardSet table */
    public static final String _RELEASE = "release_date";
    /** Column for if this set is a promo card set or not. From the cardSet table. */
    public static final String _PROMO = "promotional";


    /** Unique card identifier. From the cardTrans and cardData tables. */
    public static final String _ID = BaseColumns._ID;
    /** Column for the name of a card.  From the cardTrans table. */
    public static final String _NAME = "name";
    /** Column for the type of the card (as printed). From the cardTrans table. */
    public static final String _TYPE = "type";
    /** Column for requirements of a card. From the cardTrans table. */
    public static final String _REQ = "requires";
    /** Column for the text of a card. From the cardTrans table. */
    public static final String _TEXT = "text";
    /** Column for the rules attached to a card. From the cardTrans table. */
    public static final String _RULES = "rules";
    /** Column for the language code of the card. From the cardTans and cardSet tables. */
    public static final String _LANG = "language";

    /** Column for the cost of a card (in coins). From the cardData table. */
    public static final String _COST = "cost";
    /** Integer value of the card's cost. Removes any additional characters. */
    public static final String _COST_VAL = "costVal";
    /** Column for the potion cost of a card (# needed to buy). From the cardData table. */
    public static final String _POT = "potion";
    /** Column for +X buys this card provides. From the cardData table. */
    public static final String _BUY = "plusBuy";
    /** Column for +X actions this card provides. From the cardData table. */
    public static final String _ACT = "plusAction";
    /** Column for +X cards this card provides. From the cardData table. */
    public static final String _CARD = "plusCard";
    /** Column for +X coins this card provides. From the cardData table. */
    public static final String _COIN = "plusCoin";
    /** Column for the +X victory points this card provides. From the cardData table. */
    public static final String _VICTORY = "plusVictory";

    /** Column representing if this is an action card. From the cardData table. */
    public static final String _TYPE_ACT = "typeAction";
    /** Column representing if this is a reaction card. From the cardData table. */
    public static final String _TYPE_REACT = "typeReaction";
    /** Column representing if this is a curse card. From the cardData table. */
    public static final String _TYPE_CURSE = "typeCurse";
    /** Column representing if this is a treasure card. From the cardData table. */
    public static final String _TYPE_TREAS = "typeTreasure";
    /** Column representing if this is an attack card. From the cardData table. */
    public static final String _TYPE_ATK = "typeAttack";
    /** Column representing if this is a victory card. From the cardData table. */
    public static final String _TYPE_VICTORY = "typeVictory";
    /** Column representing if this is a duration card. From the cardData table. */
    public static final String _TYPE_DUR = "typeDuration";
    /** Column representing if this is a looter card. From the cardData table. */
    public static final String _TYPE_LOOT = "typeLooter";
    /** Column representing if this is a knight card. From the cardData table. */
    public static final String _TYPE_KNIGHT = "typeKnight";
    /** Column representing if this is a reserve card. From the cardData table. */
    public static final String _TYPE_RESERVE = "typeReserve";
    /** Column representing if this is a traveller card. From the cardData table. */
    public static final String _TYPE_TRAVEL = "typeTraveller";
    /** Column representing if this is an event card. From the cardData table. */
    public static final String _TYPE_EVENT = "typeEvent";

    /** Column indicating if this card is a curser meta type. From the cardData table. */
    public static final String _META_CURSER = "metaCurser";

    /** Routine used to convert number-like values (like cost) into a numeric quantity. */
    public static int parseVal(String value) {
        // Just try parsing the value
        try{ return Integer.parseInt(value); }
        catch(NumberFormatException ignored) {}

        // Parse everything but the last character
        try { return Integer.parseInt(value.substring(0, value.length() - 1)); }
        catch (NumberFormatException ignored) {}

        // Parse everything but the first character
        try{ return Integer.parseInt(value.substring(1)); }
        catch (NumberFormatException ignored){}

        // Return a value of 0 for unknown
        return 0;
    }
}