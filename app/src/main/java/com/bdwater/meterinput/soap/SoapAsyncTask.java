package com.bdwater.meterinput.soap;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by hegang on 16/6/15.
 */
public class SoapAsyncTask extends AsyncTask<String, Void, String> {

    private ISoapAsyncTaskListener TaskListener;
    private String SoapMethodName;
    private Boolean HasException = false;

    public SoapAsyncTask(String soapMethodName, ISoapAsyncTaskListener listener) {
        super();
        this.SoapMethodName = soapMethodName;
        this.TaskListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        HasException = false;
        String result = null;
        try {
            if (this.SoapMethodName.equals(SoapClient.LOGIN_METHOD)) {
                return SoapClient.Login(params[0], params[1]);
            } else if(this.SoapMethodName.equals(SoapClient.LOGIN_WITH_CURRENT_METHOD)) {
                return SoapClient.LoginWithCurrentBook(params[0], params[1], params[2]);
            } else if(this.SoapMethodName.equals(SoapClient.GET_METERS_METHOD)) {
                return SoapClient.GetMeters(params[0]);
            } else if(this.SoapMethodName.equals(SoapClient.GET_METERS_BY_QUERY)) {
                return SoapClient.GetMetersByQuery(params[0], params[1]);
            } else if(this.SoapMethodName.equals(SoapClient.GET_METER_BYID_METHOD)) {
                return SoapClient.GetMeterByID(params[0]);
            } else if(this.SoapMethodName.equals(SoapClient.GET_CUSTOMER_BILLS_METHOD)) {
                return SoapClient.GetCustomerBills(params[0]);
            } else if(this.SoapMethodName.equals(SoapClient.GET_WATER_LOGS_METHOD)) {
                return SoapClient.GetWaterLogs(params[0]);
            } else if(this.SoapMethodName.equals(SoapClient.GET_CUSTOMER_BY_METER_ID_METHOD)) {
                return SoapClient.GetCustomerByMeterID(params[0]);
            } else if(this.SoapMethodName.equals(SoapClient.UPDATE_METER_METHOD)) {
                return SoapClient.UpdateMeter(params[0]);
            } else if(this.SoapMethodName.equals(SoapClient.UPDATE_FAKE_METER_METHOD)) {
                return SoapClient.UpdateFakeMeter(params[0]);
            } else if(this.SoapMethodName.equals(SoapClient.UPDATE_METER_TASK_METHOD)) {
                return SoapClient.UpdateMeterTask(params[0]);
            } else if(this.SoapMethodName.equals(SoapClient.DELETE_METER_TASK_METHOD)) {
                return SoapClient.DeleteMeterTask(params[0]);
            } else if(this.SoapMethodName.equals(SoapClient.GET_APPLICATION_METER_TASKS_METHOD)) {
                return SoapClient.GetApplicationMeterTasks(params[0]);
            } else if(this.SoapMethodName.equals(SoapClient.UPDATE_METER_COMMENT_METHOD)) {
                return SoapClient.UpdateComment(params[0], params[1], params[2]);
            } else {
                throw new Exception("No this method name:" + this.SoapMethodName);
            }

        } catch (IOException e) {
            e.printStackTrace();
            onTaskException(e);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            onTaskException(e);
        } catch (Exception e) {
            e.printStackTrace();
            onTaskException(e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        if(HasException)
            return;

        try {
            JSONObject jsonObject = new JSONObject(s);
            Integer result = jsonObject.getInt("Result");
            switch (result) {
                case 0:
                    onTaskSuccess(jsonObject);
                    break;
                default:
                    String message = jsonObject.getString("Data");
                    onTaskFailed(result, message);
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            onTaskException(e);
        }

    }

    private void onTaskSuccess(JSONObject data) {
        if(null != this.TaskListener)
            this.TaskListener.onTaskSuccess(data);
    }
    private void onTaskException(Exception exception) {
        HasException = true;
        if(null != this.TaskListener)
            this.TaskListener.onTaskException(exception);
    }

    private void onTaskFailed(Integer result, String message) {
        if(null != this.TaskListener)
            this.TaskListener.OnTaskFailed(result, message);
    }
}