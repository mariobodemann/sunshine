package net.karmacoder.sunshine.activities;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import net.karmacoder.sunshine.Utility;
import net.karmacoder.sunshine.constant.LoaderIds;
import net.karmacoder.sunshine.data.WeatherContract;

public class DetailActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private TextView mTextView;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mTextView = (TextView) findViewById(R.id.detail_text);

        getLoaderManager().initLoader(LoaderIds.LOADER_DETAIL_WEATHER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);

        final MenuItem shareMenuItem = menu.findItem(R.id.action_details_share);
        mShareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(shareMenuItem);

        return true;
    }

    private void updateShareIntent(String text) {
        if (mShareActionProvider != null) {
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/*");
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            shareIntent.putExtra(Intent.EXTRA_TEXT, text + " #SunshineApp");
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private Uri getForecastUri() {
        return getIntent().getData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_action_settings:
                final Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, getForecastUri(), WeatherContract.FORECAST_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            final String text = Utility.convertCursorRowToUXFormat(this, cursor);
            mTextView.setText(text);
            updateShareIntent(text);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
