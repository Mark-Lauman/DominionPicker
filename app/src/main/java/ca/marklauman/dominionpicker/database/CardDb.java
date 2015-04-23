package ca.marklauman.dominionpicker.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.Locale;

import ca.marklauman.dominionpicker.R;

/** Describes the card database and provides a link to it for the {@link Provider}.
 *  This database contains only card information, not app data
 *  like history or settings.
 *  @author Mark Lauman */
public class CardDb extends SQLiteOpenHelper {

    /** The name of the card table in this database. */
    private static final String TABLE_NAME = "cards";

    /** Column for the id id number of the card (internal). */
    public static final String _ID = BaseColumns._ID;
    /** Column for the name of a card. */
    public static final String _NAME = "name";
    /** Column for the description of a card. */
    public static final String _DESC = "description";
    /** Column for the cost of a card (in coppers). */
    public static final String _COST = "cost";
    /** Column for the potion cost of a card (# needed to buy). */
    public static final String _POTION = "potion";
    /** Column for the categories of the card (action, reaction, etc) */
    public static final String _CATEGORY = "category";
    /** Column for the expansion set the card belongs to */
    public static final String _EXP = "expansion";
    /** Column for +1 buys this card provides */
    public static final String _BUY = "buy";
    /** Column for +1 actions this card provides. */
    public static final String _ACTION = "act";
    /** Column for +1 cards this card provides. */
    public static final String _DRAW = "draw";
    /** Column for +1 buying power this card provides. */
    public static final String _GOLD = "gold";
    /** Column for the value of this card in victory points. */
    public static final String _VICTORY = "victory";
    /** Column indicating if this card is a curser */
    public static final String _CURSER = "curser";

    /** Array of all column names.
     *  Useful for converting rows to arrays with a known order. */
    private static final String[] COLS =
            {_ID, _NAME, _DESC, _COST, _POTION, _CATEGORY, _EXP,
             _BUY, _ACTION, _DRAW, _GOLD, _VICTORY, _CURSER};
    
    /** The _ID value of the Black Market card. */
    public static final long ID_BLACK_MARKET = 1L;
    /** The ID value of the Young Witch card */
    public static final long ID_YOUNG_WITCH = 161L;

    /** The context for this database connection */
    private final Context context;
    /** The language this connection is in */
    public final String language;


    public CardDb(Context c) {
        super(c, "cards-" + Locale.getDefault().getLanguage() + ".db",
              null, c.getResources().getInteger(R.integer.db_version));
        context = c;
        language = Locale.getDefault().getLanguage();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the table
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + _NAME + " TEXT, "
                + _DESC + " TEXT, "
                + _COST + " TEXT, "
                + _POTION + " INTEGER, "
                + _CATEGORY + " TEXT, "
                + _EXP + " TEXT, "
                + _BUY + " TEXT, "
                + _ACTION + " TEXT, "
                + _DRAW + " TEXT, "
                + _GOLD + " TEXT, "
                + _VICTORY + " TEXT, "
                + _CURSER + " TEXT);");
        // and populate it with cards
        Resources r = context.getResources();
        addCards(db, r.getStringArray(R.array.cards_promo));
        addCards(db, r.getStringArray(R.array.cards_base));
        addCards(db, r.getStringArray(R.array.cards_alchemy));
        addCards(db, r.getStringArray(R.array.cards_intrigue));
        addCards(db, r.getStringArray(R.array.cards_prosperity));
        addCards(db, r.getStringArray(R.array.cards_seaside));
        addCards(db, r.getStringArray(R.array.cards_dark_ages));
        addCards(db, r.getStringArray(R.array.cards_cornucopia));
        addCards(db, r.getStringArray(R.array.cards_guilds));
        addCards(db, r.getStringArray(R.array.cards_hinterlands));
        addCards(db, r.getString(R.string.card_prince));
        addCards(db, r.getStringArray(R.array.cards_adventures));
    }

    /** Add all the cards from a parsed resource file to the database */
    private static void addCards(SQLiteDatabase db, String... cards) {
        db.beginTransaction();
        for (String card : cards) {
            String[] in_data = card.split(";");
            ContentValues values = new ContentValues();
            for (int i = 0; i < in_data.length; i++) {
                values.put(COLS[i + 1], in_data[i]);
            }
            db.insertWithOnConflict(TABLE_NAME, null, values,
                    SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    /** Perform an sql query on the cards table of this database */
    public Cursor query(String[] projection,
                        String selection, String[] selectionArgs,
                        String sortOrder) {
        if(sortOrder == null) sortOrder = _EXP + ", " + _NAME;
        return getReadableDatabase()
                   .query(TABLE_NAME, projection,
                          selection, selectionArgs,
                          null, null, sortOrder);
    }

}
