package barqsoft.footballscores;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;

/**
 * FootballLeagues -- singleton used to manage league number and name assignments using "arrays.xml"
 *     to configure the app rather than hard-coding this all over various java classes.
 *
 *     Created by Phil Robertson on 10/1/2015.
 */
public class FootballLeagues {
    private final String LOG_TAG = FootballLeagues.class.getSimpleName();

    private static FootballLeagues  mSingleton;

    // define leagues that we fetch, which one is the champion league and a league name hashmap
    private int                     mLeaguesToFetch[];
    private int                     mLeaguesChampions[];
    private HashMap<Integer,String> mLeagueHashMap;
    private String                  mLeagueUnknown;

    private FootballLeagues (Context context) {

        // get configuration resources from strings and arrays.xml
        mLeagueUnknown    = context.getString(R.string.league_unknown);
        mLeaguesToFetch   = context.getResources().getIntArray(R.array.leagues_to_fetch);
        mLeaguesChampions = context.getResources().getIntArray(R.array.league_champions);

        // build league number to league name hashmap
        int    leagueNumbers[]  = context.getResources().getIntArray(R.array.league_numbers);
        String leagueNames[]    = context.getResources().getStringArray(R.array.league_names);
        if (leagueNumbers.length != leagueNames.length) {
            Log.e (LOG_TAG, "Check array XML, invalid array length!");
        }
        mLeagueHashMap  = new HashMap<Integer,String> ();
        for (int i = 0; i < leagueNumbers.length; i++) {
            mLeagueHashMap.put (leagueNumbers[i], leagueNames[i]);
        }
    }

    public static FootballLeagues getInstance (Context context) {
        if (mSingleton == null) {
            mSingleton = new FootballLeagues (context);
        }
        return mSingleton;
    }

    /**
     * fetchLeague -- given the league number, return TRUE if this is a league we are interested in
     * @param leagueNumber
     * @return
     */
    public boolean fetchLeague (String leagueNumber) {
        int number = Integer.valueOf (leagueNumber);
        for (int i = 0; i < mLeaguesToFetch.length; i++) {
            if ( number == mLeaguesToFetch[i] ) {
                return true;
            }
        }
        return false;
    }

    /**
     * championsLeague -- given the league number, return TRUE if this is the champions league and
     *      we show different formatting for the match day
     * @param leagueNumber
     * @return
     */
    public boolean championsLeague (String leagueNumber) {
        int number = Integer.valueOf (leagueNumber);
        for (int i = 0; i < mLeaguesChampions.length; i++) {
            if ( number == mLeaguesChampions[i] ) {
                return true;
            }
        }
        return false;
    }

    /**
     * getLeagueName -- given the league number, return the league name
     * @param leagueNumber
     * @return
     */
    public String getLeagueName (String leagueNumber) {
        String leagueName = mLeagueHashMap.get(Integer.valueOf(leagueNumber));
        if ( leagueName ==  null ) {
            Log.d(LOG_TAG, "getLeagueName (): league number unknown, update array.xml ==> " + leagueNumber);
            leagueName = mLeagueUnknown;
        }
        return leagueName;
    }
}
