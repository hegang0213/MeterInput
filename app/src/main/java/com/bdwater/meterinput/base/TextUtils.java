package com.bdwater.meterinput.base;

/**
 * Created by hegang on 16/6/25.
 */
public class TextUtils {
    public static boolean isEmpty(String s) {
        if(s == null) return true;
        if(s.trim().length() == 0) return true;
        return false;
    }
}
