package ca.marklauman.dominionpicker.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.provider.BaseColumns;

import java.util.Locale;

import ca.marklauman.dominionpicker.R;

/** Describes the card database and provides a link to it for the {@link Provider}.
 *  This database contains only card information, not app data
 *  like history or settings.
 *  @author Mark Lauman */
public class CardDb extends SQLiteOpenHelper {
    /** The file that the database is stored in. */
    private static final String FILE_NAME = "cards.db";

    /** Table containing all card data */
    private static final String TABLE_DATA = "cardData";
    /** Table containing all card translations */
    private static final String TABLE_TRANS = "cardTrans";

    /** List of all tables accessed by the content provider */
    private static final String[] TABLES = {TABLE_DATA,
                                            TABLE_TRANS,
                                            TABLE_DATA+" NATURAL JOIN "+TABLE_TRANS};
    /** ID for the data table for the content provider */
    public static final int TABLE_ID_DATA = 0;
    /** ID for the trans table for the content provider */
    public static final int TABLE_ID_TRANS = 1;
    /** ID for the all table for the content provider */
    public static final int TABLE_ID_ALL = 2;

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
    /** Column for the value of this card in victory points.
     *  From the cardData table. */
    public static final String _VICTORY = "victory";

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

    /** The context for this database connection */
    private final Context context;

    /** The columns in the cardTrans table. */
    private static final String[] COL_TRANS = {_ID, _SET_NAME, _NAME, _TYPE,  _REQ, _DESC};
    /** The columns in the cardData table. */
    private static final String[] COL_DATA = {_ID, _SET_ID, _COST, _POT,
            _BUY, _ACT, _CARD, _COIN, _VICTORY,
            _TYPE_ACT, _TYPE_REACT, _TYPE_TREAS, _TYPE_ATK, _TYPE_VICTORY,
            _TYPE_DUR, _TYPE_LOOT, _TYPE_KNIGHT, _TYPE_RESERVE,
            _TYPE_TRAVEL, _TYPE_EVENT, _META_CURSER};


    public CardDb(Context c) {
        super(c, FILE_NAME, null,
              c.getResources().getInteger(R.integer.db_version));
        context = c;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the cardData table
        db.execSQL("CREATE TABLE " + TABLE_DATA + " ("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + _SET_ID + " INTEGER, "
                + _COST + " TEXT, "
                + _POT + " INTEGER, "
                + _BUY + " TEXT, "
                + _ACT + " TEXT, "
                + _CARD + " TEXT, "
                + _COIN + " TEXT, "
                + _VICTORY + " TEXT, "
                + _TYPE_ACT + " INTEGER, "
                + _TYPE_REACT + " INTEGER, "
                + _TYPE_TREAS + " INTEGER, "
                + _TYPE_ATK + " INTEGER, "
                + _TYPE_VICTORY + " INTEGER, "
                + _TYPE_DUR + " INTEGER, "
                + _TYPE_LOOT + " INTEGER, "
                + _TYPE_KNIGHT + " INTEGER, "
                + _TYPE_RESERVE + " INTEGER, "
                + _TYPE_TRAVEL + " INTEGER, "
                + _TYPE_EVENT + " INTEGER, "
                + _META_CURSER + " INTEGER);");
        // Populate the cardData table
        Resources r = context.getResources();
        db.beginTransaction();
        addData(db, r.getStringArray(R.array.cardData_promo));
        addData(db, r.getStringArray(R.array.cardData_base));
        addData(db, r.getStringArray(R.array.cardData_alchemy));
        addData(db, r.getStringArray(R.array.cardData_intrigue));
        addData(db, r.getStringArray(R.array.cardData_prosperity));
        addData(db, r.getStringArray(R.array.cardData_seaside));
        addData(db, r.getStringArray(R.array.cardData_dark_ages));
        addData(db, r.getStringArray(R.array.cardData_cornucopia));
        addData(db, r.getStringArray(R.array.cardData_guilds));
        addData(db, r.getStringArray(R.array.cardData_hinterlands));
        addData(db, r.getStringArray(R.array.cardData_adventures));
        db.setTransactionSuccessful();
        db.endTransaction();


        // Create the cardTrans table
        db.execSQL("CREATE TABLE " + TABLE_TRANS + " ("
                + _ID + " INTEGER, "
                + _SET_NAME + " TEXT, "
                + _NAME + " TEXT, "
                + _TYPE + " TEXT, "
                + _REQ + " TEXT, "
                + _DESC + " TEXT, "
                + _LANG + " TEXT," +
                "PRIMARY KEY ("+_ID+", "+_LANG+"));");
        // Use default locale to populate the trans table
        String lang = r.getConfiguration().locale.getLanguage();
        insertTrans(db, r, lang);
    }

