package com.slideback;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.slideback.callbak.OnInternalStateListener;
import com.slideback.callbak.OnSlideListener;
import com.slideback.widget.SlideBackLayout;

/**
 * Created by Oubowu on 2016/9/22 0022 14:31.
 * 侧滑处理
 */
// TODO: 2016/9/24 添加了上个页面的布局，如果页面有Toolbar的话其不随屏幕旋转而大小变化，永远维持进入时的宽高比
public class SlideBackHelper {

    public static ViewGroup getDecorView(Activity activity) {
        if (activity == null || activity.getWindow() == null) return null;
        View view = activity.getWindow().getDecorView();
        if (view == null) return null;
        return (ViewGroup) view;
    }

    public static Drawable getDecorViewDrawable(Activity activity) {
        ViewGroup viewGroup = getDecorView(activity);
        if (viewGroup == null) return null;
        return viewGroup.getBackground();
    }

    public static View getContentView(Activity activity) {
        ViewGroup viewGroup = getDecorView(activity);
        if (viewGroup == null) return null;
        return viewGroup.getChildAt(0);
    }

    private ActivityHelper helper;//Activity管理对象
    private Activity curActivity;//当前承载SlideBackHelper的Activity
    private Activity preActivity;//当前Activity的上一个Activity
    private OnSlideListener listener;//事件回调
    private SlideConfig config;//配置信息
    private ViewGroup currentContentView;//当前Activity显示的view
    private View preContentView;//上一个Activity显示的view

    /**
     * 附着Activity，实现侧滑
     *
     * @param activity       当前Activity
     * @param activityHelper Activity栈管理类
     * @param slideConfig    参数配置
     * @param slideListener  滑动的监听
     * @return 处理侧滑的布局，提高方法动态设置滑动相关参数
     */
    public SlideBackLayout attach(@NonNull Activity activity, @NonNull ActivityHelper activityHelper, @Nullable SlideConfig slideConfig, @Nullable OnSlideListener slideListener) {
        this.helper = activityHelper;
        this.curActivity = activity;
        this.config = slideConfig;
        this.listener = slideListener;
        final ViewGroup decorView = getDecorView(curActivity);
        if (decorView == null) return null;
        final LinearLayout contentView = (LinearLayout) decorView.getChildAt(0);
        currentContentView = contentView.findViewById(android.R.id.content);
        if (currentContentView.getBackground() == null) {
            //延迟设置背景，防止在返回时页面未透明色
            //修复在某些手机直接设置背景时背景色为黑色
            new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentContentView == null) return;
                    currentContentView.setBackground(decorView.getBackground());
                }
            }, 500);
        }
        this.preActivity = helper.getPreActivity();
        preContentView = getContentView(preActivity);
//        if (preContentView == null){
//            return null;
//        }
        decorView.removeViewAt(0);
        Drawable preDecorViewDrawable = getDecorViewDrawable(preActivity);
        SlideBackLayout slideBackLayout = new SlideBackLayout(curActivity, contentView, preContentView, preDecorViewDrawable, config, new OnInternalStateListener() {
            @Override
            public void onSlide(float percent) {
                if (listener != null) {
                    listener.onSlide(percent);
                }
            }

            @Override
            public void onOpen() {
                if (listener != null) {
                    listener.onOpen();
                }
            }

            @Override
            public void onClose(Boolean finishActivity) {
                // finishActivity为true时关闭页面，为false时不关闭页面，为null时为其他地方关闭页面时调用SlideBackLayout.isComingToFinish的回调
                if (listener != null) {
                    listener.onClose();
                }
                if ((finishActivity == null || !finishActivity) && listener != null) {
                    listener.onClose();
                }
                if (config != null && config.isRotateScreen()) {
                    if (finishActivity != null && finishActivity) {
                        // remove了preContentView后布局会重新调整，这时候contentView回到原处，所以要设不可见
                        contentView.setVisibility(View.INVISIBLE);
                    }
                    if (preActivity != null && preContentView != null && preContentView.getParent() != getDecorView(preActivity)) {
                        // Log.e("TAG", ((SlideBackLayout) preContentView[0].getParent()).getTestName() + "这里把欠人的布局放回到上个Activity");
                        preContentView.setX(0);
                        ((ViewGroup) preContentView.getParent()).removeView(preContentView);
                        getDecorView(preActivity).addView(preContentView, 0);
                    }
                }
                if (curActivity == null) return;
                curActivity.finish();
//                helper.postRemoveActivity(curActivity);
                if (preActivity == null || preActivity.isFinishing()) {
                    return;
                }
                curActivity.overridePendingTransition(R.anim.sback_anim_activity_none, R.anim.sback_anim_activity_none);
            }

            @Override
            public void onCheckPreActivity(SlideBackLayout slideBackLayout) {
                // Log.e("TAG", "--------------------------------------------------");
                // helper.printAllActivity();
                // Log.e("TAG", "--------------------------------------------------");
                if (helper == null) return;
                Activity activity = helper.getPreActivity();
                // Log.e("TAG", "SlideBackHelper-120行-onFocus(): " + preActivity[0] + ";" + activity);
                if (activity != preActivity) {
                    // Log.e("TAG", "SlideBackHelper-122行-onFocus(): 上个Activity变了");
                    preActivity = activity;
                    if (preActivity == null) return;
                    preContentView = getContentView(preActivity);
                    slideBackLayout.updatePreContentView(preContentView);
                }
            }
        });
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        decorView.addView(slideBackLayout, params);
        return slideBackLayout;
    }

    /**
     * Activity销毁时调用方法
     */
    public void onDestroy() {
        this.helper = null;
        this.curActivity = null;
        this.preActivity = null;
        this.listener = null;
        this.config = null;
        this.currentContentView = null;
        this.preContentView = null;
        Log.d("123", "onDestroy: ");
    }

}
