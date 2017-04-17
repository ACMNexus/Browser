package com.qirui.browser.provider;

import android.app.SearchManager;
import android.content.Intent;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.text.TextUtils;

import com.qirui.browser.Browser;
import com.qirui.browser.R;
import com.qirui.browser.util.StringUtils;

/*
 * Subclass AbstractCursor so we can combine multiple Cursors and add
 * "Search the web".
 * Here are the rules.
 * 1. We only have MAX_SUGGESTION_LONG_ENTRIES in the list plus
 *      "Search the web";
 * 2. If bookmark/history entries has a match, "Search the web" shows up at
 *      the second place. Otherwise, "Search the web" shows up at the first
 *      place.
 */
public class MySuggestionCursor extends AbstractCursor implements BrowserProviderKey {

    private Cursor mHistoryCursor;
    private Cursor mSuggestCursor;
    private int mHistoryCount;
    private int mSuggestionCount;
    private boolean mIncludeWebSearch;
    private String mString;
    private int mSuggestText1Id;
    private int mSuggestText2Id;
    private int mSuggestText2UrlId;
    private int mSuggestQueryId;
    private int mSuggestIntentExtraDataId;

    public MySuggestionCursor(Cursor hc, Cursor sc, String string, int count) {

        mHistoryCursor = hc;
        mSuggestCursor = sc;
        mHistoryCount = hc != null ? hc.getCount() : 0;
        mSuggestionCount = sc != null ? sc.getCount() : 0;
        if (mSuggestionCount > (count - mHistoryCount)) {
            mSuggestionCount = count - mHistoryCount;
        }
        mString = string;
        mIncludeWebSearch = string.length() > 0;

        // Some web suggest providers only give suggestions and have no description string for
        // items. The order of the result columns may be different as well. So retrieve the
        // column indices for the fields we need now and check before using below.
        if (mSuggestCursor == null) {
            mSuggestText1Id = -1;
            mSuggestText2Id = -1;
            mSuggestText2UrlId = -1;
            mSuggestQueryId = -1;
            mSuggestIntentExtraDataId = -1;
        } else {
            mSuggestText1Id = mSuggestCursor.getColumnIndex(
                    SearchManager.SUGGEST_COLUMN_TEXT_1);
            mSuggestText2Id = mSuggestCursor.getColumnIndex(
                    SearchManager.SUGGEST_COLUMN_TEXT_2);
            mSuggestText2UrlId = mSuggestCursor.getColumnIndex(
                    SearchManager.SUGGEST_COLUMN_TEXT_2_URL);
            mSuggestQueryId = mSuggestCursor.getColumnIndex(
                    SearchManager.SUGGEST_COLUMN_QUERY);
            mSuggestIntentExtraDataId = mSuggestCursor.getColumnIndex(
                    SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA);
        }
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        if (mHistoryCursor == null) {
            return false;
        }
        if (mIncludeWebSearch) {
            if (mHistoryCount == 0 && newPosition == 0) {
                return true;
            } else if (mHistoryCount > 0) {
                if (newPosition == 0) {
                    mHistoryCursor.moveToPosition(0);
                    return true;
                } else if (newPosition == 1) {
                    return true;
                }
            }
            newPosition--;
        }
        if (mHistoryCount > newPosition) {
            mHistoryCursor.moveToPosition(newPosition);
        } else {
            mSuggestCursor.moveToPosition(newPosition - mHistoryCount);
        }
        return true;
    }

    @Override
    public int getCount() {
        if (mIncludeWebSearch) {
            return mHistoryCount + mSuggestionCount + 1;
        } else {
            return mHistoryCount + mSuggestionCount;
        }
    }

    @Override
    public String[] getColumnNames() {
        return COLUMNS;
    }

