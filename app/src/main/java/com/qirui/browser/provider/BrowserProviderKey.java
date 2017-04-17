package com.qirui.browser.provider;

import android.app.SearchManager;

/**
 * Created by Luooh on 2017/4/17.
 */
public interface BrowserProviderKey {

    // shared suggestion array index, make sure to match COLUMNS
    int SUGGEST_COLUMN_INTENT_ACTION_ID = 1;
    int SUGGEST_COLUMN_INTENT_DATA_ID = 2;
    int SUGGEST_COLUMN_TEXT_1_ID = 3;
    int SUGGEST_COLUMN_TEXT_2_ID = 4;
    int SUGGEST_COLUMN_TEXT_2_URL_ID = 5;
    int SUGGEST_COLUMN_ICON_1_ID = 6;
    int SUGGEST_COLUMN_ICON_2_ID = 7;
    int SUGGEST_COLUMN_QUERY_ID = 8;
    int SUGGEST_COLUMN_INTENT_EXTRA_DATA = 9;

    // shared suggestion columns
    String[] COLUMNS = new String[]{
            "_id",
            SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_TEXT_2_URL,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_ICON_2,
            SearchManager.SUGGEST_COLUMN_QUERY,
            SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
    };

    // how many suggestions will be shown in dropdown
    // 0..SHORT: filled by browser db
    int MAX_SUGGEST_SHORT_SMALL = 3;
    // SHORT..LONG: filled by search suggestions
    int MAX_SUGGEST_LONG_SMALL = 6;

    // large screen size shows more
    int MAX_SUGGEST_SHORT_LARGE = 6;
    int MAX_SUGGEST_LONG_LARGE = 9;


    // make sure that these match the index of TABLE_NAMES
    int URI_MATCH_BOOKMARKS = 0;
    int URI_MATCH_SEARCHES = 1;
    // (id % 10) should match the table name index
    int URI_MATCH_BOOKMARKS_ID = 10;
    int URI_MATCH_SEARCHES_ID = 11;
    //
    int URI_MATCH_SUGGEST = 20;
    int URI_MATCH_BOOKMARKS_SUGGEST = 21;

    String TAG = "BrowserProvider";
    String ORDER_BY = "visits DESC, date DESC";

    String PICASA_URL = "http://picasaweb.google.com/m/" + "viewer?source=androidclient";

    String[] TABLE_NAMES = new String[]{
            "bookmarks", "searches"
    };
    String[] SUGGEST_PROJECTION = new String[]{
            "_id", "url", "title", "bookmark", "user_entered"
    };

    String SUGGEST_SELECTION =
            "(url LIKE ? OR url LIKE ? OR url LIKE ? OR url LIKE ?"
                    + " OR title LIKE ?) AND (bookmark = 1 OR user_entered = 1)";

    // 1 -> 2 add cache table
    // 2 -> 3 update history table
    // 3 -> 4 add passwords table
    // 4 -> 5 add settings table
    // 5 -> 6 ?
    // 6 -> 7 ?
    // 7 -> 8 drop proxy table
    // 8 -> 9 drop settings table
    // 9 -> 10 add form_urls and form_data
    // 10 -> 11 add searches table
    // 11 -> 12 modify cache table
    // 12 -> 13 modify cache table
    // 13 -> 14 correspond with Google Bookmarks schema
    // 14 -> 15 move couple of tables to either browser private database or webview database
    // 15 -> 17 Set it up for the SearchManager
    // 17 -> 18 Added favicon in bookmarks table for Home shortcuts
    // 18 -> 19 Remove labels table
    // 19 -> 20 Added thumbnail
    // 20 -> 21 Added touch_icon
    // 21 -> 22 Remove "clientid"
    // 22 -> 23 Added user_entered
    // 23 -> 24 Url not allowed to be null anymore.
    int DATABASE_VERSION = 24;
}
