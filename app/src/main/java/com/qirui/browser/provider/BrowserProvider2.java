/*
 * Copyright (C) 2010 he Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.qirui.browser.provider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.AbstractCursor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Browser;
import android.provider.Browser.BookmarkColumns;

import android.provider.ContactsContract.RawContacts;
import android.provider.SyncStateContract;
import android.text.TextUtils;

import com.qirui.browser.R;
import com.qirui.browser.UrlUtils;
import com.qirui.browser.widget.BookmarkThumbnailWidgetProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BrowserProvider2 extends SQLiteContentProvider {

    public static final String PARAM_GROUP_BY = "groupBy";
    public static final String PARAM_ALLOW_EMPTY_ACCOUNTS = "allowEmptyAccounts";

    public static final String LEGACY_AUTHORITY = "browser";
    static final Uri LEGACY_AUTHORITY_URI = new Uri.Builder().authority(LEGACY_AUTHORITY).scheme("content").build();

    public static interface Thumbnails {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BrowserContract.AUTHORITY_URI, "thumbnails");
        public static final String _ID = "_id";
        public static final String THUMBNAIL = "thumbnail";
    }

    public static interface OmniboxSuggestions {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(
                BrowserContract.AUTHORITY_URI, "omnibox_suggestions");
        public static final String _ID = "_id";
        public static final String URL = "url";
        public static final String TITLE = "title";
        public static final String IS_BOOKMARK = "bookmark";
    }

    static final String TABLE_BOOKMARKS = "bookmarks";
    static final String TABLE_HISTORY = "history";
    static final String TABLE_IMAGES = "images";
    static final String TABLE_SEARCHES = "searches";
    static final String TABLE_SYNC_STATE = "syncstate";
    static final String TABLE_SETTINGS = "settings";
    static final String TABLE_SNAPSHOTS = "snapshots";
    static final String TABLE_THUMBNAILS = "thumbnails";

    static final String TABLE_BOOKMARKS_JOIN_IMAGES = "bookmarks LEFT OUTER JOIN images " +
            "ON bookmarks.url = images." + BrowserContract.Images.URL;
    static final String TABLE_HISTORY_JOIN_IMAGES = "history LEFT OUTER JOIN images " +
            "ON history.url = images." + BrowserContract.Images.URL;

    static final String VIEW_ACCOUNTS = "v_accounts";
    static final String VIEW_SNAPSHOTS_COMBINED = "v_snapshots_combined";
    static final String VIEW_OMNIBOX_SUGGESTIONS = "v_omnibox_suggestions";

    static final String FORMAT_COMBINED_JOIN_SUBQUERY_JOIN_IMAGES =
            "history LEFT OUTER JOIN (%s) bookmarks " +
                    "ON history.url = bookmarks.url LEFT OUTER JOIN images " +
                    "ON history.url = images.url_key";

    static final String DEFAULT_SORT_HISTORY = BrowserContract.History.DATE_LAST_VISITED + " DESC";
    static final String DEFAULT_SORT_ACCOUNTS =
            BrowserContract.Accounts.ACCOUNT_NAME + " IS NOT NULL DESC, "
                    + BrowserContract.Accounts.ACCOUNT_NAME + " ASC";

    private static final String TABLE_BOOKMARKS_JOIN_HISTORY =
            "history LEFT OUTER JOIN bookmarks ON history.url = bookmarks.url";

    private static final String[] SUGGEST_PROJECTION = new String[]{
            qualifyColumn(TABLE_HISTORY, BrowserContract.History._ID),
            qualifyColumn(TABLE_HISTORY, BrowserContract.History.URL),
            bookmarkOrHistoryColumn(BrowserContract.Combined.TITLE),
            bookmarkOrHistoryLiteral(BrowserContract.Combined.URL,
                    Integer.toString(R.drawable.ic_bookmark_off_holo_dark),
                    Integer.toString(R.drawable.ic_history_holo_dark)),
            qualifyColumn(TABLE_HISTORY, BrowserContract.History.DATE_LAST_VISITED)};

    private static final String SUGGEST_SELECTION =
            "history.url LIKE ? OR history.url LIKE ? OR history.url LIKE ? OR history.url LIKE ?"
                    + " OR history.title LIKE ? OR bookmarks.title LIKE ?";

    private static final String ZERO_QUERY_SUGGEST_SELECTION =
            TABLE_HISTORY + "." + BrowserContract.History.DATE_LAST_VISITED + " != 0";

    private static final String IMAGE_PRUNE =
            "url_key NOT IN (SELECT url FROM bookmarks " +
                    "WHERE url IS NOT NULL AND deleted == 0) AND url_key NOT IN " +
                    "(SELECT url FROM history WHERE url IS NOT NULL)";

    static final int THUMBNAILS = 10;
    static final int THUMBNAILS_ID = 11;
    static final int OMNIBOX_SUGGESTIONS = 20;

    static final int BOOKMARKS = 1000;
    static final int BOOKMARKS_ID = 1001;
    static final int BOOKMARKS_FOLDER = 1002;
    static final int BOOKMARKS_FOLDER_ID = 1003;
    static final int BOOKMARKS_SUGGESTIONS = 1004;
    static final int BOOKMARKS_DEFAULT_FOLDER_ID = 1005;

    static final int HISTORY = 2000;
    static final int HISTORY_ID = 2001;

    static final int SEARCHES = 3000;
    static final int SEARCHES_ID = 3001;

    static final int SYNCSTATE = 4000;
    static final int SYNCSTATE_ID = 4001;

    static final int IMAGES = 5000;

    static final int COMBINED = 6000;
    static final int COMBINED_ID = 6001;

    static final int ACCOUNTS = 7000;

    static final int SETTINGS = 8000;

    static final int LEGACY = 9000;
    static final int LEGACY_ID = 9001;

    public static final long FIXED_ID_ROOT = 1;

    // Default sort order for unsync'd bookmarks
    static final String DEFAULT_BOOKMARKS_SORT_ORDER =
            BrowserContract.Bookmarks.IS_FOLDER + " DESC, position ASC, _id ASC";

    // Default sort order for sync'd bookmarks
    static final String DEFAULT_BOOKMARKS_SORT_ORDER_SYNC = "position ASC, _id ASC";

    static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static final HashMap<String, String> ACCOUNTS_PROJECTION_MAP = new HashMap<String, String>();
    static final HashMap<String, String> BOOKMARKS_PROJECTION_MAP = new HashMap<String, String>();
    static final HashMap<String, String> OTHER_BOOKMARKS_PROJECTION_MAP =
            new HashMap<String, String>();
    static final HashMap<String, String> HISTORY_PROJECTION_MAP = new HashMap<String, String>();
    static final HashMap<String, String> SYNC_STATE_PROJECTION_MAP = new HashMap<String, String>();
    static final HashMap<String, String> IMAGES_PROJECTION_MAP = new HashMap<String, String>();
    static final HashMap<String, String> COMBINED_HISTORY_PROJECTION_MAP = new HashMap<String, String>();
    static final HashMap<String, String> COMBINED_BOOKMARK_PROJECTION_MAP = new HashMap<String, String>();
    static final HashMap<String, String> SEARCHES_PROJECTION_MAP = new HashMap<String, String>();
    static final HashMap<String, String> SETTINGS_PROJECTION_MAP = new HashMap<String, String>();

    static {
        final UriMatcher matcher = URI_MATCHER;
        final String authority = BrowserContract.AUTHORITY;
        matcher.addURI(authority, "accounts", ACCOUNTS);
        matcher.addURI(authority, "bookmarks", BOOKMARKS);
        matcher.addURI(authority, "bookmarks/#", BOOKMARKS_ID);
        matcher.addURI(authority, "bookmarks/folder", BOOKMARKS_FOLDER);
        matcher.addURI(authority, "bookmarks/folder/#", BOOKMARKS_FOLDER_ID);
        matcher.addURI(authority, "bookmarks/folder/id", BOOKMARKS_DEFAULT_FOLDER_ID);
        matcher.addURI(authority, SearchManager.SUGGEST_URI_PATH_QUERY, BOOKMARKS_SUGGESTIONS);
        matcher.addURI(authority, "bookmarks/" + SearchManager.SUGGEST_URI_PATH_QUERY, BOOKMARKS_SUGGESTIONS);
        matcher.addURI(authority, "history", HISTORY);
        matcher.addURI(authority, "history/#", HISTORY_ID);
        matcher.addURI(authority, "searches", SEARCHES);
        matcher.addURI(authority, "searches/#", SEARCHES_ID);
        matcher.addURI(authority, "syncstate", SYNCSTATE);
        matcher.addURI(authority, "syncstate/#", SYNCSTATE_ID);
        matcher.addURI(authority, "images", IMAGES);
        matcher.addURI(authority, "combined", COMBINED);
        matcher.addURI(authority, "combined/#", COMBINED_ID);
        matcher.addURI(authority, "settings", SETTINGS);
        matcher.addURI(authority, "thumbnails", THUMBNAILS);
        matcher.addURI(authority, "thumbnails/#", THUMBNAILS_ID);
        matcher.addURI(authority, "omnibox_suggestions", OMNIBOX_SUGGESTIONS);

        // Legacy
        matcher.addURI(LEGACY_AUTHORITY, "searches", SEARCHES);
        matcher.addURI(LEGACY_AUTHORITY, "searches/#", SEARCHES_ID);
        matcher.addURI(LEGACY_AUTHORITY, "bookmarks", LEGACY);
        matcher.addURI(LEGACY_AUTHORITY, "bookmarks/#", LEGACY_ID);
        matcher.addURI(LEGACY_AUTHORITY,
                SearchManager.SUGGEST_URI_PATH_QUERY,
                BOOKMARKS_SUGGESTIONS);
        matcher.addURI(LEGACY_AUTHORITY,
                "bookmarks/" + SearchManager.SUGGEST_URI_PATH_QUERY,
                BOOKMARKS_SUGGESTIONS);

        // Projection maps
        HashMap<String, String> map;

        // Accounts
        map = ACCOUNTS_PROJECTION_MAP;
        map.put(BrowserContract.Accounts.ACCOUNT_TYPE, BrowserContract.Accounts.ACCOUNT_TYPE);
        map.put(BrowserContract.Accounts.ACCOUNT_NAME, BrowserContract.Accounts.ACCOUNT_NAME);
        map.put(BrowserContract.Accounts.ROOT_ID, BrowserContract.Accounts.ROOT_ID);

        // Bookmarks
        map = BOOKMARKS_PROJECTION_MAP;
        map.put(BrowserContract.Bookmarks._ID, qualifyColumn(TABLE_BOOKMARKS, BrowserContract.Bookmarks._ID));
        map.put(BrowserContract.Bookmarks.TITLE, BrowserContract.Bookmarks.TITLE);
        map.put(BrowserContract.Bookmarks.URL, BrowserContract.Bookmarks.URL);
        map.put(BrowserContract.Bookmarks.FAVICON, BrowserContract.Bookmarks.FAVICON);
        map.put(BrowserContract.Bookmarks.THUMBNAIL, BrowserContract.Bookmarks.THUMBNAIL);
        map.put(BrowserContract.Bookmarks.TOUCH_ICON, BrowserContract.Bookmarks.TOUCH_ICON);
        map.put(BrowserContract.Bookmarks.IS_FOLDER, BrowserContract.Bookmarks.IS_FOLDER);
        map.put(BrowserContract.Bookmarks.PARENT, BrowserContract.Bookmarks.PARENT);
        map.put(BrowserContract.Bookmarks.POSITION, BrowserContract.Bookmarks.POSITION);
        map.put(BrowserContract.Bookmarks.INSERT_AFTER, BrowserContract.Bookmarks.INSERT_AFTER);
        map.put(BrowserContract.Bookmarks.IS_DELETED, BrowserContract.Bookmarks.IS_DELETED);
        map.put(BrowserContract.Bookmarks.ACCOUNT_NAME, BrowserContract.Bookmarks.ACCOUNT_NAME);
        map.put(BrowserContract.Bookmarks.ACCOUNT_TYPE, BrowserContract.Bookmarks.ACCOUNT_TYPE);
        map.put(BrowserContract.Bookmarks.SOURCE_ID, BrowserContract.Bookmarks.SOURCE_ID);
        map.put(BrowserContract.Bookmarks.VERSION, BrowserContract.Bookmarks.VERSION);
        map.put(BrowserContract.Bookmarks.DATE_CREATED, BrowserContract.Bookmarks.DATE_CREATED);
        map.put(BrowserContract.Bookmarks.DATE_MODIFIED, BrowserContract.Bookmarks.DATE_MODIFIED);
        map.put(BrowserContract.Bookmarks.DIRTY, BrowserContract.Bookmarks.DIRTY);
        map.put(BrowserContract.Bookmarks.SYNC1, BrowserContract.Bookmarks.SYNC1);
        map.put(BrowserContract.Bookmarks.SYNC2, BrowserContract.Bookmarks.SYNC2);
        map.put(BrowserContract.Bookmarks.SYNC3, BrowserContract.Bookmarks.SYNC3);
        map.put(BrowserContract.Bookmarks.SYNC4, BrowserContract.Bookmarks.SYNC4);
        map.put(BrowserContract.Bookmarks.SYNC5, BrowserContract.Bookmarks.SYNC5);
        map.put(BrowserContract.Bookmarks.PARENT_SOURCE_ID, "(SELECT " + BrowserContract.Bookmarks.SOURCE_ID +
                " FROM " + TABLE_BOOKMARKS + " A WHERE " +
                "A." + BrowserContract.Bookmarks._ID + "=" + TABLE_BOOKMARKS + "." + BrowserContract.Bookmarks.PARENT +
                ") AS " + BrowserContract.Bookmarks.PARENT_SOURCE_ID);
        map.put(BrowserContract.Bookmarks.INSERT_AFTER_SOURCE_ID, "(SELECT " + BrowserContract.Bookmarks.SOURCE_ID +
                " FROM " + TABLE_BOOKMARKS + " A WHERE " +
                "A." + BrowserContract.Bookmarks._ID + "=" + TABLE_BOOKMARKS + "." + BrowserContract.Bookmarks.INSERT_AFTER +
                ") AS " + BrowserContract.Bookmarks.INSERT_AFTER_SOURCE_ID);
        map.put(BrowserContract.Bookmarks.TYPE, "CASE "
                + " WHEN " + BrowserContract.Bookmarks.IS_FOLDER + "=0 THEN "
                + BrowserContract.Bookmarks.BOOKMARK_TYPE_BOOKMARK
                + " WHEN " + BrowserContract.ChromeSyncColumns.SERVER_UNIQUE + "='"
                + BrowserContract.ChromeSyncColumns.FOLDER_NAME_BOOKMARKS_BAR + "' THEN "
                + BrowserContract.Bookmarks.BOOKMARK_TYPE_BOOKMARK_BAR_FOLDER
                + " WHEN " + BrowserContract.ChromeSyncColumns.SERVER_UNIQUE + "='"
                + BrowserContract.ChromeSyncColumns.FOLDER_NAME_OTHER_BOOKMARKS + "' THEN "
                + BrowserContract.Bookmarks.BOOKMARK_TYPE_OTHER_FOLDER
                + " ELSE " + BrowserContract.Bookmarks.BOOKMARK_TYPE_FOLDER
                + " END AS " + BrowserContract.Bookmarks.TYPE);

        // Other bookmarks
        OTHER_BOOKMARKS_PROJECTION_MAP.putAll(BOOKMARKS_PROJECTION_MAP);
        OTHER_BOOKMARKS_PROJECTION_MAP.put(BrowserContract.Bookmarks.POSITION,
                Long.toString(Long.MAX_VALUE) + " AS " + BrowserContract.Bookmarks.POSITION);

        // History
        map = HISTORY_PROJECTION_MAP;
        map.put(BrowserContract.History._ID, qualifyColumn(TABLE_HISTORY, BrowserContract.History._ID));
        map.put(BrowserContract.History.TITLE, BrowserContract.History.TITLE);
        map.put(BrowserContract.History.URL, BrowserContract.History.URL);
        map.put(BrowserContract.History.FAVICON, BrowserContract.History.FAVICON);
        map.put(BrowserContract.History.THUMBNAIL, BrowserContract.History.THUMBNAIL);
        map.put(BrowserContract.History.TOUCH_ICON, BrowserContract.History.TOUCH_ICON);
        map.put(BrowserContract.History.DATE_CREATED, BrowserContract.History.DATE_CREATED);
        map.put(BrowserContract.History.DATE_LAST_VISITED, BrowserContract.History.DATE_LAST_VISITED);
        map.put(BrowserContract.History.VISITS, BrowserContract.History.VISITS);
        map.put(BrowserContract.History.USER_ENTERED, BrowserContract.History.USER_ENTERED);

        // Sync state
        map = SYNC_STATE_PROJECTION_MAP;
        map.put(BrowserContract.SyncState._ID, BrowserContract.SyncState._ID);
        map.put(BrowserContract.SyncState.ACCOUNT_NAME, BrowserContract.SyncState.ACCOUNT_NAME);
        map.put(BrowserContract.SyncState.ACCOUNT_TYPE, BrowserContract.SyncState.ACCOUNT_TYPE);
        map.put(BrowserContract.SyncState.DATA, BrowserContract.SyncState.DATA);

        // Images
        map = IMAGES_PROJECTION_MAP;
        map.put(BrowserContract.Images.URL, BrowserContract.Images.URL);
        map.put(BrowserContract.Images.FAVICON, BrowserContract.Images.FAVICON);
        map.put(BrowserContract.Images.THUMBNAIL, BrowserContract.Images.THUMBNAIL);
        map.put(BrowserContract.Images.TOUCH_ICON, BrowserContract.Images.TOUCH_ICON);

        // Combined history half
        map = COMBINED_HISTORY_PROJECTION_MAP;
        map.put(BrowserContract.Combined._ID, bookmarkOrHistoryColumn(BrowserContract.Combined._ID));
        map.put(BrowserContract.Combined.TITLE, bookmarkOrHistoryColumn(BrowserContract.Combined.TITLE));
        map.put(BrowserContract.Combined.URL, qualifyColumn(TABLE_HISTORY, BrowserContract.Combined.URL));
        map.put(BrowserContract.Combined.DATE_CREATED, qualifyColumn(TABLE_HISTORY, BrowserContract.Combined.DATE_CREATED));
        map.put(BrowserContract.Combined.DATE_LAST_VISITED, BrowserContract.Combined.DATE_LAST_VISITED);
        map.put(BrowserContract.Combined.IS_BOOKMARK, "CASE WHEN " +
                TABLE_BOOKMARKS + "." + BrowserContract.Bookmarks._ID +
                " IS NOT NULL THEN 1 ELSE 0 END AS " + BrowserContract.Combined.IS_BOOKMARK);
        map.put(BrowserContract.Combined.VISITS, BrowserContract.Combined.VISITS);
        map.put(BrowserContract.Combined.FAVICON, BrowserContract.Combined.FAVICON);
        map.put(BrowserContract.Combined.THUMBNAIL, BrowserContract.Combined.THUMBNAIL);
        map.put(BrowserContract.Combined.TOUCH_ICON, BrowserContract.Combined.TOUCH_ICON);
        map.put(BrowserContract.Combined.USER_ENTERED, "NULL AS " + BrowserContract.Combined.USER_ENTERED);

        // Combined bookmark half
        map = COMBINED_BOOKMARK_PROJECTION_MAP;
        map.put(BrowserContract.Combined._ID, BrowserContract.Combined._ID);
        map.put(BrowserContract.Combined.TITLE, BrowserContract.Combined.TITLE);
        map.put(BrowserContract.Combined.URL, BrowserContract.Combined.URL);
        map.put(BrowserContract.Combined.DATE_CREATED, BrowserContract.Combined.DATE_CREATED);
        map.put(BrowserContract.Combined.DATE_LAST_VISITED, "NULL AS " + BrowserContract.Combined.DATE_LAST_VISITED);
        map.put(BrowserContract.Combined.IS_BOOKMARK, "1 AS " + BrowserContract.Combined.IS_BOOKMARK);
        map.put(BrowserContract.Combined.VISITS, "0 AS " + BrowserContract.Combined.VISITS);
        map.put(BrowserContract.Combined.FAVICON, BrowserContract.Combined.FAVICON);
        map.put(BrowserContract.Combined.THUMBNAIL, BrowserContract.Combined.THUMBNAIL);
        map.put(BrowserContract.Combined.TOUCH_ICON, BrowserContract.Combined.TOUCH_ICON);
        map.put(BrowserContract.Combined.USER_ENTERED, "NULL AS " + BrowserContract.Combined.USER_ENTERED);

        // Searches
        map = SEARCHES_PROJECTION_MAP;
        map.put(BrowserContract.Searches._ID, BrowserContract.Searches._ID);
        map.put(BrowserContract.Searches.SEARCH, BrowserContract.Searches.SEARCH);
        map.put(BrowserContract.Searches.DATE, BrowserContract.Searches.DATE);

        // Settings
        map = SETTINGS_PROJECTION_MAP;
        map.put(BrowserContract.Settings.KEY, BrowserContract.Settings.KEY);
        map.put(BrowserContract.Settings.VALUE, BrowserContract.Settings.VALUE);
    }

    static final String bookmarkOrHistoryColumn(String column) {
        return "CASE WHEN bookmarks." + column + " IS NOT NULL THEN " +
                "bookmarks." + column + " ELSE history." + column + " END AS " + column;
    }

    static final String bookmarkOrHistoryLiteral(String column, String bookmarkValue,
                                                 String historyValue) {
        return "CASE WHEN bookmarks." + column + " IS NOT NULL THEN \"" + bookmarkValue +
                "\" ELSE \"" + historyValue + "\" END";
    }

    static final String qualifyColumn(String table, String column) {
        return table + "." + column + " AS " + column;
    }

    DatabaseHelper mOpenHelper;
    SyncStateContentProviderHelper mSyncHelper = new SyncStateContentProviderHelper();
    // This is so provider tests can intercept widget updating
    ContentObserver mWidgetObserver = null;
    boolean mUpdateWidgets = false;
    boolean mSyncToNetwork = true;

    final class DatabaseHelper extends SQLiteOpenHelper {
        static final String DATABASE_NAME = "browser2.db";
        static final int DATABASE_VERSION = 32;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            setWriteAheadLoggingEnabled(true);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_BOOKMARKS + "(" +
                    BrowserContract.Bookmarks._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    BrowserContract.Bookmarks.TITLE + " TEXT," +
                    BrowserContract.Bookmarks.URL + " TEXT," +
                    BrowserContract.Bookmarks.IS_FOLDER + " INTEGER NOT NULL DEFAULT 0," +
                    BrowserContract.Bookmarks.PARENT + " INTEGER," +
                    BrowserContract.Bookmarks.POSITION + " INTEGER NOT NULL," +
                    BrowserContract.Bookmarks.INSERT_AFTER + " INTEGER," +
                    BrowserContract.Bookmarks.IS_DELETED + " INTEGER NOT NULL DEFAULT 0," +
                    BrowserContract.Bookmarks.ACCOUNT_NAME + " TEXT," +
                    BrowserContract.Bookmarks.ACCOUNT_TYPE + " TEXT," +
                    BrowserContract.Bookmarks.SOURCE_ID + " TEXT," +
                    BrowserContract.Bookmarks.VERSION + " INTEGER NOT NULL DEFAULT 1," +
                    BrowserContract.Bookmarks.DATE_CREATED + " INTEGER," +
                    BrowserContract.Bookmarks.DATE_MODIFIED + " INTEGER," +
                    BrowserContract.Bookmarks.DIRTY + " INTEGER NOT NULL DEFAULT 0," +
                    BrowserContract.Bookmarks.SYNC1 + " TEXT," +
                    BrowserContract.Bookmarks.SYNC2 + " TEXT," +
                    BrowserContract.Bookmarks.SYNC3 + " TEXT," +
                    BrowserContract.Bookmarks.SYNC4 + " TEXT," +
                    BrowserContract.Bookmarks.SYNC5 + " TEXT" +
                    ");");

            // TODO indices
            db.execSQL("CREATE TABLE " + TABLE_HISTORY + "(" +
                    BrowserContract.History._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    BrowserContract.History.TITLE + " TEXT," +
                    BrowserContract.History.URL + " TEXT NOT NULL," +
                    BrowserContract.History.DATE_CREATED + " INTEGER," +
                    BrowserContract.History.DATE_LAST_VISITED + " INTEGER," +
                    BrowserContract.History.VISITS + " INTEGER NOT NULL DEFAULT 0," +
                    BrowserContract.History.USER_ENTERED + " INTEGER" +
                    ");");

            db.execSQL("CREATE TABLE " + TABLE_IMAGES + " (" +
                    BrowserContract.Images.URL + " TEXT UNIQUE NOT NULL," +
                    BrowserContract.Images.FAVICON + " BLOB," +
                    BrowserContract.Images.THUMBNAIL + " BLOB," +
                    BrowserContract.Images.TOUCH_ICON + " BLOB" +
                    ");");
            db.execSQL("CREATE INDEX imagesUrlIndex ON " + TABLE_IMAGES +
                    "(" + BrowserContract.Images.URL + ")");

            db.execSQL("CREATE TABLE " + TABLE_SEARCHES + " (" +
                    BrowserContract.Searches._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    BrowserContract.Searches.SEARCH + " TEXT," +
                    BrowserContract.Searches.DATE + " LONG" +
                    ");");

            db.execSQL("CREATE TABLE " + TABLE_SETTINGS + " (" +
                    BrowserContract.Settings.KEY + " TEXT PRIMARY KEY," +
                    BrowserContract.Settings.VALUE + " TEXT NOT NULL" +
                    ");");

            createAccountsView(db);
            createThumbnails(db);

            mSyncHelper.createDatabase(db);

            if (!importFromBrowserProvider(db)) {
                createDefaultBookmarks(db);
            }

            enableSync(db);
            createOmniboxSuggestions(db);
        }

        void createOmniboxSuggestions(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_VIEW_OMNIBOX_SUGGESTIONS);
        }

        void createThumbnails(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_THUMBNAILS + " (" +
                    Thumbnails._ID + " INTEGER PRIMARY KEY," +
                    Thumbnails.THUMBNAIL + " BLOB NOT NULL" +
                    ");");
        }

        void enableSync(SQLiteDatabase db) {
            ContentValues values = new ContentValues();
            values.put(BrowserContract.Settings.KEY, BrowserContract.Settings.KEY_SYNC_ENABLED);
            values.put(BrowserContract.Settings.VALUE, 1);
            insertSettingsInTransaction(db, values);
            // Enable bookmark sync on all accounts
            AccountManager am = (AccountManager) getContext().getSystemService(
                    Context.ACCOUNT_SERVICE);
            if (am == null) {
                return;
            }
            Account[] accounts = am.getAccountsByType("com.google");
            if (accounts == null || accounts.length == 0) {
                return;
            }
            for (Account account : accounts) {
                if (ContentResolver.getIsSyncable(
                        account, BrowserContract.AUTHORITY) == 0) {
                    // Account wasn't syncable, enable it
                    ContentResolver.setIsSyncable(
                            account, BrowserContract.AUTHORITY, 1);
                    ContentResolver.setSyncAutomatically(
                            account, BrowserContract.AUTHORITY, true);
                }
            }
        }

        boolean importFromBrowserProvider(SQLiteDatabase db) {
            Context context = getContext();
            File oldDbFile = context.getDatabasePath(BrowserProvider.sDatabaseName);
            if (oldDbFile.exists()) {
                BrowserProvider.DatabaseHelper helper =
                        new BrowserProvider.DatabaseHelper(context);
                SQLiteDatabase oldDb = helper.getWritableDatabase();
                Cursor c = null;
                try {
                    String table = BrowserProvider.TABLE_NAMES[BrowserProvider.URI_MATCH_BOOKMARKS];
                    // Import bookmarks
                    c = oldDb.query(table,
                            new String[]{
                                    BookmarkColumns.URL, // 0
                                    BookmarkColumns.TITLE, // 1
                                    BookmarkColumns.FAVICON, // 2
                                    "touch_icon", // 3
                                    BookmarkColumns.CREATED, // 4
                            }, BookmarkColumns.BOOKMARK + "!=0", null,
                            null, null, null);
                    if (c != null) {
                        while (c.moveToNext()) {
                            String url = c.getString(0);
                            if (TextUtils.isEmpty(url))
                                continue; // We require a valid URL
                            ContentValues values = new ContentValues();
                            values.put(BrowserContract.Bookmarks.URL, url);
                            values.put(BrowserContract.Bookmarks.TITLE, c.getString(1));
                            values.put(BrowserContract.Bookmarks.DATE_CREATED, c.getInt(4));
                            values.put(BrowserContract.Bookmarks.POSITION, 0);
                            values.put(BrowserContract.Bookmarks.PARENT, FIXED_ID_ROOT);
                            ContentValues imageValues = new ContentValues();
                            imageValues.put(BrowserContract.Images.URL, url);
                            imageValues.put(BrowserContract.Images.FAVICON, c.getBlob(2));
                            imageValues.put(BrowserContract.Images.TOUCH_ICON, c.getBlob(3));
                            db.insert(TABLE_IMAGES, BrowserContract.Images.THUMBNAIL, imageValues);
                            db.insert(TABLE_BOOKMARKS, BrowserContract.Bookmarks.DIRTY, values);
                        }
                        c.close();
                    }
                    // Import history
                    c = oldDb.query(table,
                            new String[]{
                                    BookmarkColumns.URL, // 0
                                    BookmarkColumns.TITLE, // 1
                                    BookmarkColumns.VISITS, // 2
                                    BookmarkColumns.DATE, // 3
                                    BookmarkColumns.CREATED, // 4
                            }, BookmarkColumns.VISITS + " > 0 OR "
                                    + BookmarkColumns.BOOKMARK + " = 0",
                            null, null, null, null);
                    if (c != null) {
                        while (c.moveToNext()) {
                            ContentValues values = new ContentValues();
                            String url = c.getString(0);
                            if (TextUtils.isEmpty(url))
                                continue; // We require a valid URL
                            values.put(BrowserContract.History.URL, url);
                            values.put(BrowserContract.History.TITLE, c.getString(1));
                            values.put(BrowserContract.History.VISITS, c.getInt(2));
                            values.put(BrowserContract.History.DATE_LAST_VISITED, c.getLong(3));
                            values.put(BrowserContract.History.DATE_CREATED, c.getLong(4));
                            db.insert(TABLE_HISTORY, BrowserContract.History.FAVICON, values);
                        }
                        c.close();
                    }
                    // Wipe the old DB, in case the delete fails.
                    oldDb.delete(table, null, null);
                } finally {
                    if (c != null) c.close();
                    oldDb.close();
                    helper.close();
                }
                if (!oldDbFile.delete()) {
                    oldDbFile.deleteOnExit();
                }
                return true;
            }
            return false;
        }

        void createAccountsView(SQLiteDatabase db) {
            db.execSQL("CREATE VIEW IF NOT EXISTS v_accounts AS "
                    + "SELECT NULL AS " + BrowserContract.Accounts.ACCOUNT_NAME
                    + ", NULL AS " + BrowserContract.Accounts.ACCOUNT_TYPE
                    + ", " + FIXED_ID_ROOT + " AS " + BrowserContract.Accounts.ROOT_ID
                    + " UNION ALL SELECT " + BrowserContract.Accounts.ACCOUNT_NAME
                    + ", " + BrowserContract.Accounts.ACCOUNT_TYPE + ", "
                    + BrowserContract.Bookmarks._ID + " AS " + BrowserContract.Accounts.ROOT_ID
                    + " FROM " + TABLE_BOOKMARKS + " WHERE "
                    + BrowserContract.ChromeSyncColumns.SERVER_UNIQUE + " = \""
                    + BrowserContract.ChromeSyncColumns.FOLDER_NAME_BOOKMARKS_BAR + "\" AND "
                    + BrowserContract.Bookmarks.IS_DELETED + " = 0");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 32) {
                createOmniboxSuggestions(db);
            }
            if (oldVersion < 31) {
                createThumbnails(db);
            }
            if (oldVersion < 30) {
                db.execSQL("DROP VIEW IF EXISTS " + VIEW_SNAPSHOTS_COMBINED);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SNAPSHOTS);
            }
            if (oldVersion < 28) {
                enableSync(db);
            }
            if (oldVersion < 27) {
                createAccountsView(db);
            }
            if (oldVersion < 26) {
                db.execSQL("DROP VIEW IF EXISTS combined");
            }
            if (oldVersion < 25) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCHES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
                mSyncHelper.onAccountsChanged(db, new Account[]{}); // remove all sync info
                onCreate(db);
            }
        }

        public void onOpen(SQLiteDatabase db) {
            mSyncHelper.onDatabaseOpened(db);
        }

        private void createDefaultBookmarks(SQLiteDatabase db) {
            ContentValues values = new ContentValues();
            // TODO figure out how to deal with localization for the defaults

            // Bookmarks folder
            values.put(BrowserContract.Bookmarks._ID, FIXED_ID_ROOT);
            values.put(BrowserContract.ChromeSyncColumns.SERVER_UNIQUE, BrowserContract.ChromeSyncColumns.FOLDER_NAME_BOOKMARKS);
            values.put(BrowserContract.Bookmarks.TITLE, "Bookmarks");
            values.putNull(BrowserContract.Bookmarks.PARENT);
            values.put(BrowserContract.Bookmarks.POSITION, 0);
            values.put(BrowserContract.Bookmarks.IS_FOLDER, true);
            values.put(BrowserContract.Bookmarks.DIRTY, true);
            db.insertOrThrow(TABLE_BOOKMARKS, null, values);

            addDefaultBookmarks(db, FIXED_ID_ROOT);
        }

        private void addDefaultBookmarks(SQLiteDatabase db, long parentId) {
            Resources res = getContext().getResources();
            final CharSequence[] bookmarks = res.getTextArray(
                    R.array.bookmarks);
            int size = bookmarks.length;
            TypedArray preloads = null/*res.obtainTypedArray(R.array.bookmark_preloads)*/;
            if (preloads == null) return;
            try {
                String parent = Long.toString(parentId);
                String now = Long.toString(System.currentTimeMillis());
                for (int i = 0; i < size; i = i + 2) {
                    CharSequence bookmarkDestination = replaceSystemPropertyInString(getContext(),
                            bookmarks[i + 1]);
                    db.execSQL("INSERT INTO bookmarks (" +
                            BrowserContract.Bookmarks.TITLE + ", " +
                            BrowserContract.Bookmarks.URL + ", " +
                            BrowserContract.Bookmarks.IS_FOLDER + "," +
                            BrowserContract.Bookmarks.PARENT + "," +
                            BrowserContract.Bookmarks.POSITION + "," +
                            BrowserContract.Bookmarks.DATE_CREATED +
                            ") VALUES (" +
                            "'" + bookmarks[i] + "', " +
                            "'" + bookmarkDestination + "', " +
                            "0," +
                            parent + "," +
                            Integer.toString(i) + "," +
                            now +
                            ");");

                    int faviconId = preloads.getResourceId(i, 0);
                    int thumbId = preloads.getResourceId(i + 1, 0);
                    byte[] thumb = null, favicon = null;
                    try {
                        thumb = readRaw(res, thumbId);
                    } catch (IOException e) {
                    }
                    try {
                        favicon = readRaw(res, faviconId);
                    } catch (IOException e) {
                    }
                    if (thumb != null || favicon != null) {
                        ContentValues imageValues = new ContentValues();
                        imageValues.put(BrowserContract.Images.URL, bookmarkDestination.toString());
                        if (favicon != null) {
                            imageValues.put(BrowserContract.Images.FAVICON, favicon);
                        }
                        if (thumb != null) {
                            imageValues.put(BrowserContract.Images.THUMBNAIL, thumb);
                        }
                        db.insert(TABLE_IMAGES, BrowserContract.Images.FAVICON, imageValues);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
            } finally {
                preloads.recycle();
            }
        }

        private byte[] readRaw(Resources res, int id) throws IOException {
            if (id == 0) {
                return null;
            }
            InputStream is = res.openRawResource(id);
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int read;
                while ((read = is.read(buf)) > 0) {
                    bos.write(buf, 0, read);
                }
                bos.flush();
                return bos.toByteArray();
            } finally {
                is.close();
            }
        }

        // XXX: This is a major hack to remove our dependency on gsf constants and
        // its content provider. http://b/issue?id=2425179
        private String getClientId(ContentResolver cr) {
            String ret = "android-google";
            Cursor c = null;
            try {
                c = cr.query(Uri.parse("content://com.google.settings/partner"),
                        new String[]{"value"}, "name='client_id'", null, null);
                if (c != null && c.moveToNext()) {
                    ret = c.getString(0);
                }
            } catch (RuntimeException ex) {
                // fall through to return the default
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            return ret;
        }

        private CharSequence replaceSystemPropertyInString(Context context, CharSequence srcString) {
            StringBuffer sb = new StringBuffer();
            int lastCharLoc = 0;

            final String client_id = getClientId(context.getContentResolver());

            for (int i = 0; i < srcString.length(); ++i) {
                char c = srcString.charAt(i);
                if (c == '{') {
                    sb.append(srcString.subSequence(lastCharLoc, i));
                    lastCharLoc = i;
                    inner:
                    for (int j = i; j < srcString.length(); ++j) {
                        char k = srcString.charAt(j);
                        if (k == '}') {
                            String propertyKeyValue = srcString.subSequence(i + 1, j).toString();
                            if (propertyKeyValue.equals("CLIENT_ID")) {
                                sb.append(client_id);
                            } else {
                                sb.append("unknown");
                            }
                            lastCharLoc = j + 1;
                            i = j;
                            break inner;
                        }
                    }
                }
            }
            if (srcString.length() - lastCharLoc > 0) {
                // Put on the tail, if there is one
                sb.append(srcString.subSequence(lastCharLoc, srcString.length()));
            }
            return sb;
        }
    }

    @Override
    public SQLiteOpenHelper getDatabaseHelper(Context context) {
        synchronized (this) {
            if (mOpenHelper == null) {
                mOpenHelper = new DatabaseHelper(context);
            }
            return mOpenHelper;
        }
    }

    @Override
    public boolean isCallerSyncAdapter(Uri uri) {
        return uri.getBooleanQueryParameter(BrowserContract.CALLER_IS_SYNCADAPTER, false);
    }

    void refreshWidgets() {
        mUpdateWidgets = true;
    }

    @Override
    protected void onEndTransaction(boolean callerIsSyncAdapter) {
        super.onEndTransaction(callerIsSyncAdapter);
        if (mUpdateWidgets) {
            if (mWidgetObserver == null) {
                BookmarkThumbnailWidgetProvider.refreshWidgets(getContext());
            } else {
                mWidgetObserver.dispatchChange(false);
            }
            mUpdateWidgets = false;
        }
        mSyncToNetwork = true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case LEGACY:
            case BOOKMARKS:
                return BrowserContract.Bookmarks.CONTENT_TYPE;
            case LEGACY_ID:
            case BOOKMARKS_ID:
                return BrowserContract.Bookmarks.CONTENT_ITEM_TYPE;
            case HISTORY:
                return BrowserContract.History.CONTENT_TYPE;
            case HISTORY_ID:
                return BrowserContract.History.CONTENT_ITEM_TYPE;
            case SEARCHES:
                return BrowserContract.Searches.CONTENT_TYPE;
            case SEARCHES_ID:
                return BrowserContract.Searches.CONTENT_ITEM_TYPE;
        }
        return null;
    }

    boolean isNullAccount(String account) {
        if (account == null) return true;
        account = account.trim();
        return account.length() == 0 || account.equals("null");
    }

    Object[] getSelectionWithAccounts(Uri uri, String selection, String[] selectionArgs) {
        // Look for account info
        String accountType = uri.getQueryParameter(BrowserContract.Bookmarks.PARAM_ACCOUNT_TYPE);
        String accountName = uri.getQueryParameter(BrowserContract.Bookmarks.PARAM_ACCOUNT_NAME);
        boolean hasAccounts = false;
        if (accountType != null && accountName != null) {
            if (!isNullAccount(accountType) && !isNullAccount(accountName)) {
                selection = DatabaseUtils.concatenateWhere(selection,
                        BrowserContract.Bookmarks.ACCOUNT_TYPE + "=? AND " + BrowserContract.Bookmarks.ACCOUNT_NAME + "=? ");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[]{accountType, accountName});
                hasAccounts = true;
            } else {
                selection = DatabaseUtils.concatenateWhere(selection,
                        BrowserContract.Bookmarks.ACCOUNT_NAME + " IS NULL AND " +
                                BrowserContract.Bookmarks.ACCOUNT_TYPE + " IS NULL");
            }
        }
        return new Object[]{selection, selectionArgs, hasAccounts};
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = URI_MATCHER.match(uri);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String limit = uri.getQueryParameter(BrowserContract.PARAM_LIMIT);
        String groupBy = uri.getQueryParameter(PARAM_GROUP_BY);
        switch (match) {
            case ACCOUNTS: {
                qb.setTables(VIEW_ACCOUNTS);
                qb.setProjectionMap(ACCOUNTS_PROJECTION_MAP);
                String allowEmpty = uri.getQueryParameter(PARAM_ALLOW_EMPTY_ACCOUNTS);
                if ("false".equals(allowEmpty)) {
                    selection = DatabaseUtils.concatenateWhere(selection, SQL_WHERE_ACCOUNT_HAS_BOOKMARKS);
                }
                if (sortOrder == null) {
                    sortOrder = DEFAULT_SORT_ACCOUNTS;
                }
                break;
            }
            case BOOKMARKS_FOLDER_ID:
            case BOOKMARKS_ID:
            case BOOKMARKS: {
                // Only show deleted bookmarks if requested to do so
                if (!uri.getBooleanQueryParameter(BrowserContract.Bookmarks.QUERY_PARAMETER_SHOW_DELETED, false)) {
                    selection = DatabaseUtils.concatenateWhere(BrowserContract.Bookmarks.IS_DELETED + "=0", selection);
                }

                if (match == BOOKMARKS_ID) {
                    // Tack on the ID of the specific bookmark requested
                    selection = DatabaseUtils.concatenateWhere(selection, TABLE_BOOKMARKS + "." + BrowserContract.Bookmarks._ID + "=?");
                    selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs, new String[]{Long.toString(ContentUris.parseId(uri))});
                } else if (match == BOOKMARKS_FOLDER_ID) {
                    // Tack on the ID of the specific folder requested
                    selection = DatabaseUtils.concatenateWhere(selection, TABLE_BOOKMARKS + "." + BrowserContract.Bookmarks.PARENT + "=?");
                    selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs, new String[]{Long.toString(ContentUris.parseId(uri))});
                }

                Object[] withAccount = getSelectionWithAccounts(uri, selection, selectionArgs);
                selection = (String) withAccount[0];
                selectionArgs = (String[]) withAccount[1];
                boolean hasAccounts = (Boolean) withAccount[2];

                // Set a default sort order if one isn't specified
                if (TextUtils.isEmpty(sortOrder)) {
                    if (hasAccounts) {
                        sortOrder = DEFAULT_BOOKMARKS_SORT_ORDER_SYNC;
                    } else {
                        sortOrder = DEFAULT_BOOKMARKS_SORT_ORDER;
                    }
                }
                qb.setProjectionMap(BOOKMARKS_PROJECTION_MAP);
                qb.setTables(TABLE_BOOKMARKS_JOIN_IMAGES);
                break;
            }

            case BOOKMARKS_FOLDER: {
                // Look for an account
                boolean useAccount = false;
                String accountType = uri.getQueryParameter(BrowserContract.Bookmarks.PARAM_ACCOUNT_TYPE);
                String accountName = uri.getQueryParameter(BrowserContract.Bookmarks.PARAM_ACCOUNT_NAME);
                if (!isNullAccount(accountType) && !isNullAccount(accountName)) {
                    useAccount = true;
                }

                qb.setTables(TABLE_BOOKMARKS_JOIN_IMAGES);
                String[] args;
                String query;
                // Set a default sort order if one isn't specified
                if (TextUtils.isEmpty(sortOrder)) {
                    if (useAccount) {
                        sortOrder = DEFAULT_BOOKMARKS_SORT_ORDER_SYNC;
                    } else {
                        sortOrder = DEFAULT_BOOKMARKS_SORT_ORDER;
                    }
                }
                if (!useAccount) {
                    qb.setProjectionMap(BOOKMARKS_PROJECTION_MAP);
                    String where = BrowserContract.Bookmarks.PARENT + "=? AND " + BrowserContract.Bookmarks.IS_DELETED + "=0";
                    where = DatabaseUtils.concatenateWhere(where, selection);
                    args = new String[]{Long.toString(FIXED_ID_ROOT)};
                    if (selectionArgs != null) {
                        args = DatabaseUtils.appendSelectionArgs(args, selectionArgs);
                    }
                    query = qb.buildQuery(projection, where, null, null, sortOrder, null);
                } else {
                    qb.setProjectionMap(BOOKMARKS_PROJECTION_MAP);
                    String where = BrowserContract.Bookmarks.ACCOUNT_TYPE + "=? AND " +
                            BrowserContract.Bookmarks.ACCOUNT_NAME + "=? " +
                            "AND parent = " +
                            "(SELECT _id FROM " + TABLE_BOOKMARKS + " WHERE " +
                            BrowserContract.ChromeSyncColumns.SERVER_UNIQUE + "=" +
                            "'" + BrowserContract.ChromeSyncColumns.FOLDER_NAME_BOOKMARKS_BAR + "' " +
                            "AND account_type = ? AND account_name = ?) " +
                            "AND " + BrowserContract.Bookmarks.IS_DELETED + "=0";
                    where = DatabaseUtils.concatenateWhere(where, selection);
                    String bookmarksBarQuery = qb.buildQuery(projection,
                            where, null, null, null, null);
                    args = new String[]{accountType, accountName, accountType, accountName};
                    if (selectionArgs != null) {
                        args = DatabaseUtils.appendSelectionArgs(args, selectionArgs);
                    }

                    where = BrowserContract.Bookmarks.ACCOUNT_TYPE + "=? AND " + BrowserContract.Bookmarks.ACCOUNT_NAME + "=?" +
                            " AND " + BrowserContract.ChromeSyncColumns.SERVER_UNIQUE + "=?";
                    where = DatabaseUtils.concatenateWhere(where, selection);
                    qb.setProjectionMap(OTHER_BOOKMARKS_PROJECTION_MAP);
                    String otherBookmarksQuery = qb.buildQuery(projection,
                            where, null, null, null, null);

                    query = qb.buildUnionQuery(new String[]{bookmarksBarQuery, otherBookmarksQuery}, sortOrder, limit);

                    args = DatabaseUtils.appendSelectionArgs(args, new String[]{
                            accountType, accountName, BrowserContract.ChromeSyncColumns.FOLDER_NAME_OTHER_BOOKMARKS,
                    });
                    if (selectionArgs != null) {
                        args = DatabaseUtils.appendSelectionArgs(args, selectionArgs);
                    }
                }

                Cursor cursor = db.rawQuery(query, args);
                if (cursor != null) {
                    cursor.setNotificationUri(getContext().getContentResolver(), BrowserContract.AUTHORITY_URI);
                }
                return cursor;
            }

            case BOOKMARKS_DEFAULT_FOLDER_ID: {
                String accountName = uri.getQueryParameter(BrowserContract.Bookmarks.PARAM_ACCOUNT_NAME);
                String accountType = uri.getQueryParameter(BrowserContract.Bookmarks.PARAM_ACCOUNT_TYPE);
                long id = queryDefaultFolderId(accountName, accountType);
                MatrixCursor c = new MatrixCursor(new String[]{BrowserContract.Bookmarks._ID});
                c.newRow().add(id);
                return c;
            }

            case BOOKMARKS_SUGGESTIONS: {
                return doSuggestQuery(selection, selectionArgs, limit);
            }

            case HISTORY_ID: {
                selection = DatabaseUtils.concatenateWhere(selection, TABLE_HISTORY + "._id=?");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[]{Long.toString(ContentUris.parseId(uri))});
                // fall through
            }
            case HISTORY: {
                filterSearchClient(selectionArgs);
                if (sortOrder == null) {
                    sortOrder = DEFAULT_SORT_HISTORY;
                }
                qb.setProjectionMap(HISTORY_PROJECTION_MAP);
                qb.setTables(TABLE_HISTORY_JOIN_IMAGES);
                break;
            }

            case SEARCHES_ID: {
                selection = DatabaseUtils.concatenateWhere(selection, TABLE_SEARCHES + "._id=?");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[]{Long.toString(ContentUris.parseId(uri))});
                // fall through
            }
            case SEARCHES: {
                qb.setTables(TABLE_SEARCHES);
                qb.setProjectionMap(SEARCHES_PROJECTION_MAP);
                break;
            }

            case SYNCSTATE: {
                return mSyncHelper.query(db, projection, selection, selectionArgs, sortOrder);
            }

            case SYNCSTATE_ID: {
                selection = appendAccountToSelection(uri, selection);
                String selectionWithId =
                        (SyncStateContract.Columns._ID + "=" + ContentUris.parseId(uri) + " ")
                                + (selection == null ? "" : " AND (" + selection + ")");
                return mSyncHelper.query(db, projection, selectionWithId, selectionArgs, sortOrder);
            }

            case IMAGES: {
                qb.setTables(TABLE_IMAGES);
                qb.setProjectionMap(IMAGES_PROJECTION_MAP);
                break;
            }

            case LEGACY_ID:
            case COMBINED_ID: {
                selection = DatabaseUtils.concatenateWhere(
                        selection, BrowserContract.Combined._ID + " = CAST(? AS INTEGER)");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[]{Long.toString(ContentUris.parseId(uri))});
                // fall through
            }
            case LEGACY:
            case COMBINED: {
                if ((match == LEGACY || match == LEGACY_ID)
                        && projection == null) {
                    projection = Browser.HISTORY_PROJECTION;
                }
                String[] args = createCombinedQuery(uri, projection, qb);
                if (selectionArgs == null) {
                    selectionArgs = args;
                } else {
                    selectionArgs = DatabaseUtils.appendSelectionArgs(args, selectionArgs);
                }
                break;
            }

            case SETTINGS: {
                qb.setTables(TABLE_SETTINGS);
                qb.setProjectionMap(SETTINGS_PROJECTION_MAP);
                break;
            }

            case THUMBNAILS_ID: {
                selection = DatabaseUtils.concatenateWhere(
                        selection, Thumbnails._ID + " = ?");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[]{Long.toString(ContentUris.parseId(uri))});
                // fall through
            }
            case THUMBNAILS: {
                qb.setTables(TABLE_THUMBNAILS);
                break;
            }

            case OMNIBOX_SUGGESTIONS: {
                qb.setTables(VIEW_OMNIBOX_SUGGESTIONS);
                break;
            }

            default: {
                throw new UnsupportedOperationException("Unknown URL " + uri.toString());
            }
        }

        Cursor cursor = qb.query(db, projection, selection, selectionArgs, groupBy, null, sortOrder, limit);
        cursor.setNotificationUri(getContext().getContentResolver(), BrowserContract.AUTHORITY_URI);
        return cursor;
    }

    private Cursor doSuggestQuery(String selection, String[] selectionArgs, String limit) {
        if (TextUtils.isEmpty(selectionArgs[0])) {
            selection = ZERO_QUERY_SUGGEST_SELECTION;
            selectionArgs = null;
        } else {
            String like = selectionArgs[0] + "%";
            if (selectionArgs[0].startsWith("http")
                    || selectionArgs[0].startsWith("file")) {
                selectionArgs[0] = like;
            } else {
                selectionArgs = new String[6];
                selectionArgs[0] = "http://" + like;
                selectionArgs[1] = "http://www." + like;
                selectionArgs[2] = "https://" + like;
                selectionArgs[3] = "https://www." + like;
                // To match against titles.
                selectionArgs[4] = like;
                selectionArgs[5] = like;
                selection = SUGGEST_SELECTION;
            }
            selection = DatabaseUtils.concatenateWhere(selection,
                    BrowserContract.Bookmarks.IS_DELETED + "=0 AND " + BrowserContract.Bookmarks.IS_FOLDER + "=0");

        }
        Cursor c = mOpenHelper.getReadableDatabase().query(TABLE_BOOKMARKS_JOIN_HISTORY,
                SUGGEST_PROJECTION, selection, selectionArgs, null, null,
                null, null);

        return new SuggestionsCursor(c);
    }

    private String[] createCombinedQuery(
            Uri uri, String[] projection, SQLiteQueryBuilder qb) {
        String[] args = null;
        StringBuilder whereBuilder = new StringBuilder(128);
        whereBuilder.append(BrowserContract.Bookmarks.IS_DELETED);
        whereBuilder.append(" = 0");
        // Look for account info
        Object[] withAccount = getSelectionWithAccounts(uri, null, null);
        String selection = (String) withAccount[0];
        String[] selectionArgs = (String[]) withAccount[1];
        if (selection != null) {
            whereBuilder.append(" AND " + selection);
            if (selectionArgs != null) {
                // We use the selection twice, hence we need to duplicate the args
                args = new String[selectionArgs.length * 2];
                System.arraycopy(selectionArgs, 0, args, 0, selectionArgs.length);
                System.arraycopy(selectionArgs, 0, args, selectionArgs.length,
                        selectionArgs.length);
            }
        }
        String where = whereBuilder.toString();
        // Build the bookmark subquery for history union subquery
        qb.setTables(TABLE_BOOKMARKS);
        String subQuery = qb.buildQuery(null, where, null, null, null, null);
        // Build the history union subquery
        qb.setTables(String.format(FORMAT_COMBINED_JOIN_SUBQUERY_JOIN_IMAGES, subQuery));
        qb.setProjectionMap(COMBINED_HISTORY_PROJECTION_MAP);
        String historySubQuery = qb.buildQuery(null,
                null, null, null, null, null);
        // Build the bookmark union subquery
        qb.setTables(TABLE_BOOKMARKS_JOIN_IMAGES);
        qb.setProjectionMap(COMBINED_BOOKMARK_PROJECTION_MAP);
        where += String.format(" AND %s NOT IN (SELECT %s FROM %s)",
                BrowserContract.Combined.URL, BrowserContract.History.URL, TABLE_HISTORY);
        String bookmarksSubQuery = qb.buildQuery(null, where,
                null, null, null, null);
        // Put it all together
        String query = qb.buildUnionQuery(
                new String[]{historySubQuery, bookmarksSubQuery},
                null, null);
        qb.setTables("(" + query + ")");
        qb.setProjectionMap(null);
        return args;
    }

    int deleteBookmarks(String selection, String[] selectionArgs,
                        boolean callerIsSyncAdapter) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (callerIsSyncAdapter) {
            return db.delete(TABLE_BOOKMARKS, selection, selectionArgs);
        }

        Object[] appendedBookmarks = appendBookmarksIfFolder(selection, selectionArgs);
        selection = (String) appendedBookmarks[0];
        selectionArgs = (String[]) appendedBookmarks[1];

        ContentValues values = new ContentValues();
        values.put(BrowserContract.Bookmarks.DATE_MODIFIED, System.currentTimeMillis());
        values.put(BrowserContract.Bookmarks.IS_DELETED, 1);
        return updateBookmarksInTransaction(values, selection, selectionArgs,
                callerIsSyncAdapter);
    }

    private Object[] appendBookmarksIfFolder(String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final String[] bookmarksProjection = new String[]{
                BrowserContract.Bookmarks._ID, // 0
                BrowserContract.Bookmarks.IS_FOLDER // 1
        };
        StringBuilder newSelection = new StringBuilder(selection);
        List<String> newSelectionArgs = new ArrayList<String>();

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_BOOKMARKS, bookmarksProjection,
                    selection, selectionArgs, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String id = Long.toString(cursor.getLong(0));
                    newSelectionArgs.add(id);
                    if (cursor.getInt(1) != 0) {
                        // collect bookmarks in this folder
                        Object[] bookmarks = appendBookmarksIfFolder(
                                BrowserContract.Bookmarks.PARENT + "=?", new String[]{id});
                        String[] bookmarkIds = (String[]) bookmarks[1];
                        if (bookmarkIds.length > 0) {
                            newSelection.append(" OR " + TABLE_BOOKMARKS + "._id IN (");
                            for (String bookmarkId : bookmarkIds) {
                                newSelection.append("?,");
                                newSelectionArgs.add(bookmarkId);
                            }
                            newSelection.deleteCharAt(newSelection.length() - 1);
                            newSelection.append(")");
                        }
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return new Object[]{
                newSelection.toString(),
                newSelectionArgs.toArray(new String[newSelectionArgs.size()])
        };
    }

    @Override
    public int deleteInTransaction(Uri uri, String selection, String[] selectionArgs,
                                   boolean callerIsSyncAdapter) {
        final int match = URI_MATCHER.match(uri);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int deleted = 0;
        switch (match) {
            case BOOKMARKS_ID: {
                selection = DatabaseUtils.concatenateWhere(selection,
                        TABLE_BOOKMARKS + "._id=?");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[]{Long.toString(ContentUris.parseId(uri))});
                // fall through
            }
            case BOOKMARKS: {
                // Look for account info
                Object[] withAccount = getSelectionWithAccounts(uri, selection, selectionArgs);
                selection = (String) withAccount[0];
                selectionArgs = (String[]) withAccount[1];
                deleted = deleteBookmarks(selection, selectionArgs, callerIsSyncAdapter);
                pruneImages();
                if (deleted > 0) {
                    refreshWidgets();
                }
                break;
            }

            case HISTORY_ID: {
                selection = DatabaseUtils.concatenateWhere(selection, TABLE_HISTORY + "._id=?");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[]{Long.toString(ContentUris.parseId(uri))});
                // fall through
            }
            case HISTORY: {
                filterSearchClient(selectionArgs);
                deleted = db.delete(TABLE_HISTORY, selection, selectionArgs);
                pruneImages();
                break;
            }

            case SEARCHES_ID: {
                selection = DatabaseUtils.concatenateWhere(selection, TABLE_SEARCHES + "._id=?");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[]{Long.toString(ContentUris.parseId(uri))});
                // fall through
            }
            case SEARCHES: {
                deleted = db.delete(TABLE_SEARCHES, selection, selectionArgs);
                break;
            }

            case SYNCSTATE: {
                deleted = mSyncHelper.delete(db, selection, selectionArgs);
                break;
            }
            case SYNCSTATE_ID: {
                String selectionWithId =
                        (SyncStateContract.Columns._ID + "=" + ContentUris.parseId(uri) + " ")
                                + (selection == null ? "" : " AND (" + selection + ")");
                deleted = mSyncHelper.delete(db, selectionWithId, selectionArgs);
                break;
            }
            case LEGACY_ID: {
                selection = DatabaseUtils.concatenateWhere(
                        selection, BrowserContract.Combined._ID + " = CAST(? AS INTEGER)");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[]{Long.toString(ContentUris.parseId(uri))});
                // fall through
            }
            case LEGACY: {
                String[] projection = new String[]{BrowserContract.Combined._ID,
                        BrowserContract.Combined.IS_BOOKMARK, BrowserContract.Combined.URL};
                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                String[] args = createCombinedQuery(uri, projection, qb);
                if (selectionArgs == null) {
                    selectionArgs = args;
                } else {
                    selectionArgs = DatabaseUtils.appendSelectionArgs(
                            args, selectionArgs);
                }
                Cursor c = qb.query(db, projection, selection, selectionArgs,
                        null, null, null);
                while (c.moveToNext()) {
                    long id = c.getLong(0);
                    boolean isBookmark = c.getInt(1) != 0;
                    String url = c.getString(2);
                    if (isBookmark) {
                        deleted += deleteBookmarks(BrowserContract.Bookmarks._ID + "=?",
                                new String[]{Long.toString(id)},
                                callerIsSyncAdapter);
                        db.delete(TABLE_HISTORY, BrowserContract.History.URL + "=?",
                                new String[]{url});
                    } else {
                        deleted += db.delete(TABLE_HISTORY,
                                BrowserContract.Bookmarks._ID + "=?",
                                new String[]{Long.toString(id)});
                    }
                }
                c.close();
                break;
            }
            case THUMBNAILS_ID: {
                selection = DatabaseUtils.concatenateWhere(
                        selection, Thumbnails._ID + " = ?");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[]{Long.toString(ContentUris.parseId(uri))});
                // fall through
            }
            case THUMBNAILS: {
                deleted = db.delete(TABLE_THUMBNAILS, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown delete URI " + uri);
            }
        }
        if (deleted > 0) {
            postNotifyUri(uri);
            if (shouldNotifyLegacy(uri)) {
                postNotifyUri(LEGACY_AUTHORITY_URI);
            }
        }
        return deleted;
    }

    long queryDefaultFolderId(String accountName, String accountType) {
        if (!isNullAccount(accountName) && !isNullAccount(accountType)) {
            final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            Cursor c = db.query(TABLE_BOOKMARKS, new String[]{BrowserContract.Bookmarks._ID},
                    BrowserContract.ChromeSyncColumns.SERVER_UNIQUE + " = ?" + " AND account_type = ? AND account_name = ?",
                    new String[]{BrowserContract.ChromeSyncColumns.FOLDER_NAME_BOOKMARKS_BAR, accountType, accountName}, null, null, null);
            try {
                if (c.moveToFirst()) {
                    return c.getLong(0);
                }
            } finally {
                c.close();
            }
        }
        return FIXED_ID_ROOT;
    }

    @Override
    public Uri insertInTransaction(Uri uri, ContentValues values, boolean callerIsSyncAdapter) {
        int match = URI_MATCHER.match(uri);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long id = -1;
        if (match == LEGACY) {
            // Intercept and route to the correct table
            Integer bookmark = values.getAsInteger(BookmarkColumns.BOOKMARK);
            values.remove(BookmarkColumns.BOOKMARK);
            if (bookmark == null || bookmark == 0) {
                match = HISTORY;
            } else {
                match = BOOKMARKS;
                values.remove(BookmarkColumns.DATE);
                values.remove(BookmarkColumns.VISITS);
                values.remove("user_entered");
                values.put(BrowserContract.Bookmarks.IS_FOLDER, 0);
            }
        }
        switch (match) {
            case BOOKMARKS: {
                // Mark rows dirty if they're not coming from a sync adapter
                if (!callerIsSyncAdapter) {
                    long now = System.currentTimeMillis();
                    values.put(BrowserContract.Bookmarks.DATE_CREATED, now);
                    values.put(BrowserContract.Bookmarks.DATE_MODIFIED, now);
                    values.put(BrowserContract.Bookmarks.DIRTY, 1);

                    //hasAccounts is false
                    boolean hasAccounts = values.containsKey(BrowserContract.Bookmarks.ACCOUNT_TYPE) || values.containsKey(BrowserContract.Bookmarks.ACCOUNT_NAME);
                    String accountType = values.getAsString(BrowserContract.Bookmarks.ACCOUNT_TYPE);
                    String accountName = values.getAsString(BrowserContract.Bookmarks.ACCOUNT_NAME);
                    //hasParent is true
                    boolean hasParent = values.containsKey(BrowserContract.Bookmarks.PARENT);
                    if (hasParent && hasAccounts) {
                        // Let's make sure it's valid
                        long parentId = values.getAsLong(BrowserContract.Bookmarks.PARENT);
                        hasParent = isValidParent(accountType, accountName, parentId);
                    } else if (hasParent && !hasAccounts) {
                        //parentId is 1
                        long parentId = values.getAsLong(BrowserContract.Bookmarks.PARENT);
                        hasParent = setParentValues(parentId, values);
                    }

                    // If no parent is set default to the "Bookmarks Bar" folder
                    if (!hasParent) {
                        values.put(BrowserContract.Bookmarks.PARENT, queryDefaultFolderId(accountName, accountType));
                    }
                }

                // If no position is requested put the bookmark at the beginning of the list
                if (!values.containsKey(BrowserContract.Bookmarks.POSITION)) {
                    values.put(BrowserContract.Bookmarks.POSITION, Long.toString(Long.MIN_VALUE));
                }

                // Extract out the image values so they can be inserted into the images table
                String url = values.getAsString(BrowserContract.Bookmarks.URL);
                ContentValues imageValues = extractImageValues(values, url);
                Boolean isFolder = values.getAsBoolean(BrowserContract.Bookmarks.IS_FOLDER);
                if ((isFolder == null || !isFolder) && imageValues != null && !TextUtils.isEmpty(url)) {
                    int count = db.update(TABLE_IMAGES, imageValues, BrowserContract.Images.URL + "=?", new String[]{url});
                    if (count == 0) {
                        db.insertOrThrow(TABLE_IMAGES, BrowserContract.Images.FAVICON, imageValues);
                    }
                }

                id = db.insertOrThrow(TABLE_BOOKMARKS, BrowserContract.Bookmarks.DIRTY, values);
                refreshWidgets();
                break;
            }

            case HISTORY: {
                // If no created time is specified set it to now
                if (!values.containsKey(BrowserContract.History.DATE_CREATED)) {
                    values.put(BrowserContract.History.DATE_CREATED, System.currentTimeMillis());
                }
                String url = values.getAsString(BrowserContract.History.URL);
                url = filterSearchClient(url);
                values.put(BrowserContract.History.URL, url);

                // Extract out the image values so they can be inserted into the images table
                ContentValues imageValues = extractImageValues(values,
                        values.getAsString(BrowserContract.History.URL));
                if (imageValues != null) {
                    db.insertOrThrow(TABLE_IMAGES, BrowserContract.Images.FAVICON, imageValues);
                }

                id = db.insertOrThrow(TABLE_HISTORY, BrowserContract.History.VISITS, values);
                break;
            }

            case SEARCHES: {
                id = insertSearchesInTransaction(db, values);
                break;
            }

            case SYNCSTATE: {
                id = mSyncHelper.insert(db, values);
                break;
            }

            case SETTINGS: {
                id = 0;
                insertSettingsInTransaction(db, values);
                break;
            }

            case THUMBNAILS: {
                id = db.replaceOrThrow(TABLE_THUMBNAILS, null, values);
                break;
            }

            default: {
                throw new UnsupportedOperationException("Unknown insert URI " + uri);
            }
        }

        if (id >= 0) {
            postNotifyUri(uri);
            if (shouldNotifyLegacy(uri)) {
                postNotifyUri(LEGACY_AUTHORITY_URI);
            }
            return ContentUris.withAppendedId(uri, id);
        } else {
            return null;
        }
    }

    private String[] getAccountNameAndType(long id) {
        if (id <= 0) {
            return null;
        }
        Uri uri = ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, id);
        Cursor c = query(uri, new String[]{BrowserContract.Bookmarks.ACCOUNT_NAME, BrowserContract.Bookmarks.ACCOUNT_TYPE}, null, null, null);
        try {
            if (c.moveToFirst()) {
                String parentName = c.getString(0);
                String parentType = c.getString(1);
                return new String[]{parentName, parentType};
            }
            return null;
        } finally {
            c.close();
        }
    }

    private boolean setParentValues(long parentId, ContentValues values) {
        String[] parent = getAccountNameAndType(parentId);
        if (parent == null) {
            return false;
        }
        values.put(BrowserContract.Bookmarks.ACCOUNT_NAME, parent[0]);
        values.put(BrowserContract.Bookmarks.ACCOUNT_TYPE, parent[1]);
        return true;
    }

    private boolean isValidParent(String accountType, String accountName,
                                  long parentId) {
        String[] parent = getAccountNameAndType(parentId);
        if (parent != null
                && TextUtils.equals(accountName, parent[0])
                && TextUtils.equals(accountType, parent[1])) {
            return true;
        }
        return false;
    }

    private void filterSearchClient(String[] selectionArgs) {
        if (selectionArgs != null) {
            for (int i = 0; i < selectionArgs.length; i++) {
                selectionArgs[i] = filterSearchClient(selectionArgs[i]);
            }
        }
    }

    // Filters out the client= param for search urls
    private String filterSearchClient(String url) {
        // remove "client" before updating it to the history so that it wont
        // show up in the auto-complete list.
        int index = url.indexOf("client=");
        if (index > 0 && url.contains(".google.")) {
            int end = url.indexOf('&', index);
            if (end > 0) {
                url = url.substring(0, index)
                        .concat(url.substring(end + 1));
            } else {
                // the url.charAt(index-1) should be either '?' or '&'
                url = url.substring(0, index - 1);
            }
        }
        return url;
    }

    /**
     * Searches are unique, so perform an UPSERT manually since SQLite doesn't support them.
     */
    private long insertSearchesInTransaction(SQLiteDatabase db, ContentValues values) {
        String search = values.getAsString(BrowserContract.Searches.SEARCH);
        if (TextUtils.isEmpty(search)) {
            throw new IllegalArgumentException("Must include the SEARCH field");
        }
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_SEARCHES, new String[]{BrowserContract.Searches._ID},
                    BrowserContract.Searches.SEARCH + "=?", new String[]{search}, null, null, null);
            if (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                db.update(TABLE_SEARCHES, values, BrowserContract.Searches._ID + "=?",
                        new String[]{Long.toString(id)});
                return id;
            } else {
                return db.insertOrThrow(TABLE_SEARCHES, BrowserContract.Searches.SEARCH, values);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Settings are unique, so perform an UPSERT manually since SQLite doesn't support them.
     */
    private long insertSettingsInTransaction(SQLiteDatabase db, ContentValues values) {
        String key = values.getAsString(BrowserContract.Settings.KEY);
        if (TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("Must include the KEY field");
        }
        String[] keyArray = new String[]{key};
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_SETTINGS, new String[]{BrowserContract.Settings.KEY},
                    BrowserContract.Settings.KEY + "=?", keyArray, null, null, null);
            if (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                db.update(TABLE_SETTINGS, values, BrowserContract.Settings.KEY + "=?", keyArray);
                return id;
            } else {
                return db.insertOrThrow(TABLE_SETTINGS, BrowserContract.Settings.VALUE, values);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    @Override
    public int updateInTransaction(Uri uri, ContentValues values, String selection,
                                   String[] selectionArgs, boolean callerIsSyncAdapter) {
        int match = URI_MATCHER.match(uri);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (match == LEGACY || match == LEGACY_ID) {
            // Intercept and route to the correct table
            Integer bookmark = values.getAsInteger(BookmarkColumns.BOOKMARK);
            values.remove(BookmarkColumns.BOOKMARK);
            if (bookmark == null || bookmark == 0) {
                if (match == LEGACY) {
                    match = HISTORY;
                } else {
                    match = HISTORY_ID;
                }
            } else {
                if (match == LEGACY) {
                    match = BOOKMARKS;
                } else {
                    match = BOOKMARKS_ID;
                }
                values.remove(BookmarkColumns.DATE);
                values.remove(BookmarkColumns.VISITS);
                values.remove("user_entered");
            }
        }
        int modified = 0;
        switch (match) {
            case BOOKMARKS_ID: {
                selection = DatabaseUtils.concatenateWhere(selection,
                        TABLE_BOOKMARKS + "._id=?");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[]{Long.toString(ContentUris.parseId(uri))});
                // fall through
            }
            case BOOKMARKS: {
                Object[] withAccount = getSelectionWithAccounts(uri, selection, selectionArgs);
                selection = (String) withAccount[0];
                selectionArgs = (String[]) withAccount[1];
                modified = updateBookmarksInTransaction(values, selection, selectionArgs,
                        callerIsSyncAdapter);
                if (modified > 0) {
                    refreshWidgets();
                }
                break;
            }

            case HISTORY_ID: {
                selection = DatabaseUtils.concatenateWhere(selection, TABLE_HISTORY + "._id=?");
                selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[]{Long.toString(ContentUris.parseId(uri))});
                // fall through
            }
            case HISTORY: {
                modified = updateHistoryInTransaction(values, selection, selectionArgs);
                break;
            }

            case SYNCSTATE: {
                modified = mSyncHelper.update(mDb, values,
                        appendAccountToSelection(uri, selection), selectionArgs);
                break;
            }

            case SYNCSTATE_ID: {
                selection = appendAccountToSelection(uri, selection);
                String selectionWithId =
                        (SyncStateContract.Columns._ID + "=" + ContentUris.parseId(uri) + " ")
                                + (selection == null ? "" : " AND (" + selection + ")");
                modified = mSyncHelper.update(mDb, values,
                        selectionWithId, selectionArgs);
                break;
            }

            case IMAGES: {
                String url = values.getAsString(BrowserContract.Images.URL);
                if (TextUtils.isEmpty(url)) {
                    throw new IllegalArgumentException("Images.URL is required");
                }
                if (!shouldUpdateImages(db, url, values)) {
                    return 0;
                }
                int count = db.update(TABLE_IMAGES, values, BrowserContract.Images.URL + "=?",
                        new String[]{url});
                if (count == 0) {
                    db.insertOrThrow(TABLE_IMAGES, BrowserContract.Images.FAVICON, values);
                    count = 1;
                }
                // Only favicon is exposed in the public API. If we updated
                // the thumbnail or touch icon don't bother notifying the
                // legacy authority since it can't read it anyway.
                boolean updatedLegacy = false;
                if (getUrlCount(db, TABLE_BOOKMARKS, url) > 0) {
                    postNotifyUri(BrowserContract.Bookmarks.CONTENT_URI);
                    updatedLegacy = values.containsKey(BrowserContract.Images.FAVICON);
                    refreshWidgets();
                }
                if (getUrlCount(db, TABLE_HISTORY, url) > 0) {
                    postNotifyUri(BrowserContract.History.CONTENT_URI);
                    updatedLegacy = values.containsKey(BrowserContract.Images.FAVICON);
                }
                if (pruneImages() > 0 || updatedLegacy) {
                    postNotifyUri(LEGACY_AUTHORITY_URI);
                }
                // Even though we may be calling notifyUri on Bookmarks, don't
                // sync to network as images aren't synced. Otherwise this
                // unnecessarily triggers a bookmark sync.
                mSyncToNetwork = false;
                return count;
            }

            case SEARCHES: {
                modified = db.update(TABLE_SEARCHES, values, selection, selectionArgs);
                break;
            }

            case ACCOUNTS: {
                Account[] accounts = AccountManager.get(getContext()).getAccounts();
                mSyncHelper.onAccountsChanged(mDb, accounts);
                break;
            }

            case THUMBNAILS: {
                modified = db.update(TABLE_THUMBNAILS, values,
                        selection, selectionArgs);
                break;
            }

            default: {
                throw new UnsupportedOperationException("Unknown update URI " + uri);
            }
        }
        pruneImages();
        if (modified > 0) {
            postNotifyUri(uri);
            if (shouldNotifyLegacy(uri)) {
                postNotifyUri(LEGACY_AUTHORITY_URI);
            }
        }
        return modified;
    }

    // We want to avoid sending out more URI notifications than we have to
    // Thus, we check to see if the images we are about to store are already there
    // This is used because things like a site's favion or touch icon is rarely
    // changed, but the browser tries to update it every time the page loads.
    // Without this, we will always send out 3 URI notifications per page load.
    // With this, that drops to 0 or 1, depending on if the thumbnail changed.
    private boolean shouldUpdateImages(
            SQLiteDatabase db, String url, ContentValues values) {
        final String[] projection = new String[]{
                BrowserContract.Images.FAVICON,
                BrowserContract.Images.THUMBNAIL,
                BrowserContract.Images.TOUCH_ICON,
        };
        Cursor cursor = db.query(TABLE_IMAGES, projection, BrowserContract.Images.URL + "=?",
                new String[]{url}, null, null, null);
        byte[] nfavicon = values.getAsByteArray(BrowserContract.Images.FAVICON);
        byte[] nthumb = values.getAsByteArray(BrowserContract.Images.THUMBNAIL);
        byte[] ntouch = values.getAsByteArray(BrowserContract.Images.TOUCH_ICON);
        byte[] cfavicon = null;
        byte[] cthumb = null;
        byte[] ctouch = null;
        try {
            if (cursor.getCount() <= 0) {
                return nfavicon != null || nthumb != null || ntouch != null;
            }
            while (cursor.moveToNext()) {
                if (nfavicon != null) {
                    cfavicon = cursor.getBlob(0);
                    if (!Arrays.equals(nfavicon, cfavicon)) {
                        return true;
                    }
                }
                if (nthumb != null) {
                    cthumb = cursor.getBlob(1);
                    if (!Arrays.equals(nthumb, cthumb)) {
                        return true;
                    }
                }
                if (ntouch != null) {
                    ctouch = cursor.getBlob(2);
                    if (!Arrays.equals(ntouch, ctouch)) {
                        return true;
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return false;
    }

    int getUrlCount(SQLiteDatabase db, String table, String url) {
        Cursor c = db.query(table, new String[]{"COUNT(*)"},
                "url = ?", new String[]{url}, null, null, null);
        try {
            int count = 0;
            if (c.moveToFirst()) {
                count = c.getInt(0);
            }
            return count;
        } finally {
            c.close();
        }
    }

    /**
     * Does a query to find the matching bookmarks and updates each one with the provided values.
     */
    int updateBookmarksInTransaction(ContentValues values, String selection,
                                     String[] selectionArgs, boolean callerIsSyncAdapter) {
        int count = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final String[] bookmarksProjection = new String[]{
                BrowserContract.Bookmarks._ID, // 0
                BrowserContract.Bookmarks.VERSION, // 1
                BrowserContract.Bookmarks.URL, // 2
                BrowserContract.Bookmarks.TITLE, // 3
                BrowserContract.Bookmarks.IS_FOLDER, // 4
                BrowserContract.Bookmarks.ACCOUNT_NAME, // 5
                BrowserContract.Bookmarks.ACCOUNT_TYPE, // 6
        };
        Cursor cursor = db.query(TABLE_BOOKMARKS, bookmarksProjection,
                selection, selectionArgs, null, null, null);
        boolean updatingParent = values.containsKey(BrowserContract.Bookmarks.PARENT);
        String parentAccountName = null;
        String parentAccountType = null;
        if (updatingParent) {
            long parent = values.getAsLong(BrowserContract.Bookmarks.PARENT);
            Cursor c = db.query(TABLE_BOOKMARKS, new String[]{
                            BrowserContract.Bookmarks.ACCOUNT_NAME, BrowserContract.Bookmarks.ACCOUNT_TYPE},
                    "_id = ?", new String[]{Long.toString(parent)},
                    null, null, null);
            if (c.moveToFirst()) {
                parentAccountName = c.getString(0);
                parentAccountType = c.getString(1);
            }
            c.close();
        } else if (values.containsKey(BrowserContract.Bookmarks.ACCOUNT_NAME)
                || values.containsKey(BrowserContract.Bookmarks.ACCOUNT_TYPE)) {
            // TODO: Implement if needed (no one needs this yet)
        }
        try {
            String[] args = new String[1];
            // Mark the bookmark dirty if the caller isn't a sync adapter
            if (!callerIsSyncAdapter) {
                values.put(BrowserContract.Bookmarks.DATE_MODIFIED, System.currentTimeMillis());
                values.put(BrowserContract.Bookmarks.DIRTY, 1);
            }

            boolean updatingUrl = values.containsKey(BrowserContract.Bookmarks.URL);
            String url = null;
            if (updatingUrl) {
                url = values.getAsString(BrowserContract.Bookmarks.URL);
            }
            ContentValues imageValues = extractImageValues(values, url);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                args[0] = Long.toString(id);
                String accountName = cursor.getString(5);
                String accountType = cursor.getString(6);
                // If we are updating the parent and either the account name or
                // type do not match that of the new parent
                if (updatingParent
                        && (!TextUtils.equals(accountName, parentAccountName)
                        || !TextUtils.equals(accountType, parentAccountType))) {
                    // Parent is a different account
                    // First, insert a new bookmark/folder with the new account
                    // Then, if this is a folder, reparent all it's children
                    // Finally, delete the old bookmark/folder
                    ContentValues newValues = valuesFromCursor(cursor);
                    newValues.putAll(values);
                    newValues.remove(BrowserContract.Bookmarks._ID);
                    newValues.remove(BrowserContract.Bookmarks.VERSION);
                    newValues.put(BrowserContract.Bookmarks.ACCOUNT_NAME, parentAccountName);
                    newValues.put(BrowserContract.Bookmarks.ACCOUNT_TYPE, parentAccountType);
                    Uri insertUri = insertInTransaction(BrowserContract.Bookmarks.CONTENT_URI,
                            newValues, callerIsSyncAdapter);
                    long newId = ContentUris.parseId(insertUri);
                    if (cursor.getInt(4) != 0) {
                        // This is a folder, reparent
                        ContentValues updateChildren = new ContentValues(1);
                        updateChildren.put(BrowserContract.Bookmarks.PARENT, newId);
                        count += updateBookmarksInTransaction(updateChildren,
                                BrowserContract.Bookmarks.PARENT + "=?", new String[]{
                                        Long.toString(id)}, callerIsSyncAdapter);
                    }
                    // Now, delete the old one
                    Uri uri = ContentUris.withAppendedId(BrowserContract.Bookmarks.CONTENT_URI, id);
                    deleteInTransaction(uri, null, null, callerIsSyncAdapter);
                    count += 1;
                } else {
                    if (!callerIsSyncAdapter) {
                        // increase the local version for non-sync changes
                        values.put(BrowserContract.Bookmarks.VERSION, cursor.getLong(1) + 1);
                    }
                    count += db.update(TABLE_BOOKMARKS, values, "_id=?", args);
                }

                // Update the images over in their table
                if (imageValues != null) {
                    if (!updatingUrl) {
                        url = cursor.getString(2);
                        imageValues.put(BrowserContract.Images.URL, url);
                    }

                    if (!TextUtils.isEmpty(url)) {
                        args[0] = url;
                        if (db.update(TABLE_IMAGES, imageValues, BrowserContract.Images.URL + "=?", args) == 0) {
                            db.insert(TABLE_IMAGES, BrowserContract.Images.FAVICON, imageValues);
                        }
                    }
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    ContentValues valuesFromCursor(Cursor c) {
        int count = c.getColumnCount();
        ContentValues values = new ContentValues(count);
        String[] colNames = c.getColumnNames();
        for (int i = 0; i < count; i++) {
            switch (c.getType(i)) {
                case Cursor.FIELD_TYPE_BLOB:
                    values.put(colNames[i], c.getBlob(i));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    values.put(colNames[i], c.getFloat(i));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    values.put(colNames[i], c.getLong(i));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    values.put(colNames[i], c.getString(i));
                    break;
            }
        }
        return values;
    }

    /**
     * Does a query to find the matching bookmarks and updates each one with the provided values.
     */
    int updateHistoryInTransaction(ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        filterSearchClient(selectionArgs);
        Cursor cursor = query(BrowserContract.History.CONTENT_URI,
                new String[]{BrowserContract.History._ID, BrowserContract.History.URL},
                selection, selectionArgs, null);
        try {
            String[] args = new String[1];

            boolean updatingUrl = values.containsKey(BrowserContract.History.URL);
            String url = null;
            if (updatingUrl) {
                url = filterSearchClient(values.getAsString(BrowserContract.History.URL));
                values.put(BrowserContract.History.URL, url);
            }
            ContentValues imageValues = extractImageValues(values, url);

            while (cursor.moveToNext()) {
                args[0] = cursor.getString(0);
                count += db.update(TABLE_HISTORY, values, "_id=?", args);

                // Update the images over in their table
                if (imageValues != null) {
                    if (!updatingUrl) {
                        url = cursor.getString(1);
                        imageValues.put(BrowserContract.Images.URL, url);
                    }
                    args[0] = url;
                    if (db.update(TABLE_IMAGES, imageValues, BrowserContract.Images.URL + "=?", args) == 0) {
                        db.insert(TABLE_IMAGES, BrowserContract.Images.FAVICON, imageValues);
                    }
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    String appendAccountToSelection(Uri uri, String selection) {
        final String accountName = uri.getQueryParameter(RawContacts.ACCOUNT_NAME);
        final String accountType = uri.getQueryParameter(RawContacts.ACCOUNT_TYPE);

        final boolean partialUri = TextUtils.isEmpty(accountName) ^ TextUtils.isEmpty(accountType);
        if (partialUri) {
            // Throw when either account is incomplete
            throw new IllegalArgumentException(
                    "Must specify both or neither of ACCOUNT_NAME and ACCOUNT_TYPE for " + uri);
        }

        // Accounts are valid by only checking one parameter, since we've
        // already ruled out partial accounts.
        final boolean validAccount = !TextUtils.isEmpty(accountName);
        if (validAccount) {
            StringBuilder selectionSb = new StringBuilder(RawContacts.ACCOUNT_NAME + "="
                    + DatabaseUtils.sqlEscapeString(accountName) + " AND "
                    + RawContacts.ACCOUNT_TYPE + "="
                    + DatabaseUtils.sqlEscapeString(accountType));
            if (!TextUtils.isEmpty(selection)) {
                selectionSb.append(" AND (");
                selectionSb.append(selection);
                selectionSb.append(')');
            }
            return selectionSb.toString();
        } else {
            return selection;
        }
    }

    ContentValues extractImageValues(ContentValues values, String url) {
        ContentValues imageValues = null;
        // favicon
        if (values.containsKey(BrowserContract.Bookmarks.FAVICON)) {
            imageValues = new ContentValues();
            imageValues.put(BrowserContract.Images.FAVICON, values.getAsByteArray(BrowserContract.Bookmarks.FAVICON));
            values.remove(BrowserContract.Bookmarks.FAVICON);
        }

        // thumbnail
        if (values.containsKey(BrowserContract.Bookmarks.THUMBNAIL)) {
            if (imageValues == null) {
                imageValues = new ContentValues();
            }
            imageValues.put(BrowserContract.Images.THUMBNAIL, values.getAsByteArray(BrowserContract.Bookmarks.THUMBNAIL));
            values.remove(BrowserContract.Bookmarks.THUMBNAIL);
        }

        // touch icon
        if (values.containsKey(BrowserContract.Bookmarks.TOUCH_ICON)) {
            if (imageValues == null) {
                imageValues = new ContentValues();
            }
            imageValues.put(BrowserContract.Images.TOUCH_ICON, values.getAsByteArray(BrowserContract.Bookmarks.TOUCH_ICON));
            values.remove(BrowserContract.Bookmarks.TOUCH_ICON);
        }

        if (imageValues != null) {
            imageValues.put(BrowserContract.Images.URL, url);
        }
        return imageValues;
    }

    int pruneImages() {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.delete(TABLE_IMAGES, IMAGE_PRUNE, null);
    }

    boolean shouldNotifyLegacy(Uri uri) {
        if (uri.getPathSegments().contains("history")
                || uri.getPathSegments().contains("bookmarks")
                || uri.getPathSegments().contains("searches")) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean syncToNetwork(Uri uri) {
        if (BrowserContract.AUTHORITY.equals(uri.getAuthority())
                && uri.getPathSegments().contains("bookmarks")) {
            return mSyncToNetwork;
        }
        if (LEGACY_AUTHORITY.equals(uri.getAuthority())) {
            // Allow for 3rd party sync adapters
            return true;
        }
        return false;
    }

    static class SuggestionsCursor extends AbstractCursor {
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

    // ---------------------------------------------------
    //  SQL below, be warned
    // ---------------------------------------------------

    private static final String SQL_CREATE_VIEW_OMNIBOX_SUGGESTIONS =
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

    private static final String SQL_WHERE_ACCOUNT_HAS_BOOKMARKS =
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