    @Override
    public String getString(int columnIndex) {
        if ((mPos != -1 && mHistoryCursor != null)) {
            int type = -1; // 0: web search; 1: history; 2: suggestion
            if (mIncludeWebSearch) {
                if (mHistoryCount == 0 && mPos == 0) {
                    type = 0;
                } else if (mHistoryCount > 0) {
                    if (mPos == 0) {
                        type = 1;
                    } else if (mPos == 1) {
                        type = 0;
                    }
                }
                if (type == -1) type = (mPos - 1) < mHistoryCount ? 1 : 2;
            } else {
                type = mPos < mHistoryCount ? 1 : 2;
            }

            switch (columnIndex) {
                case SUGGEST_COLUMN_INTENT_ACTION_ID:
                    if (type == 1) {
                        return Intent.ACTION_VIEW;
                    } else {
                        return Intent.ACTION_SEARCH;
                    }

                case SUGGEST_COLUMN_INTENT_DATA_ID:
                    if (type == 1) {
                        return mHistoryCursor.getString(1);
                    } else {
                        return null;
                    }

                case SUGGEST_COLUMN_TEXT_1_ID:
                    if (type == 0) {
                        return mString;
                    } else if (type == 1) {
                        return getHistoryTitle();
                    } else {
                        if (mSuggestText1Id == -1) return null;
                        return mSuggestCursor.getString(mSuggestText1Id);
                    }

                case SUGGEST_COLUMN_TEXT_2_ID:
                    if (type == 0) {
                        return Browser.getInstance().getString(R.string.search_the_web);
                    } else if (type == 1) {
                        return null;  // Use TEXT_2_URL instead
                    } else {
                        if (mSuggestText2Id == -1) return null;
                        return mSuggestCursor.getString(mSuggestText2Id);
                    }

                case SUGGEST_COLUMN_TEXT_2_URL_ID:
                    if (type == 0) {
                        return null;
                    } else if (type == 1) {
                        return getHistoryUrl();
                    } else {
                        if (mSuggestText2UrlId == -1) return null;
                        return mSuggestCursor.getString(mSuggestText2UrlId);
                    }

                case SUGGEST_COLUMN_ICON_1_ID:
                    if (type == 1) {
                        if (mHistoryCursor.getInt(3) == 1) {
                            return Integer.valueOf(
                                    R.drawable.ic_search_category_bookmark)
                                    .toString();
                        } else {
                            return Integer.valueOf(
                                    R.drawable.ic_search_category_history)
                                    .toString();
                        }
                    } else {
                        return Integer.valueOf(
                                R.drawable.ic_search_category_suggest)
                                .toString();
                    }

                case SUGGEST_COLUMN_ICON_2_ID:
                    return "0";

                case SUGGEST_COLUMN_QUERY_ID:
                    if (type == 0) {
                        return mString;
                    } else if (type == 1) {
                        // Return the url in the intent query column. This is ignored
                        // within the browser because our searchable is set to
                        // android:searchMode="queryRewriteFromData", but it is used by
                        // global search for query rewriting.
                        return mHistoryCursor.getString(1);
                    } else {
                        if (mSuggestQueryId == -1) return null;
                        return mSuggestCursor.getString(mSuggestQueryId);
                    }

                case SUGGEST_COLUMN_INTENT_EXTRA_DATA:
                    if (type == 0) {
                        return null;
                    } else if (type == 1) {
                        return null;
                    } else {
                        if (mSuggestIntentExtraDataId == -1) return null;
                        return mSuggestCursor.getString(mSuggestIntentExtraDataId);
                    }
            }
        }
        return null;
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
        if ((mPos != -1) && column == 0) {
            return mPos;        // use row# as the _Id
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(int column) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNull(int column) {
        throw new UnsupportedOperationException();
    }

    // TODO Temporary change, finalize after jq's changes go in
    @Override
    public void deactivate() {
        if (mHistoryCursor != null) {
            mHistoryCursor.deactivate();
        }
        if (mSuggestCursor != null) {
            mSuggestCursor.deactivate();
        }
        super.deactivate();
    }

    @Override
    public boolean requery() {
        return (mHistoryCursor != null ? mHistoryCursor.requery() : false) |
                (mSuggestCursor != null ? mSuggestCursor.requery() : false);
    }

    // TODO Temporary change, finalize after jq's changes go in
    @Override
    public void close() {
        super.close();
        if (mHistoryCursor != null) {
            mHistoryCursor.close();
            mHistoryCursor = null;
        }
        if (mSuggestCursor != null) {
            mSuggestCursor.close();
            mSuggestCursor = null;
        }
    }

    /**
     * Provides the title (text line 1) for a browser suggestion, which should be the
     * webpage title. If the webpage title is empty, returns the stripped url instead.
     *
     * @return the title string to use
     */
    private String getHistoryTitle() {
        String title = mHistoryCursor.getString(2 /* webpage title */);
        if (TextUtils.isEmpty(title) || TextUtils.getTrimmedLength(title) == 0) {
            title = StringUtils.stripUrl(mHistoryCursor.getString(1 /* url */));
        }
        return title;
    }

    /**
     * Provides the subtitle (text line 2) for a browser suggestion, which should be the
     * webpage url. If the webpage title is empty, then the url should go in the title
     * instead, and the subtitle should be empty, so this would return null.
     *
     * @return the subtitle string to use, or null if none
     */
    private String getHistoryUrl() {
        String title = mHistoryCursor.getString(2 /* webpage title */);
        if (TextUtils.isEmpty(title) || TextUtils.getTrimmedLength(title) == 0) {
            return null;
        } else {
            return StringUtils.stripUrl(mHistoryCursor.getString(1 /* url */));
        }
    }
}
