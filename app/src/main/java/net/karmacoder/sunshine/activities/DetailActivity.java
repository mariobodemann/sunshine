package net.karmacoder.sunshine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailActivity extends ActionBarActivity {

    public static final String KEY_FORECAST = "FORECAST_KEY";

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mTextView = (TextView) findViewById(R.id.detail_text);
        mTextView.setText(getForecastData());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);

        final MenuItem shareMenuItem = menu.findItem(R.id.action_details_share);
        final ShareActionProvider shareActionProvider =
                (ShareActionProvider)MenuItemCompat.getActionProvider(shareMenuItem);

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/*");
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getForecastData() + " #SunshineApp");
        shareActionProvider.setShareIntent(shareIntent);

        return true;
    }

    private String getForecastData() {
        return getIntent().getStringExtra(KEY_FORECAST);
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
}
