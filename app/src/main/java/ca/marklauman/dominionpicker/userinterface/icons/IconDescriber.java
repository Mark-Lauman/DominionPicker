package ca.marklauman.dominionpicker.userinterface.icons;

import android.content.Context;
import android.content.res.Resources;

import java.util.HashMap;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.tools.Utils;

/** This class provides descriptions for the icons in this package.
 *  Descriptions are suitable for the drawable's ContentDescription
 *  and the hidden text of an ImageSpan.
 *  @author Mark Lauman */
public class IconDescriber {

    /** Resource object used to retrieve descriptions. */
    private Resources res;
    /** Default language used for descriptions. */
    private final String defLanguage;
    /** Unlabelled coin descriptions */
    private final HashMap<String, String> coinEmpty;
    /** Coin plurals mapped to language ids. */
    private final HashMap<String, Integer> coinPlurals;
    /** Unlabelled debt descriptions */
    private final HashMap<String, String> debtEmpty;
    /** Debt plurals mapped to language ids. */
    private final HashMap<String, Integer> debtPlurals;
    /** Unlabeled victory point descriptions */
    private final HashMap<String, String> vpEmpty;
    /** Victory point plurals mapped to language ids. */
    private final HashMap<String, Integer> vpPlurals;
    /** Potion descriptions */
    private final HashMap<String, String> potion;


    /** Icon describer constructor.
     *  Does not function correctly in Edit Mode. Check  */
    public IconDescriber(Context context) {
        res = context.getResources();
        defLanguage = res.getString(R.string.language);
        final String[] lang = res.getStringArray(R.array.language_codes);
        final String[] coinEm = res.getStringArray(R.array.coin);
        final int[]    coinPl = Utils.getResourceArray(context, R.array.format_coin);
        final String[] debtEm = res.getStringArray(R.array.debt);
        final int[]    debtPl = Utils.getResourceArray(context, R.array.format_debt);
        final String[] vpEm   = res.getStringArray(R.array.vp);
        final int[] vpPl   = Utils.getResourceArray(context, R.array.format_vp);
        final String[] pot  = res.getStringArray(R.array.potion);

        coinEmpty   = new HashMap<>(lang.length);
        coinPlurals = new HashMap<>(lang.length);
        debtEmpty   = new HashMap<>(lang.length);
        debtPlurals = new HashMap<>(lang.length);
        vpEmpty     = new HashMap<>(lang.length);
        vpPlurals   = new HashMap<>(lang.length);
        potion      = new HashMap<>(lang.length);

        for(int i=0; i<lang.length; i++) {
            coinEmpty.put(  lang[i], coinEm[i]);
            coinPlurals.put(lang[i], coinPl[i]);
            debtEmpty.put(  lang[i], debtEm[i]);
            debtPlurals.put(lang[i], debtPl[i]);
            vpEmpty.put(    lang[i], vpEm[i]);
            vpPlurals.put(  lang[i], vpPl[i]);
            potion.put(     lang[i], pot[i]);
        }
    }


    /** Create a description for the given debt value.
     *  @param value The value displayed on the debt token. If null or the empty string, will
     *               describe an empty debt token.
     *  @param lang The language that this description should be in. Should be a two-letter
     *              language code, such as "en" or "fr". If null, will be the default language.
     *  @return An accurate description of that debt token. */
    public String forDebt(String value, String lang) {
        if(lang == null) lang = defLanguage;
        if(value == null || value.equals(""))
            return debtEmpty.get(lang);
        return res.getQuantityString(debtPlurals.get(lang), TableCard.parseVal(value), value);
    }


    /** Create a description for the given coin value.
     *  @param value The value displayed on the coin icon. If null or the empty string, will
     *               describe an empty coin.
     *  @param lang The language that this description should be in. Should be a two-letter
     *              language code, such as "en" or "fr". If null, will be the default language.
     *  @return An accurate description of that coin icon. */
    public String forCoin(String value, String lang) {
        if(lang == null) lang = defLanguage;
        if(value == null || value.equals(""))
            return coinEmpty.get(lang);
        return res.getQuantityString(coinPlurals.get(lang), TableCard.parseVal(value), value);
    }


    /** Create a description for the given victory point icon.
     *  @param value The value displayed on the shield. If null or the empty string, will
     *               describe an empty shield.
     *  @param lang The language that this description should be in. Should be a two-letter
     *              language code, such as "en" or "fr". If null, will be the default language.
     *  @return An accurate description of the icon. */
    public String forVp(String value, String lang) {
        if(lang == null) lang = defLanguage;
        if(value == null || value.equals(""))
            return vpEmpty.get(lang);
        return res.getQuantityString(vpPlurals.get(lang), TableCard.parseVal(value), value);
    }

    /** Create a description for the given potion icon.
     *  @param lang The language that this description should be in. Should be a two-letter
     *              language code, such as "en" or "fr". If null, will be the default language.
     *  @return An accurate description of the potion. */
    public String forPotion(String lang) {
        if(lang == null) lang = defLanguage;
        return potion.get(lang);
    }
}