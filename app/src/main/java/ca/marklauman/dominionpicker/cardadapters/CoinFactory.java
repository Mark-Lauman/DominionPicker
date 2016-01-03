package ca.marklauman.dominionpicker.cardadapters;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import ca.marklauman.dominionpicker.R;

/** Factory which provides {@link CoinDrawable}s to views that need them.
 *  @author Mark Lauman */
public class CoinFactory extends DrawableFactory {
    /** 1sp on this device */
    private final float sp1;
    /** Background color for the coin */
    private final int coinBack;
    /** Edge color for the coin */
    private final int coinEdge;

    /** Create a CoinFactory from the given resources. */
    public CoinFactory(Resources res){
        sp1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1,
                res.getDisplayMetrics());
        coinBack = res.getColor(R.color.coin_back);
        coinEdge = res.getColor(R.color.coin_vp_edge);
    }


    /** Get a CoinDrawable with the provided value.
     *  @param val The value to display on the coin. */
    protected CoinDrawable makeDrawable(CharSequence val, int size) {
        CoinDrawable res = new CoinDrawable(""+val);
        res.setBounds(0, 0, size, size);
        return res;
    }

    protected int defSize() {
        return (int)(CoinDrawable.DEFAULT_SIZE*sp1+0.5f);
    }


    public class CoinDrawable extends Drawable {
        /** Default size of this drawable in sp */
        private static final int DEFAULT_SIZE = 19;

        /** Value to display in this coin */
        private final String val;
        /** Paint object used by this drawable */
        private final Paint paint;
        /** Rectangle used to determine the text bounds */
        private final Rect textBounds;

        CoinDrawable(String value) {
            paint = new Paint();
            paint.setAntiAlias(true);
            val = value;
            textBounds = new Rect();
        }

        @Override
        public void draw(Canvas canvas) {
            // Provided width and height
            float height = getBounds().height();
            float width = getBounds().width();

            // X and Y in the center of the canvas
            float x = width/2f;
            float y = height/2f;

            // The size is the lesser of width and height
            float size = (width < height) ? width : height;
            size -= 2; // for anti-aliasing on the borders

            // Draw the coin
            float coinSize = size/2f;
            paint.setColor(coinEdge);
            canvas.drawCircle(x, y, coinSize, paint);
            paint.setColor(coinBack);
            canvas.drawCircle(x, y, 0.9f*coinSize, paint);

            // Draw the text
            float fontSize = (val.length() < 2) ? 0.7f*size
                                                : 0.6f*size;
            paint.setTextSize(fontSize);
            paint.setColor(Color.BLACK);
            paint.getTextBounds(val, 0, val.length(), textBounds);
            canvas.drawText(val, x-textBounds.exactCenterX(), y-textBounds.exactCenterY(), paint);
        }


        @Override
        public int getIntrinsicHeight() {
            return defSize();
        }


        @Override
        public int getIntrinsicWidth() {
            return defSize();
        }


        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
            invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}