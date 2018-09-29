package com.vhall.classsdk.demo;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.vhall.classsdk.Constant;
import com.vhall.classsdk.VHClass;

/**
 * 微吼课堂Demo主Application类
 * 配置SDK初始化的INIT方法   传入在Web端配置的App_Key App_Secret_Key
 */
public class ClassApplication extends Application {

    public static Context context;
    public static String device;

    @Override
    public void onCreate() {
        super.onCreate();
        device = Build.BOARD + Build.DEVICE + Build.SERIAL;//SERIAL  串口序列号 保证唯一值
        Log.e(Constant.TAG, " device = " + device);
        context = this;
        VHClass.getInstance().init(context, "", "");
    }

}