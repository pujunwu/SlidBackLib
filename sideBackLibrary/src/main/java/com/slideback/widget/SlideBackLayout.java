package com.slideback.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewGroupCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.slideback.SlideConfig;
import com.slideback.callbak.OnInternalStateListener;

/**
 * Created by Oubowu on 2016/9/22 0022 15:24.
 * 滑动控制类
 */
public class SlideBackLayout extends FrameLayout {

    private static final int MIN_FLING_VELOCITY = 400;
    //    private final String mTestName;
    private boolean mCheckPreContentView;
    private boolean mIsFirstAttachToWindow;
    private ViewDragHelper mDragHelper;
    private View mContentView;
    private CacheDrawView mCacheDrawView;
    private ShadowView mShadowView;
    private View mPreContentView;
    private Drawable mPreDecorViewDrawable;
    private int mScreenWidth;
    //是否关闭全局侧滑
    private boolean mEdgeOnly = false;
    //是否关闭侧滑
    private boolean mLock = false;
    //是否可以滑动
    private boolean isSlideBack = true;

    @FloatRange(from = 0.0, to = 1.0)
    private float mSlideOutRangePercent = 0.4f;
    @FloatRange(from = 0.0, to = 1.0)
    private float mEdgeRangePercent = 0.1f;

    private float mSlideOutRange;
    private float mEdgeRange;

    private float mSlideOutVelocity;

    private boolean mIsEdgeRangeInside;

    private OnInternalStateListener mOnInternalStateListener;
    //按下的点
    private float mDownX;
    //开始滑动时的点
    private float mDownStartX;

    private float mSlidDistantX;

    private boolean mRotateScreen;

    private boolean mCloseFlagForWindowFocus;
    private boolean mCloseFlagForDetached;
    private boolean mEnableTouchEvent;
    //鼠标移动
    private VelocityTracker mVelocityTracker;

    public SlideBackLayout(Context context, View contentView, View preContentView, Drawable preDecorViewDrawable, SlideConfig config, @NonNull OnInternalStateListener onInternalStateListener) {
        super(context);
        mContentView = contentView;
        mPreContentView = preContentView;
        mPreDecorViewDrawable = preDecorViewDrawable;
        mOnInternalStateListener = onInternalStateListener;

        initConfig(config);

//        if (preContentView != null && preContentView instanceof LinearLayout) {
//            mTestName = "1号滑动";
//        } else {
//            mTestName = "2号滑动";
//        }
    }

    private void initConfig(SlideConfig config) {
        if (config == null) {
            config = new SlideConfig();
        }
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;

        ViewGroupCompat.setMotionEventSplittingEnabled(this, false);
        SlideLeftCallback slideLeftCallback = new SlideLeftCallback();
        mDragHelper = ViewDragHelper.create(this, 1.0f, slideLeftCallback);
        // 最小拖动速度
        mDragHelper.setMinVelocity(minVel);
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);

        mCacheDrawView = new CacheDrawView(getContext());
        mCacheDrawView.setVisibility(INVISIBLE);
        addView(mCacheDrawView);

        mShadowView = new ShadowView(getContext());
        mShadowView.setVisibility(INVISIBLE);
        addView(mShadowView, mScreenWidth / 28, LayoutParams.MATCH_PARENT);

        addView(mContentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mEdgeOnly = config.isEdgeOnly();
        mLock = config.isLock();
        mRotateScreen = config.isRotateScreen();

        mSlideOutRangePercent = config.getSlideOutPercent();
        mEdgeRangePercent = config.getEdgePercent();

        mSlideOutRange = mScreenWidth * mSlideOutRangePercent;
        mEdgeRange = mScreenWidth * mEdgeRangePercent;
        mSlideOutVelocity = config.getSlideOutVelocity();

        mSlidDistantX = mScreenWidth / 20.0f;

        mContentView.setFitsSystemWindows(false);
//        mContentView.getFitsSystemWindows()
        if (mRotateScreen) {
            mContentView.findViewById(android.R.id.content).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 屏蔽上个内容页的点击事件
                }
            });
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mLock) {//是否关闭侧滑
            return false;
        }
        mDownStartX = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = mDownStartX;
                isSlideBack = isSlide();
