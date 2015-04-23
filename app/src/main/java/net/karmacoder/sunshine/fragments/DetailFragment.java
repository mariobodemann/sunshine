package net.karmacoder.sunshine.fragments;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import net.karmacoder.sunshine.R;

import net.karmacoder.sunshine.Utility;
import net.karmacoder.sunshine.constant.LoaderIds;

import static net.karmacoder.sunshine.data.WeatherContract.WeatherEntry;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = DetailFragment.class.getCanonicalName();

    private static final String ARGUMENT_BUNDLE_URI_KEY = TAG + ".ARGUMENT_BUNDLE_URI_KEY";

    public static interface DetailCallback {
        void onDetailsUpdated(CharSequence summary);
    }

    public static DetailFragment instanciate(Uri uri) {
        DetailFragment fragment = new DetailFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARGUMENT_BUNDLE_URI_KEY, uri);
        fragment.setArguments(arguments);

        return fragment;
    }

    final String[] PROJECTION = new String[]{
            WeatherEntry.COLUMN_DATE,
            WeatherEntry.COLUMN_WEATHER_ID,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_DEGREES,
            WeatherEntry.COLUMN_HUMIDITY,
    };

    final int DATE_INDEX = 0;
    final int WEATHER_CONDITION_ID = 1;
    final int SUMMARY_INDEX = 2;
    final int TEMPERATURE_MAX_INDEX = 3;
    final int TEMPERATURE_MIN_INDEX = 4;
    final int PRESSURE_INDEX = 5;
    final int WIND_SPEED_INDEX = 6;
    final int WIND_DIRECTION_INDEX = 7;
    final int HUMIDITY_INDEX = 8;

    private ImageView mImage;

    private TextView mTextDate;
    private TextView mTextTemperatureMax;
    private TextView mTextTemperatureMin;
    private TextView mTextHumidity;
    private TextView mTextWind;
    private TextView mTextPressure;
    private TextView mTextSummary;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mTextDate = (TextView) rootView.findViewById(R.id.detail_item_date);
        mTextTemperatureMax = (TextView) rootView.findViewById(R.id.detail_item_temperature_max);
        mTextTemperatureMin = (TextView) rootView.findViewById(R.id.detail_item_temperature_min);
        mImage = (ImageView) rootView.findViewById(R.id.detail_item_image);
        mTextSummary = (TextView) rootView.findViewById(R.id.detail_item_summary);
        mTextHumidity = (TextView) rootView.findViewById(R.id.detail_item_humidity);
        mTextWind = (TextView) rootView.findViewById(R.id.detail_item_wind);
        mTextPressure = (TextView) rootView.findViewById(R.id.detail_item_pressure);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final FragmentActivity activity = getActivity();
        if (activity != null) {
            if (!activity.isDestroyed() && isAdded() && !isDetached()) {
                final LoaderManager loaderManager = activity.getLoaderManager();
                if (loaderManager != null && loaderManager.getLoader(LoaderIds.LOADER_DETAIL_WEATHER_ID) != null) {
                    loaderManager.restartLoader(LoaderIds.LOADER_DETAIL_WEATHER_ID, null, this);
                }
            }
        }
    }

    private void updateView(Cursor cursor) {
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            int condition = cursor.getInt(WEATHER_CONDITION_ID);
            mImage.setImageResource(Utility.getArtResourceForWeatherCondition(condition));

            mTextDate.setText(Utility.getDayName(activity, cursor.getLong(DATE_INDEX)));
            mTextSummary.setText(cursor.getString(SUMMARY_INDEX));

            mTextTemperatureMax.setText(Utility.formatTemperature(activity, cursor.getDouble(TEMPERATURE_MAX_INDEX)));
            mTextTemperatureMin.setText(Utility.formatTemperature(activity, cursor.getDouble(TEMPERATURE_MIN_INDEX)));

            mTextPressure.setText(Utility.formatPressure(activity, cursor.getDouble(PRESSURE_INDEX)));
            mTextWind.setText(Utility.getFormattedWind(activity, cursor.getFloat(WIND_SPEED_INDEX), cursor.getFloat(WIND_DIRECTION_INDEX)));
            mTextHumidity.setText(Utility.formatHumidity(activity, cursor.getDouble(HUMIDITY_INDEX)));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Uri uri;
        final Bundle arguments = getArguments();
        if (arguments == null) {
            uri = null;
        } else {
            uri = arguments.getParcelable(ARGUMENT_BUNDLE_URI_KEY);
        }

        if (uri != null) {
            return new CursorLoader(getActivity(), uri, PROJECTION, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            updateView(cursor);

            if (getActivity() instanceof DetailCallback) {
                CharSequence summary = mTextSummary.getText();
                ((DetailCallback) getActivity()).onDetailsUpdated(summary);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
