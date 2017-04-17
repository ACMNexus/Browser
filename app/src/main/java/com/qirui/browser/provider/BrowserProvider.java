/*
 * Copyright (C) 2006 The Android Open Source Project
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
 * limitations under the License.
 */

package com.qirui.browser.provider;

import android.app.SearchManager;
import android.app.backup.BackupManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriMatcher;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.provider.Browser.BookmarkColumns;
import android.util.Patterns;
import com.qirui.browser.BrowserSettings;
import com.qirui.browser.R;
import com.qirui.browser.search.SearchEngine;
import com.qirui.browser.util.IOUtils;

import java.util.Date;

public class BrowserProvider extends ContentProvider implements BrowserProviderKey {

    private BrowserSettings mSettings;
    private int mMaxSuggestionShortSize;
    private int mMaxSuggestionLongSize;
    private BackupManager mBackupManager;
    private static final UriMatcher URI_MATCHER;
    private String[] SUGGEST_ARGS = new String[5];
    private BrowserProviderDataBaseHelper mOpenHelper;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI("browser", TABLE_NAMES[URI_MATCH_BOOKMARKS], URI_MATCH_BOOKMARKS);
        URI_MATCHER.addURI("browser", TABLE_NAMES[URI_MATCH_BOOKMARKS] + "/#", URI_MATCH_BOOKMARKS_ID);
        URI_MATCHER.addURI("browser", TABLE_NAMES[URI_MATCH_SEARCHES], URI_MATCH_SEARCHES);
        URI_MATCHER.addURI("browser", TABLE_NAMES[URI_MATCH_SEARCHES] + "/#", URI_MATCH_SEARCHES_ID);
        URI_MATCHER.addURI("browser", SearchManager.SUGGEST_URI_PATH_QUERY, URI_MATCH_SUGGEST);
        URI_MATCHER.addURI("browser", TABLE_NAMES[URI_MATCH_BOOKMARKS] + "/" + SearchManager.SUGGEST_URI_PATH_QUERY, URI_MATCH_BOOKMARKS_SUGGEST);
    }

    public BrowserProvider() {
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        boolean xlargeScreenSize = (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_XLARGE;
        boolean isPortrait = (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

        if (xlargeScreenSize && isPortrait) {
            mMaxSuggestionLongSize = MAX_SUGGEST_LONG_LARGE;
            mMaxSuggestionShortSize = MAX_SUGGEST_SHORT_LARGE;
        } else {
            mMaxSuggestionLongSize = MAX_SUGGEST_LONG_SMALL;
            mMaxSuggestionShortSize = MAX_SUGGEST_SHORT_SMALL;
        }
        mOpenHelper = new BrowserProviderDataBaseHelper(context);
        mBackupManager = new BackupManager(context);
        // we added "picasa web album" into default bookmarks for version 19.
        // To avoid erasing the bookmark table, we added it explicitly for
        // version 18 and 19 as in the other cases, we will erase the table.
        if (DATABASE_VERSION == 18 || DATABASE_VERSION == 19) {
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
            boolean fix = p.getBoolean("fix_picasa", true);
            if (fix) {
                fixPicasaBookmark();
                Editor ed = p.edit();
                ed.putBoolean("fix_picasa", false);
                ed.apply();
            }
        }
        mSettings = BrowserSettings.getInstance();
        return true;
    }

    private void fixPicasaBookmark() {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id FROM bookmarks WHERE " + "bookmark = 1 AND url = ?", new String[]{PICASA_URL});
        try {
            if (!cursor.moveToFirst()) {
                // set "created" so that it will be on the top of the list
                db.execSQL("INSERT INTO bookmarks (title, url, visits, " +
                        "date, created, bookmark)" + " VALUES('" +
                        getContext().getString(R.string.picasa) + "', '"
                        + PICASA_URL + "', 0, 0, " + new Date().getTime()
                        + ", 1);");
            }
        } finally {
            IOUtils.closeCursor(cursor);
        }
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
                        String[] selectionArgs, String sortOrder)
            throws IllegalStateException {
        int match = URI_MATCHER.match(url);
        if (match == -1) {
            throw new IllegalArgumentException("Unknown URL");
        }

        if (match == URI_MATCH_SUGGEST || match == URI_MATCH_BOOKMARKS_SUGGEST) {
            // Handle suggestions
            return doSuggestQuery(selection, selectionArgs, match == URI_MATCH_BOOKMARKS_SUGGEST);
        }

        String[] projection = null;
        if (projectionIn != null && projectionIn.length > 0) {
            projection = new String[projectionIn.length + 1];
            System.arraycopy(projectionIn, 0, projection, 0, projectionIn.length);
            projection[projectionIn.length] = "_id AS _id";
        }

        String whereClause = null;
        if (match == URI_MATCH_BOOKMARKS_ID || match == URI_MATCH_SEARCHES_ID) {
            whereClause = "_id = " + url.getPathSegments().get(1);
        }

        Cursor c = mOpenHelper.getReadableDatabase().query(TABLE_NAMES[match % 10], projection,
                DatabaseUtils.concatenateWhere(whereClause, selection), selectionArgs,
                null, null, sortOrder, null);
        c.setNotificationUri(getContext().getContentResolver(), url);
        return c;
    }

    private Cursor doSuggestQuery(String selection, String[] selectionArgs, boolean bookmarksOnly) {
        String suggestSelection;
        String[] myArgs;
        if (selectionArgs[0] == null || selectionArgs[0].equals("")) {
            return new MySuggestionCursor(null, null, "", mMaxSuggestionLongSize);
        } else {
            String like = selectionArgs[0] + "%";
            if (selectionArgs[0].startsWith("http")
                    || selectionArgs[0].startsWith("file")) {
                myArgs = new String[1];
                myArgs[0] = like;
                suggestSelection = selection;
            } else {
                SUGGEST_ARGS[0] = "http://" + like;
                SUGGEST_ARGS[1] = "http://www." + like;
                SUGGEST_ARGS[2] = "https://" + like;
                SUGGEST_ARGS[3] = "https://www." + like;
                // To match against titles.
                SUGGEST_ARGS[4] = like;
                myArgs = SUGGEST_ARGS;
                suggestSelection = SUGGEST_SELECTION;
            }
        }

        Cursor c = mOpenHelper.getReadableDatabase().query(TABLE_NAMES[URI_MATCH_BOOKMARKS],
                SUGGEST_PROJECTION, suggestSelection, myArgs, null, null,
                ORDER_BY, Integer.toString(mMaxSuggestionLongSize));

        if (bookmarksOnly || Patterns.WEB_URL.matcher(selectionArgs[0]).matches()) {
            return new MySuggestionCursor(c, null, "", mMaxSuggestionLongSize);
        } else {
            // get search suggestions if there is still space in the list
            if (myArgs != null && myArgs.length > 1
                    && c.getCount() < (MAX_SUGGEST_SHORT_SMALL - 1)) {
                SearchEngine searchEngine = mSettings.getSearchEngine();
                if (searchEngine != null && searchEngine.supportsSuggestions()) {
                    Cursor sc = searchEngine.getSuggestions(getContext(), selectionArgs[0]);
                    return new MySuggestionCursor(c, sc, selectionArgs[0], mMaxSuggestionLongSize);
                }
            }
            return new MySuggestionCursor(c, null, selectionArgs[0], mMaxSuggestionLongSize);
        }
    }

    @Override
    public String getType(Uri url) {
        int match = URI_MATCHER.match(url);
        switch (match) {
            case URI_MATCH_BOOKMARKS:
                return "vnd.android.cursor.dir/bookmark";
            case URI_MATCH_BOOKMARKS_ID:
                return "vnd.android.cursor.item/bookmark";
            case URI_MATCH_SEARCHES:
                return "vnd.android.cursor.dir/searches";
            case URI_MATCH_SEARCHES_ID:
                return "vnd.android.cursor.item/searches";
            case URI_MATCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        boolean isBookmarkTable = false;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = URI_MATCHER.match(url);
        Uri uri = null;
        switch (match) {
            case URI_MATCH_BOOKMARKS: {
                // Insert into the bookmarks table
                long rowID = db.insert(TABLE_NAMES[URI_MATCH_BOOKMARKS], "url", initialValues);
                if (rowID > 0) {
                    uri = ContentUris.withAppendedId(Browser.BOOKMARKS_URI, rowID);
                }
                isBookmarkTable = true;
                break;
            }

            case URI_MATCH_SEARCHES: {
                // Insert into the searches table
                long rowID = db.insert(TABLE_NAMES[URI_MATCH_SEARCHES], "url", initialValues);
                if (rowID > 0) {
                    uri = ContentUris.withAppendedId(Browser.SEARCHES_URI, rowID);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URL");
        }

        if (uri == null) {
            throw new IllegalArgumentException("Unknown URL");
        }
        getContext().getContentResolver().notifyChange(uri, null);

        // Back up the new bookmark set if we just inserted one.
        // A row created when bookmarks are added from scratch will have
        // bookmark=1 in the initial value set.
        if (isBookmarkTable
                && initialValues.containsKey(BookmarkColumns.BOOKMARK)
                && initialValues.getAsInteger(BookmarkColumns.BOOKMARK) != 0) {
            mBackupManager.dataChanged();
        }
        return uri;
    }

    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = URI_MATCHER.match(url);
        if (match == -1 || match == URI_MATCH_SUGGEST) {
            throw new IllegalArgumentException("Unknown URL");
        }

        // need to know whether it's the bookmarks table for a couple of reasons
        boolean isBookmarkTable = (match == URI_MATCH_BOOKMARKS_ID);
        String id = null;

        if (isBookmarkTable || match == URI_MATCH_SEARCHES_ID) {
            StringBuilder sb = new StringBuilder();
            if (where != null && where.length() > 0) {
                sb.append("( ");
                sb.append(where);
                sb.append(" ) AND ");
            }
            id = url.getPathSegments().get(1);
            sb.append("_id = ");
            sb.append(id);
            where = sb.toString();
        }

        ContentResolver cr = getContext().getContentResolver();

        // we'lll need to back up the bookmark set if we are about to delete one
        if (isBookmarkTable) {
            Cursor cursor = cr.query(Browser.BOOKMARKS_URI,
                    new String[]{BookmarkColumns.BOOKMARK},
                    "_id = " + id, null, null);
            if (cursor.moveToNext()) {
                if (cursor.getInt(0) != 0) {
                    // yep, this record is a bookmark
                    mBackupManager.dataChanged();
                }
            }
            cursor.close();
        }

        int count = db.delete(TABLE_NAMES[match % 10], where, whereArgs);
        cr.notifyChange(url, null);
        return count;
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = URI_MATCHER.match(url);
        if (match == -1 || match == URI_MATCH_SUGGEST) {
            throw new IllegalArgumentException("Unknown URL");
        }

        if (match == URI_MATCH_BOOKMARKS_ID || match == URI_MATCH_SEARCHES_ID) {
            StringBuilder sb = new StringBuilder();
            if (where != null && where.length() > 0) {
                sb.append("( ");
                sb.append(where);
                sb.append(" ) AND ");
            }
            String id = url.getPathSegments().get(1);
            sb.append("_id = ");
            sb.append(id);
            where = sb.toString();
        }

        ContentResolver cr = getContext().getContentResolver();

        // Not all bookmark-table updates should be backed up.  Look to see
        // whether we changed the title, url, or "is a bookmark" state, and
        // request a backup if so.
        if (match == URI_MATCH_BOOKMARKS_ID || match == URI_MATCH_BOOKMARKS) {
            boolean changingBookmarks = false;
            // Alterations to the bookmark field inherently change the bookmark
            // set, so we don't need to query the record; we know a priori that
            // we will need to back up this change.
            if (values.containsKey(BookmarkColumns.BOOKMARK)) {
                changingBookmarks = true;
            } else if ((values.containsKey(BookmarkColumns.TITLE)
                    || values.containsKey(BookmarkColumns.URL))
                    && values.containsKey(BookmarkColumns._ID)) {
                // If a title or URL has been changed, check to see if it is to
                // a bookmark.  The ID should have been included in the update,
                // so use it.
                Cursor cursor = cr.query(Browser.BOOKMARKS_URI,
                        new String[]{BookmarkColumns.BOOKMARK},
                        BookmarkColumns._ID + " = "
                                + values.getAsString(BookmarkColumns._ID), null, null);
                if (cursor.moveToNext()) {
                    changingBookmarks = (cursor.getInt(0) != 0);
                }
                cursor.close();
            }

            // if this *is* a bookmark row we're altering, we need to back it up.
            if (changingBookmarks) {
                mBackupManager.dataChanged();
            }
        }

        int ret = db.update(TABLE_NAMES[match % 10], values, where, whereArgs);
        cr.notifyChange(url, null);
        return ret;
    }

    public static Cursor getBookmarksSuggestions(ContentResolver cr, String constraint) {
        Uri uri = Uri.parse("content://browser/" + SearchManager.SUGGEST_URI_PATH_QUERY);
        return cr.query(uri, SUGGEST_PROJECTION, SUGGEST_SELECTION,
                new String[]{constraint}, ORDER_BY);
    }
}
