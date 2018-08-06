package com.bdwater.meterinput.model;

import com.bdwater.meterinput.base.DoubleHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by hegang on 16/6/14.
 */
public class Meter implements Serializable {
    public String MeterID;
    public String BookID;
    public String CardIndex;
    public String Name;
    public Boolean IsPaid;

    public String CustomerID;
    public String CustomerNo;
    public String MeterPosition;

    public Boolean IsFake;
    public Boolean IsSerialMeter;
    public Integer WaterStatus;
    public Integer MeterStatus;
    public Boolean HasCustomerBill;

    public Double Deposit;

    public String LastDate;
    public Integer LastValue;
    public String CurrentDate;
    public Integer CurrentValue;
    public Integer BaseValue;
    public Integer SerialValue;
    public Integer WaterQuantity;
    public String Price;
    public String CurrentPay;
    public String PayBefore;
    public Integer MeterTaskStatus;
    public Integer MeterTaskBaseValue;
    public Integer CurrentTotalWaterQuantity;
    public String CurrentComment;
    public String Comment;
    public Integer AverageWater;

    private Double fakePay = 0d;
    private FakeMeter[] fakeMeters = new FakeMeter[] {};

    public void setFakeMeters(FakeMeter[] fakeMeters) {
        this.fakeMeters = fakeMeters;
        Double fp = 0d;
        for(FakeMeter fm : fakeMeters) {
            fp = DoubleHelper.add(fp, fm.Pay);
        }
        fakePay = fp;
    }

    public FakeMeter[] getFakeMeters() {
        return fakeMeters;
    }

    public Double getFakePay() {
        return fakePay;
    }

    @Override
    public String toString() {
        String s = "";
        if(this.WaterStatus != 0)
            s = "[已抄]";
        if(this.IsFake)
            s += "<补差>";
        s += this.CustomerNo;
        return s + " - " + this.Name;
    }

