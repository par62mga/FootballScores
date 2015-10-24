package barqsoft.footballscores;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ViewHolder
{
    // make view field references immutable to protect access while avoiding extra overhead of get methods
    public final TextView home_name;
    public final TextView away_name;
    public final TextView score;
    public final TextView date;

    public final ImageView home_crest;
    public final ImageView away_crest;

    // this really does not belong here, but at least access is cleaned up with getters/setters
    private int    mMatchId;
    private String mMatchDay;
    private String mLeagueName;

    // content descriptions
    private String mMatchContent = null;
    private String mScoreContent = null;
    private String mExtraContent = null;

    public ViewHolder (View view) {
        home_name = (TextView) view.findViewById(R.id.home_name);
        away_name = (TextView) view.findViewById(R.id.away_name);
        score     = (TextView) view.findViewById(R.id.score_textview);
        date      = (TextView) view.findViewById(R.id.data_textview);

        home_crest = (ImageView) view.findViewById(R.id.home_crest);
        away_crest = (ImageView) view.findViewById(R.id.away_crest);
    }

    public void setMatchId (int matchId) {
        mMatchId = matchId;
    }

    public int getMatchId () {
        return mMatchId;
    }

    public void setMatchDay (String matchDay) {
        mMatchDay = matchDay;
    }

    public String getMatchDay () {
        return mMatchDay;
    }

    public void setLeagueName (String leagueName) {
        mLeagueName = leagueName;
    }

    public String getLeagueName () {
        return mLeagueName;
    }

    public void setMatchContent (String content) {
        mMatchContent = content;
    }

    public void setScoreContent (String content) {
        mScoreContent = content;
    }

    public void setExtraContent (String content) {
        mExtraContent = content;
    }

    public String getContent () {
        StringBuilder result = new StringBuilder ();
        if ( mMatchContent != null ) {
            result.append(mMatchContent);
        }
        if ( mScoreContent != null ) {
            result.append (mScoreContent);
        }
        return result.toString();
    }

    public String getDetailContent () {
        StringBuilder result = new StringBuilder (getContent());
        if ( mExtraContent != null ) {
            result.append(mExtraContent);
        }
        return result.toString();
    }
}
