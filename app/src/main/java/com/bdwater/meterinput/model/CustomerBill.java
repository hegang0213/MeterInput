package com.bdwater.meterinput.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hegang on 16/6/23.
 */
public class CustomerBill {
    public String BillDate ;
    public String PriceTitle ;
    public String Price ;
    public String LastDate ;
    public String LastValue ;
    public String CurrentDate ;
    public String CurrentValue ;
    public String BaseValue ;
    public String SerialValue ;
    public String WaterQuantity ;
    public String Compensation ;
    public String CurrentPay ;
    public String PayBefore ;

    public static CustomerBill parse(JSONObject obj) throws JSONException {
        CustomerBill cb = new CustomerBill();
        cb.BillDate = obj.getString("BillDate");
        cb.PriceTitle = obj.getString("PriceTitle");
        cb.Price = obj.getString("Price");
        cb.LastDate = obj.getString("LastDate");
        cb.LastValue = obj.getString("LastValue");
        cb.CurrentDate = obj.getString("CurrentDate");
        cb.CurrentValue = obj.getString("CurrentValue");
        cb.BaseValue = obj.getString("BaseValue");
        cb.SerialValue = obj.getString("SerialValue");
        cb.WaterQuantity = obj.getString("WaterQuantity");
        cb.Compensation = obj.getString("Compensation");
        cb.CurrentPay = obj.getString("CurrentPay");
        cb.PayBefore = obj.getString("PayBefore");
        return cb;
    }
}
