package com.bdwater.meterinput.base;

import com.bdwater.meterinput.model.FakeMeter;

/**
 * Created by hegang on 16/6/22.
 */
public class JsonUpdateMeter {
    public String MeterID;
    public boolean IsFake;
    public int CurrentValue;
    public int MeterStatus;
    public int BaseValue;
    public boolean IsSerialMeter;
    public int SerialValue;
    public int CheckType;

    public FakeMeter[] FakeMeters;
}

