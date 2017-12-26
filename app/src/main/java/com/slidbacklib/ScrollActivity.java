package com.slidbacklib;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.slidbacklib.utils.SlideBackLayoutUtils;

/**
 * ===============================
 * 描    述：
 * 作    者：pjw
 * 创建日期：2017/12/26 16:06
 * ===============================
 */
public class ScrollActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_view);
        findViewById(R.id.iView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(ScrollActivity.this, MainActivity.class));
                startActivity(new Intent(ScrollActivity.this, WebViewActivity.class));
            }
        });
//        findViewById(R.id.iView1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(ScrollActivity.this, WebViewActivity.class));
//            }
//        });
        SlideBackLayoutUtils.sideBack(this);
    }


    @Override
    protected void onDestroy() {
        SlideBackLayoutUtils.onDestroy(this);
        super.onDestroy();
    }
}
