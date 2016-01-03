package ca.marklauman.dominionpicker.cardinfo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.cardadapters.CoinFactory;
import ca.marklauman.dominionpicker.cardadapters.DrawableFactory;
import ca.marklauman.dominionpicker.cardadapters.VPFactory;
import ca.marklauman.tools.XmlTextView;

/** A TextView that displays card info, stored as an xml string.
 *  Is capable of drawing coins, victory points, large versions of these,
 *  horizontal rules, bold and italic.
 *  Created by Mark on 2015-12-27.
 *  @author Mark Lauman */
public class InfoTextView extends XmlTextView {
    private CoinFactory coins;
    private VPFactory vps;

    public InfoTextView(Context context) {
        super(context);
        setup(context, null);
    }

    public InfoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public InfoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InfoTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context, attrs);
    }


    private void setup(Context context, AttributeSet attrSet) {
        Resources res = context.getResources();
        coins = new CoinFactory(res);
        vps = new VPFactory(res);
        setHrRes(R.layout.card_info_hr);
        if (isInEditMode()) setText(R.string.demo_card_text);
    }


    @Override
    protected void sectionStarted() {
        coins.newSpannableView();
        vps.newSpannableView();
    }


    @Override
    protected void tagCompleted(SpannableStringBuilder txt, String tag, int start, int end) {
        switch (tag) {
            case "vp":  inlineDrawable(txt, vps, start, end);   break;
            case "c":   inlineDrawable(txt, coins, start, end); break;
            case "big": txt.setSpan(new RelativeSizeSpan(4.0f), start, end,
                                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);   break;
            case "br":  txt.insert(start, "\n"); break;
            case "b":   txt.setSpan(new StyleSpan(Typeface.BOLD), start, end,
                                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);   break;
            case "i":   txt.setSpan(new StyleSpan(Typeface.ITALIC), start, end,
                                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);   break;
        }
    }

    private void inlineDrawable(SpannableStringBuilder txt, DrawableFactory factory,
                                     int start, int end) {
        CharSequence content = txt.subSequence(start, end);
        if(content.length() == 0) {
            txt.insert(start, " ");
            end++;
        }
        Resources res = mContext.getResources();
        int size = (tagActive("big")) ? res.getDimensionPixelSize(R.dimen.vp_size_large)
                                      : res.getDimensionPixelSize(R.dimen.vp_size_small);
        txt.setSpan(factory.getSpan(content, size),
                    start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }
}