//                Log.d("123", "onInterceptTouchEvent: 按下事件");
                break;
            case MotionEvent.ACTION_MOVE:
                // 优化侧滑的逻辑，不要一有稍微的滑动就被ViewDragHelper拦截掉了
                if (mDownStartX - mDownX < mSlidDistantX) {
//                    Log.d("123", "onInterceptTouchEvent: 滑动没有拦截");
                    return false;
                }
//                Log.d("123", "onInterceptTouchEvent: 滑动继续执行");
                break;
        }
        if (isSlideBack) {
            if (mEdgeOnly) {//是否关闭全局侧滑
                mIsEdgeRangeInside = isEdgeRangeInside(mDownStartX);
                return mIsEdgeRangeInside && mDragHelper.shouldInterceptTouchEvent(event);
            } else {//开启全局侧滑
                return mDragHelper.shouldInterceptTouchEvent(event);
            }
        } else {
//            Log.d("123", "onInterceptTouchEvent: 上个页面被销毁：" + event.getAction());
            mIsEdgeRangeInside = isEdgeRangeInside(mDownX);
            if (MotionEvent.ACTION_MOVE != event.getAction()) {
//                Log.d("123", "onInterceptTouchEvent: 不是滑动");
                return false;
            }
//            Log.d("123", "onInterceptTouchEvent: " + mEdgeOnly + ",mDownX:" + isEdgeRangeInside(mDownX));
            return !mEdgeOnly || mIsEdgeRangeInside;
        }
    }

    /**
     * 是否在监听滑动范围
     */
    private boolean isEdgeRangeInside(float x) {
        return x <= mEdgeRange;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLock) {
            return super.onTouchEvent(event);
        }
//        Log.d("123", "onTouchEvent: 开始监听");
        if (mEdgeOnly && !mIsEdgeRangeInside) {
            return super.onTouchEvent(event);
        }
