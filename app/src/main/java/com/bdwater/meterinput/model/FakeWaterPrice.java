package com.bdwater.meterinput.model;

/**
 * Created by hegang on 16/6/21.
 */
public class FakeWaterPrice {
    public String WaterPriceID;
    public Double Price;

    @Override
    public String toString() {
        return Price.toString();
    }
}
