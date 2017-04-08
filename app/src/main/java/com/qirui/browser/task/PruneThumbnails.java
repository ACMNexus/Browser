package com.qirui.browser.task;

import android.content.ContentResolver;
import android.content.Context;
import com.qirui.browser.provider.BrowserProvider2;
import java.util.List;

/**
 * Created by Luooh on 2017/3/29.
 */
public class PruneThumbnails implements Runnable {

    private Context mContext;
    private List<Long> mIds;

    public PruneThumbnails(Context context, List<Long> preserveIds) {
        mContext = context.getApplicationContext();
        mIds = preserveIds;
    }

    @Override
    public void run() {
        ContentResolver cr = mContext.getContentResolver();
        if (mIds == null || mIds.size() == 0) {
            cr.delete(BrowserProvider2.Thumbnails.CONTENT_URI, null, null);
        } else {
            int length = mIds.size();
            StringBuilder where = new StringBuilder();
            where.append(BrowserProvider2.Thumbnails._ID);
            where.append(" not in (");
            for (int i = 0; i < length; i++) {
                where.append(mIds.get(i));
                if (i < (length - 1)) {
                    where.append(",");
                }
            }
            where.append(")");
            cr.delete(BrowserProvider2.Thumbnails.CONTENT_URI, where.toString(), null);
        }
    }
}
