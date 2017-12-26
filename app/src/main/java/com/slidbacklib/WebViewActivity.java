package com.slidbacklib;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slidbacklib.utils.SlideBackLayoutUtils;

import static com.slidbacklib.R.id.tv_title_bar_right_text;

/**
 * ===============================
 * 描    述：
 * 作    者：pjw
 * 创建日期：2017/12/26 16:09
 * ===============================
 */
public class WebViewActivity extends AppCompatActivity {

    TextView tvTitleBarRightText;
    WebView cdWebView;
    ProgressBar webviewProgressBar;
    RelativeLayout cdRootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        cdWebView = (WebView) findViewById(R.id.cd_web_view);
        tvTitleBarRightText = (TextView) findViewById(R.id.tv_title_bar_right_text);
        webviewProgressBar = (ProgressBar) findViewById(R.id.webview_progress_bar);
        cdRootView = (RelativeLayout) findViewById(R.id.cd_root_view);
        findViewById(R.id.iv_web_title_bar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initData();
        SlideBackLayoutUtils.sideBack(this);
    }

    private void initData() {
        initWebView();
        cdWebView.setWebViewClient(mWebViewClient);
        cdWebView.setWebChromeClient(mWebChromeClient);
        cdWebView.loadUrl("https://www.baidu.com/");

        tvTitleBarRightText.setVisibility(View.VISIBLE);
        tvTitleBarRightText.setText("跳转页面");
        tvTitleBarRightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WebViewActivity.this, MainActivity.class));
            }
        });
    }

    private void initWebView() {
        WebSettings webSetting = cdWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowUniversalAccessFromFileURLs(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(false);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(true);
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
//        webSetting.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSetting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    /**
     * 设置WebChromeClient
     * 处理进度条和标题显示
     */
    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView webView, int newProgress) {
            if (webviewProgressBar != null) {
                if (newProgress == 100) {
                    webviewProgressBar.setProgress(100);
                    webviewProgressBar.setVisibility(View.GONE);
                } else {
                    if (!webviewProgressBar.isShown()) {
                        webviewProgressBar.setVisibility(View.VISIBLE);
                    }
                    webviewProgressBar.setProgress(newProgress);
                }
            }
            super.onProgressChanged(webView, newProgress);
        }

//        public void openFileChooser(ValueCallback<Uri> filePathCallback) {
//            mFilePathCallback = filePathCallback;
//            openSelectImageDialog();
//        }
//
//        public void openFileChooser(ValueCallback filePathCallback, String acceptType) {
//            mFilePathCallback = filePathCallback;
//            openSelectImageDialog();
//        }
//
//        public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture) {
//            mFilePathCallback = filePathCallback;
//            openSelectImageDialog();
//        }
//
//        @Override
//        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
//            mFilePathCallbacks = filePathCallback;
//            openSelectImageDialog();
//            return true;
//        }
    };

    /**
     * 设置WebViewClient
     * 处理Url拦截事项
     */
    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    };

    @Override
    protected void onDestroy() {

        if (cdWebView != null) {
            cdWebView.destroy();
        }
        if (cdRootView != null && cdWebView != null) {
            cdRootView.removeView(cdWebView);
            cdRootView.removeAllViews();
            cdWebView = null;
            cdRootView = null;
        }
        SlideBackLayoutUtils.onDestroy(this);
        super.onDestroy();
    }

}
