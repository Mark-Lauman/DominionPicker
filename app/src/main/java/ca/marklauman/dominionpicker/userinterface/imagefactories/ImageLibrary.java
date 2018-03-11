package ca.marklauman.dominionpicker.userinterface.imagefactories;

import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import java.util.HashMap;

/** This object caches images so they can be re-used later.
 *  @author Mark Lauman */
class ImageLibrary {
    /** Cached drawable resources (value, size, drawable) */
    private final HashMap<CharSequence, SparseArray<Drawable>> drawables;


    ImageLibrary() {
        drawables = new HashMap<>();
    }


    /** Get a drawable resource with the given value and size.
     *  @return The Drawable resource created for this value and size,
     *          or null if no such resource has been made. */
    Drawable getDrawable(CharSequence value, int size) {
        SparseArray<Drawable> sizes = getAvailableDrawables(value);
        return sizes.get(size);
    }

    /** Set the drawable resource with a given value and size */
    synchronized void setDrawable(CharSequence value, int size, Drawable drawable) {
        SparseArray<Drawable> sizes = getAvailableDrawables(value);
        sizes.put(size, drawable);
    }

    /** Get the drawable resources available for a given value.
     *  Also update the internal data structure if there is no set available for that value. */
    private synchronized SparseArray<Drawable> getAvailableDrawables(CharSequence value) {
        SparseArray<Drawable> sizes = drawables.get(value);
        if(sizes == null) {
            sizes = new SparseArray<>(1);
            drawables.put(value, sizes);
        }
        return sizes;
    }
}