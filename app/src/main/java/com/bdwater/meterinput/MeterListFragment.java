package com.bdwater.meterinput;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bdwater.meterinput.base.AppUtils;
import com.bdwater.meterinput.model.Book;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.base.IReturnValueListener;
import com.bdwater.meterinput.model.Meter;
import com.bdwater.meterinput.soap.ISoapAsyncTaskListener;
import com.bdwater.meterinput.soap.SoapAsyncTask;
import com.bdwater.meterinput.soap.SoapClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class MeterListFragment extends Fragment {

    IReturnValueListener mListener;
    ProgressBar mProgressBar;
    ListView mListView;

    SoapAsyncTask mTask;

    CApplication mApp;
    CurrentContext mCC;
    Book mBook;
    Boolean mIsDataChanged = false;

    Integer mCheckPosition = -1;
    SimpleMeterArrayAdapter mAdapter;

    public MeterListFragment() {
        // Required empty public constructor
        mApp = AppUtils.App;
        mCC = mApp.getCurrentContext();
    }
    public void setListener(IReturnValueListener l) {
        mListener = l;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meter_list, container, false);

        mProgressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        mListView = (ListView) view.findViewById(R.id.listView);
        mListView.setOnItemClickListener(clickListener);
        if(mIsDataChanged)
            load();
        return view;
    }

    public void loadByBook(Book book) {
        if(mBook != book) {
            mCheckPosition = -1;
            mBook = book;
            if (isVisible()) {
                load();
            } else {
                mIsDataChanged = true;
            }
        }
    }
    private void load() {
        if(mTask != null) {
            Log.d("MeterListFragment", "Cancel mTask");
            mTask.cancel(true);
        }
        mProgressBar.setVisibility(View.VISIBLE);
        Log.d("MeterListFragment", "Book is null:" + (mBook == null));

        if(mBook == null) return;

        loading();
        mTask = new SoapAsyncTask(SoapClient.GET_METERS_METHOD, listener);
        mTask.execute(mBook.BookID);
    }

    ListView.OnItemClickListener clickListener = new ListView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mListView.setItemChecked(position, true);
            mCheckPosition = position;
            mAdapter.setCheckedPosition(position);
            mAdapter.notifyDataSetChanged();
            if(mListener != null) {
                mListener.onReturnValue(mAdapter.getItem(position));
            }
        }
    };
    private void loading() {
        mListView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }
    private void loaded() {
        mProgressBar.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
    }
    private void showMessage(final String message) {
        mListView.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    ISoapAsyncTaskListener listener = new ISoapAsyncTaskListener() {
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

                mCC.setMeters(meters);
                mCheckPosition = mCC.getMeterPosition(mCC.getCurrentMeter());

                mAdapter = new SimpleMeterArrayAdapter(getContext(), meters);
                mAdapter.setCheckedPosition(mCheckPosition);
                mListView.setAdapter(mAdapter);

                if(mCheckPosition >= 0) {
                    mListView.setSelection(mCheckPosition);
                    mListView.smoothScrollToPosition(mCheckPosition);
                }
                mIsDataChanged = false;

            } catch (JSONException e) {
                e.printStackTrace();
                showMessage(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                showMessage(e.getMessage());
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
            load();
            showMessage("网络不可或尚未链接，请确认后再试");
        }
    };


}
