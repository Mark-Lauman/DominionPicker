package ca.marklauman.dominionpicker.userinterface.icons;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import ca.marklauman.dominionpicker.R;

/** Icon representing a potion. */
public class PotionIcon extends Icon {


    private final IconDescriber mDescriber;

    private final Drawable potion;


    public PotionIcon(Context context, IconDescriber describer) {
        potion = ContextCompat.getDrawable(context, R.drawable.ic_dom_potion);
        super.setBounds(potion.getBounds());
        mDescriber = describer;
    }


    @Override
    public void draw(Canvas canvas) {
        potion.draw(canvas);
    }


    @Override
    public void setHeight(int height) {
        //noinspection SuspiciousNameCombination
        setBounds(0, 0, height, height);
    }


    @Override
    public void setText(String text) {}

    @Override
    public String getDescription(String lang) {
        return mDescriber == null ? "" : mDescriber.forPotion(lang);
    }


    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        potion.setBounds(left, top, right, bottom);
        super.setBounds(left, top, right, bottom);
    }

    @Override
    public void setBounds(Rect rect) {
        potion.setBounds(rect);
        super.setBounds(rect);
    }


    @Override
    public void setAlpha(int alpha) {
        potion.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        potion.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return potion.getOpacity();
    }
}
