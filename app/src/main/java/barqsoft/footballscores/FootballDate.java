package barqsoft.footballscores;

import android.content.Context;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FootballDate -- class used to get date/time based on offset from current date. Moves logic that
 *     was sprinkled around to one place
 */
public class FootballDate {
    private static final int DAY_MILLISECONDS = 1000 * 60 * 60 * 24;

    private Time mCurrentTime;
    private long mCurrentDateInMillis;
    private long mFootballDateInMillis;
    private Date mFootballDate;

    /**
     * FootballDate -- instantiate class based on relative day (-2 through 2)
     * @param relativeDay
     */
    public FootballDate (int relativeDay) {
        mCurrentTime = new Time();
        mCurrentTime.setToNow();

        mCurrentDateInMillis  = System.currentTimeMillis();
        mFootballDateInMillis = mCurrentDateInMillis +(relativeDay*DAY_MILLISECONDS);
        mFootballDate         = new Date(mFootballDateInMillis);
    }

    /**
     * getFootballDate -- returns YYYY-MM-DD "database friendly" representation of the date
     * @param context
     * @return
     */
    public String getFootballDate (Context context) {
        SimpleDateFormat mFormat = new SimpleDateFormat(
                context.getString(R.string.format_fragment_date));
        return mFormat.format (mFootballDate);
    }

    /**
     * getFootballDate -- returns human-readable "day name" representation of the date
     * @param context
     * @return
     */
    public String getFootballDayName (Context context) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.
        int julianDay = Time.getJulianDay(mFootballDateInMillis, mCurrentTime.gmtoff);
        int currentJulianDay = Time.getJulianDay(mCurrentDateInMillis, mCurrentTime.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        } else if ( julianDay == currentJulianDay -1) {
            return context.getString(R.string.yesterday);
        } else {
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat(
                    context.getString(R.string.format_day_name));
            return dayFormat.format(mFootballDateInMillis);
        }
    }
}

