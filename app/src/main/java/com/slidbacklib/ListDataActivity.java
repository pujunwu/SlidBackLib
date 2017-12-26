package com.slidbacklib;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.slidbacklib.commonadapter.RecyclerAdapter;
import com.slidbacklib.commonadapter.viewholders.RecyclerViewHolder;
import com.slidbacklib.utils.SlideBackLayoutUtils;
import com.slidbacklib.utils.StringUtils;

/**
 * ===============================
 * 描    述：
 * 作    者：pjw
 * 创建日期：2017/12/26 16:01
 * ===============================
 */
public class ListDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);
        RecyclerView rvRecycler = (RecyclerView) findViewById(R.id.rvRecycler);
        rvRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        RecyclerAdapter<String> adapter = new RecyclerAdapter<String>(R.layout.item_list_data) {
            @Override
            protected void onBindData(RecyclerViewHolder recyclerViewHolder, int i, String s) {
            }
        };

        adapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int i) {
                startActivity(new Intent(ListDataActivity.this, ScrollActivity.class));
            }
        });
        rvRecycler.setAdapter(adapter);
        adapter.clear();
        adapter.addItems(StringUtils.getDatas(30));

        SlideBackLayoutUtils.sideBack(this);
    }

    @Override
    protected void onDestroy() {
        SlideBackLayoutUtils.onDestroy(this);
        super.onDestroy();
    }
}
