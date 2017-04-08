package com.qirui.browser.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;

import java.io.Serializable;
import java.util.List;
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

    public static void startNextPagerForResult(Activity activity, Class<?> clazz, int requestCode) {
        Intent intent = new Intent(activity, clazz);
        activity.startActivityForResult(intent, requestCode);
    }

    public static boolean supportVoice(Context context) {
        PackageManager pm = context.getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return activities.size() != 0;
    }

    public static void startVoiceRecognizer(Activity activity, int VOICE_RESULT) {
        Intent voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voice.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        activity.startActivityForResult(voice, VOICE_RESULT);
    }
}
