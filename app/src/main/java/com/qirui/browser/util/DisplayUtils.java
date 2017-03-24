package com.qirui.browser.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * Created by Luooh on 2017/2/11.
 */
public class DisplayUtils {

    private static int sRealHeight;
    private static int mScreenHeight = 0;
    private static int sNavBarHeight = -1;

    public static void initRealSize(Context context) {
        if (context != null) {
            WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            try {
                Class<?> disPlayClass = Class.forName("android.view.Display");
                Point realSize = new Point();
                Method method = disPlayClass.getMethod("getRealSize", Point.class);
                method.invoke(display, realSize);
                sRealHeight = realSize.y;
            } catch (Exception e) {
                sRealHeight = getScreenHeight(context);
            }
            sNavBarHeight = sRealHeight - getScreenHeight(context);
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5f);
    }

    public static int getScreenWidth(Context context) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕的高度
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        if(mScreenHeight != 0) {
            return mScreenHeight;
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(dm);
        mScreenHeight = dm.heightPixels;
        return mScreenHeight;
    }

    /**
     * 获取底部导航栏高度
     *
     * @param context
     * @return
     */
    public static int getNavBarHeight(Context context) {
        if (sNavBarHeight < 0) {
            initRealSize(context);
        }
        return sNavBarHeight;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取控件的高度
     */
    public static int getHeightOfView(View view) {
        if (view == null)
            return -1;
        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        int height = view.getMeasuredHeight();
        return height;

    }

    /**
     * 渐变颜色
     * @param fromColor
     * @param toColor
     * @param positionOffset
     * @return
     */
    public static int changeColor(int fromColor, int toColor, float positionOffset) {
        int fromR = Color.red(fromColor);
        int fromG = Color.green(fromColor);
        int fromB = Color.blue(fromColor);
        int toR = Color.red(toColor);
        int toG = Color.green(toColor);
        int toB = Color.blue(toColor);
        int diffR = toR - fromR;
        int diffG = toG - fromG;
        int diffB = toB - fromB;
        int red = fromR + (int) ((diffR * positionOffset));
        int green = fromG + (int) ((diffG * positionOffset));
        int blue = fromB + (int) ((diffB * positionOffset));
        return Color.rgb(red, green, blue);
    }
}
