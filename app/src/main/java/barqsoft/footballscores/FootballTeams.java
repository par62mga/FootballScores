package barqsoft.footballscores;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.util.HashMap;

/**
 * FootballLeagues -- singleton used to manage team names and map them to team crest resource files.
 *     All of this is configured and managed using "arrays.xml"
 *     TODO: it would be nice to fetch and store team crest URLs from the API and cache/render
 *         directly from SVG at some point
 *
 *     Created by Phil Robertson on 10/1/2015.
 */
public class FootballTeams {
    private final String LOG_TAG = FootballTeams.class.getSimpleName();

    private static FootballTeams mSingleton;

    // mTeamCrests used to retrieve team crests from a drawable typed array
    //     follows recommendation from:
    //         stackoverflow.com/questions/6945678/android-storing-r-drawable-ids-in-xml-array
    private TypedArray              mTeamCrests;
    private HashMap<String,Integer> mTeamHashMap;
    private int                     mCrestNotFound;

    private FootballTeams (Context context) {

        // default icon when no team crest found
        mCrestNotFound  = R.drawable.no_icon;

        // build simple hashmap to look up team crest drawable from team name
        String teamNames[]  = context.getResources().getStringArray(R.array.team_crest_names);
        mTeamCrests = context.getResources().obtainTypedArray(R.array.team_crest_images);
        if (teamNames.length != mTeamCrests.length()) {
            Log.e (LOG_TAG, "Check array XML, invalid array length!");
        }
        mTeamHashMap  = new HashMap<String,Integer> ();
        for (int i = 0; i < teamNames.length; i++) {
            mTeamHashMap.put (teamNames[i], i);
        }
    }

    public static FootballTeams getInstance (Context context) {
        if (mSingleton == null) {
            mSingleton = new FootballTeams (context);
        }
        return mSingleton;
    }

    /**
     * getTeamCrest -- given the team name, fetch and return the matching resource ID
     * @param teamName
     * @return
     */
    public int getTeamCrest (String teamName) {
        Integer teamCrestIndex = (teamName == null) ? null : mTeamHashMap.get(teamName);
        if ( teamCrestIndex ==  null ) {
            Log.d(LOG_TAG, "getTeamCrest (): Team crest not found for ==> " + teamName);
            return mCrestNotFound;
        } else {
            return mTeamCrests.getResourceId(teamCrestIndex, mCrestNotFound);
        }
    }

    /**
     * getFormattedScore -- shared utility to format team scores based on RTL requirements
     * @param context
     * @param homeGoals
     * @param awayGoals
     * @return
     */
    public String getFormattedScore (Context context, int homeGoals, int awayGoals) {
        if ((homeGoals < 0) || (awayGoals < 0)) {
            return context.getString(R.string.format_score_empty);
        } else {
            String formatString;

            if (TextUtils.getLayoutDirectionFromLocale(null) == View.LAYOUT_DIRECTION_RTL) {
                formatString = context.getString(R.string.format_score_rtl);
            } else {
                formatString = context.getString(R.string.format_score);
            }
            return String.format(formatString, homeGoals, awayGoals);
        }
    }
}
