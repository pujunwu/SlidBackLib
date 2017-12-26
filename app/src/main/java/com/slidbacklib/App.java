package com.slidbacklib;

import android.app.Application;
import android.content.Context;

import com.slideback.ActivityHelper;

/**
 * ===============================
 * 描    述：
 * 作    者：pjw
 * 创建日期：2017/12/26 16:20
 * ===============================
 */
public class App extends Application {

    private ActivityHelper mActivityHelper;

    //当前app实例对象
    public static App sApp;
    //全局上下文对象
    public static Context sContext;


    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        sContext = this;
        getActivityHelper();
    }

    public ActivityHelper getActivityHelper() {
        if (mActivityHelper == null) {
            mActivityHelper = new ActivityHelper();
            registerActivityLifecycleCallbacks(mActivityHelper);
        }
        return mActivityHelper;
    }

}
