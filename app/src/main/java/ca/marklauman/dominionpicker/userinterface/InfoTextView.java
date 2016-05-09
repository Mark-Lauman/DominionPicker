package ca.marklauman.dominionpicker.userinterface;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.userinterface.imagefactories.CoinFactory;
import ca.marklauman.dominionpicker.userinterface.imagefactories.ImageFactory;
import ca.marklauman.dominionpicker.userinterface.imagefactories.VPFactory;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.tools.Utils;
import ca.marklauman.tools.XmlTextView;

/** A TextView that displays card info, stored as an xml string.
 *  Is capable of drawing coins, victory points, large versions of these,
 *  horizontal rules, bold and italic.
 *  Created by Mark on 2015-12-27.
 *  @author Mark Lauman */
public class InfoTextView extends XmlTextView {
    /** Factory used to build the coin icons */
    private CoinFactory coins;
    /** Factory used to build the victory point icons */
    private VPFactory vps;


    /** String used for empty coin icons */
    private String coinStr;
    /** Plural resource used for full coin icons */
    private int coinPlural = 0;
    /** String used for empty vp icons */
    private String vpStr;
    /** Plural resource used for full vp icons */
    private int vpPlural = 0;
    /** String used for empty potion icons */
    private String potStr;
    /** Plural resource used for full potion icons */
    private int potPlural = 0;


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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InfoTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context);
    }

    /** Deprecated in favor of {@link #setText(String, String)}. */
    @Override
    @Deprecated
    public void setText(String text) {
        throw new UnsupportedOperationException("Please use setText with a language parameter");
    }


    /** Set the string on display, and the language used to display it. */
    public void setText(String text, String language) {
        Context c = getContext();
        Resources res = getResources();

        int langId = -1;
        String[] langCodes = res.getStringArray(R.array.language_codes);
        for(int i=0; i<langCodes.length; i++) {
            if(langCodes[i].equals(language)) {
                langId = i; break;
            }
        }
        if(langId == -1) return;

        // Utils.getResourceArray does not work in edit mode
        if(!isInEditMode()) {
            coinStr    = res.getStringArray(R.array.coin)[langId];
            coinPlural = Utils.getResourceArray(c, R.array.format_coin)[langId];
            vpStr      = res.getStringArray(R.array.vp)[langId];
            vpPlural   = Utils.getResourceArray(c, R.array.format_vp)[langId];
            potStr     = res.getStringArray(R.array.potion)[langId];
            potPlural  = Utils.getResourceArray(c, R.array.format_potion)[langId];
        }

        super.setText(text);
    }


    private void setup(Context context) {
        Resources res = context.getResources();
        coins = new CoinFactory(res);
        vps = new VPFactory(res);
        setHrRes(R.layout.card_info_hr);
        setTextViewRes(R.layout.card_info_txt);
        if (isInEditMode()) setText(context.getString(R.string.demo_card_text),
                                    "en");
    }


    @Override
    protected void sectionStarted() {
        coins.newSpannableView();
        vps.newSpannableView();
    }


    @Override
    protected void tagCompleted(SpannableStringBuilder txt, String tag, int start, int end) {
        switch (tag) {
            case "vp":  inlineDrawable(txt, start, end, vps,
                                       vpPlural, vpStr);   break;
            case "c":   inlineDrawable(txt, start, end, coins,
                                       coinPlural, coinStr); break;
            case "pot": inlineDrawable(txt, start, end, R.drawable.ic_dom_potion,
                                       potPlural, potStr); break;
            case "big": txt.setSpan(new RelativeSizeSpan(3f), start, end,
                                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);   break;
            case "br":  txt.insert(start, "\n"); break;
            case "b":   txt.setSpan(new StyleSpan(Typeface.BOLD), start, end,
                                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);   break;
            case "i":   txt.setSpan(new StyleSpan(Typeface.ITALIC), start, end,
                                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);   break;
        }
    }


    /** Apply an ImageSpan from the given factory to the txt */
    private void inlineDrawable(SpannableStringBuilder txt, int start, int end,
                                ImageFactory factory, int pluralRes, String single) {
        CharSequence content = txt.subSequence(start, end);
        Resources res = getResources();
        int size;
        if(tagActive("big")) size = res.getDimensionPixelSize(R.dimen.drawable_size_large);
        else size = (tagActive("b")) ? res.getDimensionPixelSize(R.dimen.drawable_size_med)
                                     : res.getDimensionPixelSize(R.dimen.drawable_size_small);
        inlineDrawable(txt, start, end,
                       factory.getSpan(content, size), pluralRes, single);
    }


    /** Apply an ImageSpan from a given drawable resource to the txt */
    private void inlineDrawable(SpannableStringBuilder txt, int start, int end,
                                int drawRes, int pluralRes, String single) {
        Resources res = getResources();
        ImageSpan span = new ImageSpan(getContext(), drawRes);
        int size;
        if(tagActive("big")) size = res.getDimensionPixelSize(R.dimen.drawable_size_large);
        else size = (tagActive("b")) ? res.getDimensionPixelSize(R.dimen.drawable_size_med)
                                     : res.getDimensionPixelSize(R.dimen.drawable_size_small);
        span.getDrawable().setBounds(0, 0, size, size);
        inlineDrawable(txt, start, end, span, pluralRes, single);
    }


    /** Apply an ImageSpan to the txt */
    private void inlineDrawable(SpannableStringBuilder txt, int start, int end,
                                ImageSpan span, int pluralRes, String single) {
        Resources res = getResources();
        CharSequence content = txt.subSequence(start, end);
        String newContent = ""+content;
        if(pluralRes != 0 && single != null) {
            newContent = (content.length()==0)
                    ? single
                    : res.getQuantityString(pluralRes, TableCard.parseVal(""+content), content);
        }
        if(newContent.length() == 0) newContent = "_";
        txt.replace(start, end, newContent);
        txt.setSpan(span, start, start+newContent.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }
}