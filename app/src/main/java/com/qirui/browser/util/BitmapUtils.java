package com.qirui.browser.util;

import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;

/**
 * Created by lenvo on 2017/3/24.
 */
public class BitmapUtils {

    public static byte[] compressBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
