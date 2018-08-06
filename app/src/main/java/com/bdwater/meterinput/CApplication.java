package com.bdwater.meterinput;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.bdwater.meterinput.model.Book;
import com.bdwater.meterinput.base.CurrentContext;
import com.bdwater.meterinput.model.Meter;
import com.bdwater.meterinput.model.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;


 /**
 * Created by hegang on 16/6/14.
 */
public class CApplication extends Application {

    private CurrentContext _currentContext;
    public CurrentContext getCurrentContext() {
        if(null == _currentContext)
            _currentContext = new CurrentContext();
        return _currentContext;
    }

    private SharedPreferences getMySharedPreferences() {
        return getSharedPreferences("root", Context.MODE_PRIVATE);
    }
    private void setObject(String key, Object object) {
        SharedPreferences sp = getMySharedPreferences();

        //创建字节输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //创建字节对象输出流
        ObjectOutputStream out = null;
        try {
            //然后通过将字对象进行64转码，写入key值为key的sp中
            out = new ObjectOutputStream(baos);
            out.writeObject(object);
            String objectVal = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, objectVal);
            editor.commit();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressWarnings("unchecked")
    public <T> T getObject(String key, Class<T> clazz) {
        SharedPreferences sp = getMySharedPreferences();
        if (sp.contains(key)) {
            String objectVal = sp.getString(key, null);
            byte[] buffer = Base64.decode(objectVal, Base64.DEFAULT);
            //一样通过读取字节流，创建字节流输入流，写入对象并作强制转换
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(bais);
                T t = (T) ois.readObject();
                return t;
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bais != null) {
                        bais.close();
                    }
                    if (ois != null) {
                        ois.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    public void loadSharedPreferences() {
        SharedPreferences sp = getMySharedPreferences();
        CurrentContext cc = getCurrentContext();
        if(sp.contains("User")) {
            User user = getObject("User", User.class);
            cc.setUser(user);
        }

        if(sp.contains("CurrentBook")) {
            Book book = getObject("CurrentBook", Book.class);
            cc.setCurrentBook(book);
        }

        if(sp.contains("CurrentMeter")) {
            Meter meter = getObject("CurrentMeter", Meter.class);
            cc.setCurrentMeter(meter);
        }
    }
    public void saveUser(User user) {
        setObject("User", user);
    }
    public void saveCurrentBook(Book book) {
        setObject("CurrentBook", book);
    }
    public void saveCurrentMeter(Meter meter) {
        setObject("CurrentMeter", meter);
    }


}
