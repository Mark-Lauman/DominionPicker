package ca.marklauman.dominionpicker.database;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/** Utility class used to format timestamps into a format that is visually pleasing.
 *  @author Mark Lauman */
public class TimestampFormatter {
    /** Formatter used to display date & time. */
    private final DateFormat dtFormat;
    /** Formatter used to check the date is today. */
    private final DateFormat dFormat;
    /** Formatter used to display the time without date. */
    private final DateFormat tFormat;
    /** Today as a string */
    private final String today;

    /** Date object used to store the time as it is being formatted */
    private final Date time;


    public TimestampFormatter() {
        Locale loc = Locale.getDefault();
        dtFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, loc);
        dFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, loc);
        tFormat = DateFormat.getTimeInstance(DateFormat.SHORT, loc);
        time = GregorianCalendar.getInstance().getTime();
        today = dFormat.format(time);
    }

    public String format(long milliseconds) {
        time.setTime(milliseconds);
        String date = dFormat.format(time);
        return (date.equals(today)) ? tFormat.format(time)
                                    : dtFormat.format(time);
    }

    public String formatShort(long milliseconds) {
        time.setTime(milliseconds);
        String date = dFormat.format(time);
        return (date.equals(today)) ? tFormat.format(time)
                                    : date;
    }

    public String formatLong(long milliseconds) {
        time.setTime(milliseconds);
        return dtFormat.format(time);
    }
}