package ca.marklauman.dominionpicker.userinterface.imagefactories;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

/** Factory responsible for creating Drawable objects for use in ListViews.
 *  Minimizes the creation of new drawables to reduce lag caused by garbage collection.
 *  @author Mark Lauman */
public abstract class ImageFactory {
    public static final int SIZE_LRG = 2;
    public static final int SIZE_MED = 1;
    public static final int SIZE_SML = 0;

    public abstract Drawable getDrawable(CharSequence value, int size);

    public abstract ImageSpan getSpan(CharSequence value, int size);

    abstract void newSpannableView();
}