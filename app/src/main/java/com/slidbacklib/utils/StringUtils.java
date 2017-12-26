package com.slidbacklib.utils;

import java.util.ArrayList;

/**
 * ===============================
 * 描    述：
 * 作    者：pjw
 * 创建日期：2017/11/1 13:48
 * ===============================
 */
public class StringUtils {

    public static ArrayList<String> getDatas(int count) {
        ArrayList<String> arrayList = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            arrayList.add("i:" + i);
        }
        return arrayList;
    }

}
