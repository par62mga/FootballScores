package barqsoft.footballscores;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseContract
{
    public static final String SCORES_TABLE = "scores_table";

    public static final class scores_table implements BaseColumns
    {
        //Moved column numbers here to make it clear what order columns are inserted into table
        //Define column numbers (must match ScoresDBHelper database create column order)
        public static final int COL_DATE = 1;
        public static final int COL_MATCHTIME = 2;
        public static final int COL_HOME = 3;
        public static final int COL_AWAY = 4;
        public static final int COL_LEAGUE = 5;
        public static final int COL_HOME_GOALS = 6;
        public static final int COL_AWAY_GOALS = 7;
        public static final int COL_ID = 8;
        public static final int COL_MATCHDAY = 9;
        public static final int COL_HOME_URL = 10;
        public static final int COL_AWAY_URL = 11;

        //Table data
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_COL = "home";
        public static final String AWAY_COL = "away";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_ID = "match_id";
        public static final String MATCH_DAY = "match_day";
        public static final String HOME_URL_COL = "home_url";
        public static final String AWAY_URL_COL = "away_url";

        //public static Uri SCORES_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH)
                //.build();

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        public static Uri buildScoreWithLeague()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("league").build();
        }
        public static Uri buildScoreWithId()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("id").build();
        }
        public static Uri buildScoreWithDate()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("date").build();
        }

        //Added buildScoreWithRange to support queries between to dates
        public static Uri buildScoreWithRange(){
            return BASE_CONTENT_URI.buildUpon().appendPath("range").build();
        }

        //Added buildScoreWithDateAndMatch to support launching activity with these arguments
        public static Uri buildScoreWithDateAndMatch(String date, int match) {
            return BASE_CONTENT_URI.buildUpon()
                .appendPath(date)
                .appendPath(String.valueOf(match))
                .build();
        }

        public static String getDateFromUri (Uri uri) {
            return uri.getPathSegments().get(0);
        }

        public static int getMatchFromUri (Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }
    //URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";
    public static final String PATH = "scores";
    public static Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);
}
