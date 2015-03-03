package net.karmacoder.sunshine.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.karmacoder.sunshine.activities.R;
import net.karmacoder.sunshine.tasks.RequestWeatherTask;

import java.util.ArrayList;
import java.util.Collections;

public class ForecastFragment extends Fragment {

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

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_forecast_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_refresh:
                new RequestWeatherTask(new RequestWeatherTask.ResponseListener() {
                    @Override
                    public void onResponseReturned(String[] response) {
                        mWeatherData.clear();
                        Collections.addAll(mWeatherData, response);
                        mListAdapter.notifyDataSetChanged();
                    }
                }).execute("10405,de");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
