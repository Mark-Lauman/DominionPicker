package ca.marklauman.dominionpicker.userinterface.icons;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;

import ca.marklauman.dominionpicker.R;

/** Icon used to represent coin values.
 *  @author Mark Lauman */
public class CoinIcon extends Icon {

    /** Describer used to describe this icon */
    private final IconDescriber mDescriber;
    /** Value to display in this coin */
    private String value;

    /** Paint object used by this drawable */
    private final Paint paint;
    /** Rectangle used to determine the text bounds */
    private final Rect textBounds;
    /** Color of the coin itself */
    private final int coin;
    /** Color of the outline around the coin */
    private final int edge;


    /** Default constructor.
     *  @param context The application context.
     *  @param describer This IconDescriber will be used
     *                   to provide the description for this drawable.
     *  @param text The starting text to display on the token.
     *              (Can be changed with {@link #setText(String)}) */
    public CoinIcon(Context context, IconDescriber describer, String text) {
        mDescriber = describer;
        value = text;
        paint = new Paint();
        paint.setAntiAlias(true);
        textBounds = new Rect();
        coin = ContextCompat.getColor(context, R.color.coin_back);
        edge = ContextCompat.getColor(context, R.color.drawable_edge);
    }


    @Override
    public void draw(Canvas canvas) {
        // the radius is the smaller of the width and height
        float x = getBounds().centerX();
        float y = getBounds().centerY();
        float radius = getBounds().height();
        if(getBounds().width() < radius) radius = getBounds().width();
        radius = radius / 2f;

        // Draw the coin
        paint.setColor(coin);
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(edge);
        paint.setStrokeWidth(radius / 8f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(x,y, radius, paint);

        // Draw the text
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(value.length() < 2 ? radius * 1.5f : radius);
        paint.getTextBounds(value, 0, value.length(), textBounds);
        canvas.drawText(value, x-textBounds.exactCenterX(), y-textBounds.exactCenterY(), paint);
    }


    @Override
    public void setHeight(int height) {
        //noinspection SuspiciousNameCombination
        setBounds(0, 0, height, height);
    }


    @Override
    public void setText(String text) {
        value = text;
        invalidateSelf();
    }


    @Override
    public String getDescription(String lang) {
        return mDescriber == null ? "" : mDescriber.forCoin(value, lang);
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