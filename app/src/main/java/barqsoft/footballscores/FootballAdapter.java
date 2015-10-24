package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Phil Robertson on 10/24/2015.
 */
public class FootballAdapter extends CursorAdapter {
    private final String LOG_TAG = FootballAdapter.class.getSimpleName ();

    // move selected match logic away from activity as matches may be selected on each page
    private int  mSelectedMatchId;
    private View mSelectedMatchView;

    // added initial match selection to restore on screen rotation
    public FootballAdapter(Context context, Cursor cursor, int flags, int selectedMatchId) {
        super(context,cursor,flags);
        mSelectedMatchId   = selectedMatchId;
        mSelectedMatchView = null;
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        View       view   = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        //Log.v(LOG_TAG,"new View inflated");
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        FootballTeams    teams  = FootballTeams.getInstance(context);
        FootballLeagues leagues = FootballLeagues.getInstance(context);

        ViewHolder holder = (ViewHolder) view.getTag();

        String homeName  = cursor.getString(DatabaseContract.scores_table.COL_HOME);
        String awayName  = cursor.getString(DatabaseContract.scores_table.COL_AWAY);
        int    homeGoals = cursor.getInt(DatabaseContract.scores_table.COL_HOME_GOALS);
        int    awayGoals = cursor.getInt(DatabaseContract.scores_table.COL_AWAY_GOALS);
        String matchTime = cursor.getString(DatabaseContract.scores_table.COL_MATCHTIME);
        String leagueNum = cursor.getString(DatabaseContract.scores_table.COL_LEAGUE);
        String leagueName= leagues.getLeagueName(leagueNum);
        String matchDay  = getMatchDay(
                context, cursor.getInt(DatabaseContract.scores_table.COL_MATCHDAY), leagueNum);
        //String homeUrl   = cursor.getString(DatabaseContract.scores_table.COL_HOME_URL);
        //String awayUrl   = cursor.getString(DatabaseContract.scores_table.COL_AWAY_URL);

        holder.home_name.setText (homeName);
        holder.away_name.setText (awayName);
        holder.date.setText (matchTime);
        holder.score.setText(teams.getFormattedScore(context, homeGoals, awayGoals));

        holder.home_crest.setImageResource (teams.getTeamCrest(cursor.getString(DatabaseContract.scores_table.COL_HOME)));
        holder.away_crest.setImageResource (teams.getTeamCrest(cursor.getString(DatabaseContract.scores_table.COL_AWAY)));
        //new DownloadSVG(holder.home_crest).execute(homeUrl);
        //new DownloadSVG(holder.away_crest).execute(awayUrl);

        // save match ID, day and league to support expanding/collapsing detail views
        holder.setMatchId((int) cursor.getDouble(DatabaseContract.scores_table.COL_ID));
        holder.setMatchDay(matchDay);
        holder.setLeagueName(leagueName);

        // get content descriptions for team names, scores and leage/match day
        holder.setMatchContent(String.format(context.getString(R.string.format_match_content),
                homeName, awayName, matchTime));
        if ( (homeGoals >= 0) && (awayGoals >= 0)) {
            holder.setScoreContent(String.format(context.getString(R.string.format_score_content),
                    homeGoals, awayGoals));
        }
        holder.setExtraContent(String.format(context.getString(R.string.format_extra_content),
                leagueName, matchDay));
        view.setContentDescription(context.getString(R.string.content_score) + holder.getContent());

        //Log.v(LOG_TAG,holder.home_name.getText() + " Vs. " +
        //      holder.away_name.getText() +" id " + String.valueOf(holder.match_id));
        //Log.v(LOG_TAG,String.valueOf(detail_match_id));

        // see if we need to inflate the extra view
        if (holder.getMatchId() == mSelectedMatchId) {
            showMatchDetailView(context, view);
        } else {
            // fixes an issues where sometimes on rotate, detail shown when not selected...
            // perhaps the view is being recycled and not "deflated"???
            ViewGroup  container = (ViewGroup)view.findViewById(R.id.details_fragment_container);
            container.removeAllViews();
        }
    }

