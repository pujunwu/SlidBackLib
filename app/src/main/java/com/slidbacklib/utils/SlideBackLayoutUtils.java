package com.slidbacklib.utils;

import android.app.Activity;

import com.slidbacklib.App;
import com.slideback.SlideBackHelper;
import com.slideback.SlideConfig;
import com.slideback.callbak.OnSlideListenerAdapter;
import com.slideback.widget.SlideBackLayout;

import java.util.HashMap;

/**
 * ===============================
 * 描    述：SlideBackLayoutUtils
 * 作    者：pjw
 * 创建日期：2017/12/20 17:28
 * ===============================
 */
public class SlideBackLayoutUtils {

    private static HashMap<Activity, SlideBackLayout> mBackLayoutHashMap = new HashMap<>(3);

    /**
     * 设置侧滑返回，默认配置
     */
    public static void sideBack(Activity activity) {
        sideBack(activity, getDefaultSlideConfig(), null);
    }

    public static void sideBack(Activity activity, SlideConfig slideConfig, OnSlideListenerAdapter adapter) {
        SlideBackHelper slideBackHelper = new SlideBackHelper();
        SlideBackLayout slideBackLayout = slideBackHelper.attach(
                // 当前Activity
                activity,
                //ActivityHelper
                App.sApp.getActivityHelper(),
                // 参数的配置
                slideConfig,
                // 滑动的监听
                adapter);
        if (slideBackLayout == null) return;
        //添加到集合中
        mBackLayoutHashMap.put(activity, slideBackLayout);
        //添加到栈
        App.sApp.getActivityHelper().setActivityInfo(activity, slideBackLayout, slideBackHelper);
//        //边缘响应的最大值
//        slideBackLayout.setEdgeRangePercent(0.2f);
//        //非快速滑动，关闭页面的最小值
//        slideBackLayout.setSlideOutRangePercent(0.3f);
//        //是否关闭全局侧滑，默认开启全局侧滑
//        slideBackLayout.edgeOnly(true);
//        //是否关闭侧滑，默认开启侧滑
//        slideBackLayout.lock(false);
    }

    /**
     * 获取默认配置
     */
    public static SlideConfig getDefaultSlideConfig() {
        // 参数的配置
        return new SlideConfig()
                // 屏幕是否旋转
                .setRotateScreen(false)
                // 是否关闭全局侧滑
                .setEdgeOnly(true)
                // 是否关闭侧滑
                .setLock(false)
                // 边缘响应滑动区域的最大值 0~1
                .setEdgePercent(0.2f)
                // 非快速滑动，关闭页面的最小值 0~1
                .setSlideOutPercent(0.4f);
    }

    public static void onDestroy(Activity activity) {
        //Activity销毁调用方法
        SlideBackLayout slideBackLayout = mBackLayoutHashMap.get(activity);
        if (slideBackLayout != null) {
            slideBackLayout.isComingToFinish();
            mBackLayoutHashMap.remove(activity);
        }
    }

}
