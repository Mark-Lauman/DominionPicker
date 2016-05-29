package ca.marklauman.dominionpicker.userinterface.icons;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;

import ca.marklauman.dominionpicker.database.TableCard;

/** A combination icon that is used to display the price of a card.
 *  Includes the cost in coins, debt tokens and potions.
 *  @author Mark Lauman */
public class PriceIcon extends Icon {

    /** The coin cost of this price. */
    private final CoinIcon mCoin;
    /** The debt cost of this price. */
    private final DebtIcon mDebt;
    /** The potion cost of this price. */
    private final PotionIcon mPotion;

    /** Should the coin be visible in this price. */
    private boolean showCoin = false;
    /** Should the debt be visible in this price. */
    private boolean showDebt = false;
    /** Should the potion be visible in this price. */
    private boolean showPotion = false;


    public PriceIcon(Context context, IconDescriber describer) {
        mCoin = new CoinIcon(context, describer, "");
        mDebt = new DebtIcon(context, describer, "");
        mPotion = new PotionIcon(context, describer);
    }


    @Override
    public void draw(Canvas canvas) {
        if(showCoin) {
            mCoin.draw(canvas);
            canvas.translate(mCoin.width(), 0);
        }
        if(showDebt) {
            mDebt.draw(canvas);
            canvas.translate(mDebt.width(), 0);
        }
        if(showPotion)
            mPotion.draw(canvas);
    }


    @Override
    public void setHeight(int height) {
        setBounds(0, 0, calcWidth(), height);
    }


    private int calcWidth() {
        float width = 0f;
        if(showCoin) width += mCoin.width();
        if(showDebt) width += mDebt.width();
        if(showPotion) width += mPotion.width();
        return (int)width;
    }


    public void setValue(String coins, int debt, int potion, int landmark) {
        // Determine what drawables to display
        showDebt = debt != 0;
        showPotion = potion != 0;
        int costVal = TableCard.parseVal(coins);
        showCoin = costVal != 0 || (landmark == 0 && !showDebt && !showPotion);

        // Set the values of the visible drawables
        if(showCoin) mCoin.setText(coins);
        if(showDebt) mDebt.setText(""+debt);

        // Reset this drawable's bounds
        setHeight(mCoin.height());
    }


    public String getDescription(String lang) {
        String desc = "";
        if(showCoin) desc += mCoin.getDescription(lang);
        if(showDebt) {
            if(desc.length() != 0) desc += " ";
            desc += mDebt.getDescription(lang);
        }
        if(showPotion) {
            if(desc.length() != 0) desc += " ";
            desc += mPotion.getDescription(lang);
        }
        return desc;
    }


    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        mCoin.setHeight(bottom);
        mDebt.setHeight(bottom);
        mPotion.setHeight(bottom);
        super.setBounds(left, top, right, bottom);
    }

    @Override @Deprecated
    public void setBounds(Rect rect) {
        throw new UnsupportedOperationException("Please use the other setBounds()");
    }


    @Override
    public void setAlpha(int alpha) {
        mCoin.setAlpha(alpha);
        mDebt.setAlpha(alpha);
        mPotion.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mCoin.setColorFilter(colorFilter);
        mDebt.setColorFilter(colorFilter);
        mPotion.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}