    /**
     * getMatchDay -- moved logic from Utilities as it's only needed here. Also changed code to
     *     use configuration from xml file (leagues) rather than hard-coded strings.
     * @param context
     * @param match_day
     * @param league_num
     * @return String - match day description based on league type and day
     */
    private String getMatchDay(Context context, int match_day, String league_num) {
        FootballLeagues leagues = FootballLeagues.getInstance(context);

        // make sure arrays.xml identifies the correct "champions league"
        if (leagues.championsLeague(league_num)) {
            if (match_day <= 6) {
                return context.getString(R.string.match_day_champions_6);
            } else if(match_day == 7 || match_day == 8) {
                return context.getString(R.string.match_day_champions_knockout);
            } else if(match_day == 9 || match_day == 10) {
                return context.getString(R.string.match_day_champions_quarterfinal);
            } else if(match_day == 11 || match_day == 12) {
                return context.getString(R.string.match_day_champions_semifinal);
            } else {
                return context.getString(R.string.match_day_champions_final);
            }
        } else {
            return String.format(context.getString(R.string.format_match_day), match_day);
        }
    }

    /**
     * showMatchDetailView -- inflates match detail and saves reference to view and match ID
     * @param context
     * @param view
     */
    private void showMatchDetailView (final Context context, View view) {

        final ViewHolder holder   = (ViewHolder) view.getTag();
        LayoutInflater inflater   = (LayoutInflater)context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View           detailView = inflater.inflate(R.layout.detail_fragment, null);
        ViewGroup      container  = (ViewGroup)view.findViewById(R.id.details_fragment_container);

        // Log.v (LOG_TAG, "showMatchDetailView()" + String.valueOf(holder.getMatchId()));
        container.addView(
                detailView,
                0,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        ((TextView)detailView.findViewById(R.id.matchday_textview)).setText(holder.getMatchDay());
        ((TextView) detailView.findViewById(R.id.league_textview)).setText(holder.getLeagueName());
        view.setContentDescription(
                context.getString(R.string.content_detail) + holder.getDetailContent());
        ((Button)detailView.findViewById(R.id.share_button)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //add Share Action
                        String formatString;
                        // probably overkill, but support RTL in share text
                        if (TextUtils.getLayoutDirectionFromLocale(null) == View.LAYOUT_DIRECTION_RTL) {
                            formatString = context.getString(R.string.format_share_rtl);
                        } else {
                            formatString = context.getString(R.string.format_share);
                        }
                        String shareText = String.format(
                                formatString,
                                holder.home_name.getText(),
                                holder.score.getText(),
                                holder.away_name.getText(),
                                context.getString (R.string.share_hash_tag));
                        context.startActivity(
                                createShareForecastIntent(context.getString(R.string.share_type),
                                        shareText));
                    }
                });

        mSelectedMatchId   = holder.getMatchId ();
        mSelectedMatchView = view;
    }

    private Intent createShareForecastIntent(String shareType, String shareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType(shareType);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        return shareIntent;
    }

    /**
     * hideMatchDetailView -- hides presently selected match detail
     * @param context
     * @param view
     */
    private void hideMatchDetailView (final Context context, View view) {
        ViewHolder holder    = (ViewHolder) view.getTag();
        ViewGroup  container = (ViewGroup)view.findViewById(R.id.details_fragment_container);

        container.removeAllViews();
        if (holder.getMatchId() == mSelectedMatchId) {
            mSelectedMatchId   = 0;
            mSelectedMatchView = null;
        }
    }

    /**
     * getSelectedMatchId -- used by fragment to get presently selected match for saving state
     * @return int -- currently selected match or zero
     */
    public int getSelectedMatchId () {
        return mSelectedMatchId;
    }


    /**
     * changeMatchDetailView -- used by onClickListner to change selected match to this view
     * @param context
     * @param view
     */
    public void changeMatchDetailView (final Context context, View view) {
        hideActiveDetailView(context);
        showMatchDetailView(context, view);
    }

    /**
     * hideActiveDetailView -- assists in back handling, hiding active match detail shown
     * @param context
     * @return TRUE when active detail was found and hidden
     */
    public boolean hideActiveDetailView(final Context context) {
        if ( mSelectedMatchView != null ) {
            hideMatchDetailView (context, mSelectedMatchView);
            return true;
        }
        return false;
    }

}
