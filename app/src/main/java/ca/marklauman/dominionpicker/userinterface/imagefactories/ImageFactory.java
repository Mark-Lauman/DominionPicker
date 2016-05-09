package ca.marklauman.dominionpicker.userinterface.imagefactories;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

/** Factory responsible for creating Drawable objects for use in ListViews.
 *  Minimizes the creation of new drawables to reduce lag caused by garbage collection.
 *  @author Mark Lauman */
public interface ImageFactory {

    Drawable getDrawable(CharSequence value, int size);

    ImageSpan getSpan(CharSequence value, int size);

    void newSpannableView();


}