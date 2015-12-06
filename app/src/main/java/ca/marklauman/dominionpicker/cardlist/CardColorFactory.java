package ca.marklauman.dominionpicker.cardlist;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

import ca.marklauman.dominionpicker.R;
import ca.marklauman.dominionpicker.database.TableCard;

/** Builds the background drawables to show card color.
 *  @author Mark Lauman */
class CardColorFactory {

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

    /** 8dp in the current context */
    private final float dp8;
    /** Colors used to represent each card type */
    private final int[] color;
    /** Column indexes corresponding to each card type */
    private final int[] column;
    /** Loop variable used to store the colors used by the current card. */
    private final ArrayList<Integer> curColors = new ArrayList<>(2);

    public CardColorFactory(Resources res, @NonNull Cursor cardList) {
        dp8 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                                        res.getDisplayMetrics());

        // Card colors, sorted by priority
        color = new int[8];
        color[_action] = res.getColor(R.color.type_act);
        color[_treasure] = res.getColor(R.color.type_treasure);
        color[_reserve] = res.getColor(R.color.type_reserve);
        color[_victory] = res.getColor(R.color.type_victory);
        color[_dur] = res.getColor(R.color.type_dur);
        color[_react] = res.getColor(R.color.type_react);
        color[_curse] = res.getColor(R.color.type_curse);
        color[_event] = res.getColor(R.color.type_event);

        // Columns sorted by color priority
        column = new int[8];
        column[_action] = cardList.getColumnIndex(TableCard._TYPE_ACT);
        column[_treasure] = cardList.getColumnIndex(TableCard._TYPE_TREAS);
        column[_reserve] = cardList.getColumnIndex(TableCard._TYPE_RESERVE);
        column[_victory] = cardList.getColumnIndex(TableCard._TYPE_VICTORY);
        column[_dur] = cardList.getColumnIndex(TableCard._TYPE_DUR);
        column[_react] = cardList.getColumnIndex(TableCard._TYPE_REACT);
        column[_curse] = cardList.getColumnIndex(TableCard._TYPE_CURSE);
        column[_event] = cardList.getColumnIndex(TableCard._TYPE_EVENT);
    }


    public synchronized void updateBackground(@NonNull View view, @NonNull Cursor cursor) {
        curColors.clear();

        /* Some types of Action cards do not use the default Action color.
         * If they have their own color, then action will be set to false later. */
        boolean action = cursor.getInt(column[_action])!=0;

        // Check each card type and add its color to curColors.
        // Some card types will also disable the default action color.
        if(cursor.getInt(column[_treasure])!=0) {
            curColors.add(color[_treasure]);
        } if(cursor.getInt(column[_reserve])!=0) {
            curColors.add(color[_reserve]);
            action = false;
        } if(cursor.getInt(column[_victory])!=0) {
            curColors.add(color[_victory]);
        } if(cursor.getInt(column[_dur])!=0) {
            curColors.add(color[_dur]);
            action = false;
        } if(cursor.getInt(column[_react])!=0) {
            curColors.add(color[_react]);
            action = false;
        } if(cursor.getInt(column[_curse])!=0)
            curColors.add(color[_curse]);
        if(cursor.getInt(column[_event])!=0)
            curColors.add(color[_event]);

        // Build an array of current colors.
        int offset = 0;
        int[] colors;
        if(action) {
            colors = new int[curColors.size()+1];
            colors[0] = color[_action];
            offset = 1;
        } else colors = new int[curColors.size()];
        for(int i=0; i+offset<colors.length; i++)
            colors[i+offset] = curColors.get(i);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            view.setBackgroundDrawable(new CardBackground(colors));
        else view.setBackground(new CardBackground(colors));
    }


    /** Get the typeAction column from the underlying cursor */
    public int getActionColumn() {
        return column[_action];
    }


    private class CardBackground extends Drawable {
        /** List of colors that this card has */
        private final int[] mColors;
        /** Paint object used to draw the background */
        private final Paint paint = new Paint();

        public CardBackground(int... colors) {
            mColors = colors;
        }

        @Override
        public void draw(Canvas canvas) {
            if(mColors == null || mColors.length < 1) return;

            float height = getBounds().height();
            float width = getBounds().width();
            float divWidth = width / mColors.length;

            // Draw the first color
            paint.setColor(mColors[0]);
            canvas.drawRect(0, 0, divWidth - dp8, height, paint);

            // Draw any other colors
            for(int i=1; i<mColors.length; i++) {
                paint.setShader(new LinearGradient(i*divWidth - dp8, 0, i*divWidth + dp8, 0,
                                mColors[i - 1], mColors[i], Shader.TileMode.MIRROR));
                canvas.drawRect(i*divWidth - dp8, 0, i*divWidth + dp8, height, paint);
                paint.setShader(null);
                paint.setColor(mColors[i]);
                canvas.drawRect(i*divWidth+ dp8, 0, (i+1)*divWidth - dp8, height, paint);
            }

            // Draw the last transition
            canvas.drawRect(width - dp8, 0, width, height, paint);
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
            return PixelFormat.TRANSLUCENT;
        }
    }

}