//        Log.d("123", "onTouchEvent: mEnableTouchEvent:" + mEnableTouchEvent);
        if (!mEnableTouchEvent) {
            return super.onTouchEvent(event);
        }
        if (mCloseFlagForDetached || mCloseFlagForWindowFocus) {
            // 针对快速滑动的时候，页面关闭的时候移除上个页面的时候，布局重新调整，这时候我们把contentView设为invisible，
            // 但是还是可以响应DragHelper的处理，所以这里根据页面关闭的标志位不给处理事件了
            // Log.e("TAG", mTestName + "都要死了，还处理什么触摸事件！！");
            return super.onTouchEvent(event);
        }
        if (isSlideBack) {
//            Log.d("123", "onTouchEvent: 上个页面没有销毁");
            mDragHelper.processTouchEvent(event);
        } else {
//            Log.d("123", "onTouchEvent: 上个页面被销毁");
            switch (event.getAction()) {
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    //intunitis表示速率的基本时间单位。unitis值为1的表示是，
                    // 一毫秒时间单位内运动了多少个像素， unitis值为1000表示一秒（1000毫秒）时间单位内运动了多少个像素
                    mVelocityTracker.computeCurrentVelocity(1000);
                    if (getXVelocity() >= mDragHelper.getMinVelocity() * 0.8 || event.getX() - mDownStartX >= mSlideOutRange) {
                        //关闭页面
                        mCloseFlagForWindowFocus = true;
                        mCloseFlagForDetached = true;
                        mOnInternalStateListener.onClose(true);
                    }
                    cancelVelocity();
                    return true;
                default:
                    addMovement(event);
                    break;
            }
        }
        return true;
    }

    public void addMovement(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private float getXVelocity() {
        if (mVelocityTracker == null) {
            return 0;
        }
        return mVelocityTracker.getXVelocity();
    }

    public void cancelVelocity() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void addPreContentView() {
        if (isSlideBack) return;
        if (mPreContentView != null && mPreContentView.getParent() != SlideBackLayout.this) {
            // Log.e("TAG", mTestName + ": 我要把上个页面的内容页加到我这里啦！");
            mPreContentView.setTag("notScreenOrientationChange");
            ViewGroup viewGroup = (ViewGroup) mPreContentView.getParent();
            if (viewGroup != null)
                viewGroup.removeView(mPreContentView);
            SlideBackLayout.this.addView(mPreContentView, 0);
            mShadowView.setVisibility(VISIBLE);
        }
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    public void isComingToFinish() {
        if (mRotateScreen) {
            mCloseFlagForDetached = true;
            mCloseFlagForWindowFocus = false;
            mOnInternalStateListener.onClose(null);
            if (mPreContentView != null)
                mPreContentView.setX(0);
        }
    }

    public void updatePreContentView(View contentView) {
        mPreContentView = contentView;
        mCacheDrawView.drawCacheView(mPreContentView);
    }

    private boolean isSlide() {
        if (mPreContentView == null) return false;
        Context context = mPreContentView.getContext();
        if (context == null || !(context instanceof Activity) || ((Activity) context).isFinishing()) {
            return false;
        }
        return true;
    }

    private class SlideLeftCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //是否可以移动
            return isSlideBack && child == mContentView;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            //分别为即将移动到的位置，比如横向的情况下，我希望只在ViewGroup的内部移动，
            //即：最小>=paddingleft，最大<=ViewGroup.getWidth()-paddingright-child.getWidth
            return Math.max(Math.min(mScreenWidth, left), 0);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mScreenWidth;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //手指抬起时回调方法
            if (!isSlideBack) {
                return;
            }
            if (releasedChild == mContentView) {
                if (xvel > mSlideOutVelocity) {
                    mDragHelper.settleCapturedViewAt(mScreenWidth, 0);
                    invalidate();
                    return;
                }
                if (mContentView.getLeft() < mSlideOutRange) {
                    mDragHelper.settleCapturedViewAt(0, 0);
                } else {
                    mDragHelper.settleCapturedViewAt(mScreenWidth, 0);
                }
                invalidate();
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (!isSlideBack) {
                return;
            }
            switch (state) {
                case ViewDragHelper.STATE_IDLE:
                    if (mContentView.getLeft() == 0) {
                        // 2016/9/22 0022 回到原处
                        mOnInternalStateListener.onOpen();
                    } else if (mContentView.getLeft() == mScreenWidth) {
                        // 2016/9/22 0022 结束Activity
                        // 这里再绘制一次是因为在屏幕旋转的模式下，remove了preContentView后布局会重新调整
                        if (mRotateScreen && mCacheDrawView.getVisibility() == INVISIBLE) {
                            mCacheDrawView.setBackground(mPreDecorViewDrawable);
                            if (mPreContentView != null)
                                mCacheDrawView.drawCacheView(mPreContentView);
                            mCacheDrawView.setVisibility(VISIBLE);
                            // Log.e("TAG", mTestName + ": 这里再绘制一次是因为在屏幕旋转的模式下，remove了preContentView后布局会重新调整");
                            mCloseFlagForWindowFocus = true;
                            mCloseFlagForDetached = true;
                            // Log.e("TAG", mTestName + ": 滑动到尽头了这个界面要死了，把preContentView给回上个Activity");
                            // 这里setTag是因为下面的回调会把它移除出当前页面，这时候会触发它的onDetachedFromWindow事件，
                            // 而它的onDetachedFromWindow实际上是来处理屏幕旋转的，所以设置个tag给它，让它知道是当前界面移除它的，并不是屏幕旋转导致的
                            if (mPreContentView != null)
                                mPreContentView.setTag("notScreenOrientationChange");
                            mOnInternalStateListener.onClose(true);
                            if (mPreContentView != null)
                                mPreContentView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mCacheDrawView.setBackground(mPreDecorViewDrawable);
                                        mCacheDrawView.drawCacheView(mPreContentView);
                                    }
                                }, 10);
                        } else if (!mRotateScreen) {
                            mCloseFlagForWindowFocus = true;
                            mCloseFlagForDetached = true;
                            mOnInternalStateListener.onClose(true);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (!mRotateScreen && mCacheDrawView.getVisibility() == INVISIBLE) {
                mCacheDrawView.setBackground(mPreDecorViewDrawable);
                if (mPreContentView != null)
                    mCacheDrawView.drawCacheView(mPreContentView);
                mCacheDrawView.setVisibility(VISIBLE);
            } else if (mRotateScreen) {
                if (!mCheckPreContentView) {
                    // 在旋转屏幕的模式下，这里的检查很有必要，比如一个滑动activity先旋转了屏幕，然后再返回上个滑动activity的时候，由于屏幕旋转上个activity会重建，步骤是：
                    // 上个activity会先新建一个activity，再把之前的销毁，所以新建的activity调SlideBackLayout.attach的时候传的上个activity实际上是要删掉的activity
                    // (因为要删掉的activity的destroy有延时的，还没销毁掉)，这就出错了;
                    // 所以这里还要在当前页面取得焦点的时候回调，去检查下看是不是上个activity改了，改了再重新赋值
                    mCheckPreContentView = true;
                    // 只需要检查一次上个Activity是不是变了
                    // Log.e("TAG","只需要检查一次上个Activity是不是变了");
                    mOnInternalStateListener.onCheckPreActivity(SlideBackLayout.this);
                }
                addPreContentView();
            }
            if (mShadowView.getVisibility() != VISIBLE) {
                mShadowView.setVisibility(VISIBLE);
            }
            final float percent = left * 1.0f / mScreenWidth;
            mOnInternalStateListener.onSlide(percent);
            if (mRotateScreen) {
                // // Log.e("TAG", "滑动上个页面");
                if (mPreContentView != null)
                    mPreContentView.setX(-mScreenWidth / 2 + percent * (mScreenWidth / 2));
            } else {
                mCacheDrawView.setX(-mScreenWidth / 2 + percent * (mScreenWidth / 2));
            }
            mShadowView.setX(mContentView.getX() - mShadowView.getWidth());
            mShadowView.redraw(1 - percent);
        }
    }

    public void edgeOnly(boolean edgeOnly) {
        mEdgeOnly = edgeOnly;
    }

    public boolean isEdgeOnly() {
        return mEdgeOnly;
    }

    public void lock(boolean lock) {
        mLock = lock;
    }

    public boolean isLock() {
        return mLock;
    }

    public void setSlideOutRangePercent(float slideOutRangePercent) {
        mSlideOutRangePercent = slideOutRangePercent;
        mSlideOutRange = mScreenWidth * mSlideOutRangePercent;
    }

    public float getSlideOutRangePercent() {
        return mSlideOutRangePercent;
    }

    public void setEdgeRangePercent(float edgeRangePercent) {
        mEdgeRangePercent = edgeRangePercent;
        mEdgeRange = mScreenWidth * mEdgeRangePercent;
    }

    public float getEdgeRangePercent() {
        return mEdgeRangePercent;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            mEnableTouchEvent = true;
            // 当前页面
            if (!mIsFirstAttachToWindow) {
                mIsFirstAttachToWindow = true;
            }
        } else {
            if (mRotateScreen) {
                // 1.跳转到另外一个Activity，例如也是需要滑动的，这时候就需要取当前Activity的contentView，所以这里把preContentView给回上个Activity
                if (mCloseFlagForWindowFocus) {
                    mCloseFlagForWindowFocus = false;
                    // Log.e("TAG", mTestName + ": onWindowFocusChanged前已经调了关闭");
                } else {
                    // Log.e("TAG", mTestName + ": 跳转到另外一个Activity，取这个Activity的contentView前把preContentView给回上个Activity");
                    mOnInternalStateListener.onClose(false);
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mEnableTouchEvent = false;
        cancelVelocity();
        // // Log.e("TAG", "SlideBackLayout-345行-onDetachedFromWindow(): " + this);
        if (mRotateScreen) {
            // 1.旋转屏幕的时候必调此方法，这里掉onClose目的是把preContentView给回上个Activity
            if (mCloseFlagForDetached) {
                mCloseFlagForDetached = false;
                // Log.e("TAG", mTestName + ": onDetachedFromWindow(): " + "已经调了关闭");
            } else {
                if (getTag() != null && getTag().equals("notScreenOrientationChange")) {
                    // 说明是手动删的不关旋转屏幕的事，所以不处理
                    // Log.e("TAG", mTestName + ":说明是手动删的不关旋转屏幕的事，所以不处理");
                    setTag(null);
                } else {
                    // Log.e("TAG", mTestName + ":屏幕旋转了，重建界面: 把preContentView给回上个Activity");
                    mOnInternalStateListener.onClose(false);
                }
            }
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        // Log.e("TAG", mTestName + ": SlideBackLayout-338行-onConfigurationChanged(): " + mScreenWidth);
        ViewGroup.LayoutParams layoutParams = mShadowView.getLayoutParams();
        layoutParams.width = mScreenWidth / 28;
        layoutParams.height = LayoutParams.MATCH_PARENT;
    }

    /**
     * 上一个页面被关闭
     */
    public void setPreContentDestroy() {
        mPreContentView = null;
        Log.d("123", "上个页面被关闭了");
    }

}
