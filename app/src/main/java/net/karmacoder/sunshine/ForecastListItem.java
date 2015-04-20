package net.karmacoder.sunshine;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.karmacoder.sunshine.activities.R;

public class ForecastListItem extends LinearLayout {
    public ForecastListItem(Context context) {
        super(context);
    }

    public ForecastListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ForecastListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ForecastListItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ImageView iconView;
    public TextView dateText;
    public TextView descriptionText;
    public TextView maxTextView;
    public TextView minTextView;

    @Override
    protected void onFinishInflate() {
        iconView = (ImageView) findViewById(R.id.forecast_item_image);
        dateText = (TextView) findViewById(R.id.forecast_item_date);
        descriptionText = (TextView) findViewById(R.id.forecast_item_summary);
        maxTextView = (TextView) findViewById(R.id.forecast_item_temperature_max);
        minTextView = (TextView) findViewById(R.id.forecast_item_temperature_min);
    }
}
