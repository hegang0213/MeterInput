package com.bdwater.meterinput.base;

/**
 * Created by hegang on 16/6/23.
 */
public class NumberUtils {
    public static int toInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public static double toDouble(String s, double defaultValue) {
        try{
            return Double.parseDouble(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
