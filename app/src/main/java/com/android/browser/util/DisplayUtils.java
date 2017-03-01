package com.android.browser.util;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;

/**
 * Created by Luooh on 2017/2/11.
 */
public class DisplayUtils {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int getScreenWidth(Context context) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
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
