package barqsoft.footballscores;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment {
    private static final String LOG_TAG = PagerFragment.class.getSimpleName();

    // used to save/restore selected page in this fragment rather than the activity
    private static final String PAGE_NUM_KEY = "Current_Page";

    private static final int NUM_PAGES  = 5;
    private static final int TODAY_PAGE = 2;

    private ViewPager        mPagerHandler;
    private LocalPageAdapter mPagerAdapter;

    // used to store date and match selected by Widget launching the activity
    private String           mSelectedDate;
    private int              mSelectedMatch;

    // just keep dates in the array, this fixes a nasty problem on screen rotation where new
    // fragments were created even when old ones were recycled by the page adapter
    // private MainScreenFragment[] mViewFragments = new MainScreenFragment[5];
    private FootballDate[]   mPagerDates = new FootballDate[5];

    public MainScreenFragment getActiveFragment () {
        // the original code returned the wrong fragment here, moved logic to the page adapter
        return mPagerAdapter.getFragmentAt(mPagerHandler.getCurrentItem());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        int    currentPage  = TODAY_PAGE;

        View rootView   = inflater.inflate(R.layout.pager_fragment, container, false);

        // see if the activity was launched with a selected date and match
        Uri contentUri = (getActivity().getIntent() != null) ?
                getActivity().getIntent().getData() : null;
        if (contentUri != null) {
            mSelectedDate  = DatabaseContract.scores_table.getDateFromUri(contentUri);
            mSelectedMatch = DatabaseContract.scores_table.getMatchFromUri(contentUri);
            Log.d (LOG_TAG, "onCreateView() got URI with date ==> " + mSelectedDate +
                    " match ==> " + String.valueOf(mSelectedMatch));
        }
        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new LocalPageAdapter(getChildFragmentManager());

        for (int i = 0;i < NUM_PAGES;i++) {
            int relativeDay;

            // handle RTL layouts by ordering fragment pages/titles using this "date" array
            if (TextUtils.getLayoutDirectionFromLocale(null) == View.LAYOUT_DIRECTION_RTL) {
                relativeDay = (NUM_PAGES - i - 1) - TODAY_PAGE;
            } else {
                relativeDay = i - TODAY_PAGE;
            }

            mPagerDates[i] = new FootballDate(relativeDay);
            if ( (mSelectedDate != null) &&
                    mSelectedDate.contentEquals(mPagerDates[i].getFootballDate(getActivity())) ) {
                currentPage = i;
            }
        }

        mPagerHandler.setAdapter(mPagerAdapter);

        // update current page if there is a saved instance state
        if ( (savedInstanceState != null) && (savedInstanceState.containsKey(PAGE_NUM_KEY)) ) {
            currentPage = savedInstanceState.getInt (PAGE_NUM_KEY);
            mSelectedDate  = null;
            mSelectedMatch = 0;
        }
        mPagerHandler.setCurrentItem(currentPage);
        return rootView;
    }

    @Override
    public void onSaveInstanceState (Bundle outInstanceState) {
        // save selected page here
        outInstanceState.putInt(PAGE_NUM_KEY, mPagerHandler.getCurrentItem());
        super.onSaveInstanceState(outInstanceState);
    }

    private class LocalPageAdapter extends FragmentStatePagerAdapter {

        // store reference to fragments here. They are created by "getItem" and then are recycled
        // with instantiateItem during a screen rotation...very tricky and not well documented
        private MainScreenFragment[] mActiveFragments = new MainScreenFragment[5];

        public LocalPageAdapter (FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem (ViewGroup container, int position) {
            Fragment createdFragment = (Fragment)super.instantiateItem (container, position);
            String   fragmentDate    = mPagerDates[position].getFootballDate(getActivity());

            // save reference to what might be a recycled fragment and make sure date is correct
            mActiveFragments[position] = (MainScreenFragment)createdFragment;
            mActiveFragments[position].setFragmentDate(fragmentDate);
            if ( (mSelectedDate != null) && mSelectedDate.contentEquals(fragmentDate) ) {
                mActiveFragments[position].setSelectedMatch(mSelectedMatch);
            }
            return createdFragment;
        }

        @Override
        public Fragment getItem(int page) {
            // this is the right time to create a new fragment in a page adapter
            MainScreenFragment fragment = new MainScreenFragment();
            String   fragmentDate       = mPagerDates[page].getFootballDate(getActivity());

            // save reference to newly created fragment for good measure...
            mActiveFragments[page] = fragment;
            fragment.setFragmentDate(fragmentDate);
            if ( (mSelectedDate != null) && mSelectedDate.contentEquals(fragmentDate) ) {
                fragment.setSelectedMatch(mSelectedMatch);
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }


        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            // must refer to pager dates as fragment is not instantiated prior to this call
            return mPagerDates[position].getFootballDayName(getActivity());
        }

        /**
         * getFragmentAt -- returns fragment instance that was either newly created or restored
         *     after a screen rotation
         * @param position
         * @return MainScreenFragment referenced at the given position
         */
        public MainScreenFragment getFragmentAt (int position) {
            return mActiveFragments[position];
        }
    }
}
