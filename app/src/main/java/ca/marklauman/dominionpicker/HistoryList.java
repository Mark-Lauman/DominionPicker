/* Copyright (c) 2015 Mark Christopher Lauman
 *
 * Licensed under the The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.                                                                  */
package ca.marklauman.dominionpicker;

import android.provider.BaseColumns;

/** Table used to hold the history of all shuffles.
 *  TODO: Currently unused. Add this table ASAP.
 *  @author Mark Lauman                          */
@SuppressWarnings("WeakerAccess")
public class HistoryList {
    /** The name of this table */
    public static final String NAME = "history";

    /** Java Long. Timestamp of the shuffle. */
    public static final String _TIME = BaseColumns._ID;
    /** Boolean. If the shuffle is a favorite or not */
    public static final String _FAV = "favorite";
    /** Boolean. {@code true} if colonies + platinum are in use. */
    public static final String _COSTLY = "costly";
    /** Boolean. {@code true} if shelters are in use. */
    public static final String _SHELTERS = "shelters";
    /** Java Long. The id of the bane card if present.
     *  -1 if no bane card is in this shuffle.      */
    public static final String _BANE = "bane";
    /** Java Long. Prefix for the columns for each card ID
     *  in the shuffle (the card columns are named
     *  {@code _CARD + num} where 0 <= num <= 10.
     *  The value in 10 may be -1 if there is no 11th card) */
    public static final String _CARD = "card_";

    /** SQL Create table statement. */
    public static final String CREATE_TABLE =
            "CREATE TABLE "+ NAME + " ("
                    + _TIME + " INTEGER PRIMARY KEY, "
                    + _FAV + " INTEGER, "
                    + _COSTLY + " INTEGER, "
                    + _SHELTERS + " INTEGER, "
                    + _BANE + " INTEGER, "
                    + _CARD + "0 INTEGER, "
                    + _CARD + "1 INTEGER, "
                    + _CARD + "2 INTEGER, "
                    + _CARD + "3 INTEGER, "
                    + _CARD + "4 INTEGER, "
                    + _CARD + "5 INTEGER, "
                    + _CARD + "6 INTEGER, "
                    + _CARD + "7 INTEGER, "
                    + _CARD + "8 INTEGER, "
                    + _CARD + "9 INTEGER, "
                    + _CARD + "10 INTEGER);";
    /** SQL drop table statement. */
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + NAME;
}