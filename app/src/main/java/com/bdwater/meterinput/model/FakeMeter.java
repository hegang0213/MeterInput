package com.bdwater.meterinput.model;

/**
 * Created by hegang on 16/6/20.
 */
public class FakeMeter {
    public String Title;
    public String WaterPriceID;
    public String Price;
    public Double FakePrice;
    public Double WaterQuantity;
    public Double Pay;

    public static FakeMeter clone(FakeMeter fm) {
        FakeMeter clone = new FakeMeter();
        clone.Title = fm.Title;
        clone.WaterPriceID = fm.WaterPriceID;
        clone.Price = fm.Price;
        clone.FakePrice = fm.FakePrice;
        clone.WaterQuantity = fm.WaterQuantity;
        clone.Pay = fm.Pay;
        return clone;
    }

    public static FakeMeter createForOnlyFake() {
        FakeMeter result = new FakeMeter();
        result.Title = "";
        result.WaterPriceID = "";
        result.Price = "";
        result.FakePrice = 0d;
        result.WaterQuantity = 0d;
        result.Pay = 0d;
        return result;
    }
}
