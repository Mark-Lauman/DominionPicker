package ca.marklauman.dominionpicker.userinterface.icons;

import android.graphics.drawable.Drawable;

/** Template for the various icon drawables in this directory.
 *
 * Created by Mark on 2016-05-20.
 */
public abstract class Icon extends Drawable {

    /** Alternative to {@link #setBounds(int, int, int, int)}.
     *  The width is automatically set in respect to the height. */
    public abstract void setHeight(int height);

    /** Change the text on display in this icon.
     *  (This is the number inside a coin, etc.) */
    public abstract void setText(String text);

    /** Retrieve the description for this icon.
     *  (I would recommend using an {@link IconDescriber} instance to retrieve this description) */
    public abstract String getDescription(String lang);

    /** Returns the smaller of a and b. */
    static float minFloat(float a, float b) {
        return a < b ? a : b;
    }

    /** Retrieve the width of this icon */
    public int width() {
        return getBounds().width();
    }

    /** Retrieve the height of this icon */
    public int height() {
        return getBounds().height();
    }

    /** Retrieve the center x of this icon */
    float centerX() {
        return width()/2f;
    }

    /** Retrieve the center y of this icon */
    float centerY() {
        return height()/2f;
    }
}