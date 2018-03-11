package ca.marklauman.dominionpicker.community;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;

import ca.marklauman.dominionpicker.R;

/** A simple button for sending emails to me.
 *  @author Mark Lauman */
public class EmailButton extends AppCompatButton implements View.OnClickListener {
    /** Who this message is for. */
    private static final String target = "android@marklauman.ca";
    /** Error message to display if there is no email client set up. */
    private final static int error_msg = R.string.no_email;
    /** Subject of the email composted by this email. */
    private String subject = "Dominion Card #123";

    public EmailButton(Context context) {
        super(context);
        setOnClickListener(this);
    }

    public EmailButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    public EmailButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("message/rfc822");
        intent.setData(Uri.parse("mailto:"+Uri.encode(target)
                                 +"?subject="+Uri.encode(subject)));
        Context c = getContext();
        try {
            c.startActivity(Intent.createChooser(intent, getText()));
        } catch (android.content.ActivityNotFoundException ex) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getText());
            builder.setMessage(error_msg);
            builder.create().show();
        }
    }

    /** Set the subject of the email sent by this button */
    public void setSubject(String subject) {
        this.subject = subject;
    }
}
