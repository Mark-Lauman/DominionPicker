package ca.marklauman.dominionpicker.community;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.tools.preferences.IntentPreference;

/** Controls the "Community" screen in the Options, and all sub-screens.
 *  @author Mark Lauman */
public class ActivityComm extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setDisplayHomeAsUpEnabled(true);

        switch(getIntent().getIntExtra(IntentPreference.EXTRA, 0)) {
            case R.string.contrib:
                setTitle(R.string.contrib);
                setContentView(R.layout.activity_comm_contrib);
                break;
            case R.string.open_source:
                setTitle(R.string.open_source);
                setContentView(R.layout.activity_comm_open);
                findViewById(R.id.open_apache)
                        .setOnClickListener(new WebListener(R.string.apache_url));
                findViewById(R.id.open_sql)
                        .setOnClickListener(new WebListener(R.string.open_sql_url));
                findViewById(R.id.open_list)
                        .setOnClickListener(new WebListener(R.string.open_list_url));
                findViewById(R.id.open_picasso)
                        .setOnClickListener(new WebListener(R.string.open_picasso_url));
                break;
            default:
                setTitle(R.string.community);
                setContentView(R.layout.activity_comm);
                findViewById(R.id.email)
                        .setOnClickListener(new EmailListener());
                findViewById(R.id.github)
                        .setOnClickListener(new WebListener(R.string.github_url));
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(android.R.id.home == item.getItemId()) {
            finish();
            return true;
        } else return super.onOptionsItemSelected(item);
    }


    /** Used to send an email to Mark. */
    private class EmailListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("message/rfc822");
            intent.setData(Uri.parse("mailto:" + Uri.encode("android@marklauman.ca")
                    + "?subject=" + Uri.encode(getString(R.string.app_name))));
            startActivity(Intent.createChooser(intent, getString(R.string.email_mark)));
        }
    }


    /** Opens the web browser to the given url */
    private class WebListener implements View.OnClickListener {
        private Uri uri;

        WebListener(int urlRes) {
            uri = Uri.parse(getString(urlRes));
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivity(Intent.createChooser(intent, getString(R.string.email_mark)));
        }
    }
}