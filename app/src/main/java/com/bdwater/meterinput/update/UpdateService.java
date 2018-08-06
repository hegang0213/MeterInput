package com.bdwater.meterinput.update;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.bdwater.meterinput.base.NetworkUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by hegang on 16/7/5.
 */
public class UpdateService {
    private Context context;
    private ProgressDialog progressDialog;
    private UpdateInfo updateInfo;
    private String filename = "update.apk";

    public UpdateService(Context context) {
        this.context = context;
    }

    public void getUpdateInfo() throws Exception {
        String path = NetworkUtils.UPDATE_URL;
        StringBuffer sb = new StringBuffer();
        String line = null;
        BufferedReader reader = null;
        try {
            // 创建一个url对象
            URL url = new URL(path);
            // 通過url对象，创建一个HttpURLConnection对象（连接）
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            // 通过HttpURLConnection对象，得到InputStream
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            // 使用io流读取文件
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            // parse json string to UpdateInfo object
            String jsonString = sb.toString();
            UpdateInfo info = new UpdateInfo();
            JSONObject jsonObject = new JSONObject(jsonString);
            info.versionCode = jsonObject.getInt("versionCode");
            info.url  = jsonObject.getString("url");
            info.description = jsonObject.getString("description");
            this.updateInfo = info;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("升级应用程序");
        builder.setMessage(updateInfo.description);
        builder.setCancelable(false);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    dialog.dismiss();
                    showDownloadDialog(updateInfo.url);     //在下面的代码段
                } else {
                    Toast.makeText(context, "SD卡不可用，请插入SD卡",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }

        });
        builder.create().show();
    }

    public boolean isNeedUpdate() {

        //String v = updateInfo.getVersion(); // 最新版本的版本号
        Log.i("update", "remote version code:" + Integer.toString(updateInfo.versionCode));
        Log.i("update", "local version code:" + Integer.toString(getVersion()));
        return (updateInfo.versionCode > getVersion());
    }

    // 获取当前版本的版本号
    private int getVersion() {
        try {
            return NetworkUtils.getVersionCode(context);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.i("update", "NameNotFoundException");
            return -1;
        }
    }

    private void showDownloadDialog(final String urlString) {
        progressDialog = new ProgressDialog(context);    //进度条，在下载的时候实时更新进度，提高用户友好度
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("正在下载");
        progressDialog.setMessage("请稍候...");
        progressDialog.setProgress(0);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream output = null;
                try {
                    File file = new File(Environment.getExternalStorageDirectory(), filename);
                    //创建HttpURL连接
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == 200) {
                        int max = conn.getContentLength();
                        //设置进度条对话框的最大值
                        progressDialog.setMax(max);
                        int count = 0;
                        InputStream input = conn.getInputStream();
                        output = new FileOutputStream(file);
                        byte[] buffer = new byte[4 * 1024];
                        int len = 0;
                        while ((len = input.read(buffer)) != -1) {
                            output.write(buffer, 0, len);
                            //设置进度条对话框进度
                            count = count + len;
                            progressDialog.setProgress(count);
                        }
                        output.flush();
                        update();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        if(output != null)
                            output.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();

    }

    //安装文件，一般固定写法
    void update() {
        progressDialog.dismiss();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), filename)),
                "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
