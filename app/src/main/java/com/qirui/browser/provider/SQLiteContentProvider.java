/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import com.qirui.browser.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * General purpose {@link ContentProvider} base class that uses SQLiteDatabase for storage.
 */
public abstract class SQLiteContentProvider extends ContentProvider implements BrowserProvider2Key {

    protected SQLiteDatabase mDb;
    private Set<Uri> mChangedUris;
    private SQLiteOpenHelper mOpenHelper;

    private static final int SLEEP_AFTER_YIELD_DELAY = 4000;
    private final ThreadLocal<Boolean> mApplyingBatch = new ThreadLocal();

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
        map.put(BrowserContract.Bookmarks._ID, StringUtils.qualifyColumn(TABLE_BOOKMARKS, BrowserContract.Bookmarks._ID));
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
        map.put(BrowserContract.History._ID, StringUtils.qualifyColumn(TABLE_HISTORY, BrowserContract.History._ID));
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
        map.put(BrowserContract.Combined._ID, StringUtils.bookmarkOrHistoryColumn(BrowserContract.Combined._ID));
        map.put(BrowserContract.Combined.TITLE, StringUtils.bookmarkOrHistoryColumn(BrowserContract.Combined.TITLE));
        map.put(BrowserContract.Combined.URL, StringUtils.qualifyColumn(TABLE_HISTORY, BrowserContract.Combined.URL));
        map.put(BrowserContract.Combined.DATE_CREATED, StringUtils.qualifyColumn(TABLE_HISTORY, BrowserContract.Combined.DATE_CREATED));
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

    /**
     * Maximum number of operations allowed in a batch between yield points.
     */
    private static final int MAX_OPERATIONS_PER_YIELD_POINT = 500;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mOpenHelper = getDatabaseHelper(context);
        mChangedUris = new HashSet();
        return true;
    }

    /**
     * Returns a {@link SQLiteOpenHelper} that can open the database.
     */
    public abstract SQLiteOpenHelper getDatabaseHelper(Context context);

    /**
     * The equivalent of the {@link #insert} method, but invoked within a transaction.
     */
    public abstract Uri insertInTransaction(Uri uri, ContentValues values,
            boolean callerIsSyncAdapter);

    /**
     * The equivalent of the {@link #update} method, but invoked within a transaction.
     */
    public abstract int updateInTransaction(Uri uri, ContentValues values, String selection,
            String[] selectionArgs, boolean callerIsSyncAdapter);

    /**
     * The equivalent of the {@link #delete} method, but invoked within a transaction.
     */
    public abstract int deleteInTransaction(Uri uri, String selection, String[] selectionArgs,
            boolean callerIsSyncAdapter);

    /**
     * Call this to add a URI to the list of URIs to be notified when the transaction
     * is committed.
     */
    protected void postNotifyUri(Uri uri) {
        synchronized (mChangedUris) {
            mChangedUris.add(uri);
        }
    }

    public boolean isCallerSyncAdapter(Uri uri) {
        return false;
    }

    public SQLiteOpenHelper getDatabaseHelper() {
        return mOpenHelper;
    }

    private boolean applyingBatch() {
        return mApplyingBatch.get() != null && mApplyingBatch.get();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result = null;
        boolean callerIsSyncAdapter = isCallerSyncAdapter(uri);
        boolean applyingBatch = applyingBatch();
        if (!applyingBatch) {
            mDb = mOpenHelper.getWritableDatabase();
            mDb.beginTransaction();
            try {
                result = insertInTransaction(uri, values, callerIsSyncAdapter);
                mDb.setTransactionSuccessful();
            } finally {
                mDb.endTransaction();
            }

            onEndTransaction(callerIsSyncAdapter);
        } else {
            result = insertInTransaction(uri, values, callerIsSyncAdapter);
        }
        return result;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int numValues = values.length;
        boolean callerIsSyncAdapter = isCallerSyncAdapter(uri);
        mDb = mOpenHelper.getWritableDatabase();
        mDb.beginTransaction();
        try {
            for (int i = 0; i < numValues; i++) {
                Uri result = insertInTransaction(uri, values[i], callerIsSyncAdapter);
                mDb.yieldIfContendedSafely();
            }
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }

        onEndTransaction(callerIsSyncAdapter);
        return numValues;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        boolean callerIsSyncAdapter = isCallerSyncAdapter(uri);
        boolean applyingBatch = applyingBatch();
        if (!applyingBatch) {
            mDb = mOpenHelper.getWritableDatabase();
            mDb.beginTransaction();
            try {
                count = updateInTransaction(uri, values, selection, selectionArgs,
                        callerIsSyncAdapter);
                mDb.setTransactionSuccessful();
            } finally {
                mDb.endTransaction();
            }

            onEndTransaction(callerIsSyncAdapter);
        } else {
            count = updateInTransaction(uri, values, selection, selectionArgs, callerIsSyncAdapter);
        }

        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        boolean callerIsSyncAdapter = isCallerSyncAdapter(uri);
        boolean applyingBatch = applyingBatch();
        if (!applyingBatch) {
            mDb = mOpenHelper.getWritableDatabase();
            mDb.beginTransaction();
            try {
                count = deleteInTransaction(uri, selection, selectionArgs, callerIsSyncAdapter);
                mDb.setTransactionSuccessful();
            } finally {
                mDb.endTransaction();
            }

            onEndTransaction(callerIsSyncAdapter);
        } else {
            count = deleteInTransaction(uri, selection, selectionArgs, callerIsSyncAdapter);
        }
        return count;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        int ypCount = 0;
        int opCount = 0;
        boolean callerIsSyncAdapter = false;
        mDb = mOpenHelper.getWritableDatabase();
        mDb.beginTransaction();
        try {
            mApplyingBatch.set(true);
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                if (++opCount >= MAX_OPERATIONS_PER_YIELD_POINT) {
                    throw new OperationApplicationException(
                            "Too many content provider operations between yield points. "
                                    + "The maximum number of operations per yield point is "
                                    + MAX_OPERATIONS_PER_YIELD_POINT, ypCount);
                }
                final ContentProviderOperation operation = operations.get(i);
                if (!callerIsSyncAdapter && isCallerSyncAdapter(operation.getUri())) {
                    callerIsSyncAdapter = true;
                }
                if (i > 0 && operation.isYieldAllowed()) {
                    opCount = 0;
                    if (mDb.yieldIfContendedSafely(SLEEP_AFTER_YIELD_DELAY)) {
                        ypCount++;
                    }
                }
                results[i] = operation.apply(this, results, i);
            }
            mDb.setTransactionSuccessful();
            return results;
        } finally {
            mApplyingBatch.set(false);
            mDb.endTransaction();
            onEndTransaction(callerIsSyncAdapter);
        }
    }

    protected void onEndTransaction(boolean callerIsSyncAdapter) {
        Set<Uri> changed;
        synchronized (mChangedUris) {
            changed = new HashSet<Uri>(mChangedUris);
            mChangedUris.clear();
        }
        ContentResolver resolver = getContext().getContentResolver();
        for (Uri uri : changed) {
            boolean syncToNetwork = !callerIsSyncAdapter && syncToNetwork(uri);
            resolver.notifyChange(uri, null, syncToNetwork);
        }
    }

    protected boolean syncToNetwork(Uri uri) {
        return false;
    }
}
