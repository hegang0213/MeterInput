package com.bdwater.meterinput.main;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bdwater.meterinput.SimpleMeterArrayAdapter;
import com.bdwater.meterinput.base.AppUtils;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.model.Meter;
import com.bdwater.meterinput.base.TextUtils;
import com.bdwater.meterinput.soap.ISoapAsyncTaskListener;
import com.bdwater.meterinput.soap.SoapAsyncTask;
import com.bdwater.meterinput.soap.SoapClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hegang on 16/6/25.
 */
public class MeterSearchPanel implements ListView.OnItemClickListener, ISoapAsyncTaskListener {
    private View root;
    private ProgressBar progressBar;
    private ListView listView;
    private SimpleMeterArrayAdapter adapter;

    private Context context;
    private CurrentContext appContext;

    private static final int QUICK_SEARCH = 295;
    private static final int SUBMIT_SEARCH = 373;
    private int queryMode = QUICK_SEARCH;

    private SoapAsyncTask task;
    private OnMeterClickedListener listener;

    public MeterSearchPanel(Context context) {
        this.context = context;
        this.appContext = AppUtils.App.getCurrentContext();
    }

    public void setRoot(View root) {
        this.root = root;
        this.root.setVisibility(View.GONE);
    }

    public View getRoot() {
        return root;
    }

    public void setListView(ListView listView) {
        if(this.listView != listView) {
            this.listView = listView;
            this.listView.setOnItemClickListener(this);
        }
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
        this.progressBar.setVisibility(View.GONE);
    }
    public void show() {
        this.root.setVisibility(View.VISIBLE);
    }
    public void dismiss() {
        this.root.setVisibility(View.GONE);
        this.cancelTask();
    }
    public void setOnMeterClickListener(OnMeterClickedListener l) {
        this.listener = l;
    }
    // quick search means that search meter(s) from
    // previous selected book
    public void quickSearch(String query) {
        queryMode = QUICK_SEARCH;

        // start quick search, cancel webservice task
        cancelTask();

        ArrayList<Meter> result = new ArrayList<Meter>();
        Meter[] meters;
        if(!TextUtils.isEmpty(query)) {
            // filter cache meter list of previous selected book
            meters = appContext.getMeters();
            if (null != meters) {
                for (int i = 0; i < meters.length; i++) {
                    if (meters[i].CustomerNo.contains(query) || meters[i].Name.contains(query))
                        result.add(meters[i]);
                }
            }
            meters = (Meter[])result.toArray(new Meter[result.size()]);
        } else {
            // query is empty, return empty meter list
            meters = new Meter[] {};
        }
        // create adapter and fill list view
        this.adapter = new SimpleMeterArrayAdapter(context, meters);
        this.listView.setAdapter(this.adapter);
    }
    // search meter(s) via webservice
    public void submitSearch(String query) {
        queryMode = SUBMIT_SEARCH;

        executeTask(query);
    }
    private void executeTask(String queryString) {
        loading();
        task = new SoapAsyncTask(SoapClient.GET_METERS_BY_QUERY, this);
        task.execute(appContext.getUser().UserID, queryString);
    }
    private void cancelTask() {
        if(this.task != null && this.task.getStatus() == AsyncTask.Status.RUNNING)
            task.cancel(true);

        loaded();
    }
    private void loading() {
        this.progressBar.setVisibility(View.VISIBLE);
        this.listView.setVisibility(View.INVISIBLE);
    }
    private void loaded() {
        this.progressBar.setVisibility(View.GONE);
        this.listView.setVisibility(View.VISIBLE);
    }
    private void showMessage(final String message) {
        this.listView.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // click meter item from list view by user
        // call meter click listener callback
        onMeterClicked((Meter)this.listView.getItemAtPosition(position));
    }

    private void onMeterClicked(Meter meter) {
        if(null != this.listener) {
            // calls on click of listener
            listener.onClick(meter);
        }
    }

    @Override
    public void onTaskSuccess(JSONObject obj) {
        try {
            JSONArray jsonMeters = obj.getJSONArray("Data");
            int length = jsonMeters.length();
            Meter[] meters = new Meter[length];
            for(int i = 0; i < length; i++) {
                JSONObject jsonMeter = jsonMeters.getJSONObject(i);
                meters[i] = Meter.parseSimple(jsonMeter);
            }

            this.adapter = new SimpleMeterArrayAdapter(this.context, meters);
            this.listView.setAdapter(this.adapter); }
        catch (JSONException e) {
            e.printStackTrace();
            showMessage(e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage(ex.getMessage());
        } finally {
            loaded();
        }
    }

    @Override
    public void OnTaskFailed(Integer result, String message) {
        loaded();
        showMessage(message);
    }

    @Override
    public void onTaskException(Exception exception) {
        loaded();
        showMessage("网络不可或尚未链接，请确认后再试");
    }


    public interface OnMeterClickedListener {
        void onClick(Meter meter);
    }
}
