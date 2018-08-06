package com.bdwater.meterinput.soap;

import org.json.JSONObject;

/**
 * Created by hegang on 16/6/17.
 */

public interface ISoapAsyncTaskListener {
    void onTaskSuccess(JSONObject obj);
    void OnTaskFailed(Integer result, String message);
//    void onTaskCancel();
    void onTaskException(Exception exception);
}