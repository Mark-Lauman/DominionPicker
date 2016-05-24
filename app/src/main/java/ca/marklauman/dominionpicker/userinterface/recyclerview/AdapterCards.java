package ca.marklauman.dominionpicker.userinterface.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import ca.marklauman.dominionpicker.ActivityCardInfo;
import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.userinterface.imagefactories.CardColorFactory;
import ca.marklauman.dominionpicker.userinterface.imagefactories.CoinFactory;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.dominionpicker.userinterface.imagefactories.DebtFactory;
import ca.marklauman.tools.Utils;
import ca.marklauman.tools.recyclerview.dragdrop.BasicTouchAdapter;
import ca.marklauman.tools.recyclerview.dragdrop.TouchCallback;

/** Basic adapter used to display cards from {@link TableCard}.
 *  @author Mark Lauman */
public class AdapterCards extends BasicTouchAdapter<AdapterCards.ViewHolder> {

    /** The columns used by this adapter. Any other columns provided will be ignored. */
    public static final String[] COLS_USED =
           {TableCard._ID, TableCard._NAME, TableCard._SET_NAME, TableCard._TYPE,
            TableCard._SET_ID, TableCard._COST, TableCard._DEBT, TableCard._POT,
            TableCard._REQ, TableCard._LANG,
            TableCard._TYPE_ACT, TableCard._TYPE_TREAS, TableCard._TYPE_VICTORY,     // colorFactory
            TableCard._TYPE_DUR, TableCard._TYPE_REACT, TableCard._TYPE_RESERVE,     // required
            TableCard._TYPE_CURSE, TableCard._TYPE_EVENT, TableCard._TYPE_LANDMARK}; // rows


    /** Context attached to the RecyclerView that this adapter oversees */
    protected final Context mContext;
    /** True if swipe is enabled for this adapter */
    private final boolean hasSwipe;
    /** Factory used to set the card color */
    private final CardColorFactory colorFactory;
    /** Factory used to make the coin drawable */
    private final CoinFactory coinFactory;
    /** Factory used to make the debt drawable */
    private final DebtFactory debtFactory;
    /** Size of the factory images */
    public final int imgSize;
    /** Icon used if an expansion is unknown */
    private final Drawable set_none;
    /** Icon used for each expansion */
    private final Drawable[] set_icons;
    /** Formatter string used to label the card details button */
    private final String cardDetails;
    /** Plurals used for the coin descriptors. The key is the language of the card. */
    private final HashMap<String, Integer> coinDesc;
    /** Plurals used for the debt descriptors. THe key is the language of the card. */
    private final HashMap<String, Integer> debtDesc;


    /** Cursor on display in this adapter. */
    protected Cursor mCursor;
    /** Index of the "_id" column for {@link #mCursor}. */
    protected int _id;
    /** Index of the "name" column for {@link #mCursor}. */
    protected int _name;
    /** Index of the "set_id" column for {@link #mCursor}. */
    protected int _set_id;
    /** Index of the "set_name" column for {@link #mCursor}. */
    protected int _set_name;
    /** Index of the "cost" column for {@link #mCursor}. */
    protected int _cost;
    /** Index of the "debt" column for {@link #mCursor}. */
    protected int _debt;
    /** Index of the "potion" column for {@link #mCursor}. */
    protected int _potion;
    /** Index of the "language" column for {@link #mCursor}. */
    protected int _language;
    /** Index of the "type" column for {@link #mCursor}. */
    protected int _type;
    /** Index of the "requires" column for {@link #mCursor}. */
    protected int _requires;
    /** Index of the landmark card type */
    protected int _type_landmark;


    /** Listener to be notified if a card is clicked. */
    private Listener mListener = null;
    /** Monitors this adapter until a card is clicked */
    public interface Listener {
        /** Called when a card is clicked anywhere but its picture
         *  (a click on its picture launches the details panel without notification).
         *  @param holder The ViewHolder that was clicked
         *  @param position The position of the card in this list.
         *  @param id The id of the card.
         *  @param longClick If the click was long or not. */
        void onItemClick(ViewHolder holder, int position, long id, boolean longClick);
    }


    /** Standard constructor. This card adapter does not support swipe to dismiss */
    public AdapterCards(RecyclerView view) {
        this(view, false);
    }

    /** Hidden constructor. If requested, swipe to dismiss will be turned on. */
    protected AdapterCards(RecyclerView view, boolean dismiss) {
        super(view, dismiss ? TouchCallback.forDismissList() : new TouchCallback(0, 0));
        hasStableIds();
        mContext = view.getContext();
        hasSwipe = dismiss;
        Resources res   = view.getResources();
        imgSize = res.getDimensionPixelSize(R.dimen.card_thumb_size);
        colorFactory = new CardColorFactory(mContext);
        coinFactory = new CoinFactory(mContext);
        debtFactory = new DebtFactory(mContext);
        set_none = ContextCompat.getDrawable(mContext, R.drawable.ic_set_unknown);
        set_icons = Utils.getDrawableArray(mContext, R.array.card_set_icons);
        cardDetails = res.getString(R.string.card_details_button);

        // fetch the coin & debt descriptions
        int[] form_coin = Utils.getResourceArray(mContext, R.array.format_coin);
        int[] form_debt = Utils.getResourceArray(mContext, R.array.format_debt);
        String[] lang = res.getStringArray(R.array.language_codes);
        coinDesc = new HashMap<>(lang.length);
        debtDesc = new HashMap<>(lang.length);
        for(int i=0; i<lang.length; i++) {
            coinDesc.put(lang[i], form_coin[i]);
            debtDesc.put(lang[i], form_debt[i]);
        }
    }


