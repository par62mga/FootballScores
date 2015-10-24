package barqsoft.footballscores;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Changes made as part of the Football scores project:
 *
 * Required Components in the Rubric:
 * 1) Football Scores Widget -- implemented in widget/ScoresWidget and ScoresWidget. The "Track
 *    football scores" widget shows matches for yesterday, today and tomorrow.
 * 2) Content descriptions -- implemented content descriptions that include all match and score
 *    detail with a better description of what is happening when buttons are clicked.
 * 3) Layout mirroring -- implemented layout mirroring for both the app and widget.
 *
 * Optional Components:
 * 1) Extra error cases found and addressed (see list below)
 * 2) Football scores widget was implemented as a collection widget and based on advanced android
 *    course notes
 * 3) Included all "UI" strings into strings.xml, included "translatable=false" where needed and
 *    also added "arrays.xml" file to manage translation of team name to team crest.
 *    1) Strings moved league numbers and names to xml files; did the same for team names/crests
 *
 * Error cases found/accounted for:
 *    - When match detail selected, fixed "not known league, please report". This was corrected by
 *      creating "arrays.xml" and FootballLeagues class to better manage mapping of league number
 *      to league names.
 *    - When opening match details, all rows were updated. Corrected this by only updating the view
 *      in the row that changed
 *    - Improved back button handling, when row detail is shown it is now hidden first before
 *      allowing "back" to exit the app
 *    - Save/restore instance state was broken in that only one selected match was stored, not one
 *      per fragment. To fix, moved selected match logic into ScoresAdapter and save/restore in the
 *      MainScreenFragment lifecycle
 *    - removed maxSDK from manifest and synchronized targetSDK with build.gradle settings
 *    - Lifecycle of fragments was broken on screen rotation. The FragmentStatePagerAdapter
 *      recycles fragments on rotate rather than using the new ones that were not properly
 *      populated with views...fixed by moving logic to manage fragments fully within the adapter
 *    - widget found "today" scores, but "today" page does not show anything...need to investigate
 *
 * Other cleanup:
 *    - ViewHolder - cleaned up access to fields and also changed any public access fields to
 *      "final" to make them immutable, but still efficient to access
 *    - removed maxSDK from manifest and synchronized targetSDK with build.gradle settings
 *    - scoresAdapter - changed to FootballAdapter to follow standard java naming conventions
 */

public class MainActivity extends ActionBarActivity
{
    private static String LOG_TAG   = MainActivity.class.getSimpleName();

    private static String FRAGMENT_TAG = "Pager_Fragment";

    private PagerFragment mPagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "Reached MainActivity onCreate");
        if (savedInstanceState == null) {
            mPagerFragment = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mPagerFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed () {
        // this logic was added to undo showing any active row detail that was previous opened up
        // by the user. When no detail is shown, the app is exited
        MainScreenFragment fragment = mPagerFragment.getActiveFragment ();
        if (! fragment.hideActiveDetailView() ) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // simplified code here to only save/restore the pager. The logic to track active pages and
        // the selected match was broken and fixed by moving this save/restore logic to fragments
        getSupportFragmentManager().putFragment(outState, FRAGMENT_TAG, mPagerFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // simplified code here to only save/restore the pager.
        mPagerFragment = (PagerFragment) getSupportFragmentManager().getFragment(
                savedInstanceState,
                FRAGMENT_TAG);
        super.onRestoreInstanceState(savedInstanceState);
    }
}
