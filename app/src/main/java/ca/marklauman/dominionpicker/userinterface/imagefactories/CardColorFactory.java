package ca.marklauman.dominionpicker.userinterface.imagefactories;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.util.ArrayList;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;
import ca.marklauman.tools.Utils;

/** Builds the background drawables to show card color.
 *  TODO: Replace with alternate version.
 *  @author Mark Lauman */
public class CardColorFactory {

    // Cards types listed by color priority
    /** Action card id */
    private static final int _action = 0;
    /** Treasure card id */
    private static final int _treasure = 1;
    /** Reserve card id */
    private static final int _reserve = 2;
    /** Victory card id */
    private static final int _victory = 3;
    /** Duration card id */
    private static final int _dur = 4;
    /** Reaction card id */
    private static final int _react = 5;
    /** Curse card id */
    private static final int _curse = 6;
    /** Event card id */
    private static final int _event = 7;
    /** Landmark card id */
    private static final int _landmark = 8;

    private static final ImageLibrary lib = new ImageLibrary();

    /** Width of the card color side border */
    private final float width;
    /** Colors used to represent each card type */
    private final int[] color;
    /** Loop variable used to store the colors used by the current card. */
    private final ArrayList<Integer> curColors = new ArrayList<>(2);
    /** Column indexes corresponding to each card type */
    private int[] column;

    public CardColorFactory(Context context) {
        width = context.getResources()
                       .getDimension(R.dimen.card_color_width);

        // Card colors, sorted by priority
        color = new int[9];
        color[_action]   = ContextCompat.getColor(context, R.color.type_act);
        color[_treasure] = ContextCompat.getColor(context, R.color.type_treasure);
        color[_reserve]  = ContextCompat.getColor(context, R.color.type_reserve);
        color[_victory]  = ContextCompat.getColor(context, R.color.type_victory);
        color[_dur]      = ContextCompat.getColor(context, R.color.type_dur);
        color[_react]    = ContextCompat.getColor(context, R.color.type_react);
        color[_curse]    = ContextCompat.getColor(context, R.color.type_curse);
        color[_event]    = ContextCompat.getColor(context, R.color.type_event);
        color[_landmark] = ContextCompat.getColor(context, R.color.type_landmark);
    }


    public void changeCursor(Cursor cursor) {
        // Columns sorted by color priority
        column = new int[9];
        column[_action] = cursor.getColumnIndex(TableCard._TYPE_ACT);
        column[_treasure] = cursor.getColumnIndex(TableCard._TYPE_TREAS);
        column[_reserve] = cursor.getColumnIndex(TableCard._TYPE_RESERVE);
        column[_victory] = cursor.getColumnIndex(TableCard._TYPE_VICTORY);
        column[_dur] = cursor.getColumnIndex(TableCard._TYPE_DUR);
        column[_react] = cursor.getColumnIndex(TableCard._TYPE_REACT);
        column[_curse] = cursor.getColumnIndex(TableCard._TYPE_CURSE);
        column[_event] = cursor.getColumnIndex(TableCard._TYPE_EVENT);
        column[_landmark] = cursor.getColumnIndex(TableCard._TYPE_LANDMARK);
    }


    public void updateBackground(@NonNull View view, @NonNull Cursor cursor) {
        if(column == null) {
            view.setBackgroundResource(android.R.color.transparent);
            return;
        }
        curColors.clear();

        /* Some types of Action cards do not use the default Action color.
         * If they have their own color, then action will be set to false later. */
        boolean action = cursor.getInt(column[_action])!=0;

        // Check each card type and add its color to curColors.
        // Some card types will also disable the default action color.
        if(cursor.getInt(column[_treasure])!=0) {
            curColors.add(_treasure);
        } if(cursor.getInt(column[_reserve])!=0) {
            curColors.add(_reserve);
            action = false;
        } if(cursor.getInt(column[_victory])!=0) {
            curColors.add(_victory);
        } if(cursor.getInt(column[_dur])!=0) {
            curColors.add(_dur);
            action = false;
        } if(cursor.getInt(column[_react])!=0) {
            curColors.add(_react);
            action = false;
        } if(cursor.getInt(column[_curse])!=0)
            curColors.add(_curse);
        if(cursor.getInt(column[_event])!=0)
            curColors.add(_event);
        if(cursor.getInt(column[_landmark])!=0)
            curColors.add(_landmark);

        String val = Utils.join(",", curColors);
        if(action) val = (val.length() == 0) ? ""+_action : _action+","+val;
        if(val.length() == 0) val = ""+_action;

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            view.setBackgroundDrawable(getDrawable(val, (int)(width+0.5f)));
        } else view.setBackground(getDrawable(val, (int)(width+0.5f)));
    }


    private Drawable getDrawable(CharSequence value, int size) {
        Drawable res = lib.getDrawable(value, size);
        if(res == null)
            res = makeDrawable(value, size);
        return res;
    }


    private synchronized CardBackground makeDrawable(CharSequence value, int size) {
        String str = ""+value;
        String[] split = str.split(",");
        int[] colors = new int[split.length];
        for(int i=0; i<split.length; i++)
            colors[i] = color[Integer.parseInt(split[i])];
        CardBackground res = new CardBackground(colors);
        lib.setDrawable(value, size, res);
        return res;
    }


    public class CardBackground extends Drawable {
        /** List of colors that this card has */
        private final int[] mColors;
        /** Paint object used to draw the background */
        private final Paint paint = new Paint();

        CardBackground(int... colors) {
            mColors = colors;
        }


        @Override
        public void draw(@NonNull Canvas canvas) {
            if(mColors == null || mColors.length < 1) return;

            // width is determined by the factory
            float height = getBounds().height();
            float divWidth = width / mColors.length;

            for(int i=0; i<mColors.length; i++) {
                paint.setColor(mColors[i]);
                canvas.drawRect(i*divWidth, 0, (i+1)*divWidth, height, paint);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
            invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            paint.setColorFilter(cf);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }

}