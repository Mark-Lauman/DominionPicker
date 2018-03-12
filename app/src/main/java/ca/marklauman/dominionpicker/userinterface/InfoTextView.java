package ca.marklauman.dominionpicker.userinterface;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.userinterface.icons.CoinIcon;
import ca.marklauman.dominionpicker.userinterface.icons.DebtIcon;
import ca.marklauman.dominionpicker.userinterface.icons.Icon;
import ca.marklauman.dominionpicker.userinterface.icons.IconDescriber;
import ca.marklauman.dominionpicker.userinterface.icons.PotionIcon;
import ca.marklauman.dominionpicker.userinterface.icons.VPIcon;
import ca.marklauman.tools.XmlTextView;

/** A TextView that displays card info, stored as an xml string.
 *  Is capable of drawing coins, victory points, large versions of these,
 *  horizontal rules, bold and italic.
 *  Created by Mark on 2015-12-27.
 *  @author Mark Lauman */
public class InfoTextView extends XmlTextView {

    /** Mode used for spans in this XmlTextView (shorthand) */
    private static final int SPAN_MODE = Spannable.SPAN_INCLUSIVE_EXCLUSIVE;

    /** Describer used to describe inline icons */
    private IconDescriber describe;
    /** Current language used in this InfoTextView. */
    private String lang;


    public InfoTextView(Context context) {
        super(context);
        setup(context);
    }

    public InfoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public InfoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }


    private void setup(Context context) {
        setHrRes(R.layout.card_info_hr);
        setTextViewRes(R.layout.card_info_txt);
        if(isInEditMode())
            setText(context.getString(R.string.demo_card_text), "en");
    }


    /** Set the describer used to label icons in this text view */
    public void setDescriber(IconDescriber describer) {
        describe = describer;
    }


    /** Deprecated in favor of {@link #setText(String, String)}. */
    @Override
    @Deprecated
    public void setText(String text) {
        throw new UnsupportedOperationException("Please use setText with a language parameter");
    }


    /** Set the string on display, and the language used to display it. */
    public void setText(String text, String language) {
        lang = language;
        super.setText(text);
    }


    @Override
    protected void sectionStarted() {}


    @Override
    protected void tagCompleted(SpannableStringBuilder txt, String tag, int start, int end) {
        String content = ""+txt.subSequence(start, end);
        switch (tag) {
            case "vp":  inlineIcon(txt, start, end, new VPIcon(getContext(), describe, content));
                        break;
            case "debt":inlineIcon(txt, start, end, new DebtIcon(getContext(), describe, content));
                        break;
            case "c":   inlineIcon(txt, start, end, new CoinIcon(getContext(), describe, content));
                        break;
            case "pot": inlineIcon(txt, start, end, new PotionIcon(getContext(), describe));
                        break;
            case "big": txt.setSpan(new RelativeSizeSpan(3f), start, end, SPAN_MODE);
                        break;
            case "br":  txt.insert(start, "\n");
                        break;
            case "b":   txt.setSpan(new StyleSpan(Typeface.BOLD), start, end, SPAN_MODE);
                        break;
            case "i":   txt.setSpan(new StyleSpan(Typeface.ITALIC), start, end, SPAN_MODE);
                        break;
        }
    }


    /** Applies an icon to the specified range.
     *  @param txt The spannable string that the icon will be applied to.
     *  @param start Index of the first character to be covered by the icon.
     *  @param end Index of the first character NOT covered by the icon after start.
     *  @param icon The icon to place in that range. */
    private void inlineIcon(SpannableStringBuilder txt, int start, int end, Icon icon) {
        // Determine the height & alignment of the icon
        int align = ImageSpan.ALIGN_BASELINE;
        int height = getLineHeight();
        if(tagActive("big")) height *= 3;
        else if(tagActive("b")) {
            height *= 1.2;
            align = ImageSpan.ALIGN_BOTTOM;
        }
        icon.setHeight(height);

        // Apply the icon
        String desc = icon.getDescription(lang);
        txt.replace(start, end, desc);
        txt.setSpan(new ImageSpan(icon, align), start, start+desc.length(), SPAN_MODE);
    }
}