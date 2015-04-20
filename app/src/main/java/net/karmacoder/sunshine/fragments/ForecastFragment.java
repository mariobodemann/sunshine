package net.karmacoder.sunshine.fragments;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import net.karmacoder.sunshine.Utility;
import net.karmacoder.sunshine.activities.R;
import net.karmacoder.sunshine.adapter.ForecastAdapter;
import net.karmacoder.sunshine.constant.LoaderIds;
import net.karmacoder.sunshine.data.WeatherContract;

import static net.karmacoder.sunshine.data.WeatherContract.WeatherEntry;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ForecastFragment.class.getCanonicalName();
    private static final String LAST_KNOWN_POSITION = TAG + ".LAST_KNOWN_POSITION";

    private int mLastKnownPosition = -1;

    public static interface ForecastCallback {
        void onForecastItemClicked(Cursor cursor);
    }

    private ForecastAdapter mForecastAdapter;
    private ListView mListView;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().getLoaderManager().initLoader(LoaderIds.LOADER_MAIN_WEATHER_ID, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(LAST_KNOWN_POSITION, mLastKnownPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) rootView.findViewById(R.id.forecast_list_view);
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mForecastAdapter.setShouldHighlightTodayItem(getResources().getBoolean(R.bool.highlight_today));
        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (getActivity() instanceof ForecastCallback) {
                            final Cursor cursor = (Cursor) mForecastAdapter.getItem(position);
                            ((ForecastCallback) getActivity()).onForecastItemClicked(cursor);
                        }

                        mLastKnownPosition = position;
                    }
                }
        );

        if (savedInstanceState != null) {
            mLastKnownPosition = savedInstanceState.getInt(LAST_KNOWN_POSITION, -1);
        }

        if (mLastKnownPosition > -1) {
            mListView.smoothScrollToPosition(mLastKnownPosition);
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String locationSetting = Utility.getPreferredLocation(getActivity());
        final String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";
        final Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                WeatherContract.FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mForecastAdapter.swapCursor(cursor);

        if (mLastKnownPosition > -1) {
            mListView.smoothScrollToPosition(mLastKnownPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}
