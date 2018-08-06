package com.bdwater.meterinput.metertask;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.bdwater.meterinput.CApplication;
import com.bdwater.meterinput.R;
import com.bdwater.meterinput.base.AppUtils;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.model.Meter;
import com.bdwater.meterinput.model.MeterTask;
import com.bdwater.meterinput.model.WaterStatus;
import com.bdwater.meterinput.soap.ISoapAsyncTaskListener;
import com.bdwater.meterinput.soap.SoapAsyncTask;
import com.bdwater.meterinput.soap.SoapClient;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class MeterTaskActivityFragment extends Fragment {

    CApplication cApp;
    CurrentContext cc;

    Meter meter;

    View progressBar;

    EditText starter;
    EditText customerNo;
    Spinner startNote;
    EditText comment;

    Button submit;

    String[] startNodeArray = new String[] {
            "水表污损",
            "表腿漏水",
            "管道跑水",
            "换防盗闸",
            "更换普通闸",
            "表井维修",
            "其它原因"
    };

    SoapAsyncTask task;

    public MeterTaskActivityFragment() {
        cApp = AppUtils.App;
        cc = cApp.getCurrentContext();
        meter = cc.getCurrentMeter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_meter_task, container, false);

        progressBar = v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        starter = (EditText)v.findViewById(R.id.starter);
        customerNo = (EditText)v.findViewById(R.id.customerNo);
        startNote = (Spinner)v.findViewById(R.id.startNote);
        comment = (EditText)v.findViewById(R.id.comment);

        submit = (Button)v.findViewById(R.id.submit);
        submit.setOnClickListener(submitClickListener);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                startNodeArray);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        startNote.setAdapter(adapter);
        starter.setText(cc.getUser().Name);
        customerNo.setText(meter.CustomerNo);

        return v;
    }

    Button.OnClickListener submitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MeterTask mt = new MeterTask();
            mt.MeterID = meter.MeterID;
            mt.Starter = cc.getUser().Name;
            mt.StartNote = startNote.getSelectedItem().toString();
            mt.Comment = comment.getText().toString();

            try {
                String para = mt.toUpdateJsonString();

                task = new SoapAsyncTask(SoapClient.UPDATE_METER_TASK_METHOD, listener);
                task.execute(para);

                submitting();
            } catch (JSONException e) {
                e.printStackTrace();
                submitted();

                AppUtils.showAlertDialog(getActivity(), "错误", e.getMessage());
            }
        }
    };

    private void submitting() {
        AppUtils.closeSoftInput(getActivity());
        progressBar.setVisibility(View.VISIBLE);

        startNote.setEnabled(false);
        submit.setEnabled(false);
    }
    private void submitted() {
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);

                startNote.setEnabled(true);
                submit.setEnabled(true);
            }
        });

    }

    ISoapAsyncTaskListener listener = new ISoapAsyncTaskListener() {
        @Override
        public void onTaskSuccess(JSONObject obj) {
            submitted();
            AppUtils.showAlertDialog(getActivity(), "完成", "此表的维修记录已经提交");
        }

        @Override
        public void OnTaskFailed(Integer result, String message) {
            submitted();
            AppUtils.showAlertDialog(getActivity(), "失败", message);

        }

        @Override
        public void onTaskException(Exception exception) {
            submitted();
            AppUtils.showAlertDialog(getActivity(), "错误", exception.getMessage());
        }
    };
}
