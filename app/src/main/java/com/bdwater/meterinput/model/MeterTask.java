package com.bdwater.meterinput.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hegang on 16/7/20.
 */
public class MeterTask {
    public String CustomerName;
    public String CustomerNo;
    public String MeterNum;

    public String MeterID;
    public String Starter;
    public String StartNote;
    public String Comment;

    public String MeterTaskID;
    public String MeterTaskTypeName;
    public String MeterTaskStatusName;

    public String StartDate;
    public String StartMeterValue;

    public String Approver;
    public String ApproveDate;
    public String ApproveNote;

    public String Ender;
    public String EndDate;
    public String EndNote;
    public String EndMeterValue;

    public String toUpdateJsonString() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("MeterID", this.MeterID);
        obj.put("Starter", this.Starter);
        obj.put("StartNote", this.StartNote);
        obj.put("Comment", this.Comment);
        return obj.toString();
    }

    public void parseFrom(JSONObject obj) throws JSONException {
        this.CustomerNo = obj.getString("CustomerNo");
        this.CustomerName = obj.getString("CustomerName");

        this.MeterTaskID = obj.getString("MeterTaskID");
        this.StartDate = obj.getString("StartDate");
        this.StartNote = obj.getString("StartNote");
        this.StartMeterValue = obj.getString("StartMeterValue");
        this.Comment = obj.getString("Comment");
    }
}
