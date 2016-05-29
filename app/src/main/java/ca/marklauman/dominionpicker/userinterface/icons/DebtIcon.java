package ca.marklauman.dominionpicker.userinterface.icons;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;

import ca.marklauman.dominionpicker.R;

/** Icon used for Debt Tokens on cards and in prices.
 *  @author Mark Lauman */
public class DebtIcon extends Icon {
    /** The height of the {@link #hex} path in pixels. */
    private static final float DEF_HEIGHT = 10;
    /** The width of the {@link #hex} path in pixels. */
    private static final float DEF_WIDTH = (float)(DEF_HEIGHT / Math.sin(Math.PI / 3));


    /** Describer used to describe this icon */
    private final IconDescriber mDescriber;
    /** Value on display in this coin */
    private String value;

    /** Paint object used by this drawable */
    private final Paint paint;
    /** Path used to draw the hexagon */
    private final Path hex;
    /** Matrix used to center the hexagon. */
    private final Matrix translate;
    /** Matrix used to scale the hexagon. */
    private final Matrix scale;

    /** Rectangle used to determine the text bounds */
    private final Rect textBounds;
    /** Color on the back of the icon */
    private final int back;

    /** Default constructor.
     *  @param context The application context.
     *  @param describer This IconDescriber will be used
     *                   to provide the description for this drawable.
     *  @param text The starting text to display on the token.
     *              (Can be changed with {@link #setText(String)}) */
    public DebtIcon(Context context, IconDescriber describer, String text) {
        mDescriber = describer;
        value = text;
        paint = new Paint();
        paint.setAntiAlias(true);
        translate = new Matrix();
        scale = new Matrix();
        textBounds = new Rect();
        back = ContextCompat.getColor(context, R.color.debt);

        // Create the hex
        hex = new Path();
        final float x = DEF_WIDTH / 2f;
        final float y = DEF_HEIGHT / 2f;
        final float radius = DEF_WIDTH / 2f;
        final double section = Math.PI / 3;
        hex.moveTo((float)(x + radius * Math.cos(0)),
                   (float)(y + radius * Math.sin(0)));
        for (int i = 1; i < 6; i++)
            hex.lineTo((float)(x + radius * Math.cos(section * i)),
                       (float)(y + radius * Math.sin(section * i)));
        hex.close();
    }


    @Override
    public void draw(Canvas canvas) {

        // Compute the transforms for the hex
        float scaleRatio = minFloat(height() / DEF_HEIGHT,
                                    width()  / DEF_WIDTH);
        scale.reset();
        scale.setScale(scaleRatio, scaleRatio);
        hex.transform(scale);
        translate.reset();
        translate.setTranslate( (width()  - scaleRatio * DEF_WIDTH) / 2f,
                (height() - scaleRatio * DEF_HEIGHT) / 2f);
        hex.transform(translate);

        // Draw the hex
        paint.setColor(back);
        canvas.drawPath(hex, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(scaleRatio);
        canvas.drawPath(hex, paint);
        
        // revert the transforms
        translate.invert(translate);
        hex.transform(translate);
        scale.invert(scale);
        hex.transform(scale);

        // Draw the text
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(value.length() < 2 ? 8f * scaleRatio
                                             : 6f * scaleRatio);
        paint.getTextBounds(value, 0, value.length(), textBounds);
        canvas.drawText(value, centerX() - textBounds.exactCenterX(),
                               centerY() - textBounds.exactCenterY(), paint);
    }


    @Override
    public void setHeight(int height) {
        setBounds(0, 0, (int)(height / Math.sin(Math.PI / 3)), height);
    }


    public void setText(String text) {
        value = text;
        invalidateSelf();
    }


    @Override
    public String getDescription(String lang) {
        return mDescriber == null ? "" : mDescriber.forDebt(value, lang);
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