package com.vhall.classsdk.demo;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.tencent.mmkv.MMKV;
import com.vhall.classsdk.VHClass;
import com.vhall.classsdk.utils.Constant;

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
        MMKV.initialize(this);
        device = Build.BOARD + Build.DEVICE + Build.SERIAL;//SERIAL  串口序列号 保证唯一值
        Log.e(Constant.TAG, " device = " + device);
        context = this;
        //线上 a39259f4283861c1eb789d4b018c5f02   edf494f08cceee9f3dc74a44bc994fc7
        //线下 207038a004fcc7012fec958ce992ef8a   dda37cdf83997044539ad9c35ab87be1
        VHClass.getInstance().init(context, "a39259f4283861c1eb789d4b018c5f02", "edf494f08cceee9f3dc74a44bc994fc7");
    }

}
