package ca.marklauman.dominionpicker.userinterface.imagefactories;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.style.ImageSpan;

import ca.marklauman.dominionpicker.R;

/** Factory which provides {@link VPDrawable}s to views that need them.
 *  @author Mark Lauman */
public class VPFactory extends ImageFactory {

    private static final ImageLibrary lib = new ImageLibrary();

    /** Available image sizes. */
    private final int[] img_size;
    /** Border color used around the shield */
    private final int border;
    /** Background color of a positive victory point shield. */
    private final int victory;
    /** Background color of a negative victory point shield. */
    private final int curse;
    /** Current ImageSpan id value (increments as they are retrieved) */
    private int spanId = 0;

    /** Create a CoinFactory from the given resources. */
    public VPFactory(Context context){
        Resources res = context.getResources();
        img_size = new int[]{res.getDimensionPixelSize(R.dimen.drawable_size_small),
                             res.getDimensionPixelSize(R.dimen.drawable_size_med),
                             res.getDimensionPixelSize(R.dimen.drawable_size_large)};
        border = ContextCompat.getColor(context, R.color.drawable_edge);
        victory = ContextCompat.getColor(context, R.color.vp_plus);
        curse = ContextCompat.getColor(context, R.color.vp_minus);
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


    private VPDrawable makeDrawable(CharSequence val, int size) {
        VPDrawable res = new VPDrawable(""+val);
        res.setBounds(0, 0, img_size[size], img_size[size]);
        lib.setDrawable(val, size, res);
        return res;
    }


    public class VPDrawable extends Drawable {
        private static final int DEFAULT_SIZE = 19;

        private final String val;
        private final boolean positive;
        private final Paint paint;
        private final Path shieldOuter;
        private final Path shieldInner;
        private final Matrix matrix;
        private final Rect textBounds;

        VPDrawable(String value) {
            val = value;
            positive = !val.startsWith("-");
            paint = new Paint();
            paint.setAntiAlias(true);
            matrix = new Matrix();
            textBounds = new Rect();

            shieldOuter = new Path();
            shieldOuter.moveTo(1.02f, 0.05f);
            shieldOuter.cubicTo(3.75f, 1.19f, 7.26f, 1.38f, 9.45f, 0.08f);
            shieldOuter.cubicTo(12.5f, 1.35f, 15.29f, 1.1f, 17.88f, 0.12f);
            shieldOuter.lineTo(18.02f, 5.22f);
            shieldOuter.cubicTo(15.92f, 5.37f, 14.82f, 8.6f, 17.21f, 10.51f);
            shieldOuter.cubicTo(13.79f, 21.73f, 5.39f, 21.93f, 1.79f, 10.36f);
            shieldOuter.cubicTo(3.41f, 9.61f, 4.29f, 6.02f, 0.97f, 5.14f);

            shieldInner = new Path();
            shieldInner.moveTo(2f, 1.48f);
            shieldInner.cubicTo(4.36f, 2.2f, 8.04f, 2.14f, 9.56f, 1.19f);
            shieldInner.cubicTo(12.38f, 2.24f, 14.93f, 2.04f, 16.92f, 1.48f);
            shieldInner.lineTo(17f, 4.44f);
            shieldInner.cubicTo(14.4f, 5.71f, 14.3f, 8.99f, 16.06f, 10.77f);
            shieldInner.cubicTo(12.87f, 20.74f, 6.08f, 19.99f, 2.98f, 10.75f);
            shieldInner.cubicTo(5.27f, 8.66f, 4.14f, 5.17f, 1.98f, 4.39f);

        }

        @Override
        public void draw(Canvas canvas) {
            // Provided width and height
            final float height = getBounds().height();
            final float width = getBounds().width();

            // x, y and drawing size
            float x = 0;
            float y = 0;
            final float size = (width < height) ? width : height;
            if(size == width) y = (height-size)/2f;
            else x = (width-size)/2f;

            // Draw the shield
            paint.setColor(border);
            drawPath(canvas, paint, shieldOuter, x, y, size);
            if(positive) paint.setColor(victory);
            else paint.setColor(curse);
            drawPath(canvas, paint, shieldInner, x, y, size);

            // Draw the text
            float fontSize = (val.length() < 2) ? 0.7f*size
                                                : 0.6f*size;
            paint.setTextSize(fontSize);
            paint.setColor(Color.BLACK);
            paint.getTextBounds(val, 0, val.length(), textBounds);
            float center = width/2f;
            canvas.drawText(val, x+center-textBounds.exactCenterX(),
                                 y+center-textBounds.exactCenterY(), paint);
        }


        private void drawPath(Canvas canvas, Paint paint, Path path, float x, float y, float size) {
            // Transform the path to be at x, y and with the right size
            matrix.reset();
            matrix.setTranslate(x, y);
            float scale = size/DEFAULT_SIZE;
            matrix.setScale(scale, scale);
            path.transform(matrix);

            // Draw the path
            canvas.drawPath(path, paint);

            // Restore the path to its original state
            matrix.invert(matrix);
            path.transform(matrix);
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