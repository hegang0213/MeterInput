package com.bdwater.meterinput.base;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by hegang on 16/7/5.
 */
public class NetworkUtils {
//    public static String WEBSERVICE_URL = "http://121.18.65.239/waterinputservicetest/webservice.asmx";
    public static String WEBSERVICE_URL = "http://121.18.65.239:50080/waterinputservice/webservice.asmx";
    public static final String WEBSERVICE_NAMESPACE = "http://tempuri.org/";
    public static String UPDATE_URL = "http://121.18.65.239:50080/waterinputservice/update/update.json";

    private static int current_url_index = -1;
    private static String[] lineNames = new String[] {
            "电信1",
            "联通1"
    };
    private static String[] webservice_urls = new String[] {
//            "http://121.18.65.239/waterinputservicetest/webservice.asmx"
            "http://222.222.178.213:1111/webservice.asmx",
            "http://121.18.65.239:50080/waterinputservice/webservice.asmx"
    };
    private static String[] update_urls = new String[] {
            "http://222.222.178.213:1111/update/update.json",
            "http://121.18.65.239:50080/waterinputservice/update/update.json"
    };

    public static int getCurrentUrlIndex() {
        return current_url_index;
    }
    public static int webUrlCount() {
        return webservice_urls.length;
    }
    public static void setWebUrl() {
        if(current_url_index == -1)
            current_url_index = 0;
        else
            current_url_index = 1;
        WEBSERVICE_URL = webservice_urls[current_url_index];
        UPDATE_URL = update_urls[current_url_index];
    }
    public static String getLineName() {
        if(current_url_index == -1) return "";
        return lineNames[current_url_index];
    }

    public static int getVersionCode(Context context) throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(
                context.getPackageName(), 0);
        return packageInfo.versionCode;
    }
    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
