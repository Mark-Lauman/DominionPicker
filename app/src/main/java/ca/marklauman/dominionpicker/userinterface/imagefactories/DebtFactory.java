package ca.marklauman.dominionpicker.userinterface.imagefactories;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.style.ImageSpan;

import ca.marklauman.dominionpicker.R;

/** Factory which provides {@link DebtDrawable}s to views that need them.
 *  @author Mark Lauman */
public class DebtFactory extends ImageFactory {

    private static final ImageLibrary lib = new ImageLibrary();

    /** Available image sizes. */
    private final int[] img_size;
    /** Background color for the debt icon */
    private final int back;
    /** Current ImageSpan id value (increments as they are retrieved) */
    private int spanId = 0;

    /** Create a CoinFactory from the given resources. */
    public DebtFactory(Context context){
        Resources res = context.getResources();
        img_size = new int[]{res.getDimensionPixelSize(R.dimen.drawable_size_small),
                             res.getDimensionPixelSize(R.dimen.drawable_size_med),
                             res.getDimensionPixelSize(R.dimen.drawable_size_large)};
        back = ContextCompat.getColor(context, R.color.debt);
    }

    @Override
    public Drawable getDrawable(CharSequence value, int size) {
        Drawable res = lib.getDrawable(value, size);
        if(res == null) res = makeDrawable(value, size);
        return res;
    }

    @Override
    public ImageSpan getSpan(CharSequence value, int size) {
        ImageSpan res = lib.getSpan(value, size, spanId);
        if(res == null) {
            makeDrawable(value, size);
            res = lib.getSpan(value, size, spanId);
        }
        spanId++;
        return res;
    }

    @Override
    public void newSpannableView() {
        spanId = 0;
    }

    /** Get a DebtDrawable with the provided value.
     *  @param val The value to display on the coin. */
    private DebtDrawable makeDrawable(CharSequence val, int size) {
        DebtDrawable res = new DebtDrawable(""+val);
        res.setBounds(0, 0, img_size[size], img_size[size]);
        lib.setDrawable(val, size, res);
        return res;
    }


    public class DebtDrawable extends Drawable {
        /** Value to display in this coin */
        private final String val;
        /** Paint object used by this drawable */
        private final Paint paint;
        /** Path used to draw the hexagon */
        private final Path hex;
        /** Rectangle used to determine the text bounds */
        private final Rect textBounds;

        DebtDrawable(String value) {
            paint = new Paint();
            paint.setAntiAlias(true);
            val = value;
            hex = new Path();
            textBounds = new Rect();
        }

        @Override
        public void draw(Canvas canvas) {
            // Compute the radius of the hex - its the basis for all the measurements
            float radius = (float) (getBounds().height() / (2 * Math.sin(Math.PI / 3)));
            // 2 * radius should be less than the width of this drawable
            if(getBounds().width() < 2 * radius) radius = getBounds().width() / 2;

            // X and Y in the center of the canvas
            float x = getBounds().centerX();
            float y = getBounds().centerY();

            // Draw the hexagon
            hex.reset();
            final float section = (float) (2.0 * Math.PI / 6);
            hex.moveTo(x + radius * (float)Math.cos(0),
                       y + radius * (float)Math.sin(0));
            for (int i = 1; i < 8; i++)
                hex.lineTo(x + radius * (float)Math.cos(section * i),
                           y + radius * (float)Math.sin(section * i));
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(back);
            canvas.drawPath(hex, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(radius / 7f);
            canvas.drawPath(hex, paint);

            // Draw the text
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            float fontSize = (val.length() < 2) ? radius * 1.3f
                                                : radius;
            paint.setTextSize(fontSize);
            paint.setColor(Color.WHITE);
            paint.getTextBounds(val, 0, val.length(), textBounds);
            canvas.drawText(val, x-textBounds.exactCenterX(), y-textBounds.exactCenterY(), paint);
        }


        @Override
        public int getIntrinsicHeight() {
            return img_size[SIZE_MED];
        }


        @Override
        public int getIntrinsicWidth() {
            return img_size[SIZE_MED];
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