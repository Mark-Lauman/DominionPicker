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
public class CoinFactory extends DrawableFactory<CoinFactory.CoinDrawable> {
    /** Only one CoinFactory is used across the app */
    private static final CoinFactory factory = new CoinFactory();

    /** 1sp on this device */
    private float sp1 = -1f;
    /** Background color for the coin */
    private int coinBack;
    /** Edge color for the coin */
    private int coinEdge;

    /** Cannot be instantiated outside of this class */
    private CoinFactory(){}

    /** Get a CoinFactory instance */
    public static CoinFactory getInstance(Resources res) {
        factory.updateResources(res);
        return factory;
    }

    private void updateResources(Resources res) {
        sp1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1,
                res.getDisplayMetrics());
        coinBack = res.getColor(R.color.coin_back);
        coinEdge = res.getColor(R.color.coin_vp_edge);
    }


    /** Get a CoinDrawable with the provided value.
     *  @param val The value to display on the coin. */
    protected CoinDrawable makeDrawable(String val) {
        return new CoinDrawable(val);
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
            paint.setColor(coinEdge);
            canvas.drawCircle(x, y, size / 2f, paint);
            paint.setColor(coinBack);
            canvas.drawCircle(x, y, (size - 2 * sp1) / 2f, paint);

            // Draw the text
            paint.setTextSize(size - 6f * sp1);
            paint.setColor(Color.BLACK);
            paint.getTextBounds(val, 0, val.length(), textBounds);
            canvas.drawText(val, x-textBounds.exactCenterX(), y-textBounds.exactCenterY(), paint);
        }


        @Override
        public int getIntrinsicHeight() {
            return (int)(DEFAULT_SIZE*sp1+0.5f);
        }


        @Override
        public int getIntrinsicWidth() {
            return (int)(DEFAULT_SIZE*sp1+0.5f);
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