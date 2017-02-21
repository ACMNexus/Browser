package com.android.browser.util;

import android.content.Context;
import android.content.Intent;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by Luooh on 2017/2/16.
 */

public class ActivityUtils {

    public static void startNextPager(Context context, Class<?> clazz, Map<String, ? extends Serializable> map) {
        Intent intent = new Intent(context, clazz);
        if(map != null && map.size() > 0) {
            Set<String> keySets = map.keySet();
            for(String key : keySets) {
                intent.putExtra(key, map.get(key));
            }
        }
        context.startActivity(intent);
    }

    public static void startNextPager(Context context, Class<?> clazz) {
        startNextPager(context, clazz, null);
    }
}
