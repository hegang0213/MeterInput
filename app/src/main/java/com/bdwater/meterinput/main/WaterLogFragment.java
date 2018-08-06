package com.bdwater.meterinput.main;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bdwater.meterinput.CApplication;
import com.bdwater.meterinput.R;
import com.bdwater.meterinput.base.AppUtils;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.model.FakeMeter;
import com.bdwater.meterinput.model.Meter;
import com.bdwater.meterinput.model.WaterLog;
import com.bdwater.meterinput.soap.ISoapAsyncTaskListener;
import com.bdwater.meterinput.soap.SoapAsyncTask;
import com.bdwater.meterinput.soap.SoapClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WaterLogFragment extends Fragment implements IMeterChangedNotifier {
    CApplication mApp;
    CurrentContext mCC;

    MeterLoadState meterLoadState = new MeterLoadState();
    SoapAsyncTask mTask;

    List<WaterLog> waterLogs = new ArrayList<WaterLog>();

    SwipeRefreshLayout refreshLayout;
    ExpandableListView listView;
    WaterLogListAdapter adapter;

    TextView footerTextView;

    public WaterLogFragment() {
        // Required empty public constructor
        mApp = AppUtils.App;
        mCC = mApp.getCurrentContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_water_log, container, false);
        refreshLayout = (SwipeRefreshLayout)v;
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                executeTask();
            }
        });

        listView = (ExpandableListView)v.findViewById(R.id.expandableListView);
        adapter = new WaterLogListAdapter(getActivity(), waterLogs);
        listView.setAdapter(adapter);
        listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
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
                //adapter.clear();
                JSONArray waterLogArray = obj.getJSONArray("Data");
                waterLogs.clear();
                for(int i = 0; i < waterLogArray.length(); i++) {
                    WaterLog cb = WaterLog.parse(waterLogArray.getJSONObject(i));
                    waterLogs.add(cb);
                }
                footerTextView.setText("数量：" + String.valueOf(waterLogs.size()) + " 显示最近一年的历史记录");
                adapter = new WaterLogListAdapter(getActivity(), waterLogs);
                listView.setAdapter(adapter);
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

        Log.d("CustomerBill", "executeTask");

        mTask = new SoapAsyncTask(SoapClient.GET_WATER_LOGS_METHOD, listener);
        mTask.execute(meterLoadState.MeterID);
    }


    class WaterLogListAdapter extends BaseExpandableListAdapter {
        List<WaterLog> mGroup;
        LayoutInflater mInflater;
        int mDips;

        public WaterLogListAdapter(Context context, List<WaterLog> objects) {
            //super(context, R.layout.customerbill_item_card, objects);
            mDips = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics());
            mGroup = objects;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getGroupCount() {
            return mGroup.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mGroup.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mGroup.get(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupViewHolder holder;
            if(convertView != null) {
                holder = (GroupViewHolder)convertView.getTag();
            } else {
                holder = new GroupViewHolder();
                convertView = mInflater.inflate(R.layout.waterlog_item_card_parent, null);

                holder.logDate = (TextView) convertView.findViewById(R.id.logDate);
                holder.waterQuantity = (TextView)convertView.findViewById(R.id.waterQuantity);
                holder.price = (TextView)convertView.findViewById(R.id.price);
                holder.currentPay = (TextView)convertView.findViewById(R.id.currentPay);
                holder.logDate.setPadding(mDips, 0, 0, 0);
                convertView.setTag(holder);
            }
            WaterLog cb = (WaterLog)getGroup(groupPosition);
            holder.logDate.setText(cb.LogDate);
            holder.waterQuantity.setText(cb.WaterQuantity);
            holder.price.setText(cb.Price + (cb.IsTieredWater ? " [阶梯水价]": ""));
            holder.currentPay.setText(cb.CurrentPay);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            DetailViewHolder holder;
            if(convertView != null) {
                holder = (DetailViewHolder)convertView.getTag();
            } else {
                holder = new DetailViewHolder();
                convertView = mInflater.inflate(R.layout.waterlog_item_card, null);

                holder.logDate = (EditText)convertView.findViewById(R.id.logDate);
                holder.currentTotalWaterQuantity = (EditText)convertView.findViewById(R.id.currentTotalWaterQuantity);
                holder.priceTitle = (EditText)convertView.findViewById(R.id.priceTitle);
                holder.peoples = (EditText)convertView.findViewById(R.id.peoples);
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
            WaterLog cb = (WaterLog)getChild(groupPosition, childPosition);
            holder.logDate.setText(cb.LogDate);
            holder.currentTotalWaterQuantity.setText(cb.CurrentTotalWaterQuantity);
            holder.priceTitle.setText(cb.PriceTitle);
            holder.peoples.setText(cb.Peoples);
            holder.waterQuantity.setText(cb.WaterQuantity);
            holder.price.setText(cb.Price + (cb.IsTieredWater ? " [阶梯水价]": ""));
            holder.currentPay.setText(cb.CurrentPay);
            holder.lastDate.setText(cb.LastDate);
            holder.lastValue.setText(cb.LastValue);
            holder.currentDate.setText(cb.CurrentDate);
            holder.currentValue.setText(cb.CurrentValue);
            holder.baseValue.setText(cb.BaseValue);
            holder.serialValue.setText(cb.SerialValue);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        private class GroupViewHolder {
            TextView logDate;
            TextView price;
            TextView waterQuantity;
            TextView currentPay;
        }

        private class DetailViewHolder {
            EditText logDate;
            EditText currentTotalWaterQuantity;

            EditText priceTitle;
            EditText peoples;

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
