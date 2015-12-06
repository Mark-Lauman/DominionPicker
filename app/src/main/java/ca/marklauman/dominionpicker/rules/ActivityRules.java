package ca.marklauman.dominionpicker.rules;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import ca.marklauman.dominionpicker.R;

/** TODO: Delete this class
 *  Temporary activity used to hold the new rules fragment */
public class ActivityRules extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout swapPanel = new FrameLayout(this);
        swapPanel.setId(R.id.content_frame);
        setContentView(swapPanel);

        FragmentRules frag = new FragmentRules();
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.content_frame, frag)
                                   .commit();
    }
}