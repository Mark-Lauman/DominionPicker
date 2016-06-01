package ca.marklauman.dominionpicker.userinterface;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/** Drawable resources take a while to load, but {@link Context#getDrawable(int)}
 *  caches drawables to make it load faster in the future.
 *  This calls {@link Context#getDrawable(int)} on each drawable passed
 *  so they will be available quickly if needed.
 *  @author Mark Lauman */
public class DrawableLoader extends AsyncTask<Integer, Void, Void> {

    /** Application context (Retrieved from the ImageView) */
    private final Context context;


    private DrawableLoader(Context context) {
        this.context = context.getApplicationContext();
    }


    public static void load(Context context, Integer... resIds) {
        DrawableLoader loader = new DrawableLoader(context);
        loader.execute(resIds);
    }


    public static void load(Context context, int... resIds) {
        Integer[] newIds = new Integer[resIds.length];
        for(int i=0; i<resIds.length; i++)
            newIds[i] = resIds[i];
        load(context, newIds);
    }


    /** Load the drawable resource on another thread. */
    @Override
    protected Void doInBackground(Integer... resIds) {
        if(resIds != null)
            for(Integer resId : resIds)
                ContextCompat.getDrawable(context, resId);
        return null;
    }
}