package com.slideback;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.slideback.widget.SlideBackLayout;

import java.util.Stack;

/**
 * Created by Oubowu on 2016/9/20 3:28.
 */
public class ActivityHelper implements Application.ActivityLifecycleCallbacks {

    private static Stack<ActivityEntity> mActivityStack;

    public ActivityHelper() {
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(new ActivityEntity(activity));
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // Log.e("TAG", "ActivityHelper-销毁: " + activity);
        postRemoveActivity(activity);
    }

    /**
     * 获取上一个页面
     */
    public Activity getPreActivity() {
        if (mActivityStack == null) {
            return null;
        }
        int size = mActivityStack.size();
        if (size < 2) {
            return null;
        }
        return mActivityStack.elementAt(size - 2).mActivity;
    }

    /**
     * 获取下一个页面
     */
    public ActivityEntity getNextActivity(Activity activity) {
        int current = getActivityEntityIndex(activity);
        if (current == -1) return null;
        int size = mActivityStack.size();
        if (size < 2 || size - 1 <= current) {
            return null;
        }
        return mActivityStack.elementAt(current + 1);
    }


//    /**
//     * 获取上一个页面
//     */
//    public Activity getPreActivity(SlideBackLayout slideBackLayout) {
//        if (mActivityStack == null) {
//            return null;
//        }
//        int size = mActivityStack.size();
//        if (size < 2) {
//            return null;
//        }
//        int index = 0;
//        for (int i = 0; i < size; i++) {
//            ActivityEntity entity = mActivityStack.elementAt(i);
//            if (entity != null && entity.mSlideBackLayout == slideBackLayout) {
//                index = i;
//                break;
//            }
//        }
//        if (index - 2 >= 0) {
//            return mActivityStack.elementAt(index - 2).mActivity;
//        }
//        return null;
//    }

    public void finishAllActivity() {
        if (mActivityStack == null) {
            return;
        }
        for (ActivityEntity entity : mActivityStack) {
            if (entity.mSlideBackHelper != null)
                entity.mSlideBackHelper.onDestroy();
            entity.mActivity.finish();
        }
    }

    public void printAllActivity() {
        if (mActivityStack == null) {
            return;
        }
        for (int i = 0; i < mActivityStack.size(); i++) {
            Log.e("TAG", "位置" + i + ": " + mActivityStack.get(i));
        }
    }

    /**
     * 强制删掉activity，用于用户快速滑动页面的时候，因为页面还没来得及destroy导致的问题
     *
     * @param activity 删掉的activity
     */
    void postRemoveActivity(Activity activity) {
        if (mActivityStack == null) {
            return;
        }
        //获取下一个页面
//        ActivityEntity entityNext = getNextActivity(activity);
//        if (entityNext != null && entityNext.mSlideBackLayout != null) {
//            entityNext.mSlideBackLayout.setPreContentDestroy();
//        }
        ActivityEntity entity = getActivityEntity(activity);
        if (entity != null && entity.mSlideBackHelper != null) {
            entity.mSlideBackHelper.onDestroy();
            entity.mActivity = null;
            entity.mSlideBackLayout = null;
            entity.mSlideBackHelper = null;
        }
        mActivityStack.remove(entity);
    }

    private ActivityEntity getActivityEntity(Activity mActivity) {
        if (mActivity == null) return null;
        for (ActivityEntity entity : mActivityStack) {
            if (mActivity == entity.mActivity) return entity;
        }
        return null;
    }

    private int getActivityEntityIndex(Activity mActivity) {
        if (mActivity == null) return -1;
        for (int i = 0, size = mActivityStack.size(); i < size; i++) {
            if (mActivity == mActivityStack.elementAt(i).mActivity) return i;
        }
        return -1;
    }

    public void setActivityInfo(Activity mActivity, SlideBackLayout mSlideBackLayout, SlideBackHelper mSlideBackHelper) {
        ActivityEntity entity = getActivityEntity(mActivity);
        entity.mSlideBackHelper = mSlideBackHelper;
        entity.mSlideBackLayout = mSlideBackLayout;
    }

    private class ActivityEntity {
        Activity mActivity;
        SlideBackLayout mSlideBackLayout;
        SlideBackHelper mSlideBackHelper;

        ActivityEntity(Activity mActivity) {
            this(mActivity, null);
        }

        ActivityEntity(Activity mActivity, SlideBackLayout mSlideBackLayout) {
            this(mActivity, mSlideBackLayout, null);
        }

        ActivityEntity(Activity mActivity, SlideBackLayout mSlideBackLayout, SlideBackHelper mSlideBackHelper) {
            this.mActivity = mActivity;
            this.mSlideBackLayout = mSlideBackLayout;
            this.mSlideBackHelper = mSlideBackHelper;
        }
    }


}
