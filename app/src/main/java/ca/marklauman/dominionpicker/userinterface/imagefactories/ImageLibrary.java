package ca.marklauman.dominionpicker.userinterface.imagefactories;

import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

import java.util.HashMap;

/** This object caches images so they can be re-used later.
 *  @author Mark Lauman */
class ImageLibrary {
    /** Cached drawable resources (value, size, drawable) */
    private final HashMap<CharSequence, HashMap<Integer, Drawable>> drawables;
    /** Cached ImageSpans created from the drawable resources (value, size, drawable, id) */
    private final HashMap<CharSequence, HashMap<Integer, HashMap<Integer, ImageSpan>>> spans;


    public ImageLibrary() {
        drawables = new HashMap<>();
        spans = new HashMap<>();
    }


    /** Get a drawable resource with the given value and size.
     *  @return The Drawable resource created for this value and size,
     *          or null if no such resource has been made. */
    public Drawable getDrawable(CharSequence value, int size) {
        HashMap<Integer, Drawable> sizes = getAvailableDrawables(value);
        return sizes.get(size);
    }

    /** Set the drawable resource with a given value and size */
    public synchronized void setDrawable(CharSequence value, int size, Drawable drawable) {
        HashMap<Integer, Drawable> sizes = getAvailableDrawables(value);
        sizes.put(size, drawable);
    }

    /** Get the drawable resources available for a given value.
     *  Also update the internal data structure if there is no set available for that value. */
    private synchronized HashMap<Integer, Drawable> getAvailableDrawables(CharSequence value) {
        HashMap<Integer, Drawable> sizes = drawables.get(value);
        if(sizes == null) {
            sizes = new HashMap<>(1);
            drawables.put(value, sizes);
        }
        return sizes;
    }

    /** Get an ImageSpan based off the drawable with the given value and size.
     *  @param id A unique id value will receive a unique ImageSpan instance.
     *  @return An ImageSpan for the given size and value.
     *  {@code null} if the drawable for this value and size has not been set yet. */
    public synchronized ImageSpan getSpan(CharSequence value, int size, int id) {
        // Get the available sizes for the given value
        HashMap<Integer, HashMap<Integer, ImageSpan>> sizes = spans.get(value);
        if(sizes == null) {
            sizes = new HashMap<>(1);
            spans.put(value, sizes);
        }

        // Get the available instances for the given size
        HashMap<Integer, ImageSpan> instances = sizes.get(size);
        if(instances == null) {
            instances = new HashMap<>(1);
            sizes.put(size, instances);
        }

        // Return the ImageSpan with this id
        ImageSpan res = instances.get(id);
        if(res != null) return res;

        // We have no ImageSpan with that id. So lets try to make one
        Drawable d = getDrawable(value, size);
        if(d == null) return null;
        res = new ImageSpan(d, DynamicDrawableSpan.ALIGN_BOTTOM);
        instances.put(id, res);
        return res;
    }
}