    public static void Load(Meter meter, JSONObject jsonMeter) throws JSONException {
        meter.CustomerID = jsonMeter.getString("CustomerID");
        meter.CustomerNo = jsonMeter.getString("CustomerNo");
        meter.MeterPosition = jsonMeter.getString("MeterPosition");
        meter.IsPaid = jsonMeter.getBoolean("IsPaid");
        meter.Deposit = jsonMeter.getDouble("Deposit");

        meter.IsFake = jsonMeter.getBoolean("IsFake");
        meter.LastDate = jsonMeter.getString("LastDate").substring(0, 10);
        meter.LastValue = jsonMeter.getInt("LastValue");
        meter.CurrentDate = jsonMeter.getString("CurrentDate");
        meter.CurrentValue = jsonMeter.getInt("CurrentValue");
        meter.WaterStatus = jsonMeter.getInt("WaterStatus");
        meter.MeterStatus = jsonMeter.getInt("MeterStatus");
        meter.BaseValue = jsonMeter.getInt("BaseValue");
        meter.IsSerialMeter = jsonMeter.getBoolean("IsSerialMeter");
        meter.SerialValue = jsonMeter.getInt("SerialValue");
        meter.WaterQuantity = jsonMeter.getInt("WaterQuantity");
        meter.Price = jsonMeter.getString("Price");
        meter.CurrentPay = jsonMeter.getString("CurrentPay");
        meter.PayBefore = jsonMeter.getString("PayBefore");
        meter.Comment = jsonMeter.getString("Comment");
        meter.AverageWater = jsonMeter.getInt("AverageWater");

        if(meter.CurrentDate.equals("null"))
            meter.CurrentDate = "";
        else if(meter.CurrentDate.length() > 10)
            meter.CurrentDate = meter.CurrentDate.substring(0, 10);


        if(meter.PayBefore.equals("null")) {
            meter.PayBefore = "";
        } else if (meter.PayBefore.length() > 10) {
            meter.PayBefore = meter.PayBefore.substring(0, 10);
        }

//        if(meter.WaterStatus != 0 && !meter.HasCustomerBill)
//            meter.IsPaid = true;

        meter.MeterTaskStatus = jsonMeter.getInt("MeterTaskStatus");
        meter.MeterTaskBaseValue = jsonMeter.getInt("MeterTaskBaseValue");

        meter.CurrentTotalWaterQuantity = jsonMeter.getInt("CurrentTotalWaterQuantity");
        meter.CurrentComment = jsonMeter.getString("CurrentComment");

        // load fake meters
        JSONArray jsonFakeMeters = jsonMeter.getJSONArray("FakeMeters");
        int length = jsonFakeMeters.length();
        FakeMeter[] fakeMeters = new FakeMeter[length];
        for(int i = 0; i < length; i++) {
            FakeMeter fm = new FakeMeter();
            JSONObject jsonFm = jsonFakeMeters.getJSONObject(i);
            fm.WaterPriceID = jsonFm.getString("WaterPriceID");
            fm.Title = jsonFm.getString("Title");
            fm.Price = jsonFm.getString("Price");
            fm.FakePrice = jsonFm.getDouble("FakePrice");
            fm.WaterQuantity = jsonFm.getDouble("WaterQuantity");
            fm.Pay = jsonFm.getDouble("Pay");
            fakeMeters[i] = fm;
        }
        meter.setFakeMeters(fakeMeters);
    }
    public static Meter parseSimple(JSONObject jsonSimpleMeter) throws JSONException {
        Meter meter = new Meter();
        meter.MeterID = jsonSimpleMeter.getString("MeterID");
        meter.BookID = jsonSimpleMeter.getString("BookID");
        meter.CardIndex  = jsonSimpleMeter.getString("CardIndex");
        meter.Name = jsonSimpleMeter.getString("Name");
        meter.CustomerNo = jsonSimpleMeter.getString("CustomerNo");
        meter.WaterStatus = jsonSimpleMeter.getInt("WaterStatus");
        meter.IsFake = jsonSimpleMeter.getBoolean("IsFake");
        meter.MeterTaskStatus = jsonSimpleMeter.getInt("MeterTaskStatus");
        meter.HasCustomerBill = jsonSimpleMeter.getBoolean("HasCustomerBill");

        if(meter.WaterStatus != 0 && !meter.HasCustomerBill)
            meter.IsPaid = true;
        return meter;
    }


    public FakeMeter[] FakeMeters;
    public String toJsonUpdateMeter() throws JSONException {
        JSONObject meter = new JSONObject();
        meter.put("MeterID", this.MeterID);
        meter.put("IsFake", this.IsFake);
        meter.put("CurrentValue", this.CurrentValue);
        meter.put("MeterStatus", this.MeterStatus);
        meter.put("BaseValue", this.BaseValue);
        meter.put("IsSerialMeter", this.IsSerialMeter);
        meter.put("SerialValue", this.SerialValue);
        meter.put("CheckType", 101);
        meter.put("CurrentComment", this.CurrentComment);

        JSONArray array = new JSONArray();
        for (FakeMeter fakeMeter: fakeMeters) {
            JSONObject jsonFakeMeter = new JSONObject();
            jsonFakeMeter.put("Title", fakeMeter.Title);
            jsonFakeMeter.put("WaterPriceID", fakeMeter.WaterPriceID);
            jsonFakeMeter.put("Price", fakeMeter.Price);
            jsonFakeMeter.put("WaterQuantity", fakeMeter.WaterQuantity);
            jsonFakeMeter.put("Pay", fakeMeter.Pay);
            array.put(jsonFakeMeter);
        }
        meter.put("FakeMeters", array);
        return meter.toString();
    }
    public String toJsonFakeMeter() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("MeterID", this.MeterID);
        obj.put("IsFake", this.IsFake);
        obj.put("Title", this.Name);
        obj.put("FakeWaterPriceID", fakeMeters[0].WaterPriceID);
        obj.put("Pay", fakeMeters[0].Pay);
        obj.put("CheckType", 1);
        return obj.toString();
    }
}
