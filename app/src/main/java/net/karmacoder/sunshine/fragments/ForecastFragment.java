package net.karmacoder.sunshine.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.karmacoder.sunshine.activities.DetailActivity;
import net.karmacoder.sunshine.activities.R;
import net.karmacoder.sunshine.activities.SettingsActivity;
import net.karmacoder.sunshine.tasks.RequestWeatherTask;

import java.util.ArrayList;
import java.util.Collections;

import static net.karmacoder.sunshine.tasks.RequestWeatherTask.RequestParameter;

public class ForecastFragment extends Fragment {

    private static final String TAG = ForecastFragment.class.getCanonicalName();

    private final ArrayList<String> mWeatherData = new ArrayList<>();
    private ArrayAdapter<String> mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast, R.id.forecast_item_text, mWeatherData);

        final ListView list = (ListView)rootView.findViewById(R.id.forecast_list_view);
        list.setAdapter(mListAdapter);

        list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        startDetailsActivity(mListAdapter.getItem(position));
                    }
                }
        );

        final View emptyView = inflater.inflate(R.layout.forecast_empty_view, list, false);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDetailsActivity("CURRENTLY EMPTY");
            }
        });
        list.setEmptyView(emptyView);

        setHasOptionsMenu(true);

        return rootView;
    }

    private void startDetailsActivity(String forecast) {
        final Intent intent = new Intent(getActivity().getApplicationContext(),
                DetailActivity.class);
        intent.putExtra(DetailActivity.KEY_FORECAST, forecast);
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
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                "geo:0,0?q=" + preferredLocation));

        final FragmentActivity activity = getActivity();
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            Log.e(TAG, "Could not find an app for locations ...");
        }
    }

    private void updateWeather() {
        new RequestWeatherTask(new RequestWeatherTask.ResponseListener() {
            @Override
            public void onResponseReturned(String[] response) {
                mWeatherData.clear();
                Collections.addAll(mWeatherData, response);
                mListAdapter.notifyDataSetChanged();
            }
        }).execute(getRequestParameter());
    }

    private RequestParameter getRequestParameter() {
        RequestParameter parameter = new RequestParameter();
        parameter.location = getPreferredLocation();
        parameter.units = getParameter(R.string.preference_units_key, R.string.preference_units_default);
        parameter.appid = getParameter(R.string.preference_api_key_key, R.string.preference_api_key_default);
        parameter.mockResults = getBooleanParameter(R.string.preference_mock_results_key, false);
        return parameter;
    }

    private String getPreferredLocation() {
        return getParameter(R.string.preference_location_key, R.string.preference_location_default);
    }

    private String getParameter(@StringRes int keyId, @StringRes int defaultId) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String defaultValue = getString(defaultId);
        return preferences.getString(getString(keyId), defaultValue);
    }

    private boolean getBooleanParameter(@StringRes int keyId, boolean defaultValue) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return preferences.getBoolean(getString(keyId), defaultValue);
    }
}
