package com.bdwater.meterinput.main;

import com.bdwater.meterinput.model.FakeMeter;

/**
 * Created by hegang on 16/6/18.
 */
public interface IMeterChangedNotifier {
    void notifyDataChanged();
    void notifyFakeMeterChanged(FakeMeter[] fakeMeters);
}
