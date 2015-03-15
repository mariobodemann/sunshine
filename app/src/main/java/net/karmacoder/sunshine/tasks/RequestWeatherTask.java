package net.karmacoder.sunshine.tasks;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class RequestWeatherTask extends AsyncTask<RequestWeatherTask.RequestParameter, Void, String[]> {
    private static final String TAG = RequestWeatherTask.class.getCanonicalName();

    private static final String OWM_LIST = "list";
    private static final String OWM_WEATHER = "weather";
    private static final String OWM_TEMPERATURE = "temp";
    private static final String OWM_MAX = "max";
    private static final String OWM_MIN = "min";
    private static final String OWM_DESCRIPTION = "main";

    private static final int TIMEOUT = (int) TimeUnit.SECONDS.toMicros(1);
    private static final String[] MOCKED_RESULTS = new String[]{
            "Today - Something - 10 / 10",
            "Tomorrow - Something - 10 / 10",
            "Someday - Something - 10 / 10",
    };

    public static class RequestParameter {
        public String location;
        public String units;
        public String appid;
        public boolean mockResults;
    }

    public static interface ResponseListener {
        public void onResponseReturned(String[] response);
    }

    private final ResponseListener mListener;

    public RequestWeatherTask(ResponseListener listener) {
        mListener = listener;
    }

    @Override
    protected String[] doInBackground(RequestParameter... params) {
        if (params.length != 1) {
            return null;
        }

        final RequestParameter requestParameter = params[0];
        if (requestParameter.mockResults) {
            return MOCKED_RESULTS;
        }

        String response = null;
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = createConnection(requestParameter);
            if (urlConnection != null) {
                urlConnection.connect();
                response = readResponse(urlConnection);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            response = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        try {
            return getWeatherDataFromJson(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private HttpURLConnection createConnection(RequestParameter parameter) throws MalformedURLException {
        final URL url = buildUrl(parameter);
        HttpURLConnection urlConnection;
        try {
            Log.d(TAG, "Connecting to " + url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIMEOUT);
            urlConnection.setReadTimeout(TIMEOUT);
        } catch (IOException e) {
            return null;
        }

        if (urlConnection != null) {
            try {
                urlConnection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                urlConnection.disconnect();
            }
        }
        return urlConnection;
    }

    private URL buildUrl(RequestParameter parameter) throws MalformedURLException {
        Uri.Builder builder = Uri.parse("http://api.openweathermap.org")
                .buildUpon()
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("forecast")
                .appendPath("daily")
                .appendQueryParameter("q", parameter.location)
                .appendQueryParameter("mode", "json")
                .appendQueryParameter("units", parameter.units)
                .appendQueryParameter("cnt", "7");

        if (!TextUtils.isEmpty(parameter.appid)) {
            builder.appendQueryParameter("APPID", parameter.appid);
        }


        return new URL(builder.build().toString());
    }

    private String readResponse(HttpURLConnection urlConnection) throws IOException {
        final InputStream inputStream;
        try {
            inputStream = urlConnection.getInputStream();
        } catch (IOException e) {
            throw new IOException("Could not get input stream", e.getCause());
        }

        final StringBuffer buffer = new StringBuffer();
        String response;

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }
        } catch (IOException e) {
            try {
                reader.close();
            } catch (final IOException innerException) {
                Log.e(TAG, "Error closing stream, after reading line", innerException);
            }
            throw new IOException("Could not get input stream", e.getCause());
        }

        if (buffer.length() == 0) {
            response = null;
        } else {
            response = buffer.toString();
        }

        return response;
    }

    private String getReadableDateString(long time) {
        final SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    private String formatHighLows(double high, double low) {
        final long roundedHigh = Math.round(high);
        final long roundedLow = Math.round(low);

        return roundedHigh + "/" + roundedLow;
    }

    private String[] getWeatherDataFromJson(String forecastJsonStr)
            throws JSONException {
        if (TextUtils.isEmpty(forecastJsonStr)) {
            return new String[]{"NO DATA"};
        }

        final JSONObject forecastJson = new JSONObject(forecastJsonStr);
        final JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        final Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        String[] resultStrings = new String[weatherArray.length()];
        for (int i = 0; i < weatherArray.length(); i++) {
            String day;
            String description;
            String highAndLow;

            JSONObject dayForecast = weatherArray.getJSONObject(i);

            long dateTime;

            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);

            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrings[i] = day + " - " + description + " - " + highAndLow;
        }

        return resultStrings;
    }

    @Override
    protected void onPostExecute(String[] response) {
        super.onPostExecute(response);

        if (mListener != null) {
            if (response == null) {
                response = new String[]{"FAILED TO PARSE"};
            }

            mListener.onResponseReturned(response);
        }
    }
}
