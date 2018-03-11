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
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import ca.marklauman.dominionpicker.R;

/** Icon used to represent victory point values.
 *  @author Mark Lauman */
public class VPIcon extends Icon {
    /** The size of the {@link #shield} path in pixels */
    private static final int DEFAULT_SIZE = 19;

    private final IconDescriber mDescriber;
    private String value;

    private final Paint paint;
    private final Path shield;
    private final Matrix translate;
    private final Matrix scale;
    private final Rect textBounds;
    /** Border color used around the shield */
    private final int border;
    /** Background color of a victory point shield. */
    private final int back;


    public VPIcon(Context context, IconDescriber describer, String text) {
        mDescriber = describer;
        value = text;
        paint = new Paint();
        paint.setAntiAlias(true);
        scale = new Matrix();
        translate = new Matrix();
        textBounds = new Rect();
        border = ContextCompat.getColor(context, R.color.drawable_edge);
        back = ContextCompat.getColor(context, R.color.vp_plus);

        shield = new Path();
        shield.moveTo(1.02f, 0.05f);
        shield.cubicTo(3.75f, 1.19f, 7.26f, 1.38f, 9.45f, 0.08f);
        shield.cubicTo(12.5f, 1.35f, 15.29f, 1.1f, 17.88f, 0.12f);
        shield.lineTo(18.02f, 5.22f);
        shield.cubicTo(15.92f, 5.37f, 14.82f, 8.6f, 17.21f, 10.51f);
        shield.cubicTo(13.79f, 21.73f, 5.39f, 21.93f, 1.79f, 10.36f);
        shield.cubicTo(3.41f, 9.61f, 4.29f, 6.02f, 0.97f, 5.14f);
        shield.close();

    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Scale the shield to match the size
        final float size = minFloat(width(), height());
        scale.reset();
        scale.setScale(size / DEFAULT_SIZE, size / DEFAULT_SIZE);
        shield.transform(scale);
        translate.reset();
        translate.setTranslate((width() - size) / 2f, (height() - size) / 2f);
        shield.transform(translate);

        // Draw the shield
        paint.setColor(back);
        canvas.drawPath(shield, paint);
        paint.setColor(border);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(size / 16f);
        canvas.drawPath(shield, paint);

        // Restore the shield to default size
        translate.invert(translate);
        shield.transform(translate);
        scale.invert(scale);
        shield.transform(scale);

        // Draw the text
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(value.length() < 2 ? 0.7f*size
                                             : 0.6f*size);
        paint.getTextBounds(value, 0, value.length(), textBounds);
        canvas.drawText(value, centerX()-textBounds.exactCenterX(),
                               centerY()-textBounds.exactCenterY(), paint);
    }


    @Override
    public void setHeight(int height) {
        //noinspection SuspiciousNameCombination
        setBounds(0, 0, height, height);
    }


    public void setText(String text) {
        value = text;
        invalidateSelf();
    }


    @Override
    public String getDescription(String lang) {
        return mDescriber == null ? "" : mDescriber.forVp(value, lang);
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