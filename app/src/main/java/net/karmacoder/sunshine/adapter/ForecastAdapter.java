package net.karmacoder.sunshine.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.karmacoder.sunshine.Utility;
import net.karmacoder.sunshine.activities.R;

import static net.karmacoder.sunshine.Utility.convertCursorRowToUXFormat;
import static net.karmacoder.sunshine.data.WeatherContract.COL_WEATHER_DATE;
import static net.karmacoder.sunshine.data.WeatherContract.COL_WEATHER_DESC;
import static net.karmacoder.sunshine.data.WeatherContract.COL_WEATHER_MAX_TEMP;
import static net.karmacoder.sunshine.data.WeatherContract.COL_WEATHER_MIN_TEMP;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    public ForecastAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        TextView tv = (TextView) view;
        tv.setText(convertCursorRowToUXFormat(mContext, cursor));
    }
}