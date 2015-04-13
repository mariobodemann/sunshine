package net.karmacoder.sunshine.fragments;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import net.karmacoder.sunshine.Utility;
import net.karmacoder.sunshine.activities.DetailActivity;
import net.karmacoder.sunshine.activities.R;
import net.karmacoder.sunshine.activities.SettingsActivity;
import net.karmacoder.sunshine.adapter.ForecastAdapter;
import net.karmacoder.sunshine.constant.LoaderIds;
import net.karmacoder.sunshine.constant.LocalBroadcastAction;
import net.karmacoder.sunshine.data.WeatherContract;
import net.karmacoder.sunshine.tasks.FetchWeatherTask;

import static net.karmacoder.sunshine.data.WeatherContract.WeatherEntry;
import static net.karmacoder.sunshine.data.WeatherContract.WeatherEntry.buildWeatherLocationWithDate;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ForecastFragment.class.getCanonicalName();

    private ForecastAdapter mForecastAdapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final FragmentActivity activity = getActivity();
        activity.getLoaderManager().initLoader(LoaderIds.LOADER_MAIN_WEATHER_ID, null, this);

        activity.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateWeather();
                activity.getLoaderManager().restartLoader(LoaderIds.LOADER_MAIN_WEATHER_ID, null, ForecastFragment.this);
            }
        }, new IntentFilter(LocalBroadcastAction.BROADCAST_ACTION_LOCATION_UPDATED));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView list = (ListView)rootView.findViewById(R.id.forecast_list_view);
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        list.setAdapter(mForecastAdapter);

        list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final Cursor cursor = (Cursor) mForecastAdapter.getItem(position);
                        if (cursor != null) {
                            startDetailsActivity(cursor);
                        }
                    }
                }
        );

        setHasOptionsMenu(true);

        return rootView;
    }

    private void startDetailsActivity(Cursor cursor) {
        final Intent intent = new Intent(getActivity().getApplicationContext(),
                DetailActivity.class);
        final String location = Utility.getPreferredLocation(getActivity());
        final long weatherDate = cursor.getLong(WeatherContract.COL_WEATHER_DATE);
        final Uri uri = buildWeatherLocationWithDate(location, weatherDate);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_forecast, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_refresh:
                updateWeather();
                return true;
            case R.id.menu_action_verify_location:
                verifyLocation();
                return true;
            case R.id.menu_action_settings:
                final Intent intent = new Intent(getActivity().getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void verifyLocation() {
        final String preferredLocation = getPreferredLocation();
        final Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:0,0").buildUpon()
                    .appendQueryParameter("q", preferredLocation)
                    .build());

        final FragmentActivity activity = getActivity();
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            Log.e(TAG, "Could not find an app for locations ...");
        }
    }

    private void updateWeather() {
        new FetchWeatherTask(getActivity()).execute(Utility.getPreferredLocation(getActivity()));
    }

    private String getPreferredLocation() {
        return getParameter(R.string.preference_location_key, R.string.preference_location_default);
    }

    private String getParameter(@StringRes int keyId, @StringRes int defaultId) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String defaultValue = getString(defaultId);
        return preferences.getString(getString(keyId), defaultValue);
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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}
