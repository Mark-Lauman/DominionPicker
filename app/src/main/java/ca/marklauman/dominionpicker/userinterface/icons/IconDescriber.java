package ca.marklauman.dominionpicker.userinterface.icons;

import android.content.Context;
import android.content.Intent;
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
    private final Resources res;
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
     *  Does not function correctly in edit mode. Check this before calling. */
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
     *  @param value The value on display inside this token. Null/empty for an empty token.
     *  @param lang The two-letter code for this language (eg. "fr", "en", etc).
     *  @return The debt token description for the given language.
     *          If the language was not found, the default language is used instead. */
    public String forDebt(String value, String lang) {
        return getString(value, lang, debtEmpty, debtPlurals);
    }


    /** Create a description for the given coin value.
     *  @param value The value on display inside this coin. Null/empty for an empty coin.
     *  @param lang The two-letter code for this language (eg. "fr", "en", etc).
     *  @return The coin description for the given language.
     *          If the language was not found, the default language is used instead. */
    public String forCoin(String value, String lang) {
        return getString(value, lang, coinEmpty, coinPlurals);
    }


    /** Create a description for the given victory point icon.
     *  @param value The value on display inside the shield. Null/empty for an empty shield.
     *  @param lang The two-letter code for this language (eg. "fr", "en", etc).
     *  @return The victory point description for the given language.
      *         If the language was not found, the default language is used instead. */
    public String forVp(String value, String lang) {
        return getString(value, lang, vpEmpty, vpPlurals);
    }

    /** Create a description for the given potion icon.
     *  @param lang The two-letter code for this language (eg. "fr", "en", etc).
     *  @return The potion description for the given language.
     *          If the language was not found, the default language is used instead. */
    public String forPotion(String lang) {
        return getString(null, lang, potion, null);
    }


    /** Used internally to fetch descriptions.
     *  @param value The value on display inside the icon. Null/empty for an empty icon.
     *  @param lang The two-letter code for this language (eg. "fr", "en", etc).
     *  @param empty The map of all the descriptions for an empty icon.
     *  @param plurals The map of all descriptions for a full icon.
     *  @return The icon description for the given language.
     *          If the language was not found, the default language is used instead. */
    private String getString(String value, String lang,
                             HashMap<String, String> empty, HashMap<String, Integer> plurals) {
        if(lang == null) lang = defLanguage;
        String desc;
        if(value == null || value.equals("")) {
            // Retrieve description for empty icons
            desc = empty.get(lang);
            if(desc == null) desc = empty.get(defLanguage);
            return desc;
        } else {
            // Retrieve description for full icons
            Integer plural = plurals.get(lang);
            if(plural == null) plurals.get(defLanguage);
            return res.getQuantityString((plural == null) ? 0 : plural,
                                         TableCard.parseVal(value), value);
        }
    }
}