package net.karmacoder.sunshine.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.karmacoder.sunshine.ForecastListItem;
import net.karmacoder.sunshine.Utility;
import net.karmacoder.sunshine.data.WeatherContract;
import net.karmacoder.sunshine.R;

import static net.karmacoder.sunshine.Utility.formatTemperature;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_OTHER = 1;

    private static final int VIEW_TYPE_COUNT = 2;

    private boolean mShouldHighlightTodayItem;

    public ForecastAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mShouldHighlightTodayItem) ? VIEW_TYPE_TODAY : VIEW_TYPE_OTHER;
    }

    /*
                Remember that these views are reused as needed.
             */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final int layoutId;
        if (getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        } else {
            layoutId = R.layout.list_item_forecast;
        }

        return LayoutInflater.from(context).inflate(layoutId, parent, false);
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ForecastListItem itemView = (ForecastListItem) view;

        final int weatherId = cursor.getInt(WeatherContract.COL_WEATHER_CONDITION_ID);
        final int weatherIcon;
        if (getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY) {
            weatherIcon = Utility.getArtResourceForWeatherCondition(weatherId);
        } else {
            weatherIcon = Utility.getIconResourceForWeatherCondition(weatherId);
        }
        itemView.iconView.setImageResource(weatherIcon);

        // TODO Read date from cursor
        long date = cursor.getLong(WeatherContract.COL_WEATHER_DATE);
        itemView.dateText.setText(Utility.getDayName(context, date));

        // TODO Read weather description from cursor ← description was forecast
        String description = cursor.getString(WeatherContract.COL_WEATHER_DESC);
        itemView.descriptionText.setText(description);

        // Read maxTemperature temperature from cursor
        double maxTemperature = cursor.getDouble(WeatherContract.COL_WEATHER_MAX_TEMP);
        itemView.maxTextView.setText(formatTemperature(context, maxTemperature));

        // TODO Read minTemperature temperature from cursor
        double minTemperature = cursor.getDouble(WeatherContract.COL_WEATHER_MIN_TEMP);
        itemView.minTextView.setText(formatTemperature(context, minTemperature));
    }

    public void setShouldHighlightTodayItem(boolean shouldHighlight) {
        mShouldHighlightTodayItem = shouldHighlight;
    }
}