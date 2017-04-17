package com.qirui.browser.adapter;

import android.app.SearchManager;
import android.content.Intent;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.provider.BaseColumns;
import com.qirui.browser.util.UrlUtils;

/**
 * Created by lenvo on 2017/4/17.
 */
public class SuggestionsCursor extends AbstractCursor {

    private static final int ID_INDEX = 0;
    private static final int URL_INDEX = 1;
    private static final int TITLE_INDEX = 2;
    private static final int ICON_INDEX = 3;
    private static final int LAST_ACCESS_TIME_INDEX = 4;

    // shared suggestion array index, make sure to match COLUMNS
    private static final int SUGGEST_COLUMN_INTENT_ACTION_ID = 1;
    private static final int SUGGEST_COLUMN_INTENT_DATA_ID = 2;
    private static final int SUGGEST_COLUMN_TEXT_1_ID = 3;
    private static final int SUGGEST_COLUMN_TEXT_2_TEXT_ID = 4;
    private static final int SUGGEST_COLUMN_TEXT_2_URL_ID = 5;
    private static final int SUGGEST_COLUMN_ICON_1_ID = 6;
    private static final int SUGGEST_COLUMN_LAST_ACCESS_HINT_ID = 7;

    // shared suggestion columns
    private static final String[] COLUMNS = new String[]{
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_TEXT_2_URL,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_LAST_ACCESS_HINT};

    private final Cursor mSource;

    public SuggestionsCursor(Cursor cursor) {
        mSource = cursor;
    }

    @Override
    public String[] getColumnNames() {
        return COLUMNS;
    }

    @Override
    public String getString(int columnIndex) {
        switch (columnIndex) {
            case ID_INDEX:
                return mSource.getString(columnIndex);
            case SUGGEST_COLUMN_INTENT_ACTION_ID:
                return Intent.ACTION_VIEW;
            case SUGGEST_COLUMN_INTENT_DATA_ID:
                return mSource.getString(URL_INDEX);
            case SUGGEST_COLUMN_TEXT_2_TEXT_ID:
            case SUGGEST_COLUMN_TEXT_2_URL_ID:
                return UrlUtils.stripUrl(mSource.getString(URL_INDEX));
            case SUGGEST_COLUMN_TEXT_1_ID:
                return mSource.getString(TITLE_INDEX);
            case SUGGEST_COLUMN_ICON_1_ID:
                return mSource.getString(ICON_INDEX);
            case SUGGEST_COLUMN_LAST_ACCESS_HINT_ID:
                return mSource.getString(LAST_ACCESS_TIME_INDEX);
        }
        return null;
    }

    @Override
    public int getCount() {
        return mSource.getCount();
    }

    @Override
    public double getDouble(int column) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat(int column) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(int column) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(int column) {
        switch (column) {
            case ID_INDEX:
                return mSource.getLong(ID_INDEX);
            case SUGGEST_COLUMN_LAST_ACCESS_HINT_ID:
                return mSource.getLong(LAST_ACCESS_TIME_INDEX);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(int column) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNull(int column) {
        return mSource.isNull(column);
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        return mSource.moveToPosition(newPosition);
    }
}
