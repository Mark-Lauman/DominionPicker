package ca.marklauman.dominionpicker.userinterface.icons;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.support.annotation.NonNull;
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
    /** Value in the middle of this icon */
    private String value;

    /** Paint object used to draw things */
    private final Paint paint;
    /** Path for the hexagon */
    private final Path hex;

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
        hex = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        textBounds = new Rect();
        back = ContextCompat.getColor(context, R.color.debt);
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        // Determine the scale of the icon
        float scale = minFloat(height() / DEF_HEIGHT,
                               width()  / DEF_WIDTH);

        // Calculate the hexagon
        float radius = scale * DEF_WIDTH / 2f;
        hex.reset();
        hex.moveTo(centerX() + radius, centerY());
        for(int i=1; i<6; i++)
            hex.lineTo((float)(centerX() + radius * Math.cos(i * Math.PI/3)),
                       (float)(centerY() + radius * Math.sin(i * Math.PI/3)));
        hex.close();

        // Draw the hexagon
        paint.setColor(back);
        canvas.drawPath(hex, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(scale);
        canvas.drawPath(hex, paint);

        // Draw the text
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(value.length() < 2 ? 8f*scale : 6f*scale);
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