package com.qirui.browser.provider;

import android.content.UriMatcher;
import android.net.Uri;
import com.qirui.browser.R;
import com.qirui.browser.util.StringUtils;
import java.util.HashMap;

/**
 * Created by Luooh on 2017/4/17.
 */
public interface BrowserProvider2Key {

    String PARAM_GROUP_BY = "groupBy";
    String PARAM_ALLOW_EMPTY_ACCOUNTS = "allowEmptyAccounts";

    String LEGACY_AUTHORITY = "browser";
    Uri LEGACY_AUTHORITY_URI = new Uri.Builder().authority(LEGACY_AUTHORITY).scheme("content").build();

    interface Thumbnails {
        Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "thumbnails");
        String _ID = "_id";
        String THUMBNAIL = "thumbnail";
    }

    public interface OmniboxSuggestions {
        Uri CONTENT_URI = Uri.withAppendedPath(
                BrowserContract.AUTHORITY_URI, "omnibox_suggestions");
        String _ID = "_id";
        String URL = "url";
        String TITLE = "title";
        String IS_BOOKMARK = "bookmark";
    }

    String TABLE_BOOKMARKS = "bookmarks";
    String TABLE_HISTORY = "history";
    String TABLE_IMAGES = "images";
    String TABLE_SEARCHES = "searches";
    String TABLE_SYNC_STATE = "syncstate";
    String TABLE_SETTINGS = "settings";
    String TABLE_SNAPSHOTS = "snapshots";
    String TABLE_THUMBNAILS = "thumbnails";

    String TABLE_BOOKMARKS_JOIN_IMAGES = "bookmarks LEFT OUTER JOIN images " +
            "ON bookmarks.url = images." + BrowserContract.Images.URL;
    String TABLE_HISTORY_JOIN_IMAGES = "history LEFT OUTER JOIN images " +
            "ON history.url = images." + BrowserContract.Images.URL;

    String VIEW_ACCOUNTS = "v_accounts";
    String VIEW_SNAPSHOTS_COMBINED = "v_snapshots_combined";
    String VIEW_OMNIBOX_SUGGESTIONS = "v_omnibox_suggestions";

    String FORMAT_COMBINED_JOIN_SUBQUERY_JOIN_IMAGES =
            "history LEFT OUTER JOIN (%s) bookmarks " +
                    "ON history.url = bookmarks.url LEFT OUTER JOIN images " +
                    "ON history.url = images.url_key";

    String DEFAULT_SORT_HISTORY = BrowserContract.History.DATE_LAST_VISITED + " DESC";
    String DEFAULT_SORT_ACCOUNTS =
            BrowserContract.Accounts.ACCOUNT_NAME + " IS NOT NULL DESC, "
                    + BrowserContract.Accounts.ACCOUNT_NAME + " ASC";

    String TABLE_BOOKMARKS_JOIN_HISTORY =
            "history LEFT OUTER JOIN bookmarks ON history.url = bookmarks.url";

    String[] SUGGEST_PROJECTION = new String[]{
            StringUtils.qualifyColumn(TABLE_HISTORY, BrowserContract.History._ID),
            StringUtils.qualifyColumn(TABLE_HISTORY, BrowserContract.History.URL),
            StringUtils.bookmarkOrHistoryColumn(BrowserContract.Combined.TITLE),
            StringUtils.bookmarkOrHistoryLiteral(BrowserContract.Combined.URL,
                    Integer.toString(R.drawable.ic_bookmark_off_holo_dark),
                    Integer.toString(R.drawable.ic_history_holo_dark)),
            StringUtils.qualifyColumn(TABLE_HISTORY, BrowserContract.History.DATE_LAST_VISITED)};

    String SUGGEST_SELECTION =
            "history.url LIKE ? OR history.url LIKE ? OR history.url LIKE ? OR history.url LIKE ?"
                    + " OR history.title LIKE ? OR bookmarks.title LIKE ?";

    String ZERO_QUERY_SUGGEST_SELECTION =
            TABLE_HISTORY + "." + BrowserContract.History.DATE_LAST_VISITED + " != 0";

   String IMAGE_PRUNE =
            "url_key NOT IN (SELECT url FROM bookmarks " +
                    "WHERE url IS NOT NULL AND deleted == 0) AND url_key NOT IN " +
                    "(SELECT url FROM history WHERE url IS NOT NULL)";

    int THUMBNAILS = 10;
    int THUMBNAILS_ID = 11;
    int OMNIBOX_SUGGESTIONS = 20;

    int BOOKMARKS = 1000;
    int BOOKMARKS_ID = 1001;
    int BOOKMARKS_FOLDER = 1002;
    int BOOKMARKS_FOLDER_ID = 1003;
    int BOOKMARKS_SUGGESTIONS = 1004;
    int BOOKMARKS_DEFAULT_FOLDER_ID = 1005;

    int HISTORY = 2000;
    int HISTORY_ID = 2001;

    int SEARCHES = 3000;
    int SEARCHES_ID = 3001;

    int SYNCSTATE = 4000;
    int SYNCSTATE_ID = 4001;

    int IMAGES = 5000;

    int COMBINED = 6000;
    int COMBINED_ID = 6001;

    int ACCOUNTS = 7000;

    int SETTINGS = 8000;

    int LEGACY = 9000;
    int LEGACY_ID = 9001;

    long FIXED_ID_ROOT = 1;

    // Default sort order for unsync'd bookmarks
    String DEFAULT_BOOKMARKS_SORT_ORDER = BrowserContract.Bookmarks.IS_FOLDER + " DESC, position ASC, _id ASC";

    // Default sort order for sync'd bookmarks
    String DEFAULT_BOOKMARKS_SORT_ORDER_SYNC = "position ASC, _id ASC";

    UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    HashMap<String, String> ACCOUNTS_PROJECTION_MAP = new HashMap();
    HashMap<String, String> BOOKMARKS_PROJECTION_MAP = new HashMap();
    HashMap<String, String> OTHER_BOOKMARKS_PROJECTION_MAP = new HashMap();
    HashMap<String, String> HISTORY_PROJECTION_MAP = new HashMap();
    HashMap<String, String> SYNC_STATE_PROJECTION_MAP = new HashMap();
    HashMap<String, String> IMAGES_PROJECTION_MAP = new HashMap();
    HashMap<String, String> COMBINED_HISTORY_PROJECTION_MAP = new HashMap();
    HashMap<String, String> COMBINED_BOOKMARK_PROJECTION_MAP = new HashMap();
    HashMap<String, String> SEARCHES_PROJECTION_MAP = new HashMap();
    HashMap<String, String> SETTINGS_PROJECTION_MAP = new HashMap();

    // ---------------------------------------------------
    //  SQL below, be warned
    // ---------------------------------------------------
    String SQL_CREATE_VIEW_OMNIBOX_SUGGESTIONS =
            "CREATE VIEW IF NOT EXISTS v_omnibox_suggestions "
                    + " AS "
                    + "  SELECT _id, url, title, 1 AS bookmark, 0 AS visits, 0 AS date"
                    + "  FROM bookmarks "
                    + "  WHERE deleted = 0 AND folder = 0 "
                    + "  UNION ALL "
                    + "  SELECT _id, url, title, 0 AS bookmark, visits, date "
                    + "  FROM history "
                    + "  WHERE url NOT IN (SELECT url FROM bookmarks"
                    + "    WHERE deleted = 0 AND folder = 0) "
                    + "  ORDER BY bookmark DESC, visits DESC, date DESC ";

    String SQL_WHERE_ACCOUNT_HAS_BOOKMARKS =
            "0 < ( "
                    + "SELECT count(*) "
                    + "FROM bookmarks "
                    + "WHERE deleted = 0 AND folder = 0 "
                    + "  AND ( "
                    + "    v_accounts.account_name = bookmarks.account_name "
                    + "    OR (v_accounts.account_name IS NULL AND bookmarks.account_name IS NULL) "
                    + "  ) "
                    + "  AND ( "
                    + "    v_accounts.account_type = bookmarks.account_type "
                    + "    OR (v_accounts.account_type IS NULL AND bookmarks.account_type IS NULL) "
                    + "  ) "
                    + ")";
}
