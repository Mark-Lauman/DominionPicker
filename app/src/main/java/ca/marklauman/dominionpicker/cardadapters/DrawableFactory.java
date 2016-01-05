package ca.marklauman.dominionpicker.cardadapters;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import java.util.ArrayList;
import java.util.HashMap;

/** Factory responsible for creating Drawable objects for use in ListViews.
 *  Minimizes the creation of new drawables to reduce lag caused by garbage collection.
 *  @author Mark Lauman */
public abstract class DrawableFactory {

    /** Cached drawable resources (value, size, drawable) */
    private static final HashMap<CharSequence, HashMap<Integer, Drawable>> drawables
            = new HashMap<>();
    /** Cached ImageSpans created from the drawable resources (value, size, drawable, id) */
    private static final HashMap<CharSequence, HashMap<Integer, ArrayList<ImageSpan>>> spans
            = new HashMap<>();
    /** Current ImageSpan object to retrieve */
    private int spanId = 0;

    /** Alias of {@code getDrawable(val, defSize()} */
    public final synchronized Drawable getDrawable(CharSequence val) {
        return getDrawable(val, defSize());
    }

    /** Create a drawable resource with the given value and size.
     *  @param val The value used in the construction of this drawable.
     *  @param size The size of this drawable resource in pixels.
     *  @return The Drawable resource created for this value and size. Calling this method
     *  twice with the same parameters will result in the same Drawable instance being returned.
     *  Calling this method from separate DrawableFactory instances with the same parameters
     *  will also result in the same Drawable instance being returned. */
    public final synchronized Drawable getDrawable(CharSequence val, int size) {
        // Get the available sizes for a given value
        HashMap<Integer, Drawable> sizes = drawables.get(val);
        if(sizes == null) {
            sizes = new HashMap<>(1);
            drawables.put(val, sizes);
        }

        // Get the drawable in the right size
        Drawable result = sizes.get(size);
        if(result == null) {
            result = makeDrawable(val, size);
            sizes.put(size, result);
        }
        return result;
    }

    /** Alias of {@code getSpan(val, defSize()) */
    public final synchronized ImageSpan getSpan(CharSequence val) {
        return getSpan(val, defSize());
    }

    /** This will return a new ImageSpan resource each time it is called,
     *  using this factory's Drawables to create them. The only exception is
     *  if {@link #newSpannableView()} is called. If that happens, then ImageSpan
     *  instances will be re-used to reduce memory overhead.
     *  @param val The value used to create the drawable resource.
     *  @param size The size of the resulting ImageSpan in pixels.
     *  @return A new ImageSpan resource, or one created previously for a different spannable */
    public final synchronized ImageSpan getSpan(CharSequence val, int size) {
        // Get the available sizes for a given value
        HashMap<Integer, ArrayList<ImageSpan>> sizes = spans.get(val);
        if(sizes == null) {
            sizes = new HashMap<>(1);
            spans.put(val, sizes);
        }

        // Get the available ImageSpan objects in this size
        ArrayList<ImageSpan> available = sizes.get(size);
        if(available == null) {
            available = new ArrayList<>(1);
            sizes.put(size, available);
        }

        // Get an ImageSpan instance.
        ImageSpan result;
        if(available.size() <= spanId) {
            result = new ImageSpan(getDrawable(val, size), ImageSpan.ALIGN_BOTTOM);
            available.add(result);
        } else result = available.get(spanId);
        spanId++;
        return result;
    }

    /** Tells the factory that we have moved on to another spannable view, and the
     *  ImageSpan resources can be re-used. */
    public void newSpannableView() {
        spanId = 0;
    }

    protected abstract Drawable makeDrawable(CharSequence val, int size);

    protected abstract int defSize();
}