    /** Attach a listener to this adapter. That listener will be notified when cards are clicked. */
    public void setListener(Listener listener) {
        mListener = listener;
    }


    /** Change the cursor on display. This forces a rebind of all views. */
    public void changeCursor(Cursor cursor) {
        mCursor = cursor;
        if(cursor == null) return;
        colorFactory.changeCursor(cursor);
        _id = cursor.getColumnIndex(TableCard._ID);
        _name = cursor.getColumnIndex(TableCard._NAME);
        _set_id = cursor.getColumnIndex(TableCard._SET_ID);
        _set_name = cursor.getColumnIndex(TableCard._SET_NAME);
        _cost = cursor.getColumnIndex(TableCard._COST);
        _debt = cursor.getColumnIndex(TableCard._DEBT);
        _potion = cursor.getColumnIndex(TableCard._POT);
        _language = cursor.getColumnIndex(TableCard._LANG);
        _type = cursor.getColumnIndex(TableCard._TYPE);
        _requires = cursor.getColumnIndex(TableCard._REQ);
        _type_landmark = cursor.getColumnIndex(TableCard._TYPE_LANDMARK);
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.list_item_card, parent, false));
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        final long id = mCursor.getLong(_id);
        final String name = mCursor.getString(_name);
        final String cost = mCursor.getString(_cost);
        final boolean potion =  0 != mCursor.getInt(_potion);

        // Card color, image and name
        colorFactory.updateBackground(holder.color, mCursor);
        Picasso.with(mContext)
               .load("file:///android_asset/card_images/"
                     + String.format(Locale.US, "%03d", id) + ".jpg")
               .resize(imgSize, imgSize)
               .into(holder.image);
        holder.details.setContentDescription(String.format(Locale.US, cardDetails, name));
        holder.name.setText(name);

        // The set icon and name
        Drawable set_icon = set_none;
        try {
            set_icon = set_icons[mCursor.getInt(_set_id)];
        } catch(ArrayIndexOutOfBoundsException ignored) {}
        holder.set.setImageDrawable(set_icon);
        holder.set.setContentDescription(mCursor.getString(_set_name));

        // The cost in coins, debt and potions
        final int costVal = TableCard.parseVal(cost);
        final int debt = mCursor.getInt(_debt);
        /* Show the coin if we have a nonzero cost
         * or if this is not a landmark and has no other costs. */
        if(costVal != 0 || (mCursor.getInt(_type_landmark) == 0 && !potion && debt == 0)) {
            holder.cost.setContentDescription(
                              mContext.getResources()
                                      .getQuantityString(coinDesc.get(mCursor.getString(_language)),
                                                         costVal, cost));
            holder.cost.setImageDrawable(coinFactory.getDrawable(cost, 0));
            holder.cost.setVisibility(View.VISIBLE);
        } else holder.cost.setVisibility(View.GONE);
        if(debt != 0) {
            holder.debt.setContentDescription(
                              mContext.getResources()
                                      .getQuantityString(debtDesc.get(mCursor.getString(_language)),
                                                         debt, ""+debt));
            holder.debt.setImageDrawable(debtFactory.getDrawable(""+debt, DebtFactory.SIZE_SML));
            holder.debt.setVisibility(View.VISIBLE);
        } else holder.debt.setVisibility(View.GONE);
        holder.potion.setVisibility(potion ? View.VISIBLE : View.GONE);

        // The card type and requirements
        holder.type.setText(mCursor.getString(_type));
        String req = mCursor.getString(_requires);
        holder.requires.setText(req);
        holder.requires.setVisibility( (req == null || req.equals("")) ? View.GONE : View.VISIBLE);
    }


    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(_id);
    }

    @Override
    public void onItemMove(int i, int i1) { /* Never called */ }

    @Override
    public void onDismiss(int i) { /* Ignores dismiss events. Descendants may not. */ }


    /** Get the display name of a card
     *  @param position The position of the card in this adapter. */
    public String getName(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getString(_name);
    }


    /** Launch the details panel for a specified card */
    public static void launchDetails(Context context, long cardId) {
        Intent info = new Intent(context, ActivityCardInfo.class);
        info.putExtra(ActivityCardInfo.PARAM_ID, cardId);
        context.startActivity(info);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(android.R.id.background) public View background;
        @BindView(R.id.card_color)         public View color;
        @BindView(R.id.card_image)         public ImageView image;
        @BindView(R.id.card_details)       public ImageView details;
        @BindView(R.id.card_set)           public ImageView set;
        @BindView(R.id.card_name)          public TextView name;
        @BindView(R.id.card_extra)         public TextView extra;
        @BindView(R.id.card_cost)          public ImageView cost;
        @BindView(R.id.card_debt)          public ImageView debt;
        @BindView(R.id.card_potion)        public ImageView potion;
        @BindView(R.id.card_type)          public TextView type;
        @BindView(R.id.card_requires)      public TextView requires;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        /** View the details of this card. Triggered when the card's image is clicked. */
        @OnClick(R.id.card_details)
        public void launchDetails() {
            mCursor.moveToPosition(getAdapterPosition());
            AdapterCards.launchDetails(mContext, mCursor.getLong(_id));
        }

        private void notifyClick(boolean longClick) {
            if(mListener == null) return;
            final int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            mListener.onItemClick(this, position, mCursor.getLong(_id), longClick);
        }

        @OnClick(R.id.click_area)
        public void click() {
            notifyClick(false);
        }

        @OnLongClick(R.id.click_area)
        public boolean longClick() {
            notifyClick(true);
            return !hasSwipe;
        }
    }
}