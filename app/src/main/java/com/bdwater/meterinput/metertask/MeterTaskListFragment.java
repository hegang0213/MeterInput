package com.bdwater.meterinput.metertask;


import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bdwater.meterinput.R;
import com.bdwater.meterinput.base.AppUtils;
import com.bdwater.meterinput.model.MeterTask;
import com.bdwater.meterinput.model.WaterLog;
import com.bdwater.meterinput.soap.ISoapAsyncTaskListener;
import com.bdwater.meterinput.soap.SoapAsyncTask;
import com.bdwater.meterinput.soap.SoapClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeterTaskListFragment extends Fragment {

    SwipeRefreshLayout refreshLayout;
    View progressBar;
    View hint;
    ExpandableListView listView;

    View actionView;
    Button intoEditModeButton;
    Button exitEditModeButton;
    Button deleteButton;

    ExpandableListAdapter adapter;

    SoapAsyncTask task;
    Integer taskType = METER_TASK_LIST;
    private static final int METER_TASK_LIST = 0;
    private static final int DELETE_METER_TASKS = 937;

    List<MeterTask> meterTasks = new ArrayList<MeterTask>();

    public MeterTaskListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_meter_task_list, container, false);
        refreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.refreshLayout);
        progressBar = v.findViewById(R.id.progressBar);
        hint = v.findViewById(R.id.hint);
        refreshLayout.setOnRefreshListener(refreshListener);

        listView = (ExpandableListView)v.findViewById(R.id.expandableListView);
        listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                refreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                Log.d("MeterTaskList", "refreshLayout enabled:" + String.valueOf(refreshLayout.isEnabled()));
            }
        });

        actionView = v.findViewById(R.id.actionView);
        actionView.setVisibility(View.GONE);
        intoEditModeButton = (Button)v.findViewById(R.id.intoEditModeButton);
        intoEditModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intoEditModeButton.setVisibility(View.GONE);
                exitEditModeButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);

                adapter.setEditable(true);
                adapter.notifyDataSetChanged();
            }
        });
        exitEditModeButton = (Button)v.findViewById(R.id.exitEditModeButton);
        exitEditModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intoEditModeButton.setVisibility(View.VISIBLE);
                exitEditModeButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);

                adapter.setEditable(false);
                adapter.notifyDataSetChanged();
            }
        });
        deleteButton = (Button)v.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MeterTask[] checkedItems = adapter.getCheckedItems();
                if(checkedItems.length == 0) {
                    AppUtils.showAlertDialog(getActivity(), "提示", "请勾选相应的选项后再进行操作");
                    return;
                } else
                    AppUtils.showAlertDialog(getActivity(), "确认", "您是否确定要删除已选择的申请？",
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String idListString = "";
                            for(int i = 0; i < checkedItems.length; i++) {
                                idListString += checkedItems[i].MeterTaskID + ",";
                            }
                            if(idListString.length() > 0)
                                idListString = idListString.substring(0, idListString.length() - 1);

                            progressBar.setVisibility(View.VISIBLE);
                            taskType = DELETE_METER_TASKS;
                            task = new SoapAsyncTask(SoapClient.DELETE_METER_TASK_METHOD, listener);
                            task.execute(idListString);
                            //AppUtils.showAlertDialog(getActivity(), "", idListString);
                        }
                    });

            }
        });

        return v;
    }
    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            load();
        }
    };

    private void load() {
        taskType = METER_TASK_LIST;
        task = new SoapAsyncTask(SoapClient.GET_APPLICATION_METER_TASKS_METHOD, listener);
        task.execute(AppUtils.App.getCurrentContext().getUser().Name);
        actionView.setVisibility(View.GONE);
    }
    private void loaded() {
        hint.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        refreshLayout.setRefreshing(false);
        actionView.setVisibility(View.VISIBLE);
    }
    private void fillData(List<MeterTask> meterTasks) {
        boolean editable = false;
        if(adapter != null)
            editable = adapter.getEditable();

        adapter = new ExpandableListAdapter(getActivity(), meterTasks);
        adapter.setEditable(editable);
        listView.setAdapter(adapter);
    }

    ISoapAsyncTaskListener listener = new ISoapAsyncTaskListener() {
        @Override
        public void onTaskSuccess(JSONObject obj) {
            loaded();
            try {
                if(taskType == METER_TASK_LIST) {
                    JSONArray meterTaskArray = obj.getJSONArray("Data");
                    meterTasks.clear();
                    for (int i = 0; i < meterTaskArray.length(); i++) {
                        MeterTask mt = new MeterTask();
                        mt.parseFrom(meterTaskArray.getJSONObject(i));
                        meterTasks.add(mt);
                    }
                    fillData(meterTasks);
                } else {
                    MeterTask[] deletedItems = adapter.getCheckedItems();
                    for (MeterTask mt : deletedItems) {

                        try {
                            meterTasks.remove(mt);
                        } catch(Exception ex){

                        }
                    }
                    boolean editable = adapter.getEditable();
                    adapter = new ExpandableListAdapter(getActivity(), meterTasks);
                    adapter.setEditable(editable);
                    listView.setAdapter(adapter);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void OnTaskFailed(Integer result, String message) {
            loaded();
            AppUtils.showAlertDialog(getActivity(), "失败", message);
        }

        @Override
        public void onTaskException(Exception exception) {
            loaded();
            AppUtils.showAlertDialog(getActivity(), "错误", exception.getMessage());
        }
    };

    class ExpandableListAdapter extends BaseExpandableListAdapter {
        List<MeterTask> mGroup;
        LayoutInflater mInflater;
        int mDips;
        boolean mEditable = false;
        HashMap<MeterTask, Boolean> mChecked = new HashMap<>();

        public void setEditable(boolean mEditable) {
            this.mEditable = mEditable;
        }
        public boolean getEditable() {
            return this.mEditable;
        }
        public MeterTask[] getCheckedItems() {
            int size = mChecked.size();
            return mChecked.keySet().toArray(new MeterTask[size]);
        }

        public ExpandableListAdapter(Context context, List<MeterTask> objects) {
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
                convertView = mInflater.inflate(R.layout.meter_task_application_parent_list_item, null);

                holder.startDateEdit = (CheckBox)convertView.findViewById(R.id.startDateEdit);
                holder.startDateEdit.setOnCheckedChangeListener(listener);
                holder.startDate = (TextView)convertView.findViewById(R.id.startDate);
                holder.customerNo = (TextView)convertView.findViewById(R.id.customerNo);
                holder.startNote = (TextView)convertView.findViewById(R.id.startNote);
                AppUtils.setMargins(holder.startDateEdit, mDips, 0, 0, 0);
                AppUtils.setMargins(holder.startDate, mDips, 0, 0, 0);
                convertView.setTag(holder);
            }
            MeterTask entity = (MeterTask)getGroup(groupPosition);

            holder.startDateEdit.setVisibility(View.GONE);
            holder.startDateEdit.setText(entity.StartDate);
            holder.startDateEdit.setTag(entity);

            holder.startDate.setText(entity.StartDate);
            holder.startDate.setVisibility(View.GONE);
            holder.customerNo.setText(entity.CustomerNo);
            holder.startNote.setText(entity.StartNote);
            if(mEditable) {
                holder.startDateEdit.setVisibility(View.VISIBLE);
                holder.startDate.setVisibility(View.GONE);
            } else {
                holder.startDateEdit.setVisibility(View.GONE);
                holder.startDate.setVisibility(View.VISIBLE);
            }
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            DetailViewHolder holder;
            if(convertView != null) {
                holder = (DetailViewHolder)convertView.getTag();
            } else {
                holder = new DetailViewHolder();
                convertView = mInflater.inflate(R.layout.meter_task_application_list_item, null);

                holder.customerName = (EditText)convertView.findViewById(R.id.customerName);
                holder.comment = (EditText)convertView.findViewById(R.id.comment);
                convertView.setTag(holder);
            }
            MeterTask entity = (MeterTask) getChild(groupPosition, childPosition);
            holder.customerName.setText(entity.CustomerName);
            holder.comment.setText(entity.Comment);
            return convertView;
        }
        CheckBox.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.getTag() == null) return;
                MeterTask mt = (MeterTask)buttonView.getTag();
                if(isChecked) {
                    if(!mChecked.containsKey(mt))
                        mChecked.put(mt, true);
                } else {
                    if(mChecked.containsKey(mt))
                        mChecked.remove(mt);
                }


            }
        };

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        private class GroupViewHolder {
            CheckBox startDateEdit;
            TextView startDate;
            TextView customerNo;
            TextView startNote;
        }

        private class DetailViewHolder {
            EditText customerName;
            EditText comment;
        }
    }
}
