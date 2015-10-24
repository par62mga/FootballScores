package barqsoft.footballscores.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.FootballDate;
import barqsoft.footballscores.FootballTeams;
import barqsoft.footballscores.R;

/**
 * ScoresWidgetService -- handles updating football scores widget remote views. This code was
 *     heavily influenced by the widget implementation found in the Udacity Sunshine app.
 *
 *     Finds football scores for yesterday, today and tomorrow and shows them in the widget remote
 *     view.
 *
 *     Created by Phil Robertson on 10/5/2015.
 */
public class ScoresWidgetService extends RemoteViewsService {
    private final String LOG_TAG = ScoresWidgetService.class.getSimpleName();

    // define projection used to retrieve scores for the widget
    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.SCORES_TABLE + "." + DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID
    };

    // these indices must match the above projection
    private static final int INDEX_ID = 0;
    private static final int INDEX_DATE = 1;
    private static final int INDEX_TIME = 2;
    private static final int INDEX_HOME = 3;
    private static final int INDEX_AWAY = 4;
    private static final int INDEX_HOME_GOALS = 5;
    private static final int INDEX_AWAY_GOALS = 6;
    private static final int INDEX_MATCH_ID = 7;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        // Log.d (LOG_TAG, "onGetViewFactory()");
        return new ScoresViewsFactory();
    }

    public class ScoresViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        // used to get dates/strings used for search/display of football dates
        private FootballDate dateYesterday = null;
        private FootballDate dateToday = null;
        private FootballDate dateTomorrow = null;
        private String       stringYesterday;
        private String       stringToday;
        private String       stringTomorrow;
        private String       dayNameYesterday;
        private String       dayNameToday;
        private String       dayNameTomorrow;

        private Cursor data = null;

        @Override
        public void onCreate() {
            // Nothing to do
        }

        @Override
        public void onDataSetChanged() {
            Log.d(LOG_TAG, "onDataSetChanged()");
            if (data != null) {
                data.close();
            }
            // This method is called by the app hosting the widget (e.g., the launcher)
            // However, our ContentProvider is not exported so it doesn't have access to the
            // data. Therefore we need to clear (and finally restore) the calling identity so
            // that calls use our process and permission
            final long identityToken = Binder.clearCallingIdentity();

            // get dates based on current time in milliseconds
            dateYesterday = new FootballDate(-1);
            dateToday     = new FootballDate(0);
            dateTomorrow  = new FootballDate(1);

            // get strings for yesterday, today and tomorrow
            stringYesterday  = dateYesterday.getFootballDate(ScoresWidgetService.this);
            stringToday      = dateToday.getFootballDate(ScoresWidgetService.this);
            stringTomorrow   = dateTomorrow.getFootballDate(ScoresWidgetService.this);

            // get database friendly date strings for search/comparison
            dayNameYesterday = dateYesterday.getFootballDayName(ScoresWidgetService.this);
            dayNameToday     = dateToday.getFootballDayName(ScoresWidgetService.this);
            dayNameTomorrow  = dateTomorrow.getFootballDayName(ScoresWidgetService.this);

            // search for football scores from yesterday to tomorrow
            // NOTE: matches are already ordered by time, but we should be able to do an order by
            //     clause like date" + ", " + "time" + " ASC" here...
            Uri    scoresByRangeUri = DatabaseContract.scores_table.buildScoreWithRange();
            String selectionArgs[]  = new String[] {
                    stringYesterday,
                    stringTomorrow
            };
            data = getContentResolver().query(
                    scoresByRangeUri, // Uri matched by content provider
                    SCORES_COLUMNS,   // projection
                    null,             // selection clause (inserted by ScoresProvider)
                    selectionArgs,    // selection args
                    DatabaseContract.scores_table.DATE_COL + " ASC"); // order by clause
            Log.d (LOG_TAG, "numberOfRecords ==> " + String.valueOf(data.getCount()));
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            // Log.d(LOG_TAG, "onDestroy()");
            if (data != null) {
                data.close();
                data = null;
            }
        }

        @Override
        public int getCount() {
            // Log.d(LOG_TAG, "getCount()");
            return data == null ? 0 : data.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            // Log.d(LOG_TAG, "getViewAt() position ==> " + String.valueOf(position));
            if (position == AdapterView.INVALID_POSITION ||
                    data == null || !data.moveToPosition(position)) {
                return null;
            }
            FootballTeams teams = FootballTeams.getInstance(ScoresWidgetService.this);
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);

            String formatString;
            String matchDate = data.getString(INDEX_DATE);
            if (matchDate.contentEquals(stringYesterday)) {
                matchDate = dayNameYesterday;
            } else if (matchDate.contentEquals(stringToday)) {
                matchDate = dayNameToday;
            } else if (matchDate.contentEquals(stringTomorrow)) {
                matchDate = dayNameTomorrow;
            }
            String homeName  = data.getString(INDEX_HOME);
            String awayName  = data.getString(INDEX_AWAY);
            int    homeGoals = data.getInt(INDEX_HOME_GOALS);
            int    awayGoals = data.getInt(INDEX_AWAY_GOALS);
            String matchTime = data.getString(INDEX_TIME);

            views.setTextViewText(R.id.widget_day_name, matchDate);
            views.setTextViewText(R.id.widget_home_name, homeName);
            views.setTextViewText(R.id.widget_away_name, awayName);
            views.setTextViewText(R.id.widget_time_textview, matchTime);
            views.setTextViewText(R.id.widget_score_textview,
                    teams.getFormattedScore(ScoresWidgetService.this, homeGoals, awayGoals));
            views.setImageViewResource(R.id.widget_home_crest,
                    teams.getTeamCrest(data.getString(INDEX_HOME)));
            views.setImageViewResource(R.id.widget_away_crest,
                    teams.getTeamCrest(data.getString(INDEX_AWAY)));

            String matchContent = String.format(getString(R.string.format_widget_content),
                    homeName, awayName, matchDate, matchTime);
            String scoreContent = "";
            if ( (homeGoals >= 0) && (awayGoals >= 0)) {
                scoreContent = String.format(getString(R.string.format_score_content),
                        homeGoals, awayGoals);
            }
            views.setContentDescription(R.id.widget_list_item, matchContent + scoreContent);

            // add date and match ID to the intent to open date + detail view...
            final Intent fillInIntent = new Intent();
            Uri matchUri = DatabaseContract.scores_table.buildScoreWithDateAndMatch(
                    data.getString(INDEX_DATE), (int)data.getDouble(INDEX_MATCH_ID));
            fillInIntent.setData(matchUri);
            views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            // Log.d(LOG_TAG, "getLoadingView()");
            return new RemoteViews(getPackageName(), R.layout.widget_list_item);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            // Log.d(LOG_TAG, "getItemId()");
            if (data.moveToPosition(position))
                return data.getLong(INDEX_ID);
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
