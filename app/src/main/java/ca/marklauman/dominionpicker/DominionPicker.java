package ca.marklauman.dominionpicker;

import android.app.Application;

import ca.marklauman.dominionpicker.settings.Pref;

/** Application object used to run one-time scripts when any part of the app starts.
 *  @author Mark Lauman */
public class DominionPicker extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Pref.setup(this);
    }
}