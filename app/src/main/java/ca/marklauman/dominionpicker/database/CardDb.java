package ca.marklauman.dominionpicker.database;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import ca.marklauman.dominionpicker.R;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/** Describes the card database and provides a link to it for the {@link Provider}.
 *  This database contains only card information, not history.
 *  @author Mark Lauman */
public class CardDb extends SQLiteAssetHelper {
    /** The file that the database is stored in. */
    static final String FILE_NAME = "card.db";

    /** Table containing all card data */
    static final String TABLE_DATA = "cardData";
    /** Table containing all card translations */
    static final String TABLE_TRANS = "cardTrans";
    /** View that spans all card tables. */
    static final String VIEW_ALL = "cardAll";

    /** Each card has a unique id, visible across all tables */
    public static final String _ID = BaseColumns._ID;

    /** Column for expansion name. From the cardTrans table. */
    public static final String _SET_NAME = "set_name";
    /** Column for the name of a card.  From the cardTrans table. */
    public static final String _NAME = "name";
    /** Column for the type of the card (as printed).
     *  From the cardTrans table. */
    public static final String _TYPE = "type";
    /** Column for requirements of a card. From the cardTrans table. */
    public static final String _REQ = "requires";
    /** Column for the description of a card. From the cardTrans table. */
    public static final String _DESC = "description";
    /** Column for the language code of the card. From the cardTans table. */
    public static final String _LANG = "language";

    /** Column for the expansion id of the card.
     *  From the cardData table. */
    public static final String _SET_ID = "set_id";
    /** Column for the cost of a card (in coins).
     *  From the cardData table. */
    public static final String _COST = "cost";
    /** Column for the potion cost of a card (# needed to buy).
     *  From the cardData table. */
    public static final String _POT = "potion";
    /** Column for +X buys this card provides. From the cardData table. */
    public static final String _BUY = "plusBuy";
    /** Column for +X actions this card provides. From the cardData table. */
    public static final String _ACT = "plusAction";
    /** Column for +X cards this card provides. From the cardData table. */
    public static final String _CARD = "plusCard";
    /** Column for +X coins this card provides. From the cardData table. */
    public static final String _COIN = "plusCoin";
    /** Column for the +X victory points this card provides.
     *  From the cardData table. */
    public static final String _VICTORY = "plusVictory";

    /** Column representing if this is an action card.
     *  From the cardData table. */
    public static final String _TYPE_ACT = "typeAction";
    /** Column representing if this is a reaction card.
     *  From the cardData table. */
    public static final String _TYPE_REACT = "typeReaction";
    /** Column representing if this is a treasure card.
     *  From the cardData table. */
    public static final String _TYPE_TREAS = "typeTreasure";
    /** Column representing if this is an attack card.
     *  From the cardData table. */
    public static final String _TYPE_ATK = "typeAttack";
    /** Column representing if this is a victory card.
     *  From the cardData table. */
    public static final String _TYPE_VICTORY = "typeVictory";
    /** Column representing if this is a duration card.
     *  From the cardData table. */
    public static final String _TYPE_DUR = "typeDuration";
    /** Column representing if this is a looter card.
     *  From the cardData table. */
    public static final String _TYPE_LOOT = "typeLooter";
    /** Column representing if this is a knight card.
     *  From the cardData table. */
    public static final String _TYPE_KNIGHT = "typeKnight";
    /** Column representing if this is a reserve card.
     *  From the cardData table. */
    public static final String _TYPE_RESERVE = "typeReserve";
    /** Column representing if this is a traveller card.
     *  From the cardData table. */
    public static final String _TYPE_TRAVEL = "typeTraveller";
    /** Column representing if this is an event card.
     *  From the cardData table. */
    public static final String _TYPE_EVENT = "typeEvent";

    /** Column indicating if this card is a curser meta type.
     *  From the cardData table. */
    public static final String _META_CURSER = "metaCurser";
    
    /** The _ID value of the Black Market card. */
    public static final long ID_BLACK_MARKET = 1L;
    /** The ID value of the Young Witch card */
    public static final long ID_YOUNG_WITCH = 161L;
    /** The _SET value of the prosperity set */
    public static final int SET_PROSPERITY = 11;
    /** The _SET value of the dark ages set */
    public static final int SET_DARK_AGES = 4;

    public CardDb(Context c) {
        super(c, FILE_NAME, null, c.getResources().getInteger(R.integer.db_version));
    }

    /** Perform an sql query on this database */
    Cursor query(String table, String[] projection,
                 String selection, String[] selectionArgs,
                 String sortOrder) {
        return getReadableDatabase()
                   .query(table, projection,
                          selection, selectionArgs,
                          null, null, sortOrder);
    }
}