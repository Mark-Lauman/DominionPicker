package ca.marklauman.dominionpicker.cardadapters;

import android.graphics.drawable.Drawable;

import java.util.HashMap;

/** Factory responsible for creating Drawable objects for use in ListViews.
 *  Minimizes the creation of new drawables to reduce lag caused by garbage collection.
 *  @author Mark Lauman */
abstract class DrawableFactory<T extends Drawable> {
    /** Drawables available for instant use */
    private final HashMap<String, HashMap<Integer, T>> drawables = new HashMap<>();

    public final synchronized T getDrawable(String val, int id) {
        // Check for the value of this drawable
        HashMap<Integer, T> valid = drawables.get(val);
        if(valid == null) {
            valid = new HashMap<>(1);
            drawables.put(val, valid);
        }

        // Check for the id of the drawable
        T result = valid.get(id);
        if(result == null) {
            result = makeDrawable(val);
            valid.put(id, result);
        }
        return result;
    }

    protected abstract T makeDrawable(String val);
}