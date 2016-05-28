package ca.marklauman.dominionpicker.userinterface;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/** Used to load a drawable resource into an ImageView.
 *  Less efficient than Picasso, but works with VectorDrawables.
 *  @author Mark Lauman */
public class DrawableLoader extends AsyncTask<Integer, Void, Drawable> {

    /** Application context (Retrieved from the ImageView) */
    private final Context context;
    /** Weak reference to the ImageView.
     *  This allows the ImageView to be garbage collected if the view is destroyed. */
    private final WeakReference<ImageView> imageView;


    private DrawableLoader(ImageView view) {
        context = view.getContext().getApplicationContext();
        imageView = new WeakReference<>(view);
    }


    /** Create a DrawableLoader that will insert the drawable into the given ImageView. */
    public static DrawableLoader into(ImageView view) {
        return new DrawableLoader(view);
    }

    /** Start loading the given drawable resource and place it in the ImageView. */
    public void place(int drawableResource) {
        execute(drawableResource);
    }

    /** Load the drawable resource on another thread. */
    @Override
    protected Drawable doInBackground(Integer... drawableRes) {
        return ContextCompat.getDrawable(context, drawableRes[0]);
    }

    /** Apply the drawable resource on the UI thread. */
    @Override
    protected void onPostExecute(Drawable drawable) {
        if(imageView == null || drawable == null) return;
        final ImageView view = imageView.get();
        if(view != null) view.setImageDrawable(drawable);
    }
}