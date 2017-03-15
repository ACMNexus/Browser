package com.qirui.browser.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.qirui.browser.DateSortedExpandableListAdapter;
import com.qirui.browser.HistoryItem;
import com.qirui.browser.fragments.HistoryFragment;
import com.qirui.browser.util.BookmarkUtils;

/**
 * Created by Luooh on 2017/3/1.
 */
public class HistoryAdapter extends DateSortedExpandableListAdapter {

    private Context mContext;
    private Cursor mHistoryCursor;
    private Drawable mFaviconBackground;

    public HistoryAdapter(Context context) {
        super(context, HistoryFragment.HistoryQuery.INDEX_DATE_LAST_VISITED);
        this.mContext = context;
        this.mFaviconBackground = BookmarkUtils.createListFaviconBackground(context);
    }

    @Override
    public void changeCursor(Cursor cursor) {
        this.mHistoryCursor = cursor;
        super.changeCursor(cursor);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        if (moveCursorToChildPosition(groupPosition, childPosition)) {
            Cursor cursor = mHistoryCursor;
            return cursor.getLong(HistoryFragment.HistoryQuery.INDEX_ID);
        }
        return 0;
    }

    @Override
    public boolean isEmpty() {
        if (!super.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        HistoryItem item;
        if (convertView == null || !(convertView instanceof HistoryItem)) {
            item = new HistoryItem(mContext);
            // Add padding on the left so it will be indented from the
            // arrows on the group views.
            item.setFaviconBackground(mFaviconBackground);
        } else {
            item = (HistoryItem) convertView;
        }

        // Bail early if the Cursor is closed.
        if (!moveCursorToChildPosition(groupPosition, childPosition)) {
            return item;
        }

        Cursor cursor = mHistoryCursor;
        item.setName(cursor.getString(HistoryFragment.HistoryQuery.INDEX_TITE));
        String url = cursor.getString(HistoryFragment.HistoryQuery.INDEX_URL);
        item.setUrl(url);
        byte[] data = cursor.getBlob(HistoryFragment.HistoryQuery.INDEX_FAVICON);
        if (data != null) {
            item.setFavicon(BitmapFactory.decodeByteArray(data, 0, data.length));
        }
        item.setIsBookmark(cursor.getInt(HistoryFragment.HistoryQuery.INDEX_IS_BOOKMARK) == 1);
        return item;
    }
}
