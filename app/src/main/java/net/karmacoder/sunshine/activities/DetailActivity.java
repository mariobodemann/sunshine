package net.karmacoder.sunshine.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;

import net.karmacoder.sunshine.R;
import net.karmacoder.sunshine.fragments.DetailFragment;

public class DetailActivity extends ActionBarActivity implements DetailFragment.DetailCallback {

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        DetailFragment fragment = DetailFragment.instanciate(getIntent().getData());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.weather_detail_container, fragment, DetailFragment.TAG)
                .addToBackStack(DetailFragment.TAG)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);

        final MenuItem shareMenuItem = menu.findItem(R.id.action_details_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_action_settings:
                final Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetailsUpdated(CharSequence summary) {
        if (mShareActionProvider != null) {
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/*");
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            shareIntent.putExtra(Intent.EXTRA_TEXT, summary + " #SunshineApp");
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
}
