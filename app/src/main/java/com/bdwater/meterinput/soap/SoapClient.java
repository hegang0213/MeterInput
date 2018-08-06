package com.bdwater.meterinput.soap;

import com.bdwater.meterinput.base.NetworkUtils;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by hegang on 16/6/13.
 */
public class SoapClient {
//    static final String SERVICE_URL = NetworkUtils.WEBSERVICE_URL;
    static final String NAMESPACE = NetworkUtils.WEBSERVICE_NAMESPACE;
    public static String LOGIN_METHOD = "LoginAndGetBook";
    public static String LOGIN_WITH_CURRENT_METHOD = "LoginAndGetBookWithCurrent";
    public static String GET_METERS_METHOD = "GetMeters";
    public static String GET_METER_BYID_METHOD = "GetMeterByID";
    public static String GET_CUSTOMER_BY_METER_ID_METHOD = "GetCustomerByMeterID";
    public static String GET_METERS_BY_QUERY = "GetMetersByQuery";
    public static String GET_CUSTOMER_BILLS_METHOD = "GetCustomerBills";
    public static String GET_WATER_LOGS_METHOD = "GetWaterLogs";
    public static String UPDATE_METER_METHOD = "UpdateMeter";
    public static String UPDATE_FAKE_METER_METHOD = "UpdateFakeMeter";
    public static String UPDATE_METER_TASK_METHOD = "UpdateMeterTask";
    public static String DELETE_METER_TASK_METHOD = "DeleteMeterTask";
    public static String GET_APPLICATION_METER_TASKS_METHOD = "GetApplicationMeterTasks";
    public static String UPDATE_METER_COMMENT_METHOD = "UpdateComment";

    public static String Login(String username, String password) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, LOGIN_METHOD);
        request.addProperty("username", username);
        request.addProperty("password", password);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + LOGIN_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
    public static String LoginWithCurrentBook(String username, String password, String bookID) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, LOGIN_WITH_CURRENT_METHOD);
        request.addProperty("username", username);
        request.addProperty("password", password);
        request.addProperty("bookIDString", bookID);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + LOGIN_WITH_CURRENT_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
    public static String GetMeters(String bookID) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, GET_METERS_METHOD);
        request.addProperty("bookIDString", bookID);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + GET_METERS_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
    public static String GetMetersByQuery(String userID, String queryString) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, GET_METERS_BY_QUERY);
        request.addProperty("userIDString", userID);
        request.addProperty("queryString", queryString);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + GET_METERS_BY_QUERY, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
    public static String GetMeterByID(String meterIDString) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, GET_METER_BYID_METHOD);
        request.addProperty("meterIDString", meterIDString);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + GET_METER_BYID_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
    public static String GetCustomerByMeterID(String meterIDString) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, GET_CUSTOMER_BY_METER_ID_METHOD);
        request.addProperty("meterIDString", meterIDString);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + GET_CUSTOMER_BY_METER_ID_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
    public static String GetCustomerBills(String meterIDString) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, GET_CUSTOMER_BILLS_METHOD);
        request.addProperty("meterIDString", meterIDString);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + GET_CUSTOMER_BILLS_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
    public static String GetWaterLogs(String meterIDString) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, GET_WATER_LOGS_METHOD);
        request.addProperty("meterIDString", meterIDString);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + GET_WATER_LOGS_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }

    public static String UpdateMeter(String updateMeterJsonString) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, UPDATE_METER_METHOD);
        request.addProperty("jsonUpdateMeterString", updateMeterJsonString);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + UPDATE_METER_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
    public static String UpdateFakeMeter(String jsonUpdateFakeMeterString) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, UPDATE_FAKE_METER_METHOD);
        request.addProperty("jsonUpdateFakeMeterString", jsonUpdateFakeMeterString);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + UPDATE_FAKE_METER_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
    public static String UpdateMeterTask(String jsonUpdateMeterTaskString) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, UPDATE_METER_TASK_METHOD);
        request.addProperty("jsonUpdateMeterTaskString", jsonUpdateMeterTaskString);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + UPDATE_METER_TASK_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
    public static String DeleteMeterTask(String meterTaskIDListString) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, DELETE_METER_TASK_METHOD);
        request.addProperty("meterTaskIDListString", meterTaskIDListString);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + DELETE_METER_TASK_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
    public static String GetApplicationMeterTasks(String starter) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, GET_APPLICATION_METER_TASKS_METHOD);
        request.addProperty("starter", starter);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + GET_APPLICATION_METER_TASKS_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
    public static String UpdateComment(String meterIDString, String currentComment, String comment) throws IOException, XmlPullParserException {
        // 创建soapObject对象并传入命名空间和方法名
        SoapObject request = new SoapObject(NAMESPACE, UPDATE_METER_COMMENT_METHOD);
        request.addProperty("meterIDString", meterIDString);
        request.addProperty("currentComment", currentComment);
        request.addProperty("comment", comment);

        // 创建SoapSerializationEnvelope对象并传入SOAP协议的版本号
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = request;
        // 设置与.NET提供的Web service保持有良好的兼容性
        envelope.dotNet = true;

        // 创建HttpTransportSE传说对象 传入webservice服务器地址
        HttpTransportSE ht = new HttpTransportSE(NetworkUtils.WEBSERVICE_URL);
        ht.debug = true;
        ht.call(NAMESPACE + UPDATE_METER_COMMENT_METHOD, envelope);

        if(envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            SoapPrimitive sp = (SoapPrimitive)result.getProperty(0);
            return sp.getValue().toString();
        }
        return null;
    }
}
