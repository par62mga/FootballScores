package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import barqsoft.footballscores.service.myFetchService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String LOG_TAG = MainScreenFragment.class.getSimpleName();

    // define key used to save/restore selected match for this page
    private static final String MATCH_ID_KEY = "match_id";

    private static final int SCORES_LOADER = 0;

    // stores "database friendly" date used to return scores for this date
    private String[] mFragmentDate    = new String[1];

    // scores list adapter
    private FootballAdapter mAdapter    = null;

    // currently selected match for this page or zero if none selected
    private int      mSelectedMatchId = 0;

    public MainScreenFragment()
    {
    }

    private void update_scores()
    {
        Intent service_start = new Intent(getActivity(), myFetchService.class);
        getActivity().startService(service_start);
    }

    public void setFragmentDate(String date)
    {
        // Log.d(LOG_TAG, "setFragmentDate() ==> " + date);
        mFragmentDate[0] = date;
    }

    public void setSelectedMatch(int match) {
        mSelectedMatchId = match;
    }

    /**
     * hideActiveDetailView -- supports back button navigation by hiding any presently selected
     *     match detail.
     * @return TRUE when detail was found and hidden; otherwise, FALSE
     */
    public boolean hideActiveDetailView () {
        return mAdapter.hideActiveDetailView(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        update_scores();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // moved save/restore of selected match to fragments from activity to fix issue where wrong
        // match might be restored since this is page/fragment specific
        if ( (savedInstanceState != null) && savedInstanceState.containsKey(MATCH_ID_KEY)) {
                mSelectedMatchId = savedInstanceState.getInt(MATCH_ID_KEY);
        }

        final ListView score_list = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new FootballAdapter(getActivity(),null,0, mSelectedMatchId);
        score_list.setAdapter(mAdapter);

        Log.d(LOG_TAG, "initLoader() fragment date ==> " + mFragmentDate[0]);
        getLoaderManager().initLoader(SCORES_LOADER,null,this);
        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // tell the adapter that this view was selected/changing selected match
                mAdapter.changeMatchDetailView(getActivity(), view);
                mSelectedMatchId = mAdapter.getSelectedMatchId();
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState (Bundle outInstanceState) {
        // moved save/restore of selected match to fragments from activity
        if (mSelectedMatchId != 0) {
            outInstanceState.putInt(MATCH_ID_KEY, mSelectedMatchId);
        }
        super.onSaveInstanceState(outInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(getActivity(),DatabaseContract.scores_table.buildScoreWithDate(),
                null,null,mFragmentDate,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        //Log.v(FetchScoreTask.LOG_TAG,"loader finished");
        //cursor.moveToFirst();
        /*
        while (!cursor.isAfterLast())
        {
            Log.v(FetchScoreTask.LOG_TAG,cursor.getString(1));
            cursor.moveToNext();
        }
        */

        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            i++;
            cursor.moveToNext();
        }
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mAdapter.swapCursor(null);
    }


}
