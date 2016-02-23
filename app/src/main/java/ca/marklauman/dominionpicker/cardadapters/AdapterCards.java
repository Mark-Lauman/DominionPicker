package ca.marklauman.dominionpicker.cardadapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

import ca.marklauman.dominionpicker.cardadapters.imagefactories.CardColorFactory;
import ca.marklauman.dominionpicker.cardadapters.imagefactories.CoinFactory;
import ca.marklauman.dominionpicker.ActivityCardInfo;
import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.tools.Utils;

/** Basic adapter used to display cards from {@link TableCard}.
 *  @author Mark Lauman */
public class AdapterCards extends SimpleCursorAdapter
                          implements ViewBinder {

    /** The columns used by this adapter.
     *  Any other columns provided will be ignored. */
    public static final String[] COLS_USED
            = {TableCard._ID, TableCard._NAME, TableCard._SET_NAME, TableCard._TYPE,
               TableCard._SET_ID, TableCard._COST, TableCard._POT, TableCard._REQ, TableCard._LANG,
               TableCard._TYPE_ACT, TableCard._TYPE_TREAS, TableCard._TYPE_VICTORY, // colorFactory
               TableCard._TYPE_DUR, TableCard._TYPE_REACT, TableCard._TYPE_RESERVE, // required
               TableCard._TYPE_CURSE, TableCard._TYPE_EVENT, TableCard._TYPE_ATK};  // rows

    /** Context object used to construct this adapter */
    private final Context mContext;
    /** Maps expansion names to expansion icons */
    private final Drawable[] exp_icons;
    /** Icon used for a non-existent expansion */
    private final Drawable exp_none;
    /** Maps card language to the correct coin description */
    private final HashMap<String, Integer> coinDesc;
    /** Constructs drawables reflecting the card's background color */
    private final CardColorFactory colorFactory;
    /** Constructs drawables for coins */
    private final CoinFactory coinFactory;
    /** Format string for the card icon's description */
    private final String imgDesc;
    /** Size of the card icon */
    private final int imgSize;

    /** Column index of the card's name */
    private int _name;
    /** Column index of the card's language */
    private int _lang;
    /** Column index of the card's set name */
    private int _set_name;


    /** Internal listener used to listen to clicks on the card image. */
    private final View.OnClickListener imgListen = new View.OnClickListener() {
        public void onClick(View v) {
            launchDetails((long)v.getTag());
        }
    };
    /** Internal listener used to listen to clicks on the rest of the list item. */
    private final InternalClickListener clickListen = new InternalClickListener();
     /** Listener that is notified when a list item is clicked. */
    private Listener extListen = null;


    /** Default constructor.
     *  @param context The context of the display thread. */
    public AdapterCards(Context context) {
        this(context,
             new String[]{TableCard._NAME, TableCard._COST, TableCard._POT, TableCard._SET_ID,
                          TableCard._REQ, TableCard._ID, TableCard._ID,
                          TableCard._ID, TableCard._TYPE, TableCard._TYPE},
             new int[]   {R.id.card_name, R.id.card_cost, R.id.card_potion, R.id.card_set,
                          R.id.card_requires, android.R.id.background, R.id.card_image,
                          R.id.image_overlay, R.id.card_type, R.id.card_color});
    }


    /** Used by subclasses so they can bind their own columns.
     *  @param context The context of the display thread.
     *  @param columnNames The names of the columns to bind to views.
     *  @param viewIds The view ids bound to those names. */
    AdapterCards(Context context, String[] columnNames, int[] viewIds) {
        super(context, R.layout.list_item_card, null, columnNames, viewIds);
        mContext = context;
        setViewBinder(this);
        Resources res = context.getResources();
        colorFactory = new CardColorFactory(res);
        coinFactory = new CoinFactory(res);
        imgSize = res.getDimensionPixelSize(R.dimen.card_thumb_size);

        // Load the resources
        exp_none = res.getDrawable(R.drawable.ic_set_unknown);
        exp_icons = Utils.getDrawableArray(context, R.array.card_set_icons);
        int[] form_coin = Utils.getResourceArray(context, R.array.format_coin);
        String[] lang = context.getResources().getStringArray(R.array.language_codes);
        imgDesc = res.getString(R.string.card_details_button);
        coinDesc = new HashMap<>(lang.length);
        for(int i=0; i<lang.length; i++)
            coinDesc.put(lang[i], form_coin[i]);
    }


    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        if (cursor == null) return;
        colorFactory.changeCursor(cursor);
        _name = cursor.getColumnIndex(TableCard._NAME);
        _lang = cursor.getColumnIndex(TableCard._LANG);
        _set_name = cursor.getColumnIndex(TableCard._SET_NAME);
    }


    /** Binds each column's value to its associated view.
     *  @param view The view paired to this value by the constructor.
     *  @param cursor The cursor, positioned at the right place to read the value.
     *  @param columnIndex The index of the column being bound.
     *  @return {@code true} if the view was bound successfully. If the view was not bound, the
     *  Android system will bind the value using its default methods. */
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        switch(view.getId()) {
            case R.id.card_cost:
                String cost = cursor.getString(columnIndex);
                Integer qty = TableCard.parseVal(cost);
                if(qty != 0) {
                    Resources res = mContext.getResources();
                    String lang = cursor.getString(_lang);
                    view.setVisibility(View.VISIBLE);
                    view.setContentDescription(res.getQuantityString(coinDesc.get(lang), qty, cost));
                    ((ImageView)view).setImageDrawable(coinFactory.getDrawable(cost, 0));
                } else view.setVisibility(View.GONE);
                return true;


            case android.R.id.background:
                view.setTag(cursor.getPosition() + "," + cursor.getString(columnIndex));
                view.setOnClickListener(clickListen);
                view.setOnLongClickListener(clickListen);
                return true;

            case R.id.card_color:
                colorFactory.updateBackground(view, cursor);
                return true;

            case R.id.card_potion:
                // Hide icon if equal to "0", show icon otherwise
                if (cursor.getInt(columnIndex) != 0)
                    view.setVisibility(View.VISIBLE);
                else view.setVisibility(View.GONE);
                return true;

            case R.id.card_set:
                // Match the id to a set icon
                Drawable setIcon = exp_none;
                try { setIcon = exp_icons[cursor.getInt(columnIndex)];
                } catch(Exception ignored){}
                ((ImageView)view).setImageDrawable(setIcon);

                // Label the set icon with the set name
                view.setContentDescription(cursor.getString(_set_name));
                return true;

            case R.id.image_overlay:
                long id = cursor.getLong(columnIndex);
                view.setTag(id);
                view.setOnClickListener(imgListen);
                view.setContentDescription(String.format(imgDesc, cursor.getString(_name)));
                return true;

            case R.id.card_image:
                Picasso.with(mContext)
                       .load("file:///android_asset/card_images/"
                              + String.format("%03d", cursor.getLong(columnIndex))
                              + ".jpg")
                       .resize(imgSize, imgSize)
                       .into((ImageView)view);
                return true;

            case R.id.card_requires:
                // Default binding, but hide if equal to "" or null
                String req = cursor.getString(columnIndex);
                if(cursor.isNull(columnIndex) || "".equals(req))
                    view.setVisibility(View.GONE);
                else view.setVisibility(View.VISIBLE);
                return false;
        }
        // All other columns rely on the default binding
        return false;
    }


    /** Get the name of the card at the given position */
    public String getName(int position) {
        Cursor cursor = getCursor();
        if(cursor == null || !cursor.moveToPosition(position))
            return null;
        return cursor.getString(_name);
    }


    public void setListener(Listener listener) {
        extListen = listener;
        notifyDataSetChanged();
    }


    void launchDetails(long cardId) {
        Intent info = new Intent(mContext, ActivityCardInfo.class);
        info.putExtra(ActivityCardInfo.PARAM_ID, cardId);
        mContext.startActivity(info);
    }


    private class InternalClickListener implements View.OnClickListener, View.OnLongClickListener {
        @Override
        public void onClick(View v) {
            callExt((String)v.getTag(), false);
        }

        @Override
        public boolean onLongClick(View v) {
            callExt((String)v.getTag(), true);
            return true;
        }

        private void callExt(String tag, boolean longClick) {
            if(extListen == null) return;
            int sep = tag.indexOf(',');
            extListen.onItemClick(Integer.parseInt(tag.substring(0, sep)),
                                  Long.parseLong(tag.substring(sep+1, tag.length())),
                                  longClick);
        }
    }

    public interface Listener {
        void onItemClick(int position, long id, boolean longClick);
    }
}