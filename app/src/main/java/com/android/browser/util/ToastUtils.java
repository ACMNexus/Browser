package com.android.browser.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Luooh on 2017/2/28.
 */
public class ToastUtils {

    public static void show(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
    }

    public static void show(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
