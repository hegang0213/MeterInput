package com.bdwater.meterinput.main;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bdwater.meterinput.CApplication;
import com.bdwater.meterinput.R;
import com.bdwater.meterinput.base.AppUtils;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.model.CustomerBill;
import com.bdwater.meterinput.model.FakeMeter;
import com.bdwater.meterinput.model.Meter;
import com.bdwater.meterinput.soap.ISoapAsyncTaskListener;
import com.bdwater.meterinput.soap.SoapAsyncTask;
import com.bdwater.meterinput.soap.SoapClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerBillFragment extends Fragment implements IMeterChangedNotifier {

    CApplication mApp;
    CurrentContext mCC;

    MeterLoadState meterLoadState = new MeterLoadState();
    SoapAsyncTask mTask;

    List<CustomerBill> customerBills = new ArrayList<CustomerBill>();

    SwipeRefreshLayout refreshLayout;
    ListView listView;
    CustomerBillArrayAdapter adapter;

    TextView footerTextView;

    public CustomerBillFragment() {
        // Required empty public constructor
        mApp = AppUtils.App;
        mCC = mApp.getCurrentContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_customer_bill, container, false);
        refreshLayout = (SwipeRefreshLayout)v;
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                executeTask();
            }
        });

        listView = (ListView)v.findViewById(R.id.listView);
        adapter = new CustomerBillArrayAdapter(getActivity(), customerBills);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                refreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                Log.d("CustomerBill", "refreshLayout enabled:" + String.valueOf(refreshLayout.isEnabled()));
            }
        });

        footerTextView = (TextView)v.findViewById(R.id.footerTextView);

        return v;
    }

    boolean measureRefresh = true;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        if(meterLoadState.IsDataChanged)
//            measureRefresh = true;

    }

    ISoapAsyncTaskListener listener = new ISoapAsyncTaskListener() {
        @Override
        public void onTaskSuccess(JSONObject obj) {
            try
            {
                adapter.clear();
                JSONArray customerBillArray = obj.getJSONArray("Data");
                //customerBills = new CustomerBill[customerBillArray.length()];
                for(int i = 0; i < customerBillArray.length(); i++) {
                    CustomerBill cb = CustomerBill.parse(customerBillArray.getJSONObject(i));
                    adapter.add(cb);
                }
                footerTextView.setText("账单数量：" + String.valueOf(adapter.getCount()));

                adapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
                showMessage(e.getMessage());
            }
            finally {
                loaded();

            }
        }

        @Override
        public void OnTaskFailed(Integer result, final String message) {
            loaded();
            showMessage(message);
        }

        @Override
        public void onTaskException(Exception exception) {
            loaded();
            //refreshLayout.setEnabled(true);
            showMessage(getString(R.string.network_error));
        }
    };

    @Override
    public void notifyDataChanged() {
        Meter meter = mCC.getCurrentMeter();
        if(meter == null) return;
        if(meter.MeterID.equals(meterLoadState.MeterID)) return;

        meterLoadState.MeterID = meter.MeterID;
        meterLoadState.IsDataChanged = true;
        this.load();
    }

    @Override
    public void notifyFakeMeterChanged(FakeMeter[] fakeMeters) {

    }

    private void load() {
        if(!this.isAdded()) return;

        refreshLayout.setRefreshing(true);
        executeTask();
    }
    private void loading() {

    }
    private  void loaded() {
        meterLoadState.IsDataChanged = false;
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
            }
        });
        Log.d("CustomerBill", "loaded");
    }
    private void showMessage(final String message) {
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void executeTask() {
        loading();

        Log.d("CustomerBill", "executeTaks");

        mTask = new SoapAsyncTask(SoapClient.GET_CUSTOMER_BILLS_METHOD, listener);
        mTask.execute(meterLoadState.MeterID);
    }

    class CustomerBillArrayAdapter extends ArrayAdapter<CustomerBill> {
        LayoutInflater mInflater;

        public CustomerBillArrayAdapter(Context context, List<CustomerBill> objects) {
            super(context, R.layout.customerbill_item_card, objects);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView != null) {
                holder = (ViewHolder)convertView.getTag();
            } else {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.customerbill_item_card, null);

                holder.billDate = (EditText)convertView.findViewById(R.id.billDate);
                holder.payBefore = (EditText)convertView.findViewById(R.id.payBefore);
                holder.priceTitle = (EditText)convertView.findViewById(R.id.priceTitle);
                holder.compensation = (EditText)convertView.findViewById(R.id.compensation);
                holder.waterQuantity = (EditText)convertView.findViewById(R.id.waterQuantity);
                holder.price = (EditText)convertView.findViewById(R.id.price);
                holder.currentPay = (EditText)convertView.findViewById(R.id.currentPay);
                holder.lastDate = (EditText)convertView.findViewById(R.id.lastDate);
                holder.lastValue = (EditText)convertView.findViewById(R.id.lastValue);
                holder.currentDate = (EditText)convertView.findViewById(R.id.currentDate);
                holder.currentValue = (EditText)convertView.findViewById(R.id.currentValue);
                holder.baseValue = (EditText)convertView.findViewById(R.id.baseValue);
                holder.serialValue = (EditText)convertView.findViewById(R.id.serialValue);
                convertView.setTag(holder);
            }
            CustomerBill cb = getItem(position);
            holder.billDate.setText(cb.BillDate);
            holder.payBefore.setText(cb.PayBefore);
            holder.priceTitle.setText(cb.PriceTitle);
            holder.compensation.setText(cb.Compensation);
            holder.waterQuantity.setText(cb.WaterQuantity);
            holder.price.setText(cb.Price);
            holder.currentPay.setText(cb.CurrentPay);
            holder.lastDate.setText(cb.LastDate);
            holder.lastValue.setText(cb.LastValue);
            holder.currentDate.setText(cb.CurrentDate);
            holder.currentValue.setText(cb.CurrentValue);
            holder.baseValue.setText(cb.BaseValue);
            holder.serialValue.setText(cb.SerialValue);
            return convertView;
        }

        private class ViewHolder {
            EditText billDate;
            EditText payBefore;

            EditText priceTitle;
            EditText compensation;

            EditText waterQuantity;
            EditText price;
            EditText currentPay;

            EditText lastDate;
            EditText lastValue;
            EditText currentDate;
            EditText currentValue;
            EditText baseValue;
            EditText serialValue;
        }
    }
}
