package com.android.browser.util;

import android.database.Cursor;
import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Luooh on 2017/1/9.
 */
public class IOUtils {

    public static void closeStream(Closeable closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeCursor(Cursor cursor) {
        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}
