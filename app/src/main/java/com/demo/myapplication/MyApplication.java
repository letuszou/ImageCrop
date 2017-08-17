package com.demo.myapplication;

import android.app.Application;
import cn.bmob.v3.Bmob;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Bmob.initialize(this, "8c73b20696a1316785cb9bb5a0e1981a");
    }
}