    /** Add an entry to cardData from a csv formatted string (or strings) */
    private static void addData(SQLiteDatabase db, String... cards) {
        for (String card : cards) {
            String[] in_data = card.split(";");
            ContentValues values = new ContentValues();
            for (int i = 0; i < in_data.length; i++) {
                values.put(COL_DATA[i], in_data[i]);
            }
            db.insertWithOnConflict(TABLE_DATA, null, values,
                    SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    /** Insert a translation for a given language into the cardTrans database */
    public static int insertTrans(SQLiteDatabase db, Resources res, String lang) {
        // Quick sanity check - do not insert if the language is present
        Cursor c = db.rawQuery("SELECT "+_ID+" FROM "+TABLES[1]+" WHERE "+_LANG+"=? LIMIT 1",
                               new String[]{lang});
        if(c!=null) {
            int count = c.getCount();
            c.close();
            if(0 < count) return 0;
        }

        // Change the resources over to the other language.
        Configuration conf = res.getConfiguration();
        Locale oldLoc = conf.locale;
        Locale newLoc = new Locale(lang.toLowerCase());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            conf.setLocale(newLoc);
        else conf.locale = newLoc;
        res.updateConfiguration(conf, null);

        // Insert the other language
        db.beginTransaction();
        addTransEntry(db, lang, res.getStringArray(R.array.cards_promo));
        addTransEntry(db, lang, res.getStringArray(R.array.cards_base));
        addTransEntry(db, lang, res.getStringArray(R.array.cards_alchemy));
        addTransEntry(db, lang, res.getStringArray(R.array.cards_intrigue));
        addTransEntry(db, lang, res.getStringArray(R.array.cards_prosperity));
        addTransEntry(db, lang, res.getStringArray(R.array.cards_seaside));
        addTransEntry(db, lang, res.getStringArray(R.array.cards_dark_ages));
        addTransEntry(db, lang, res.getStringArray(R.array.cards_cornucopia));
        addTransEntry(db, lang, res.getStringArray(R.array.cards_guilds));
        addTransEntry(db, lang, res.getStringArray(R.array.cards_hinterlands));
        addTransEntry(db, lang, res.getStringArray(R.array.cards_adventures));
        db.setTransactionSuccessful();
        db.endTransaction();

        // Restore the first language
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            conf.setLocale(oldLoc);
        else conf.locale = oldLoc;
        res.updateConfiguration(conf, null);
        return 256;
    }

    /** Add an entry to cardTrans from a csv formatted string (or strings) */
    private static void addTransEntry(SQLiteDatabase db, String lang, String... cards) {
        db.beginTransaction();
        for (String card : cards) {
            String[] in_data = card.split(";");
            ContentValues values = new ContentValues();
            for (int i = 0; i < in_data.length; i++) {
                values.put(COL_TRANS[i], in_data[i]);
            }
            values.put(_LANG, lang);
            db.insertWithOnConflict(TABLE_TRANS, null, values,
                    SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }




    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANS);
        onCreate(db);
    }


    /** Perform an sql query on this database */
    public Cursor query(int table_id, String[] projection,
                        String selection, String[] selectionArgs,
                        String sortOrder) {
        return getReadableDatabase()
                   .query(TABLES[table_id], projection,
                          selection, selectionArgs,
                          null, null, sortOrder);
    }
}