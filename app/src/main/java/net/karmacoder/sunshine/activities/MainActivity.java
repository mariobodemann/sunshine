package net.karmacoder.sunshine.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.karmacoder.sunshine.Utility;
import net.karmacoder.sunshine.data.WeatherContract;
import net.karmacoder.sunshine.fragments.DetailFragment;
import net.karmacoder.sunshine.fragments.ForecastFragment;
import net.karmacoder.sunshine.tasks.FetchWeatherTask;

import static net.karmacoder.sunshine.data.WeatherContract.WeatherEntry.buildWeatherLocationWithDate;


public class MainActivity extends ActionBarActivity implements ForecastFragment.ForecastCallback {

    public static final String TAG = MainActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isInTwoPanelMode()) {
            if (savedInstanceState == null) {
                DetailFragment fragment = DetailFragment.instanciate(null);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.weather_detail_container, fragment)
                        .commit();
            }
        } else {
            getSupportActionBar().setElevation(0f);
        }
    }

    private boolean isInTwoPanelMode() {
        return findViewById(R.id.weather_detail_container) != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_forecast, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
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
                final Intent intent = new Intent(this.getApplicationContext(), SettingsActivity.class);
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

        final FragmentActivity activity = this;
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            Log.e(TAG, "Could not find an app for locations ...");
        }
    }

    private void updateWeather() {
        new FetchWeatherTask(this).execute(Utility.getPreferredLocation(this));
    }

    private String getPreferredLocation() {
        return getParameter(R.string.preference_location_key, R.string.preference_location_default);
    }

    private String getParameter(@StringRes int keyId, @StringRes int defaultId) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String defaultValue = getString(defaultId);
        return preferences.getString(getString(keyId), defaultValue);
    }

    @Override
    public void onForecastItemClicked(Cursor cursor) {
        final Uri uri = getCurrentSelectedItemUri(cursor);

        if (isInTwoPanelMode()) {
            replaceDetailFragment(uri);
        } else {
            startDetailsActivity(uri);
        }
    }


    private Uri getCurrentSelectedItemUri(Cursor cursor) {
        final String location = Utility.getPreferredLocation(this);
        final long weatherDate = cursor.getLong(WeatherContract.COL_WEATHER_DATE);
        return buildWeatherLocationWithDate(location, weatherDate);
    }

    private void replaceDetailFragment(Uri uri) {
        DetailFragment fragment = DetailFragment.instanciate(uri);
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(DetailFragment.TAG)
                .replace(R.id.weather_detail_container, fragment, DetailFragment.TAG)
                .commit();
    }

    private void startDetailsActivity(Uri uri) {
        final Intent intent = new Intent(this.getApplicationContext(),
                DetailActivity.class);
        intent.setData(uri);
        startActivity(intent);
    }
}
