package com.qirui.browser.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;

import com.qirui.browser.Browser;

/**
 * Created by Luooh on 2017/3/22.
 */
public class AppUtils {

    private Context mContext;
    private static AppUtils sInstance;

    public static AppUtils getInstance() {
        if(sInstance == null) {
            sInstance = new AppUtils(Browser.getInstance());
        }
        return sInstance;
    }

    private AppUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 设置默认浏览器
     */
    public void setDefaultBrowser() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("http://www.baidu.com"));
        intent.setComponent(new ComponentName("android", "com.android.internal.app.ResolverActivity"));
        mContext.startActivity(intent);
    }

    /**
     * 获取默认浏览器信息
     *
     * @return
     */
    public ActivityInfo getDefaultBrowserInfo() {
        PackageManager pm = mContext.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://www.baidu.com"));
        ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return info.activityInfo;
    }

    /**
     * 判断是否有设置默认浏览器
     * @return
     */
    public boolean hasDefaultBrowser() {
        ActivityInfo info = getDefaultBrowserInfo();
        return info != null && !"android".equals(info.packageName);
    }

    public void jumpSettingAppDetail(String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + packageName));
        mContext.startActivity(intent);
    }
}
